package com.voting.connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VoteDAO {

    // Has this voter already voted?
    public boolean hasVoted(String voterId) {

        String sql = "SELECT 1 FROM votes WHERE voter_id = ?";

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

    // Save the vote
    public boolean submitVote(String voterId, int partyId) {

        String sql = "INSERT INTO votes (voter_id, party_id) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, voterId);
            ps.setInt(2, partyId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Vote failed: " + e.getMessage());
        }

        return false;
    }

    // Print votes per party
    public void showResults() {

        String sql = "SELECT p.party_name, p.party_code, COUNT(v.vote_id) AS total "
                   + "FROM parties p LEFT JOIN votes v ON p.party_id = v.party_id "
                   + "GROUP BY p.party_id, p.party_name, p.party_code "
                   + "ORDER BY total DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== VOTE RESULTS =====");

            while (rs.next()) {
                System.out.println(rs.getString("party_name")
                        + " (" + rs.getString("party_code") + ") : "
                        + rs.getInt("total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}