package mariadb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MariaDB {
    public static int MAX_REQUEST = 1_000;

    public static Connection getConnection()
    {
        String jdbcURL = "jdbc:mariadb://127.0.0.1:3306/test";
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
