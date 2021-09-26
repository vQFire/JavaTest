package nl.IPSENE2.groep3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static Integer MAX_INSERT = 25_000;
    public static Integer MAX_SELECT = 100_000;

    public static void main(String[] args) {
        Connection postgresqlConnection;
        Connection mariadbConnection;
        Connection mysqlConnection;

        try {
            String postgresqlJDBC = "jdbc:postgresql://127.0.0.1:5433/test_db?user=root&password=root";
            new Postgresql(DriverManager.getConnection(postgresqlJDBC));

            String mariadb = "jdbc:mariadb://127.0.0.1:3307/test?user=root&password=root";
            new MariaDB(DriverManager.getConnection(mariadb));

            String mysql = "jdbc:mysql://127.0.0.1:3306/test?user=root&password=root";
            new MySQL(DriverManager.getConnection(mysql));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
