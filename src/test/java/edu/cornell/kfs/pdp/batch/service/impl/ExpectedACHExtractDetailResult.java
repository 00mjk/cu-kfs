package edu.cornell.kfs.pdp.batch.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractDetailResultCode;
import edu.cornell.kfs.pdp.batch.fixture.ACHRowFixture;

public class ExpectedACHExtractDetailResult {

    private final ACHExtractDetailResultCode resultCode;
    private final ACHRowFixture achRow;
    private final List<ExpectedACHExtractSubResult> expectedSubResults;

    public ExpectedACHExtractDetailResult(ACHExtractDetailResultCode resultCode,
            ACHRowFixture achRow, ExpectedACHExtractSubResult... expectedSubResults) {
        this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
        this.achRow = Objects.requireNonNull(achRow, "achRow cannot be null");
        this.expectedSubResults = Stream.of(expectedSubResults)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public ACHExtractDetailResultCode getResultCode() {
        return resultCode;
    }

    public ACHRowFixture getAchRow() {
        return achRow;
    }

    public List<ExpectedACHExtractSubResult> getExpectedSubResults() {
        return expectedSubResults;
    }

}
