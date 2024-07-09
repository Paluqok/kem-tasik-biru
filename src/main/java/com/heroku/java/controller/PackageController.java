package com.heroku.java.controller;

import com.heroku.java.model.Package;
import com.heroku.java.model.Activity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PackageController {

    // Simulate the database
    private List<Package> packages;
    private List<Activity> activities;

    @GetMapping("/listPackages")
    public String listPackages(Model model) {
        model.addAttribute("packages", packages);
        return "listPackage";
    }

    @GetMapping("/createPackage")
    public String createPackageForm(Model model) {
        model.addAttribute("activities", activities);
        return "createPackage";
    }

    @PostMapping("/createPackage")
    public String createPackage(@RequestParam String packageName, @RequestParam List<Long> activityIds) {
        List<Activity> selectedActivities = activities.stream()
                .filter(activity -> activityIds.contains(activity.getActivityId()))
                .collect(Collectors.toList());
        double packagePrice = selectedActivities.stream()
                .mapToDouble(Activity::getActivityPrice)
                .sum();
        Package newPackage = new Package(null, packageName, packagePrice, selectedActivities);
        packages.add(newPackage);
        return "redirect:/listPackages";
    }

    @GetMapping("/updatePackage/{packageId}")
    public String updatePackageForm(@PathVariable Long packageId, Model model) {
        Package pkg = packages.stream()
                .filter(p -> p.getPackageId().equals(packageId))
                .findFirst()
                .orElse(null);
        model.addAttribute("package", pkg);
        model.addAttribute("activities", activities);
        return "updatePackage";
    }

    @PostMapping("/updatePackage")
    public String updatePackage(@RequestParam Long packageId, @RequestParam String packageName, @RequestParam List<Long> activityIds) {
        Package pkg = packages.stream()
                .filter(p -> p.getPackageId().equals(packageId))
                .findFirst()
                .orElse(null);
        if (pkg != null) {
            List<Activity> selectedActivities = activities.stream()
                    .filter(activity -> activityIds.contains(activity.getActivityId()))
                    .collect(Collectors.toList());
            double packagePrice = selectedActivities.stream()
                    .mapToDouble(Activity::getActivityPrice)
                    .sum();
            pkg.setPackageName(packageName);
            pkg.setPackagePrice(packagePrice);
            pkg.setActivities(selectedActivities);
        }
        return "redirect:/listPackages";
    }

    @GetMapping("/deletePackage/{packageId}")
    public String deletePackage(@PathVariable Long packageId) {
        packages.removeIf(pkg -> pkg.getPackageId().equals(packageId));
        return "redirect:/listPackages";
    }
}
