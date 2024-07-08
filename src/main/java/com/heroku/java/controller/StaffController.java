package com.heroku.java.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.sql.*;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.heroku.java.model.Staff;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;

@Controller
public class StaffController {

    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);
    private final DataSource dataSource;

    @Autowired
    public StaffController(DataSource dataSource) {
        this.dataSource = dataSource;
    }
     
    @GetMapping("/staffSignUp")
    public String createStaffAccount(){
        return "staff/staffSignUp";
    }

    @PostMapping("/createAccountStaff")
    public String createAccountStaff(@ModelAttribute("createAccountStaff") @RequestParam("staffName") String staffName,
                                   @RequestParam("staffEmail") String staffEmail,
                                   @RequestParam("staffAddress") String staffAddress,
                                   @RequestParam("staffPhoneNo") String staffPhoneNo,
                                   @RequestParam("staffPassword") String staffPassword) throws IOException {
        
        Staff staff = new Staff();
        staff.setStaffName(staffName);
        staff.setStaffEmail(staffEmail);
        staff.setStaffAddress(staffAddress);
        staff.setStaffPhoneNo(staffPhoneNo);
        staff.setStaffPassword(staffPassword);

        try (Connection connection = dataSource.getConnection()) {

            System.out.println("Received staff details:");
            System.out.println("Name: " + staff.getStaffName());
            System.out.println("Email: " + staff.getStaffEmail());
            System.out.println("Address: " + staff.getStaffAddress());
            System.out.println("Phone No: " + staff.getStaffPhoneNo());
            System.out.println("Password: " + staff.getStaffPassword());

            String staffSql = "INSERT INTO public.staff(staffname, staffemail, staffaddress, staffphoneno, staffpassword) VALUES (?, ?, ?, ?, ?) RETURNING staffid";

            try (PreparedStatement statement = connection.prepareStatement(staffSql)) {
                statement.setString(1, staff.getStaffName());
                statement.setString(2, staff.getStaffEmail());
                statement.setString(3, staff.getStaffAddress());
                statement.setString(4, staff.getStaffPhoneNo());
                statement.setString(5, staff.getStaffPassword()); // Set the Base64-encoded image

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Long staffId = resultSet.getLong("staffid");
                        staff.setStaffId(staffId);
                        System.out.println("Inserted staff with ID: " + staffId);
                    } else {
                        throw new SQLException("Failed to insert staff, no ID obtained.");
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., log error or rethrow as runtime exception
            throw new RuntimeException("Failed to insert staff", e);
        }

        return "redirect:/staffLogin"; // Return the created staff object
    }

    @GetMapping("/staffLogin")
    public String staffLogin() {
        return "staff/staffLogin";
    }

    @PostMapping("/loginStaff")
    public String staffLogins(HttpSession session,
                              @RequestParam("staffEmail") String staffEmail,
                              @RequestParam("staffPassword") String staffPassword) throws LoginException {

        logger.info("Attempting to log in staff with email: {}", staffEmail);

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT staffid, staffname, staffemail, staffphoneno, staffaddress, staffpassword FROM public.staff WHERE staffemail = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, staffEmail);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Staff staff = new Staff();
                        staff.setStaffId(resultSet.getLong("staffid"));
                        staff.setStaffName(resultSet.getString("staffname"));
                        staff.setStaffEmail(resultSet.getString("staffemail"));
                        staff.setStaffPhoneNo(resultSet.getString("staffphoneno"));
                        staff.setStaffAddress(resultSet.getString("staffaddress"));
                        staff.setStaffPassword(resultSet.getString("staffpassword"));

                        logger.info("Staff found: {}", staff.getStaffName());

                        if (staff.getStaffPassword().equals(staffPassword)) {
                            logger.info("Password matches for staff: {}", staff.getStaffName());
                            session.setAttribute("staffname", staff.getStaffName());
                            session.setAttribute("staffid", staff.getStaffId());
                            session.setAttribute("staff", staff);
                            return "redirect:/homeStaff";
                        } else {
                            logger.warn("Password does not match for staff: {}", staff.getStaffName());
                        }
                    } else {
                        logger.warn("No staff found with email: {}", staffEmail);
                    }
                }
            }
            return "redirect:/staffLogin";
        } catch (SQLException e) {
            logger.error("Error during staff login", e);
            return "redirect:/error";
        }
    }

    @GetMapping("/homeStaff")
    public String homeStaff(HttpSession session, Model model) {
        Long staffId = (Long) session.getAttribute("staffid");
        if (staffId == null) {
            logger.warn("staffId is null in session");
            return "redirect:/staffLogin";
        }

        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            logger.warn("staff is null in session");
            return "redirect:/staffLogin";
        }

        model.addAttribute("staff", staff);
        return "staff/homeStaff";
    }

    @GetMapping("/staffProfile")
    public String staffProfile(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }
        model.addAttribute("staff", staff);
        return "staff/staffProfile";
    }

    @PostMapping("/updateStaff")
    public String updateStaff(HttpSession session,
                              @RequestParam("staffName") String staffName,
                              @RequestParam("staffEmail") String staffEmail,
                              @RequestParam("staffAddress") String staffAddress,
                              @RequestParam("staffPhoneNo") String staffPhoneNo,
                              @RequestParam("staffPassword") String staffPassword) throws IOException {

        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        staff.setStaffName(staffName);
        staff.setStaffEmail(staffEmail);
        staff.setStaffAddress(staffAddress);
        staff.setStaffPhoneNo(staffPhoneNo);
        staff.setStaffPassword(staffPassword);

        try (Connection connection = dataSource.getConnection()) {
            String staffSql = "UPDATE public.staff SET staffname = ?, staffemail = ?, staffaddress = ?, staffphoneno = ?, staffpassword = ? WHERE staffid = ?";

            try (PreparedStatement statement = connection.prepareStatement(staffSql)) {
                statement.setString(1, staff.getStaffName());
                statement.setString(2, staff.getStaffEmail());
                statement.setString(3, staff.getStaffAddress());
                statement.setString(4, staff.getStaffPhoneNo());
                statement.setString(5, staff.getStaffPassword());
                statement.setLong(6, staff.getStaffId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Failed to update staff", e);
            throw new RuntimeException("Failed to update staff", e);
        }

        session.setAttribute("staff", staff);
        return "redirect:/staffProfile";
    }

    @GetMapping("/deleteStaff")
    public String deleteStaff(HttpSession session) throws IOException {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        try (Connection connection = dataSource.getConnection()) {
            String staffSql = "DELETE FROM public.staff WHERE staffid = ?";

            try (PreparedStatement statement = connection.prepareStatement(staffSql)) {
                statement.setLong(1, staff.getStaffId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Failed to delete staff", e);
            throw new RuntimeException("Failed to delete staff", e);
        }

        session.invalidate();
        return "redirect:/staffLogin";
    }
    


    
}

