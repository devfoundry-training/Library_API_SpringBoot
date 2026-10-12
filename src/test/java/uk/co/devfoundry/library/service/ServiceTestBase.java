package uk.co.devfoundry.library.service;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import uk.co.devfoundry.library.repository.SampleDataRepository;

/**
 * Service tests: no web server and no clock. Each test runs against an in-memory SQLite database built by
 * the Flyway migrations, starts from the sample data and is rolled back afterwards. Today is passed in.
 */
@DataJpaTest(properties = "DATABASE_URL=sqlite://")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookService.class, MemberService.class, LoanService.class, SampleDataService.class,
        SampleDataRepository.class})
abstract class ServiceTestBase {

    /** The fixed date of the task pack and RULES.md. */
    static final LocalDate TODAY = LocalDate.of(2026, 10, 12);

    @Autowired
    SampleDataService sampleData;

    @Autowired
    BookService books;

    @Autowired
    MemberService members;

    @Autowired
    LoanService loans;

    @BeforeEach
    void loadSampleData() {
        sampleData.reset();
    }

    static LocalDate date(String text) {
        return LocalDate.parse(text);
    }
}
