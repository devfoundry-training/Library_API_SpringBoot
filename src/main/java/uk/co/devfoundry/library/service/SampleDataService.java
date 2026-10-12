package uk.co.devfoundry.library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.co.devfoundry.library.repository.SampleDataRepository;

/** The contract's sample data: 20 books, 9 members and 12 loans with fixed ids. */
@Service
@Transactional
public class SampleDataService {

    private final SampleDataRepository sampleData;

    public SampleDataService(SampleDataRepository sampleData) {
        this.sampleData = sampleData;
    }

    /** Loads the sample data when the books table is empty. Returns true when it did. */
    public boolean loadIfEmpty() {
        if (!sampleData.isEmpty()) {
            return false;
        }
        sampleData.insertSampleData();
        sampleData.moveIdCountersPastExistingRows();
        return true;
    }

    /**
     * Puts the sample data back in one transaction, so nobody sees half of it: the next ids are then
     * book 21, member 110 and loan 13.
     */
    public void reset() {
        sampleData.deleteEverything();
        sampleData.insertSampleData();
        sampleData.moveIdCountersPastExistingRows();
    }

    @Transactional(readOnly = true)
    public void checkDatabase() {
        sampleData.ping();
    }
}
