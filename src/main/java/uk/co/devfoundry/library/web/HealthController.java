package uk.co.devfoundry.library.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.co.devfoundry.library.service.SampleDataService;

@RestController
public class HealthController {

    private final SampleDataService sampleData;

    public HealthController(SampleDataService sampleData) {
        this.sampleData = sampleData;
    }

    /** Up and able to reach the database. */
    @GetMapping("/health")
    public Map<String, String> health() {
        sampleData.checkDatabase();
        return Map.of("status", "ok");
    }
}
