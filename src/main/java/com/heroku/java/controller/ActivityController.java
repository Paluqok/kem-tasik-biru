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

    private List<Activity> activities = new ArrayList<>();

    @GetMapping("/listActivity")
    public String listActivities(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
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
                                 @RequestParam("activityName") String activityName,
                                 @RequestParam("activityDuration") String activityDuration,
                                 @RequestParam("activityPrice") int activityPrice,
                                 @RequestParam("activityImage") MultipartFile activityImage,
                                 @RequestParam("activityType") String activityType,
                                 @RequestParam(value = "equipment", required = false) String equipment,
                                 @RequestParam(value = "location", required = false) String location) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        Activity activity = new Activity();
        activity.setActivityName(activityName);
        activity.setActivityDuration(activityDuration);
        activity.setActivityPrice(activityPrice);

        if (!activityImage.isEmpty()) {
            // Save the image to the server
            // For now, we will just set a placeholder string
            activity.setActivityImage("saved/image/path/" + activityImage.getOriginalFilename());
        }

        if ("wet".equals(activityType)) {
            Wet wetActivity = new Wet(activity.getActivityId(), activity.getActivityName(), activity.getActivityDuration(), 
                                      activity.getActivityPrice(), activity.getActivityImage(), equipment);
            activities.add(wetActivity);
        } else if ("dry".equals(activityType)) {
            Dry dryActivity = new Dry(activity.getActivityId(), activity.getActivityName(), activity.getActivityDuration(), 
                                      activity.getActivityPrice(), activity.getActivityImage(), location);
            activities.add(dryActivity);
        } else {
            activities.add(activity);
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
