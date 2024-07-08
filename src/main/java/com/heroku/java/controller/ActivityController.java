package com.heroku.java.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.heroku.java.model.Activity;
import com.heroku.java.model.Dry;
import com.heroku.java.model.Wet;

@Controller
public class ActivityController {

    private final DataSource dataSource;

    @Autowired
    public ActivityController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/listActivity")
    public String listActivities(Model model) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM activity LEFT JOIN wet ON activity.activityid = wet.activityid LEFT JOIN dry ON activity.activityid = dry.activityid";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    model.addAttribute("activities", resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to list activities", e);
        }
        return "listActivity";
    }

    @GetMapping("/createActivity")
    public String createActivityForm() {
        return "createActivity";
    }

    @PostMapping("/api/activity")
    public String createActivity(@RequestParam("name") String name,
                                 @RequestParam("price") double price,
                                 @RequestParam("duration") String duration,
                                 @RequestParam("activityImage") MultipartFile activityImage,
                                 @RequestParam("activityType") String activityType,
                                 @RequestParam(value = "equipment", required = false) String equipment,
                                 @RequestParam(value = "location", required = false) String location) throws IOException {

        String imagePath = null;
        if (activityImage != null && !activityImage.isEmpty()) {
            imagePath = activityImage.getOriginalFilename();
        }

        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO activity (activityname, activityduration, activityprice, activityimage) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                statement.setString(2, duration);
                statement.setDouble(3, price);
                statement.setString(4, imagePath);
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long activityId = generatedKeys.getLong(1);

                        if ("wet".equals(activityType)) {
                            sql = "INSERT INTO wet (activityid, activityequipment) VALUES (?, ?)";
                            try (PreparedStatement wetStatement = conn.prepareStatement(sql)) {
                                wetStatement.setLong(1, activityId);
                                wetStatement.setString(2, equipment);
                                wetStatement.executeUpdate();
                            }
                        } else if ("dry".equals(activityType)) {
                            sql = "INSERT INTO dry (activityid, activitylocation) VALUES (?, ?)";
                            try (PreparedStatement dryStatement = conn.prepareStatement(sql)) {
                                dryStatement.setLong(1, activityId);
                                dryStatement.setString(2, location);
                                dryStatement.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create activity", e);
        }
        return "redirect:/activity-list";
    }

    @GetMapping("/editActivity")
    public String editActivityForm(@RequestParam("id") Long id, Model model) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM activity LEFT JOIN wet ON activity.activityid = wet.activityid LEFT JOIN dry ON activity.activityid = dry.activityid WHERE activity.activityid = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Activity activity = new Activity();
                        activity.setActivityId(resultSet.getLong("activityid"));
                        activity.setActivityName(resultSet.getString("activityname"));
                        activity.setActivityDuration(resultSet.getString("activityduration"));
                        activity.setActivityPrice(resultSet.getDouble("activityprice"));
                        activity.setActivityImage(resultSet.getString("activityimage"));

                        if (resultSet.getString("activityequipment") != null) {
                            Wet wetActivity = new Wet();
                            wetActivity.setActivityId(resultSet.getLong("activityid"));
                            wetActivity.setActivityName(resultSet.getString("activityname"));
                            wetActivity.setActivityDuration(resultSet.getString("activityduration"));
                            wetActivity.setActivityPrice(resultSet.getDouble("activityprice"));
                            wetActivity.setActivityImage(resultSet.getString("activityimage"));
                            wetActivity.setactivityEquipment(resultSet.getString("activityequipment"));
                            model.addAttribute("activity", wetActivity);
                        } else if (resultSet.getString("activitylocation") != null) {
                            Dry dryActivity = new Dry();
                            dryActivity.setActivityId(resultSet.getLong("activityid"));
                            dryActivity.setActivityName(resultSet.getString("activityname"));
                            dryActivity.setActivityDuration(resultSet.getString("activityduration"));
                            dryActivity.setActivityPrice(resultSet.getDouble("activityprice"));
                            dryActivity.setActivityImage(resultSet.getString("activityimage"));
                            dryActivity.setActivityLocation(resultSet.getString("activitylocation"));
                            model.addAttribute("activity", dryActivity);
                        } else {
                            model.addAttribute("activity", activity);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch activity for edit", e);
        }
        return "editActivity";
    }

    @PostMapping("/updateActivity")
    public String updateActivity(@ModelAttribute("activity") Activity activity,
                                 @RequestParam(value = "equipment", required = false) String equipment,
                                 @RequestParam(value = "location", required = false) String location) {

        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE activity SET activityname = ?, activityduration = ?, activityprice = ?, activityimage = ? WHERE activityid = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, activity.getActivityName());
                statement.setString(2, activity.getActivityDuration());
                statement.setDouble(3, activity.getActivityPrice());
                statement.setString(4, activity.getActivityImage());
                statement.setLong(5, activity.getActivityId());
                statement.executeUpdate();
            }

            if (activity instanceof Wet) {
                sql = "UPDATE wet SET activityequipment = ? WHERE activityid = ?";
                try (PreparedStatement wetStatement = conn.prepareStatement(sql)) {
                    wetStatement.setString(1, ((Wet) activity).getactivityEquipment());
                    wetStatement.setLong(2, activity.getActivityId());
                    wetStatement.executeUpdate();
                }
            } else if (activity instanceof Dry) {
                sql = "UPDATE dry SET activitylocation = ? WHERE activityid = ?";
                try (PreparedStatement dryStatement = conn.prepareStatement(sql)) {
                    dryStatement.setString(1, ((Dry) activity).getActivityLocation());
                    dryStatement.setLong(2, activity.getActivityId());
                    dryStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update activity", e);
        }
        return "redirect:/activity-list";
    }

    @GetMapping("/deleteActivity")
    public String deleteActivity(@RequestParam("id") Long id) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "DELETE FROM activity WHERE activityid = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete activity", e);
        }
        return "redirect:/activity-list";
    }
}
