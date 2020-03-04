package edu.cornell.kfs.pdp.batch.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractGroupResultCode;
import edu.cornell.kfs.pdp.CUPdpTestConstants;

public class ExpectedACHExtractGroupResult {

    private final ACHExtractGroupResultCode resultCode;
    private final Optional<String> baseFileName;
    private final List<ExpectedACHExtractDetailResult> expectedDetailResults;

    public ExpectedACHExtractGroupResult(ACHExtractGroupResultCode resultCode,
            ExpectedACHExtractDetailResult... expectedDetailResults) {
        this(resultCode, null, expectedDetailResults);
    }

    public ExpectedACHExtractGroupResult(ACHExtractGroupResultCode resultCode,
            String baseFileName, ExpectedACHExtractDetailResult... expectedDetailResults) {
        this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
        this.baseFileName = Optional.ofNullable(
                StringUtils.defaultIfBlank(baseFileName, null));
        this.expectedDetailResults = Stream.of(expectedDetailResults)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public ACHExtractGroupResultCode getResultCode() {
        return resultCode;
    }

    public boolean isFileResult() {
        return baseFileName.isPresent();
    }

    public String getBaseFileName() {
        return baseFileName.get();
    }

    public String getExpectedFileName() {
        return getBaseFileName() + CUPdpTestConstants.CSV_FILE_EXTENSION;
    }

    public List<ExpectedACHExtractDetailResult> getExpectedDetailResults() {
        return expectedDetailResults;
    }

}
