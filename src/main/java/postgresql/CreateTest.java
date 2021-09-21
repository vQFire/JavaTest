package postgresql;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTest {
    private static Connection connection = Postgres.getConnection();
    private static final int MAX_REQUEST = 10_000;

    public static void main(String[] args) throws SQLException {
        Statement statement = connection.createStatement();
        String create_table = "DROP TABLE test; " +
                "CREATE TABLE test (\n" +
                "  username varchar(450) NOT NULL,\n" +
                "  password varchar(450) NOT NULL,\n" +
                "  email varchar(255) NOT NULL UNIQUE,\n" +
                "  enabled integer NOT NULL DEFAULT '1',\n" +
                "  PRIMARY KEY (username)\n" +
                ")";
        statement.execute(create_table);

        long startTime = System.nanoTime();

        for (int x = 0; x < MAX_REQUEST; x++) {
            long individualTimeStart = System.nanoTime();
            Faker faker = new Faker();
            String sql = String.format("INSERT INTO test (username, password, email, enabled)" +
                            "VALUES (encode(pgp_sym_encrypt('%s', '12345678','compress-algo=1, cipher-algo=aes256'),'BASE64')," +
                            "crypt('%s', gen_salt('md5'))," +
                            "encode(pgp_sym_encrypt('%s', '12345678','compress-algo=1, cipher-algo=aes256'),'BASE64')," +
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
        System.out.printf("\nAn average of %sms per request (%s request we made)", (float)((endTime - startTime) / MAX_REQUEST) / 1_000_000, MAX_REQUEST);
    }
}
