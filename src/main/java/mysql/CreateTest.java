package mysql;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTest {
    private static Connection connection = MySQL.getConnection();

    public static void main(String[] args) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS test;");

        String create_table =
                "CREATE TABLE test (\n" +
                "  id INT NOT NULL AUTO_INCREMENT,\n" +
                "  username VARCHAR(255) NOT NULL,\n" +
                "  password VARCHAR(255) NOT NULL,\n" +
                "  email VARCHAR (255) NOT NULL UNIQUE,\n" +
                "  enabled INT NOT NULL DEFAULT '1',\n" +
                "  PRIMARY KEY (id)\n" +
                ")";

        statement.execute(create_table);

        long startTime = System.nanoTime();

        for (int x = 0; x < MySQL.MAX_REQUEST; x++) {
            long individualTimeStart = System.nanoTime();
            Faker faker = new Faker();
            String sql = String.format("""
                            INSERT INTO test (username, password, email, enabled)\s
                            VALUES (
                                TO_BASE64(AES_ENCRYPT('%s', UNHEX(SHA2('12345678',512)))),\s
                                SHA2('%s', 512),\s
                                TO_BASE64(AES_ENCRYPT('%s', UNHEX(SHA2('12345678',512)))),\s
                                %s
                            );""",
                    faker.name().username(),
                    faker.internet().password(),
                    faker.internet().emailAddress(),
                    faker.bool().bool() ? 1 : 0);

            statement.execute(sql);
            System.out.println("Trying to execute " + x + ". " + (System.nanoTime() - individualTimeStart) / 1_000_000 + "ms");
        }

        long endTime = System.nanoTime();

        System.out.printf("It took %s seconds", (endTime - startTime) / 1_000_000_000);
        System.out.printf("\nAn average of %sms per request (%s request we made)", (float)((endTime - startTime) / MySQL.MAX_REQUEST) / 1_000_000, MySQL.MAX_REQUEST);
    }
}
