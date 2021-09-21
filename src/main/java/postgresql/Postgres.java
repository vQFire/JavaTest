package postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Postgres {
    public static Connection getConnection()
    {
        String jdbcURL = "jdbc:postgresql://80.101.212.152:7273/test_db";
        String username = "root";
        String password = "root";
        try {
            System.out.println("Trying to connect...");

            return DriverManager.getConnection(jdbcURL, username, password);
        } catch (SQLException e) {
            System.out.println("Error");

            e.printStackTrace();
        }

        return null;
    }
}
