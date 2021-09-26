package nl.IPSENE2.groep3;


import com.github.javafaker.Faker;
import me.tongfei.progressbar.ProgressBar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Postgresql {
    private final Statement statement;

    public Postgresql (Connection connection) throws SQLException {
        statement = connection.createStatement();

        initializeDB();
        System.out.println("PostgreSQL has been initialized");

        insertTest();
        insertEncryptTest();
        selectTest();
        selectDecryptTest();
    }

    private void initializeDB () throws SQLException {
        String sql = """
                CREATE EXTENSION IF NOT EXISTS pgcrypto;
                DROP TABLE IF EXISTS "User", Request, Site;
                CREATE TABLE Site (
                    id          SERIAL,
                    name        varchar(100) NOT NULL UNIQUE,
                    PRIMARY KEY (id)
                );
                
                CREATE TABLE "User" (
                    id          SERIAL,
                    site_id     integer,
                    username    varchar(255) NOT NULL,
                    password    varchar(255) NOT NULL,
                    email       varchar(255) NOT NULL UNIQUE,
                    enabled     integer NOT NULL DEFAULT '1',
                    PRIMARY KEY (id),
                    FOREIGN KEY (site_id) REFERENCES Site(id)
                );
                
                CREATE TABLE Request (
                    id          SERIAL,
                    site_id     integer NOT NULL,
                    user_id     integer NOT NULL,
                    created_at  date NOT NULL DEFAULT CURRENT_DATE,
                    status      integer,
                    PRIMARY KEY (id),
                    FOREIGN KEY (user_id) REFERENCES "User"(id),
                    FOREIGN KEY (site_id) REFERENCES Site(id)
                );
                """;

        statement.execute(sql);
    }

    private void insertTest () {
        try (ProgressBar pb = new ProgressBar("PSQL|INSERT", Main.MAX_INSERT * 3L)) {
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
                INSERT INTO "User" (site_id, username, password, email, enabled) VALUES (%s, '%s', '%s', '%s', %s);
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
                INSERT INTO Request (site_id, user_id, created_at, status) VALUES (%s, %s, '%s', %s);
                """,    faker.number().numberBetween(1, Main.MAX_INSERT),
                        faker.number().numberBetween(1, Main.MAX_INSERT),
                        faker.date().birthday(),
                        faker.bool().bool() ? 1:0);

                statement.execute(SQL);

                pb.step();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertEncryptTest () {
        try (ProgressBar pb = new ProgressBar("PSQL|INSERT|CRYPT", Main.MAX_INSERT)) {
            pb.setExtraMessage("Users...");
            for (int x = 0; x < Main.MAX_INSERT; x++) {
                Faker faker = new Faker();
                String SQL = String.format("""
                INSERT INTO "User" (site_id, username, password, email, enabled)
                VALUES (%s,
                encode(pgp_sym_encrypt('%s', 'SUPER_SECRET','compress-algo=1, cipher-algo=aes256'),'BASE64'),
                crypt('%s', gen_salt('md5')),
                encode(pgp_sym_encrypt('%s', 'SUPER_SECRET','compress-algo=1, cipher-algo=aes256'),'BASE64'),
                %s);
                """,    faker.number().numberBetween(1, Main.MAX_INSERT),
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
        try (ProgressBar pb = new ProgressBar("PSQL|SELECT", Main.MAX_SELECT * 2L)) {
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
                       SELECT s.name, u.username, r.status from "User" u
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
        try (ProgressBar pb = new ProgressBar("PSQL|SELECT|DCRYPT", Main.MAX_SELECT)) {
            Faker faker = new Faker();

            for (int x = 0; x < Main.MAX_SELECT; x++) {
                String SQL = String.format("""
                         SELECT
                             pgp_sym_decrypt(decode(username, 'BASE64'), 'SUPER_SECRET','compress-algo=1, cipher-algo=aes256') as username,
                             password,
                             pgp_sym_decrypt(decode(email, 'BASE64'), 'SUPER_SECRET','compress-algo=1, cipher-algo=aes256') as email
                             from "User"
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
