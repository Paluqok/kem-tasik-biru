package com.heroku.java.controller;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.heroku.java.model.Activity;

@Controller
public class ActivityController {

    private final DataSource dataSource;

    @Autowired
    public ActivityController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/createActivities")
    public String createActivity(@ModelAttribute("createActivities") @RequestParam("activityname") String activityName,
                                   @RequestParam("activitylocation") String activityLocation,
                                   @RequestParam("activityduration") String activityDuration,
                                   @RequestParam("activityprice") double activityPrice,
                                   @RequestParam("activityimage") MultipartFile activityImage) throws IOException {
        
        Activity activity = new Activity();
        activity.setActivityName(activityName);
        activity.setActivityLocation(activityLocation);
        activity.setActivityDuration(activityDuration);
        activity.setActivityPrice(activityPrice);

        // Encode image as Base64 string
        String base64Image = Base64.getEncoder().encodeToString(activityImage.getBytes());
        activity.setActivityImage(base64Image);

        try (Connection connection = dataSource.getConnection()) {

            System.out.println("Received activity details:");
            System.out.println("Name: " + activity.getActivityName());
            System.out.println("Location: " + activity.getActivityLocation());
            System.out.println("Duration: " + activity.getActivityDuration());
            System.out.println("Price: " + activity.getActivityPrice());
            System.out.println("Image: " + activityImage.getOriginalFilename());

            String activitySql = "INSERT INTO public.activity(activityname, activitylocation, activityduration, activityprice, activityimage) VALUES (?, ?, ?, ?, ?) RETURNING activityid";

            try (PreparedStatement statement = connection.prepareStatement(activitySql)) {
                statement.setString(1, activity.getActivityName());
                statement.setString(2, activity.getActivityLocation());
                statement.setString(3, activity.getActivityDuration());
                statement.setDouble(4, activity.getActivityPrice());
                statement.setString(5, activity.getActivityImage()); // Set the Base64-encoded image

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        long activityId = resultSet.getLong("activityid");
                        activity.setActivityId(activityId);
                        System.out.println("Inserted activity with ID: " + activityId);
                    } else {
                        throw new SQLException("Failed to insert activity, no ID obtained.");
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., log error or rethrow as runtime exception
            throw new RuntimeException("Failed to insert activity", e);
        }

        return "redirect:/staffactivity"; // Return the created activity object
    }
}
