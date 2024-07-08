package com.heroku.java.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.heroku.java.model.Activity;
import com.heroku.java.model.Dry;
import com.heroku.java.model.Wet;

@Controller
@RequestMapping("/api")
public class ActivityController {

    private final DataSource dataSource;

    @Autowired
    public ActivityController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/createActivity")
    public String showCreateActivityForm() {
        return "createActivity"; // This should match the filename of your createActivity.html
    }

    @PostMapping("/activity")
    public String createActivity(
            @RequestParam("activityName") String activityName,
            @RequestParam("activityDuration") String activityDuration,
            @RequestParam("activityPrice") double activityPrice,
            @RequestParam("activityImage") MultipartFile activityImage,
            @RequestParam("activityType") String activityType,
            @RequestParam(value = "equipment", required = false) String equipment,
            @RequestParam(value = "location", required = false) String location
    ) throws IOException {
        // Handle image upload and get URL
        String imageUrl = "";
        if (activityImage != null && !activityImage.isEmpty()) {
            String fileName = activityImage.getOriginalFilename();
            Path path = Paths.get("uploads/" + fileName);
            Files.write(path, activityImage.getBytes());
            imageUrl = path.toString(); // Save the file path or URL as needed
        }

        Activity activity;
        if ("wet".equalsIgnoreCase(activityType)) {
            activity = new Wet(null, activityName, activityDuration, activityPrice, imageUrl, equipment);
        } else {
            activity = new Dry(null, activityName, activityDuration, activityPrice, imageUrl, location);
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO public.activity (activityname, activityduration, activityprice, activityimage, activitytype, activityequipment, activitylocation) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, activity.getActivityName());
                statement.setString(2, activity.getActivityDuration());
                statement.setDouble(3, activity.getActivityPrice());
                statement.setString(4, activity.getActivityImage());
                statement.setString(5, activityType);

                if ("wet".equalsIgnoreCase(activityType)) {
                    Wet wetActivity = (Wet) activity;
                    statement.setString(6, wetActivity.getActivityEquipment());
                    statement.setNull(7, Types.VARCHAR); // Set `activityLocation` as NULL for Wet type
                } else {
                    Dry dryActivity = (Dry) activity;
                    statement.setNull(6, Types.VARCHAR); // Set `activityEquipment` as NULL for Dry type
                    statement.setString(7, dryActivity.getActivityLocation());
                }

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/activity-list";
    }

    @GetMapping("/activities")
    @ResponseBody
    public List<Activity> getActivities() {
        List<Activity> activities = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM public.activity";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    Long id = resultSet.getLong("activityid");
                    String name = resultSet.getString("activityname");
                    String duration = resultSet.getString("activityduration");
                    double price = resultSet.getDouble("activityprice");
                    String image = resultSet.getString("activityimage");
                    String type = resultSet.getString("activitytype");
                    String equipment = resultSet.getString("activityequipment");
                    String location = resultSet.getString("activitylocation");

                    Activity activity;
                    if ("wet".equals(type)) {
                        activity = new Wet(id, name, duration, price, image, equipment);
                    } else {
                        activity = new Dry(id, name, duration, price, image, location);
                    }
                    activities.add(activity);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }

    @GetMapping("/editActivity/{id}")
    public String editActivity(@PathVariable("id") Long id, Model model) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM public.activity WHERE activityid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String name = resultSet.getString("activityname");
                        String duration = resultSet.getString("activityduration");
                        double price = resultSet.getDouble("activityprice");
                        String image = resultSet.getString("activityimage");
                        String type = resultSet.getString("activitytype");
                        String equipment = resultSet.getString("activityequipment");
                        String location = resultSet.getString("activitylocation");

                        Activity activity;
                        if ("wet".equals(type)) {
                            activity = new Wet(id, name, duration, price, image, equipment);
                        } else {
                            activity = new Dry(id, name, duration, price, image, location);
                        }
                        model.addAttribute("activity", activity);
                        return "editActivity";
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/activity-list";
    }

    @PostMapping("/updateActivity")
    public String updateActivity(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("duration") String duration,
            @RequestParam("price") double price,
            @RequestParam("activityImage") MultipartFile activityImage,
            @RequestParam("activityType") String activityType,
            @RequestParam(value = "equipment", required = false) String equipment,
            @RequestParam(value = "location", required = false) String location
    ) throws IOException {
        // Handle image upload and get URL
        String imageUrl = "";
        if (activityImage != null && !activityImage.isEmpty()) {
            String fileName = activityImage.getOriginalFilename();
            Path path = Paths.get("uploads/" + fileName);
            Files.write(path, activityImage.getBytes());
            imageUrl = path.toString(); // Save the file path or URL as needed
        }

        Activity activity;
        if ("wet".equalsIgnoreCase(activityType)) {
            activity = new Wet(id, name, duration, price, imageUrl, equipment);
        } else {
            activity = new Dry(id, name, duration, price, imageUrl, location);
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE public.activity SET activityname=?, activityduration=?, activityprice=?, activityimage=?, activityequipment=?, activitylocation=? WHERE activityid=?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, activity.getActivityName());
                statement.setString(2, activity.getActivityDuration());
                statement.setDouble(3, activity.getActivityPrice());
                statement.setString(4, activity.getActivityImage());

                if ("wet".equalsIgnoreCase(activityType)) {
                    Wet wetActivity = (Wet) activity;
                    statement.setString(5, wetActivity.getActivityEquipment());
                    statement.setNull(6, Types.VARCHAR); // Set `activityLocation` as NULL for Wet type
                } else {
                    Dry dryActivity = (Dry) activity;
                    statement.setNull(5, Types.VARCHAR); // Set `activityEquipment` as NULL for Dry type
                    statement.setString(6, dryActivity.getActivityLocation());
                }

                statement.setLong(7, activity.getActivityId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/activity-list";
    }

    @PostMapping("/deleteActivity")
    public String deleteActivity(@RequestParam("id") Long id) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "DELETE FROM public.activity WHERE activityid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/activity-list";
    }
}
