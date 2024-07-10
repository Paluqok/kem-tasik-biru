package com.heroku.java.controller;

import com.heroku.java.model.Booking;
import com.heroku.java.model.Package;
import com.heroku.java.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class BookingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Booking> bookingRowMapper = new RowMapper<>() {
        @Override
        public Booking mapRow(ResultSet rs, int rowNum) throws SQLException {
            Booking booking = new Booking();
            booking.setBookingId(rs.getLong("bookingid"));
            booking.setBookingStatus(rs.getString("bookingstatus"));
            booking.setStaffId(rs.getLong("staffid"));
            booking.setCustId(rs.getLong("custid"));
            booking.setPackageId(rs.getLong("packageid"));
            booking.setBookingStartDate(rs.getTimestamp("bookingstartdate").toLocalDateTime());
            booking.setBookingEndDate(rs.getTimestamp("bookingenddate").toLocalDateTime());
            return booking;
        }
    };

    @GetMapping("/createBooking")
    public String showCreateBookingForm(Model model) {
        String sql = "SELECT * FROM package";
        List<Package> packages = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Package pkg = new Package();
            pkg.setPackageId(rs.getLong("packageid"));
            pkg.setPackageName(rs.getString("packagename"));
            pkg.setPackagePrice(rs.getDouble("packageprice"));
            return pkg;
        });
        model.addAttribute("packages", packages);
        return "createBooking";
    }

    @PostMapping("/createBooking")
    public String createBooking(@RequestParam("bookingStartDate") LocalDateTime bookingStartDate,
                                @RequestParam("bookingEndDate") LocalDateTime bookingEndDate,
                                @RequestParam("packageId") Long packageId,
                                HttpSession session, Model model) {
        // Check for date clash
        String clashCheckSql = "SELECT COUNT(*) FROM booking WHERE " +
                "((bookingstartdate <= ? AND bookingenddate >= ?) OR " +
                "(bookingstartdate <= ? AND bookingenddate >= ?) OR " +
                "(bookingstartdate >= ? AND bookingenddate <= ?))";
        int clashCount = jdbcTemplate.queryForObject(clashCheckSql, Integer.class, 
                bookingStartDate, bookingStartDate, 
                bookingEndDate, bookingEndDate, 
                bookingStartDate, bookingEndDate);
        if (clashCount >= 5) {
            model.addAttribute("dateMessage", "Booking is currently full at that date.");
            return "createBooking";
        }

        // Check if booking duration is more than 3 days
        if (bookingEndDate.isAfter(bookingStartDate.plusDays(3))) {
            model.addAttribute("dateMessage", "Not available for more than 3 days.");
            return "createBooking";
        }

        // Get the customer ID from session
        Customer customer = (Customer) session.getAttribute("customer");
        Long custId = customer.getCustId();

        // Insert booking into database
        String insertSql = "INSERT INTO booking (bookingstatus, staffid, custid, packageid, bookingstartdate, bookingenddate) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, "Pending", null, custId, packageId, bookingStartDate, bookingEndDate);

        // Redirect to payment page with total price
        String packageSql = "SELECT packageprice FROM package WHERE packageid = ?";
        Double totalPrice = jdbcTemplate.queryForObject(packageSql, Double.class, packageId);
        model.addAttribute("totalPrice", totalPrice);

        return "redirect:/payment";
    }

    @GetMapping("/custViewBooking")
    public String customerViewBooking(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        Long custId = customer.getCustId();

        String sql = "SELECT b.*, p.packagename, p.packageprice " +
                "FROM booking b JOIN package p ON b.packageid = p.packageid " +
                "WHERE b.custid = ?";
        List<Booking> bookings = jdbcTemplate.query(sql, bookingRowMapper, custId);
        model.addAttribute("bookings", bookings);
        return "custViewBooking";
    }

    @GetMapping("/staffViewBooking")
    public String staffViewBooking(Model model) {
        String sql = "SELECT b.*, p.packagename, p.packageprice " +
                "FROM booking b JOIN package p ON b.packageid = p.packageid";
        List<Booking> bookings = jdbcTemplate.query(sql, bookingRowMapper);
        model.addAttribute("bookings", bookings);
        return "staffViewBooking";
    }

    @PostMapping("/approveBooking/{id}")
    public String approveBooking(@PathVariable("id") Long bookingId) {
        String sql = "UPDATE booking SET bookingstatus = 'Approved' WHERE bookingid = ?";
        jdbcTemplate.update(sql, bookingId);
        return "redirect:/staffViewBooking";
    }

    @PostMapping("/rejectBooking/{id}")
    public String rejectBooking(@PathVariable("id") Long bookingId) {
        String sql = "UPDATE booking SET bookingstatus = 'Rejected' WHERE bookingid = ?";
        jdbcTemplate.update(sql, bookingId);
        return "redirect:/staffViewBooking";
    }
}
