package uk.co.devfoundry.library.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Repository;

import uk.co.devfoundry.library.config.DatabaseUrl;

/**
 * Loads db/seed.sql (a copy of the contract's sample data) with plain JDBC, keeping its fixed ids,
 * and puts the id counters back afterwards. The SQL differs between SQLite and PostgreSQL.
 */
@Repository
public class SampleDataRepository {

    private static final ClassPathResource SEED = new ClassPathResource("db/seed.sql");

    private final JdbcTemplate jdbc;
    private final boolean postgresql;

    public SampleDataRepository(JdbcTemplate jdbc, @Value("${library.database}") String database) {
        this.jdbc = jdbc;
        this.postgresql = DatabaseUrl.POSTGRESQL.equals(database);
    }

    public boolean isEmpty() {
        Integer books = jdbc.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
        return books == null || books == 0;
    }

    public void ping() {
        jdbc.queryForObject("SELECT 1", Integer.class);
    }

    /** Empty the three tables. On PostgreSQL this also restarts the identity sequences. */
    public void deleteEverything() {
        if (postgresql) {
            jdbc.execute("TRUNCATE loans, members, books RESTART IDENTITY");
        } else {
            jdbc.execute("DELETE FROM loans");
            jdbc.execute("DELETE FROM members");
            jdbc.execute("DELETE FROM books");
        }
    }

    public void insertSampleData() {
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            ScriptUtils.executeSqlScript(connection, SEED);
            return null;
        });
    }

    /**
     * Inserting explicit ids never moves a PostgreSQL sequence, so move each one past the highest id:
     * the next ids are then book 21, member 110 and loan 13. SQLite does this by itself.
     */
    public void moveIdCountersPastExistingRows() {
        if (!postgresql) {
            return;
        }
        for (String table : new String[] {"books", "members", "loans"}) {
            jdbc.queryForObject("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), "
                    + "COALESCE(MAX(id), 0) + 1, false) FROM " + table, Long.class);
        }
    }
}
