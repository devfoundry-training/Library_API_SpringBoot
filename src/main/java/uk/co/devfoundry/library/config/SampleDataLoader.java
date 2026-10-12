package uk.co.devfoundry.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import uk.co.devfoundry.library.service.SampleDataService;

/** On start-up, after Flyway has run the migrations: load the sample data into an empty database. */
@Component
public class SampleDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    private final SampleDataService sampleData;

    public SampleDataLoader(SampleDataService sampleData) {
        this.sampleData = sampleData;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (sampleData.loadIfEmpty()) {
            log.info("Loaded the sample data: 20 books, 9 members and 12 loans");
        }
    }
}
