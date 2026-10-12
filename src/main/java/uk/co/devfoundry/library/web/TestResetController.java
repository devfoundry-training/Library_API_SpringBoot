package uk.co.devfoundry.library.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.co.devfoundry.library.service.SampleDataService;

/**
 * POST /test/reset puts the sample data back. The bean only exists when TEST_MODE=1, so otherwise the
 * route is not registered at all and the answer is the normal 404 not_found.
 */
@RestController
@ConditionalOnProperty(name = "library.test-mode", havingValue = "true")
public class TestResetController {

    private final SampleDataService sampleData;

    public TestResetController(SampleDataService sampleData) {
        this.sampleData = sampleData;
    }

    @PostMapping("/test/reset")
    public ResponseEntity<Void> reset() {
        sampleData.reset();
        return ResponseEntity.noContent().build();
    }
}
