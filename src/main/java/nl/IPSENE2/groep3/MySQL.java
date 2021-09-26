package nl.IPSENE2.groep3;

import com.github.javafaker.Faker;
import me.tongfei.progressbar.ProgressBar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {
    private static Statement statement;

    public MySQL(Connection connection) throws SQLException {
        statement = connection.createStatement();

        initializeDB();
        System.out.println("MySQL has been inialized");
        insertTest();
        insertEncryptTest();
        selectTest();
        selectDecryptTest();
    }

    private void initializeDB () throws SQLException {
        String drop = "DROP TABLE IF EXISTS Request, User, Site;";
        String site = """
                CREATE TABLE Site (
                    id INT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL UNIQUE,
                    PRIMARY KEY (id)
                );
                """;
        String user = """
                CREATE TABLE User (
                    id INT NOT NULL AUTO_INCREMENT,
                    site_id INT NOT NULL,
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    enabled integer NOT NULL DEFAULT '1',
                    PRIMARY KEY (id),
                    FOREIGN KEY (site_id) REFERENCES Site(id)
                );
                """;
        String request = """
                CREATE TABLE Request (
                    id INT NOT NULL AUTO_INCREMENT,
                    site_id INT NOT NULL,
                    user_id INT NOT NULL,
                    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status SMALLINT NOT NULL,
                    PRIMARY KEY (id),
                    FOREIGN KEY (site_id) REFERENCES Site(id),
                    FOREIGN KEY (user_id) REFERENCES User(id)
                );
                """;

        statement.addBatch(drop);
        statement.addBatch(site);
        statement.addBatch(user);
        statement.addBatch(request);
        statement.executeBatch();
    }

    private void insertTest () {
        try (ProgressBar pb = new ProgressBar("MySQL|INSERT", Main.MAX_INSERT * 3L)) {
            Faker faker = new Faker();
            pb.setExtraMessage("Sites");
            for (int x = 0; x < Main.MAX_INSERT; x++) {
                String SQL = String.format("""
                INSERT INTO Site (name) VALUES ('%s');
                """, x + " - " + faker.company().name().replace("'", "''"));

                statement.execute(SQL);

                pb.step();
            }

            pb.setExtraMessage("Users");
            for (int x = 0; x < Main.MAX_INSERT; x++) {
                String SQL = String.format("""
                INSERT INTO User (site_id, username, password, email, enabled) VALUES (%s, '%s', '%s', '%s', %s);
                """,    faker.number().numberBetween(1, Main.MAX_INSERT),
                        faker.name().username(),
                        faker.internet().password(),
                        x + faker.internet().emailAddress(),
                        faker.bool().bool() ? 1:0);

                statement.execute(SQL);

                pb.step();
            }

            pb.setExtraMessage("Requests");
            for (int x = 0; x < Main.MAX_INSERT; x++) {
                String SQL = String.format("""
                INSERT INTO Request (site_id, user_id, status) VALUES (%s, %s, %s);
                """,    faker.number().numberBetween(1, Main.MAX_INSERT / 5),
                        faker.number().numberBetween(1, Main.MAX_INSERT / 5 * 3),
                        faker.bool().bool() ? 1:0);

                statement.execute(SQL);
                pb.step();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertEncryptTest () {
        try (ProgressBar pb = new ProgressBar("MySQL|INSERT|CRYPT", Main.MAX_INSERT)) {
            Faker faker = new Faker();
            pb.setExtraMessage("Users");
            for (int x = 0; x < Main.MAX_INSERT; x++) {
                String SQL = String.format("""
                INSERT INTO User (site_id, username, password, email, enabled)
                VALUES (%s,
                TO_BASE64(AES_ENCRYPT('%s', 'SUPER_SECRET')),
                SHA2('%s', 512),
                TO_BASE64(AES_ENCRYPT('%s', 'SUPER_SECRET')),
                %s);
                """,    faker.number().numberBetween(1, Main.MAX_INSERT / 5),
                        faker.name().username(),
                        faker.internet().password(),
                        x + faker.internet().emailAddress(),
                        faker.bool().bool() ? 1:0);

                statement.execute(SQL);

                pb.step();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void selectTest () {
        try (ProgressBar pb = new ProgressBar("MySQL|SELECT", Main.MAX_SELECT * 2L)) {
            Faker faker = new Faker();

            pb.setExtraMessage("SELECT");
            for (int x = 0; x < Main.MAX_SELECT; x++) {
                String SQL = String.format("""
                       SELECT * from Site WHERE id = %s;
                        """, faker.number().numberBetween(1, Main.MAX_INSERT));
                ResultSet result = statement.executeQuery(SQL);
                if (result.next()) pb.step();
            }

            pb.setExtraMessage("JOINS");
            for (int x = 0; x < Main.MAX_SELECT; x++) {
                String SQL = String.format("""
                       SELECT s.name, u.username, r.status from User u
                       JOIN Site s ON u.site_id = s.id
                       LEFT JOIN Request r ON u.id = r.user_id
                       WHERE u.id = %s;
                       """, faker.number().numberBetween(1, Main.MAX_INSERT));
                ResultSet result = statement.executeQuery(SQL);
                if (result.next()) pb.step();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void selectDecryptTest () {
        try (ProgressBar pb = new ProgressBar("MySQL|SELECT|DCRYPT", Main.MAX_SELECT)) {
            Faker faker = new Faker();

            for (int x = 0; x < Main.MAX_SELECT; x++) {
                String SQL = String.format("""
                         SELECT
                             AES_DECRYPT(FROM_BASE64(username), 'SUPER_SECRET') as username,
                             password,
                             AES_DECRYPT(FROM_BASE64(email), 'SUPER_SECRET') as email
                             from User
                         WHERE id = %s;
                        """, faker.number().numberBetween(Main.MAX_INSERT + 1, Main.MAX_INSERT * 2L));
                ResultSet result = statement.executeQuery(SQL);
                if (result.next()) pb.step();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
