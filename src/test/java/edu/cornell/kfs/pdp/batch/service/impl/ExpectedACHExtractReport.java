package edu.cornell.kfs.pdp.batch.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpectedACHExtractReport {

    private final ExpectedACHExtractGroupResult expectedReprocessedRowsResult;
    private final List<ExpectedACHExtractGroupResult> expectedFileResults;

    public ExpectedACHExtractReport(ExpectedACHExtractGroupResult expectedReprocessedRowsResult,
            ExpectedACHExtractGroupResult... expectedFileResults) {
        this.expectedReprocessedRowsResult = Objects.requireNonNull(
                expectedReprocessedRowsResult, "expectedReprocessedRowsResult cannot be null");
        this.expectedFileResults = Stream.of(expectedFileResults)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public ExpectedACHExtractGroupResult getExpectedReprocessedRowsResult() {
        return expectedReprocessedRowsResult;
    }

    public List<ExpectedACHExtractGroupResult> getExpectedFileResults() {
        return expectedFileResults;
    }

}
