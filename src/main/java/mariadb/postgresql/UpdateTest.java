package mariadb.postgresql;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateTest {
    private static Connection connection = MariaDB.getConnection();

    public static void main(String[] args) throws SQLException {
        Statement statement = connection.createStatement();

        long startTime = System.nanoTime();

        for (int x = 0; x < MariaDB.MAX_REQUEST; x++) {
            long individualTimeStart = System.nanoTime();
            Faker faker = new Faker();
            String sql = String.format("UPDATE test " +
                            "SET email = TO_BASE64(AES_ENCRYPT('%s', '12345678')) " +
                            "WHERE id=%s",
                    faker.internet().emailAddress(),
                    faker.random().nextInt(1, MariaDB.MAX_REQUEST)
                    );

            statement.execute(sql);
            System.out.println("Trying to execute " + x + ". " + (System.nanoTime() - individualTimeStart) / 1_000_000 + "ms");
        }

        long endTime = System.nanoTime();

        System.out.printf("It took %s seconds", (endTime - startTime) / 1_000_000_000);
        System.out.printf("\nAn average of %sms per request (%s request we made)", (float)((endTime - startTime) / MariaDB.MAX_REQUEST) / 1_000_000, MariaDB.MAX_REQUEST);
    }
}
