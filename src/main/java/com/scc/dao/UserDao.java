
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // Register a new account with PENDING status.
    public boolean registerUser(
            String username,
            String fullName,
            String password,
            String roles,
            String contactNumber) {

        String sql = """
            INSERT INTO users
                (username, full_name, password, roles,
                 contact_number, account_status)
            VALUES (?, ?, ?, ?, ?, 'PENDING')
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, fullName);
            pstmt.setString(3, password);
            pstmt.setString(4, roles.toUpperCase());
            pstmt.setString(5, contactNumber);

            pstmt.executeUpdate();

            System.out.println(
                    "Registration successful! Account is PENDING approval."
            );
            return true;

        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    // Login only when credentials are correct and account is APPROVED.
    public boolean loginUser(String username, String password) {

        String sql = """
            SELECT id
            FROM users
            WHERE username = ?
              AND password = ?
              AND account_status = 'APPROVED'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // Get the role of an approved user.
    public String getApprovedUserRole(
            String username, String password) {

        String sql = """
            SELECT roles
            FROM users
            WHERE username = ?
              AND password = ?
              AND account_status = 'APPROVED'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("roles");
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to retrieve user role: " + e.getMessage()
            );
        }

        return null;
    }

    // Retrieve all users.
    public List<User> getAllUsers() {

        String sql = """
            SELECT id, full_name, username, roles, account_status
            FROM users
            ORDER BY id
            """;

        return fetchUsers(sql);
    }

    // Retrieve accounts awaiting approval.
    public List<User> getPendingUsers() {

        String sql = """
            SELECT id, full_name, username, roles, account_status
            FROM users
            WHERE account_status = 'PENDING'
            ORDER BY id
            """;

        return fetchUsers(sql);
    }

    // Shared method for retrieving users.
    private List<User> fetchUsers(String sql) {

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("roles"),
                        rs.getString("account_status")
                ));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to retrieve users: " + e.getMessage()
            );
        }

        return users;
    }

    // Approve a pending user account.
    public boolean approveUser(int userId) {

        String sql = """
            UPDATE users
            SET account_status = 'APPROVED'
            WHERE id = ?
              AND account_status = 'PENDING'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println(
                    "Unable to approve user: " + e.getMessage()
            );
            return false;
        }
    }

    // Reject a pending user account.
    public boolean rejectUser(int userId) {

        String sql = """
            UPDATE users
            SET account_status = 'REJECTED'
            WHERE id = ?
              AND account_status = 'PENDING'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println(
                    "Unable to reject user: " + e.getMessage()
            );
            return false;
        }
    }
}



