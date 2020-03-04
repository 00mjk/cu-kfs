package edu.cornell.kfs.pdp.batch;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractDetailResultCode;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public class PayeeACHAccountExtractDetailResult {
    private final ACHExtractDetailResultCode resultCode;
    private final Optional<String> netId;
    private final Optional<Date> originalProcessingDate;
    private final List<PayeeACHAccountExtractDetailSubResult> subResults;
    private final List<String> messages;

    public PayeeACHAccountExtractDetailResult(
            ACHExtractDetailResultCode resultCode, PayeeACHAccountExtractDetail achDetail, String... messages) {
        this(resultCode, achDetail.getNetID(), achDetail.getCreateDate(), Collections.emptyList(), messages);
    }

    public PayeeACHAccountExtractDetailResult(
            ACHExtractDetailResultCode resultCode, PayeeACHAccountExtractDetail achDetail, List<String> messages) {
        this(resultCode, achDetail.getNetID(), achDetail.getCreateDate(), Collections.emptyList(), messages);
    }

    public PayeeACHAccountExtractDetailResult(
            ACHExtractDetailResultCode resultCode, PayeeACHAccountExtractDetail achDetail,
            List<PayeeACHAccountExtractDetailSubResult> subResults, String... messages) {
        this(resultCode, achDetail.getNetID(), achDetail.getCreateDate(), subResults, messages);
    }

    public PayeeACHAccountExtractDetailResult(
            ACHExtractDetailResultCode resultCode, String netId, Date originalProcessingDate,
            List<PayeeACHAccountExtractDetailSubResult> subResults, String... messages) {
        this(resultCode, netId, originalProcessingDate, subResults, Arrays.asList(messages));
    }

    public PayeeACHAccountExtractDetailResult(
            ACHExtractDetailResultCode resultCode, String netId, Date originalProcessingDate,
            List<PayeeACHAccountExtractDetailSubResult> subResults, List<String> messages) {
        this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
        this.netId = Optional.ofNullable(StringUtils.defaultIfBlank(netId, null));
        this.originalProcessingDate = Optional.ofNullable(originalProcessingDate);
        this.subResults = Collections.unmodifiableList(new ArrayList<>(subResults));
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public PayeeACHAccountExtractDetailResult(
            PayeeACHAccountExtractDetailResult originalResult, ACHExtractDetailResultCode newResultCode, String... additionalMessages) {
        this(originalResult, newResultCode, Arrays.asList(additionalMessages));
    }

    public PayeeACHAccountExtractDetailResult(
            PayeeACHAccountExtractDetailResult originalResult, ACHExtractDetailResultCode newResultCode, List<String> additionalMessages) {
        Objects.requireNonNull(originalResult, "originalResult cannot be null");
        this.resultCode = Objects.requireNonNull(newResultCode, "newResultCode cannot be null");
        this.netId = originalResult.netId;
        this.originalProcessingDate = originalResult.originalProcessingDate;
        this.subResults = originalResult.subResults;
        this.messages = Stream.concat(originalResult.messages.stream(), additionalMessages.stream())
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(), Collections::unmodifiableList));
    }
    
    public boolean didProcessingFail() {
        return ACHExtractDetailResultCode.ERROR.equals(resultCode);
    }

    public boolean hasMessages() {
        return messages.size() > 0;
    }

    public ACHExtractDetailResultCode getResultCode() {
        return resultCode;
    }

    public Optional<String> getNetId() {
        return netId;
    }

    public Optional<Date> getOriginalProcessingDate() {
        return originalProcessingDate.map(date -> new Date(date.getTime()));
    }

    public List<PayeeACHAccountExtractDetailSubResult> getSubResults() {
        return subResults;
    }

    public List<String> getMessages() {
        return messages;
    }

}
