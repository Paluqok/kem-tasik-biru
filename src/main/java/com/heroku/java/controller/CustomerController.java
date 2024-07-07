package com.heroku.java.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.Customer;

@Controller
public class CustomerController {

    private final DataSource dataSource;

    @Autowired
    public CustomerController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/custSignUp")
    public String createCustomerAccount() {
        return "customer/custSignUp";
    }

    @PostMapping("/createAccountCust")
    public String createAccountCustomer(@ModelAttribute("createAccountCust") @RequestParam("custName") String custName,
                                        @RequestParam("custEmail") String custEmail,
                                        @RequestParam("custAddress") String custAddress,
                                        @RequestParam("custPhoneNo") String custPhoneNo,
                                        @RequestParam("custPassword") String custPassword) {

        Customer customer = new Customer();
        customer.setCustName(custName);
        customer.setCustEmail(custEmail);
        customer.setCustAddress(custAddress);
        customer.setCustPhoneNo(custPhoneNo);
        customer.setCustPassword(custPassword);

        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO customer (custName, custEmail, custAddress, custPhoneNo, custPassword) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, customer.getCustName());
                statement.setString(2, customer.getCustEmail());
                statement.setString(3, customer.getCustAddress());
                statement.setString(4, customer.getCustPhoneNo());
                statement.setString(5, customer.getCustPassword());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to insert customer", e);
        }

        return "redirect:/custLogin";
    }

    @GetMapping("/custLogin")
    public String loginCustomerAccount() {
        return "customer/custLogin";
    }
}
