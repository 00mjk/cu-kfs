package edu.cornell.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractGroupResultCode;

public class PayeeACHAccountExtractGroupResult {

    private final ACHExtractGroupResultCode resultCode;
    private final Optional<String> fileName;
    private final List<PayeeACHAccountExtractDetailResult> detailResults;
    private final List<String> groupMessages;

    public PayeeACHAccountExtractGroupResult(
            ACHExtractGroupResultCode resultCode, String fileName, List<PayeeACHAccountExtractDetailResult> detailResults,
            String... groupMessages) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName cannot be blank");
        }
        this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
        this.fileName = Optional.of(fileName);
        this.detailResults = Collections.unmodifiableList(new ArrayList<>(detailResults));
        this.groupMessages = Collections.unmodifiableList(Arrays.asList(groupMessages));
    }

    public PayeeACHAccountExtractGroupResult(
            ACHExtractGroupResultCode resultCode, List<PayeeACHAccountExtractDetailResult> detailResults,
            String... groupMessages) {
        this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
        this.fileName = Optional.empty();
        this.detailResults = Collections.unmodifiableList(new ArrayList<>(detailResults));
        this.groupMessages = Collections.unmodifiableList(Arrays.asList(groupMessages));
    }

    public ACHExtractGroupResultCode getResultCode() {
        return resultCode;
    }

    public boolean isFileProcessingResult() {
        return fileName.isPresent();
    }

    public String getFileName() {
        return fileName.get();
    }

    public List<PayeeACHAccountExtractDetailResult> getDetailResults() {
        return detailResults;
    }

    public List<String> getGroupMessages() {
        return groupMessages;
    }

}
