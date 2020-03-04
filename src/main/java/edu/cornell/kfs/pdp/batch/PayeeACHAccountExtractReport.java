package edu.cornell.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PayeeACHAccountExtractReport {

    private final PayeeACHAccountExtractGroupResult reprocessedRowResults;
    private final List<PayeeACHAccountExtractGroupResult> fileResults;

    public PayeeACHAccountExtractReport(PayeeACHAccountExtractGroupResult reprocessedRowResults,
            List<PayeeACHAccountExtractGroupResult> fileResults) {
        this.reprocessedRowResults = Objects.requireNonNull(reprocessedRowResults, "reprocessedRowResults cannot be null");
        this.fileResults = Collections.unmodifiableList(new ArrayList<>(fileResults));
    }

    public PayeeACHAccountExtractGroupResult getReprocessedRowResults() {
        return reprocessedRowResults;
    }

    public List<PayeeACHAccountExtractGroupResult> getFileResults() {
        return fileResults;
    }

}
