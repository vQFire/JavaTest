package postgresql;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SelectTest {
    private static Connection connection = Postgres.getConnection();

    public static void main(String[] args) throws SQLException {
        Statement statement = connection.createStatement();

        long startTime = System.nanoTime();

        for (int x = 0; x < Postgres.MAX_REQUEST; x++) {
            long individualTimeStart = System.nanoTime();
            Faker faker = new Faker();
            String sql = String.format("SELECT " +
                    "pgp_sym_decrypt(decode(username, 'BASE64'), '12345678','compress-algo=1, cipher-algo=aes256') as username," +
                    "password," +
                    "pgp_sym_decrypt(decode(email, 'BASE64'), '12345678','compress-algo=1, cipher-algo=aes256') as email " +
                    "FROM test WHERE id = %s;", faker.random().nextInt(1, Postgres.MAX_REQUEST));

            ResultSet resultSet = statement.executeQuery(sql);

            // Uncomment als je wilt data wilt zien :)
//            while (resultSet.next()) {
//                System.out.println(resultSet.getString("email"));
//            }

            System.out.println("Trying to execute " + x + ". " + (System.nanoTime() - individualTimeStart) / 1_000_000 + "ms");
        }

        long endTime = System.nanoTime();

        System.out.printf("It took %s seconds", (endTime - startTime) / 1_000_000_000);
        System.out.printf("\nAn average of %sms per request (%s request we made)", (float)((endTime - startTime) / Postgres.MAX_REQUEST) / 1_000_000, Postgres.MAX_REQUEST);
    }
}
