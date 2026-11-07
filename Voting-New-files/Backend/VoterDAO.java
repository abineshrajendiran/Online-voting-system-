package com.voting.connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VoterDAO {

    // Format: 3 capital letters + 7 digits, e.g. SYK2121150
    public static boolean isValidFormat(String voterId) {
        return voterId != null && voterId.matches("^[A-Z]{3}[0-9]{7}$");
    }

    // Is this voter ID already in the database?
    public boolean exists(String voterId) {

        String sql = "SELECT 1 FROM voters WHERE voter_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, voterId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Register a new voter
    public boolean register(String voterId, String name, String password) {

        String sql = "INSERT INTO voters (voter_id, voter_name, password) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, voterId);
            ps.setString(2, name);
            ps.setString(3, password);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }

        return false;
    }

    // Returns the Voter if ID and password match, otherwise null
    public Voter login(String voterId, String password) {

        String sql = "SELECT voter_id, voter_name FROM voters "
                   + "WHERE voter_id = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, voterId);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Voter v = new Voter();
                    v.setVoterId(rs.getString("voter_id"));
                    v.setVoterName(rs.getString("voter_name"));
                    return v;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}