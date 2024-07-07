package com.heroku.java.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.sql.*;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.heroku.java.model.Staff;

import jakarta.servlet.http.HttpSession;

@Controller
public class StaffController {

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

        return "redirect:/staff/staffLogin"; // Return the created staff object
    }

    @GetMapping("/staffLogin")
    public String staffLogin() {
        return "/staff/staffLogin";
    }

    @PostMapping("/staffLogins")
    public String staffLogins(HttpSession session,@RequestParam("staffEmail") String staffEmail, @RequestParam("staffPassword") String staffPassword,Staff staff) throws LoginException, SQLException{
        try {
            try (Connection conn = dataSource.getConnection()) {
                String sql = "SELECT staffid,staffname,staffemail,staffphoneno,staffaddress,staffpassword,managerid FROM public.staff WHERE staffemail=?";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1,staffEmail);
                
                ResultSet resultSet = statement.executeQuery();
                if(resultSet.next()) {
                    staff = new Staff();
                    staff.setStaffId(resultSet.getLong("staffid"));
                    staff.setStaffName(resultSet.getString("staffname"));
                    staff.setStaffEmail(resultSet.getString("staffemail"));
                    staff.setStaffPhoneNo(resultSet.getString("staffphoneno"));
                    staff.setStaffAddress(resultSet.getString("staffaddress"));
                    staff.setStaffPassword(resultSet.getString("staffpassword"));
                    
                    if(staff.getStaffEmail().equals(staffEmail) && staff.getStaffPassword().equals(staffPassword)) {
                   
                    session.setAttribute("staffname", staff.getStaffName());
                    session.setAttribute("staffid", staff.getStaffId());
                    
                    return "redirect:/index";
                   }
                   
                }
                     conn.close();
                     return "redirect:/staff/staffLogin";

                }

             
    }catch(SQLException e){
        return "redirect:/staff/Login";
    }
   
}


    
}

