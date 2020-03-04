package edu.cornell.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractResultCode;

public class PayeeACHAccountExtractDetailSubResult {
    private final ACHExtractResultCode resultCode;
    private final String payeeId;
    private final String payeeType;
    private final Optional<String> documentNumber;
    private final List<String> messages;

    public PayeeACHAccountExtractDetailSubResult(
            ACHExtractResultCode resultCode, PayeeACHAccount achAccount, MaintenanceDocument maintenanceDocument) {
        this(resultCode, achAccount.getPayeeIdNumber(), achAccount.getPayeeIdentifierTypeCode(),
                maintenanceDocument.getDocumentNumber(), Collections.emptyList());
    }

    public PayeeACHAccountExtractDetailSubResult(
            ACHExtractResultCode resultCode, PayeeACHAccount achAccount, String... messages) {
        this(resultCode, achAccount.getPayeeIdNumber(), achAccount.getPayeeIdentifierTypeCode(), messages);
    }

    public PayeeACHAccountExtractDetailSubResult(
            ACHExtractResultCode resultCode, String payeeId, String payeeType, String... messages) {
        this(resultCode, payeeId, payeeType, null, Arrays.asList(messages));
    }

    public PayeeACHAccountExtractDetailSubResult(ACHExtractResultCode resultCode, String payeeId, String payeeType,
            String documentNumber, List<String> messages) {
        this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
        this.payeeId = Objects.requireNonNull(payeeId, "payeeId cannot be null");
        this.payeeType = Objects.requireNonNull(payeeType, "payeeType cannot be null");
        this.documentNumber = Optional.ofNullable(documentNumber);
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public boolean didProcessingFail() {
        return ACHExtractResultCode.ERROR.equals(resultCode);
    }

    public ACHExtractResultCode getResultCode() {
        return resultCode;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public String getPayeeType() {
        return payeeType;
    }

    public Optional<String> getDocumentNumber() {
        return documentNumber;
    }

    public List<String> getMessages() {
        return messages;
    }

}
