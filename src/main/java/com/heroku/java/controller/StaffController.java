package com.heroku.java.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.sql.*;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.heroku.java.model.Staff;

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
    public String CreateAccountStaff(@ModelAttribute("createAccountStaff") Staff staff) {

        try {
            Connection conn = dataSource.getConnection();
            String sql = "INSERT INTO public.staff (staffname,staffemail,staffphonenum,staffaddress,staffpassword,managerid) VALUES (?,?,?,?,?,?)";
            PreparedStatement statement = conn.prepareStatement(sql);
			
			statement.setString(1, staff.getStaffName());
			statement.setString(2, staff.getStaffEmail());
			statement.setString(3, staff.getStaffPhoneNo());
			statement.setString(4, staff.getStaffAddress());
			statement.setString(5, staff.getStaffPassword());
			 if (staff.getManagerId() != null) {
	                statement.setInt(6, staff.getManagerId());
	            } else {
	                statement.setNull(6, java.sql.Types.INTEGER);
	            }
			
			statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/index";
    }

}
