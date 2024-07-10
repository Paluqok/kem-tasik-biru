package com.heroku.java.controller;

import com.heroku.java.model.Package;
import com.heroku.java.model.Activity;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PackageController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Package> packageRowMapper = new RowMapper<>() {
        @Override
        public Package mapRow(ResultSet rs, int rowNum) throws SQLException {
            Package pkg = new Package();
            pkg.setPackageId(rs.getLong("packageid"));
            pkg.setPackageName(rs.getString("packagename"));
            pkg.setPackagePrice(rs.getDouble("packageprice"));
            // Activities will be set separately
            return pkg;
        }
    };

    private final RowMapper<Activity> activityRowMapper = new RowMapper<>() {
        @Override
        public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {
            Activity activity = new Activity();
            activity.setActivityId(rs.getLong("activityid"));
            activity.setActivityName(rs.getString("activityname"));
            activity.setActivityDuration(rs.getString("activityduration"));
            activity.setActivityPrice(rs.getDouble("activityprice"));
            activity.setActivityImage(rs.getString("activityimage"));
            return activity;
        }
    };

    @GetMapping("/listPackages")
    public String listPackages(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM package";
        List<Package> packages = jdbcTemplate.query(sql, packageRowMapper);
        model.addAttribute("packages", packages);
        return "listPackage";
    }

    @GetMapping("/listPackagesForCustomer")
    public String listPackagesForCustomer(HttpSession session, Model model) {
        Customer cust = (Customer) session.getAttribute("cust");
        if (cust == null ) {
            return "redirect:/custLogin";
        }

        String sql = "SELECT * FROM package";
        List<Package> packages = jdbcTemplate.query(sql, packageRowMapper);
        model.addAttribute("packages", packages);
        return "listPackageForCustomer";
    }

    @GetMapping("/createPackage")
    public String createPackageForm(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM activity";
        List<Activity> activities = jdbcTemplate.query(sql, activityRowMapper);
        model.addAttribute("activities", activities);
        return "createPackage";
    }

    @PostMapping("/createPackage")
    public String createPackage(HttpSession session, @RequestParam String packageName, @RequestParam List<Long> activityIds) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM activity WHERE activityid IN (" + 
                  activityIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        List<Activity> selectedActivities = jdbcTemplate.query(sql, activityRowMapper);
        double packagePrice = selectedActivities.stream()
            .mapToDouble(Activity::getActivityPrice)
            .sum();

        String insertPackageSql = "INSERT INTO package (packagename, packageprice) VALUES (?, ?)";
        jdbcTemplate.update(insertPackageSql, packageName, packagePrice);

        Long packageId = jdbcTemplate.queryForObject("SELECT currval(pg_get_serial_sequence('package','packageid'))", Long.class);
        for (Activity activity : selectedActivities) {
            String insertPackageActivitySql = "INSERT INTO packageactivity (packageid, activityid) VALUES (?, ?)";
            jdbcTemplate.update(insertPackageActivitySql, packageId, activity.getActivityId());
        }

        return "redirect:/listPackages";
    }

    @GetMapping("/updatePackage/{packageId}")
    public String updatePackageForm(@PathVariable Long packageId, HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM package WHERE packageid = ?";
        Package pkg = jdbcTemplate.queryForObject(sql, packageRowMapper, packageId);

        String activitySql = "SELECT * FROM activity";
        List<Activity> activities = jdbcTemplate.query(activitySql, activityRowMapper);

        model.addAttribute("package", pkg);
        model.addAttribute("chosenActivities", pkg.getActivities());
        model.addAttribute("activities", activities);
        return "updatePackage";
    }

    @PostMapping("/updatePackage/{packageId}")
    public String updatePackage(HttpSession session, @RequestParam Long packageId, @RequestParam String packageName, @RequestParam List<Long> activityIds) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM activity WHERE activityid IN (" + 
                      activityIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        List<Activity> selectedActivities = jdbcTemplate.query(sql, activityRowMapper);
        double packagePrice = selectedActivities.stream()
                .mapToDouble(Activity::getActivityPrice)
                .sum();

        String updatePackageSql = "UPDATE package SET packagename = ?, packageprice = ? WHERE packageid = ?";
        jdbcTemplate.update(updatePackageSql, packageName, packagePrice, packageId);

        String deletePackageActivitySql = "DELETE FROM packageactivity WHERE packageid = ?";
        jdbcTemplate.update(deletePackageActivitySql, packageId);

        for (Activity activity : selectedActivities) {
            String insertPackageActivitySql = "INSERT INTO packageactivity (packageid, activityid) VALUES (?, ?)";
            jdbcTemplate.update(insertPackageActivitySql, packageId, activity.getActivityId());
        }

        return "redirect:/listPackages";
    }

    @GetMapping("/deletePackage/{packageId}")
    public String deletePackage(@PathVariable Long packageId, HttpSession session) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String deletePackageActivitySql = "DELETE FROM packageactivity WHERE packageid = ?";
        jdbcTemplate.update(deletePackageActivitySql, packageId);

        String deletePackageSql = "DELETE FROM package WHERE packageid = ?";
        jdbcTemplate.update(deletePackageSql, packageId);

        return "redirect:/listPackages";
    }
}
