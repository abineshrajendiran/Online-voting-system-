package com.voting.connection;
import java.sql.*;
import java.util.*;

import com.voting.connection.DBConnection;

public class PartyDAO {
	    public List<Party> getAllParties() {

	        List<Party> list = new ArrayList<>();

	        String sql = "SELECT * FROM parties";

	        try {

	            Connection con =
	                DBConnection.getConnection();

	            Statement st = con.createStatement();

	            ResultSet rs = st.executeQuery(sql);

	            while (rs.next()) {

	                Party p = new Party();

	                p.setPartyId(
	                    rs.getInt("party_id"));

	                p.setPartyName(
	                    rs.getString("party_name"));

	                p.setPartyCode(
	                    rs.getString("party_code"));

	                list.add(p);
	            }

	            con.close();

	        } catch (Exception e) {

	            System.out.println(e);
	        }

	        return list;
	    }
	}

