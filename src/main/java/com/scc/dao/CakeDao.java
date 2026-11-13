

package com.scc.dao;

import com.scc.model.Cake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CakeDao {

    public List<Cake> getAvailableCakes() {
        List<Cake> cakes = new ArrayList<>();

        String sql = """
                SELECT cake_id, cake_name, flavor, size,
                       price, availability_status
                FROM cakes
                WHERE availability_status = 'AVAILABLE'
                ORDER BY cake_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                cakes.add(new Cake(
                        rs.getInt("cake_id"),
                        rs.getString("cake_name"),
                        rs.getString("flavor"),
                        rs.getString("size"),
                        rs.getDouble("price"),
                        rs.getString("availability_status")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Unable to retrieve available cakes: " + e.getMessage());
        }

        return cakes;
    }

    public Cake getCakeById(int cakeId) {
        String sql = """
                SELECT cake_id, cake_name, flavor, size,
                       price, availability_status
                FROM cakes
                WHERE cake_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cakeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Cake(
                            rs.getInt("cake_id"),
                            rs.getString("cake_name"),
                            rs.getString("flavor"),
                            rs.getString("size"),
                            rs.getDouble("price"),
                            rs.getString("availability_status")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Unable to retrieve cake: " + e.getMessage());
        }

        return null;
    }

    public boolean addCake(
            String cakeName,
            String flavor,
            String size,
            double price) {

        if (cakeName == null || cakeName.isBlank()
                || flavor == null || flavor.isBlank()
                || size == null || size.isBlank()
                || price <= 0) {
            return false;
        }

        String sql = """
                INSERT INTO cakes
                    (cake_name, flavor, size, price, availability_status)
                VALUES (?, ?, ?, ?, 'AVAILABLE')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cakeName.trim());
            pstmt.setString(2, flavor.trim());
            pstmt.setString(3, size.trim());
            pstmt.setDouble(4, price);

            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("Unable to add cake: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCakeDetails(
            int cakeId,
            String cakeName,
            String flavor,
            String size,
            double price) {

        if (cakeId <= 0 || cakeName == null || cakeName.isBlank()
                || flavor == null || flavor.isBlank()
                || size == null || size.isBlank()
                || price <= 0) {
            return false;
        }

        String sql = """
                UPDATE cakes
                SET cake_name = ?, flavor = ?, size = ?, price = ?
                WHERE cake_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cakeName.trim());
            pstmt.setString(2, flavor.trim());
            pstmt.setString(3, size.trim());
            pstmt.setDouble(4, price);
            pstmt.setInt(5, cakeId);

            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("Unable to update cake details: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCakeAvailability(
            int cakeId,
            String availabilityStatus) {

        if (cakeId <= 0 || availabilityStatus == null
                || availabilityStatus.isBlank()) {
            return false;
        }

        String status = availabilityStatus.trim().toUpperCase();

        if (!status.equals("AVAILABLE") && !status.equals("UNAVAILABLE")) {
            System.out.println("Invalid cake availability status.");
            return false;
        }

        String sql = """
                UPDATE cakes
                SET availability_status = ?
                WHERE cake_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, cakeId);

            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("Unable to update cake availability: " + e.getMessage());
            return false;
        }
    }
}


