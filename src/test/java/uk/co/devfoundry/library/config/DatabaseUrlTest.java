package uk.co.devfoundry.library.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DatabaseUrlTest {

    @Test
    void defaultIsSqliteInTheWorkingDirectory() {
        DatabaseUrl url = DatabaseUrl.parse(null);

        assertThat(url.kind()).isEqualTo("sqlite");
        assertThat(url.jdbcUrl()).isEqualTo("jdbc:sqlite:library.db");
    }

    @Test
    void sqliteAbsolutePathAndMemory() {
        assertThat(DatabaseUrl.parse("sqlite:////data/library.db").jdbcUrl()).isEqualTo("jdbc:sqlite:/data/library.db");
        assertThat(DatabaseUrl.parse("sqlite://").jdbcUrl()).isEqualTo("jdbc:sqlite::memory:");
    }

    @Test
    void postgresqlUrlBecomesJdbcPlusCredentials() {
        DatabaseUrl url = DatabaseUrl.parse("postgresql://library:library@db:5432/library");

        assertThat(url.kind()).isEqualTo("postgresql");
        assertThat(url.jdbcUrl()).isEqualTo("jdbc:postgresql://db:5432/library");
        assertThat(url.username()).isEqualTo("library");
        assertThat(url.password()).isEqualTo("library");
    }

    @Test
    void unknownSchemeIsRefused() {
        assertThatThrownBy(() -> DatabaseUrl.parse("mysql://x/y")).isInstanceOf(IllegalArgumentException.class);
    }
}
