package mariadb.postgresql;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SelectTest {
    private static Connection connection = MariaDB.getConnection();

    public static void main(String[] args) throws SQLException {
        Statement statement = connection.createStatement();

        long startTime = System.nanoTime();

        for (int x = 0; x < MariaDB.MAX_REQUEST; x++) {
            long individualTimeStart = System.nanoTime();
            Faker faker = new Faker();

            String sql = String.format("SELECT " +
                    "id, " +
                    "AES_DECRYPT(FROM_BASE64(username), '12345678') as username, " +
                    "password, " +
                    "AES_DECRYPT(FROM_BASE64(email), '12345678') as email " +
                    "FROM test WHERE id = %s", faker.random().nextInt(1, MariaDB.MAX_REQUEST));

            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
            }

            System.out.println("Trying to execute " + x + ". " + (System.nanoTime() - individualTimeStart) / 1_000_000 + "ms");
        }

        long endTime = System.nanoTime();

        System.out.printf("It took %s seconds", (endTime - startTime) / 1_000_000_000);
        System.out.printf("\nAn average of %sms per request (%s request we made)", (float)((endTime - startTime) / MariaDB.MAX_REQUEST) / 1_000_000, MariaDB.MAX_REQUEST);
    }
}
