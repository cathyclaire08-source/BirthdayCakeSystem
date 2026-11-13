


package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // ==========================================
    // 1. REGISTER USER
    // ==========================================
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

    // ==========================================
    // 2. LOGIN USER
    // ==========================================
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

    // ==========================================
    // 3. GET APPROVED USER ROLE
    // ==========================================
    public String getApprovedUserRole(
            String username,
            String password) {

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

    // ==========================================
    // 4. GET ALL USERS
    // ==========================================
    public List<User> getAllUsers() {

        String sql = """
            SELECT id, full_name, username, roles, account_status
            FROM users
            ORDER BY id
            """;

        return fetchUsers(sql);
    }

    // ==========================================
    // 5. GET PENDING USERS
    // ==========================================
    public List<User> getPendingUsers() {

        String sql = """
            SELECT id, full_name, username, roles, account_status
            FROM users
            WHERE account_status = 'PENDING'
            ORDER BY id
            """;

        return fetchUsers(sql);
    }

    // ==========================================
    // 6. SHARED USER RETRIEVAL
    // ==========================================
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

    // ==========================================
    // 7. APPROVE USER
    // BACKWARD-COMPATIBLE METHOD
    // ==========================================
    public boolean approveUser(int userId) {
        return approveUser(userId, 0);
    }

    // ==========================================
    // 8. APPROVE USER WITH ADMIN AUDIT LOG
    // ==========================================
    public boolean approveUser(int userId, int adminId) {

        String updateSql = """
            UPDATE users
            SET account_status = 'APPROVED'
            WHERE id = ?
              AND account_status = 'PENDING'
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt =
                         conn.prepareStatement(updateSql)) {

                pstmt.setInt(1, userId);

                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated != 1) {
                    conn.rollback();
                    return false;
                }

                String description =
                        "Admin " + adminId
                                + " approved User " + userId;

                insertActivityLog(
                        conn,
                        adminId,
                        "USER_APPROVED",
                        description
                );

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Unable to approve user: " + e.getMessage()
            );

            return false;
        }
    }

    // ==========================================
    // 9. REJECT USER
    // ==========================================
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

    // ==========================================
    // 10. UPDATE USER ROLE AND STATUS
    // BACKWARD-COMPATIBLE METHOD
    // ==========================================
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus) {

        return updateUserRoleAndStatus(
                userId,
                newRole,
                newStatus,
                0
        );
    }

    // ==========================================
    // 11. UPDATE ROLE AND STATUS WITH AUDIT LOG
    // ==========================================
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus,
            int adminId) {

        String updateSql = """
            UPDATE users
            SET roles = ?, account_status = ?
            WHERE id = ?
            """;

        String selectSql = """
            SELECT roles, account_status
            FROM users
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String oldRole;
                String oldStatus;

                try (PreparedStatement select =
                             conn.prepareStatement(selectSql)) {

                    select.setInt(1, userId);

                    try (ResultSet rs = select.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        oldRole = rs.getString("roles");
                        oldStatus = rs.getString("account_status");
                    }
                }

                try (PreparedStatement update =
                             conn.prepareStatement(updateSql)) {

                    update.setString(
                            1,
                            newRole.trim().toUpperCase()
                    );

                    update.setString(
                            2,
                            newStatus.trim().toUpperCase()
                    );

                    update.setInt(3, userId);

                    if (update.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                String description =
                        "Admin " + adminId
                                + " updated User " + userId
                                + " | Role: " + oldRole + " -> " + newRole
                                + " | Status: " + oldStatus + " -> " + newStatus;

                insertActivityLog(
                        conn,
                        adminId,
                        "USER_UPDATED",
                        description
                );

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Unable to update user: " + e.getMessage()
            );

            return false;
        }
    }

    // ==========================================
    // 12. GET APPROVED USER ID
    // ==========================================
    public int getApprovedUserId(
            String username,
            String password) {

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

                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Unable to retrieve user ID: " + e.getMessage()
            );
        }

        return -1;
    }

    // ==========================================
    // 13. INSERT AUDIT LOG
    // SQL REMAINS INSIDE DAO
    // ==========================================
    private void insertActivityLog(
            Connection conn,
            int adminId,
            String actionType,
            String description
    ) throws SQLException {

        String sql = """
            INSERT INTO activity_logs
            (user_id, action_type, description)
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (adminId <= 0) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, adminId);
            }

            pstmt.setString(2, actionType);
            pstmt.setString(3, description);

            pstmt.executeUpdate();
        }
    }
}




