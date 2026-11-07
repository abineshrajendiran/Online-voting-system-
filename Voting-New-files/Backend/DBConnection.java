package com.voting.connection;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Online (Render) values come from environment variables
            String url = System.getenv("DB_URL");
            String username = System.getenv("DB_USER");
            String password = System.getenv("DB_PASS");

            // Not set -> running locally in Eclipse
            if (url == null || url.isEmpty()) {
                url = "jdbc:mysql://localhost:3306/tamilnadu_voting";
                username = "root";
                password = "Abinesh@2005";
            }

            con = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver jar is NOT added to the project");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return con;
    }
}