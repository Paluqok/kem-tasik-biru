package com.heroku.java.controller;

import com.heroku.java.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

@Controller
public class PaymentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/payment")
    public String showPaymentPage(Model model, HttpSession session) {
        // Fetch the total price from the session or previous request
        Double totalPrice = (Double) session.getAttribute("totalPrice");
        model.addAttribute("totalPrice", totalPrice);
        return "payment";
    }

    @PostMapping("/submitPayment")
    public String submitPayment(@RequestParam("paymentReceipt") MultipartFile paymentReceipt,
                                HttpSession session) throws IOException {
        // Get the booking ID from the session
        Long bookingId = (Long) session.getAttribute("bookingId");

        // Create a new Payment object and set its attributes
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentReceipt(paymentReceipt.getBytes());

        // Insert payment into the database
        String sql = "INSERT INTO payment (bookingid, paymentdate, paymentreceipt) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, payment.getBookingId(), payment.getPaymentDate(), payment.getPaymentReceipt());

        // Redirect to customer view booking page
        return "redirect:/custViewBooking";
    }
}
