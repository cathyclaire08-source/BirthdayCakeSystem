package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {

    public boolean registerUser(
            String username,
            String full_name,
            String password,
            String roles,
            String contact_number) {


        String sql = "INSERT INTO users "
                + "(username, full_name, password, roles, contact_number, account_status) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setString(1, username);
            pstmt.setString(2, full_name);
            pstmt.setString(3, password);
            pstmt.setString(4, roles.toUpperCase());
            pstmt.setString(5, contact_number);
            pstmt.executeUpdate();
            System.out.println(
                    "Successful Registration! Waiting for Approval."
            );
            return true;
        } catch (SQLException e) {
            System.err.println("Registration Failed: " + e.getMessage());
            return false;
        }

    }

    public boolean loginUser(String loginUsername, String loginPassword) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginUsername);
            pstmt.setString(2, loginPassword);

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Login Failed: " + e.getMessage());
            return false;
        }
    }
}
