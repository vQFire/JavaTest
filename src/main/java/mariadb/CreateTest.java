package mariadb;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTest {
    private static Connection connection = MariaDB.getConnection();

    public static void main(String[] args) throws SQLException {
        Statement statement = connection.createStatement();
        String create_table =
//                "SET GLOBAL innodb_encryption_threads=4; \n" +
//                "SET GLOBAL innodb_encrypt_tables=ON; \n" +
//                "SET SESSION innodb_default_encryption_key_id=1; \n" +
                "CREATE OR REPLACE TABLE test (\n" +
                "  id MEDIUMINT NOT NULL AUTO_INCREMENT,\n" +
                "  username char(255) NOT NULL,\n" +
                "  password char(255) NOT NULL,\n" +
                "  email char(255) NOT NULL UNIQUE,\n" +
                "  enabled integer NOT NULL DEFAULT '1',\n" +
                "  PRIMARY KEY (id)\n" +
                ")";
        statement.execute(create_table);

        long startTime = System.nanoTime();

        for (int x = 0; x < MariaDB.MAX_REQUEST; x++) {
            long individualTimeStart = System.nanoTime();
            Faker faker = new Faker();
            String sql = String.format("INSERT INTO test (username, password, email, enabled)" +
                            "VALUES (TO_BASE64(AES_ENCRYPT('%s', '12345678'))," +
                            "ENCRYPT('%s')," +
                            "TO_BASE64(AES_ENCRYPT('%s', '12345678'))," +
                            "%s)",
                    faker.name().username(),
                    faker.internet().password(),
                    faker.internet().emailAddress(),
                    faker.bool().bool() ? 1 : 0);

            statement.execute(sql);
            System.out.println("Trying to execute " + x + ". " + (System.nanoTime() - individualTimeStart) / 1_000_000 + "ms");
        }

        long endTime = System.nanoTime();

        System.out.printf("It took %s seconds", (endTime - startTime) / 1_000_000_000);
        System.out.printf("\nAn average of %sms per request (%s request we made)", (float)((endTime - startTime) / MariaDB.MAX_REQUEST) / 1_000_000, MariaDB.MAX_REQUEST);
    }
}
