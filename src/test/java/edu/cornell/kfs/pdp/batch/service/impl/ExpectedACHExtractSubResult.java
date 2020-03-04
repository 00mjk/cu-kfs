package edu.cornell.kfs.pdp.batch.service.impl;

import java.util.Objects;
import java.util.Optional;

import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractResultCode;
import edu.cornell.kfs.pdp.batch.fixture.PayeeACHAccountFixture;

public class ExpectedACHExtractSubResult {

    private final ACHExtractResultCode resultCode;
    private final Optional<PayeeACHAccountFixture> oldAchAccount;
    private final PayeeACHAccountFixture achAccount;

    public ExpectedACHExtractSubResult(ACHExtractResultCode resultCode,
            PayeeACHAccountFixture achAccount) {
        this(resultCode, Optional.empty(), achAccount);
    }

    public ExpectedACHExtractSubResult(ACHExtractResultCode resultCode,
            PayeeACHAccountFixture oldAchAccount, PayeeACHAccountFixture achAccount) {
        this(resultCode, Optional.of(oldAchAccount), achAccount);
    }

    public ExpectedACHExtractSubResult(ACHExtractResultCode resultCode,
            Optional<PayeeACHAccountFixture> oldAchAccount, PayeeACHAccountFixture achAccount) {
        this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
        this.oldAchAccount = Objects.requireNonNull(oldAchAccount, "oldAchAccount wrapper cannot be null");
        this.achAccount = Objects.requireNonNull(achAccount, "achAccount cannot be null");
    }

    public ACHExtractResultCode getResultCode() {
        return resultCode;
    }

    public PayeeACHAccountFixture getAchAccount() {
        return achAccount;
    }

    public boolean hasOldAchAccount() {
        return oldAchAccount.isPresent();
    }

    public PayeeACHAccountFixture getOldAchAccount() {
        return oldAchAccount.get();
    }

}
