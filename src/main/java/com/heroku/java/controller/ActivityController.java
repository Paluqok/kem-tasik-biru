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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import com.heroku.java.model.Activity;
import com.heroku.java.model.Dry;
import com.heroku.java.model.Wet;
import com.heroku.java.model.Staff;

@Controller
public class ActivityController {
    private final DataSource dataSource;

    private List<Activity> activities = new ArrayList<>();

    @Autowired
    public ActivityController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/listActivity")
    public String listActivities(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT a.activityid, a.activityname, a.activityduration, a.activityprice, a.activityimage, " +
                     "w.activityequipment, d.activitylocation " +
                     "FROM public.activity a " +
                     "LEFT JOIN public.wet w ON a.activityid = w.activityid " +
                     "LEFT JOIN public.dry d ON a.activityid = d.activityid";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("activityid");
                String name = rs.getString("activityname");
                String duration = rs.getString("activityduration");
                double price = rs.getDouble("activityprice");
                String image = rs.getString("activityimage");
                String equipment = rs.getString("activityequipment");
                String location = rs.getString("activitylocation");

                Activity activity;
                if (equipment != null) {
                    activity = new Wet(id, name, duration, price, image, equipment);
                } else if (location != null) {
                    activity = new Dry(id, name, duration, price, image, location);
                } else {
                    activity = new Activity(id, name, duration, price, image);
                }

                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("activities", activities);
        return "listActivity";
    }

    @GetMapping("/createActivity")
    public String createActivityForm(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }
        model.addAttribute("activity", new Activity());
        return "createActivity";
    }

    @PostMapping("/createActivities")
    public String createActivity(HttpSession session, 
                                 @ModelAttribute("createActivities") 
                                 @RequestParam("name") String activityName,
                                 @RequestParam("duration") String activityDuration,
                                 @RequestParam("price") int activityPrice,
                                 @RequestParam("activityImage") MultipartFile activityImage,
                                 @RequestParam("activityType") String activityType,
                                 @RequestParam(value = "equipment", required = false) String equipment,
                                 @RequestParam(value = "location", required = false) String location) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        // Handle file upload
        String activityImagePath = null;
        if (!activityImage.isEmpty()) {
            String uploadDirectory = "D:\\kem-tasik-biru\\src\\main\\resources\\uploaded_images\\"; // Define this directory for saving the image
            try {
                Path filePath = Paths.get(uploadDirectory + activityImage.getOriginalFilename());
                Files.write(filePath, activityImage.getBytes());
                activityImagePath = activityImage.getOriginalFilename(); // Save just the filename or relative path
            } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception accordingly
            }
        }

        
    String sql = "INSERT INTO public.activity(activityduration, activityname, activityprice, activityimage) VALUES (?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection()) {
        conn.setAutoCommit(false);
        try (PreparedStatement statement = conn.prepareStatement(sql, new String[] {"activityid"})) {
            statement.setString(2, activityName);
            statement.setDouble(3, activityPrice);
            statement.setString(1, activityDuration);
            statement.setString(4, activityImagePath);

            // Execute the insert statement
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long activityId = generatedKeys.getLong(1);

                        if ("wet".equals(activityType)) {
                            String wetSql = "INSERT INTO public.wet(activityid, activityequipment) VALUES (?, ?)";
                            try (PreparedStatement wetStatement = conn.prepareStatement(wetSql)) {
                                wetStatement.setLong(1, activityId);
                                wetStatement.setString(2, equipment);
                                wetStatement.executeUpdate();
                            }

                        } else if ("dry".equals(activityType)) {
                            String drySql = "INSERT INTO public.dry(activityid, activitylocation) VALUES (?, ?)";
                            try (PreparedStatement dryStatement = conn.prepareStatement(drySql)) {
                                dryStatement.setLong(1, activityId);
                                dryStatement.setString(2, location);
                                dryStatement.executeUpdate();
                            }

                        }

                        conn.commit();
                    }
                }
            } else {
                conn.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();  // Ensure rollback on exception
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return "redirect:/listActivity";
}

    @GetMapping("/updateActivity/{id}")
    public String updateActivityForm(@PathVariable Long id, HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        Activity activity = activities.stream()
                                      .filter(a -> a.getActivityId().equals(id))
                                      .findFirst()
                                      .orElse(null);
        model.addAttribute("activity", activity);
        return "updateActivity";
    }

    @PostMapping("/updateActivity/{id}")
    public String updateActivity(@PathVariable Long id, HttpSession session,
                                 @ModelAttribute Activity updatedActivity,
                                 @RequestParam("activityImage") MultipartFile activityImage) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        Activity activity = activities.stream()
                                      .filter(a -> a.getActivityId().equals(id))
                                      .findFirst()
                                      .orElse(null);

        if (activity != null) {
            activity.setActivityName(updatedActivity.getActivityName());
            activity.setActivityDuration(updatedActivity.getActivityDuration());
            activity.setActivityPrice(updatedActivity.getActivityPrice());

            if (!activityImage.isEmpty()) {
                activity.setActivityImage("saved/image/path/" + activityImage.getOriginalFilename());
            }
        }

        return "redirect:/listActivity";
    }

    @GetMapping("/deleteActivity/{id}")
    public String deleteActivity(@PathVariable Long id, HttpSession session) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        activities.removeIf(a -> a.getActivityId().equals(id));

        return "redirect:/listActivity";
    }
}
