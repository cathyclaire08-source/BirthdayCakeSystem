
package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // ==========================================
    // 1. SAVE ACTIVITY LOG
    // ==========================================
    public void logAction(
            int userId,
            String actionType,
            String description
    ) {

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            if (userId <= 0) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, userId);
            }

            pstmt.setString(2, actionType);
            pstmt.setString(3, description);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(
                    "Failed to save activity log: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // 2. GET ALL SYSTEM LOGS
    // ==========================================
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type, description, timestamp
                FROM activity_logs
                ORDER BY timestamp DESC, id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                logs.add(new ActivityLog(
                        rs.getInt("id"),
                        getNullableUserId(rs),
                        rs.getString("action_type"),
                        rs.getString("description"),
                        rs.getString("timestamp")
                ));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Failed to retrieve activity logs: " + e.getMessage()
            );
        }

        return logs;
    }

    // ==========================================
    // 3. GET LOGS BY USER ID
    // ==========================================
    public List<ActivityLog> getLogsByUserId(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type, description, timestamp
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    logs.add(new ActivityLog(
                            rs.getInt("id"),
                            getNullableUserId(rs),
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("timestamp")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Failed to retrieve user activity logs: " + e.getMessage()
            );
        }

        return logs;
    }

    // ==========================================
    // 4. CLEAR ALL HISTORICAL LOGS
    // ==========================================
    public boolean clearAllLogs() {

        String sql = "DELETE FROM activity_logs";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println(
                    "Failed to clear activity logs: " + e.getMessage()
            );
            return false;
        }
    }

    // ==========================================
    // 5. GET NULLABLE USER ID
    // ==========================================
    private int getNullableUserId(ResultSet rs) throws SQLException {

        int userId = rs.getInt("user_id");

        if (rs.wasNull()) {
            return 0;
        }

        return userId;
    }
}

