package mysql;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DeleteTest {
    private static Connection connection = MySQL.getConnection();

    public static void main(String[] args) throws SQLException {
        Statement statement = connection.createStatement();

        long startTime = System.nanoTime();

        for (int x = 0; x < MySQL.MAX_REQUEST / 2; x++) {
            long individualTimeStart = System.nanoTime();
            Faker faker = new Faker();
            String sql = String.format("DELETE FROM test WHERE id = %s",
                    faker.random().nextInt(1, MySQL.MAX_REQUEST / 2)
                    );

            statement.execute(sql);
            System.out.println("Trying to execute " + x + ". " + (System.nanoTime() - individualTimeStart) / 1_000_000 + "ms");
        }

        long endTime = System.nanoTime();

        System.out.printf("It took %s seconds", (endTime - startTime) / 1_000_000_000);
        System.out.printf("\nAn average of %sms per request (%s request we made)", (float)((endTime - startTime) / MySQL.MAX_REQUEST) / 1_000_000, MySQL.MAX_REQUEST);
    }
}
