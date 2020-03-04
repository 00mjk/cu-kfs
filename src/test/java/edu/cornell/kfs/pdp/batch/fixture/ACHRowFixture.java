package edu.cornell.kfs.pdp.batch.fixture;

import java.sql.Date;
import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpConstants.PayeeAchAccountExtractStatuses;
import edu.cornell.kfs.pdp.CUPdpTestConstants;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public enum ACHRowFixture {
    JOHN_DOE_VALID_ROW(PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW, "01/01/2016"),
    JANE_DOE_VALID_ROW(PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW, "01/01/2016"),
    JANE_DOE_VALID_ALT_ROW(PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_ALT_NEW, "01/01/2016"),
    ROBERT_SMITH_VALID_ROW(PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_EMPLOYEE_NEW, "01/01/2016"),
    ROBERT_SMITH_VALID_ALT_ROW(PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_EMPLOYEE_ALT_NEW, "01/01/2016"),
    MARY_SMITH_VALID_ROW(PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW, "01/01/2016"),
    JACK_BROWN_VALID_ROW(PayeeACHAccountFixture.JACK_BROWN_SAVINGS_ACCOUNT_EMPLOYEE_NEW, "01/01/2016"),
    
    MARY_SMITH_MATCHING_ROW(PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD, "01/01/2016"),
    
    JOHN_DOE_BAD_EMPLOYEE_ID(JOHN_DOE_VALID_ROW, fieldOverride(RowField.EMPLOYEE_ID, "1122444")),
    JOHN_DOE_BAD_NET_ID(JOHN_DOE_VALID_ROW, fieldOverride(RowField.NET_ID, "jaja99")),
    JANE_DOE_BAD_PAYMENT_TYPE(JANE_DOE_VALID_ROW, fieldOverride(RowField.PAYMENT_TYPE, "Indirect Deposit")),
    MARY_SMITH_BAD_BALANCE_ACCOUNT(MARY_SMITH_VALID_ROW, fieldOverride(RowField.BALANCE_ACCOUNT, "0")),
    ROBERT_SMITH_BAD_ROUTING_NUMBER(ROBERT_SMITH_VALID_ROW, fieldOverride(RowField.BANK_ROUTING_NUMBER, "101010101")),
    JACK_BROWN_BAD_ACCOUNT_TYPE(JACK_BROWN_VALID_ROW, fieldOverride(RowField.BANK_ACCOUNT_TYPE, "Unknown")),
    SYSTEM_USER_BAD_MULTI_FIELDS("1122444", "kfs", "SYSTEMUSER" , "KFS", "Direct Deposit" , "1", "01/01/2016",
            "First Bank", "101010101", "44333222111", "Checking"),

    BAD_EMPTY_ROW(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY),
    JOHN_DOE_BAD_NO_EMPLOYEE_ID(JOHN_DOE_VALID_ROW, fieldOverride(RowField.EMPLOYEE_ID, StringUtils.EMPTY)),
    JANE_DOE_BAD_NO_NET_ID(JANE_DOE_VALID_ROW, fieldOverride(RowField.NET_ID, StringUtils.EMPTY)),
    JANE_DOE_VALID_NO_LAST_NAME(JANE_DOE_VALID_ROW, fieldOverride(RowField.LAST_NAME, StringUtils.EMPTY)),
    MARY_SMITH_VALID_NO_FIRST_NAME(MARY_SMITH_VALID_ROW, fieldOverride(RowField.FIRST_NAME, StringUtils.EMPTY)),
    ROBERT_SMITH_BAD_NO_PAYMENT_TYPE(ROBERT_SMITH_VALID_ROW, fieldOverride(RowField.PAYMENT_TYPE, StringUtils.EMPTY)),
    JACK_BROWN_BAD_NO_BALANCE_ACCOUNT(JACK_BROWN_VALID_ROW, fieldOverride(RowField.BALANCE_ACCOUNT, StringUtils.EMPTY)),

    JOHN_DOE_VALID_NO_COMPLETED_DATE(JOHN_DOE_VALID_ROW, fieldOverride(RowField.COMPLETED_DATE, StringUtils.EMPTY)),
    JANE_DOE_BAD_NO_BANK_NAME(JANE_DOE_VALID_ROW, fieldOverride(RowField.BANK_NAME, StringUtils.EMPTY)),
    MARY_SMITH_BAD_NO_ROUTING_NUMBER(MARY_SMITH_VALID_ROW, fieldOverride(RowField.BANK_ROUTING_NUMBER, StringUtils.EMPTY)),
    ROBERT_SMITH_BAD_NO_ACCOUNT_NUMBER(ROBERT_SMITH_VALID_ROW, fieldOverride(RowField.BANK_ACCOUNT_NUMBER, StringUtils.EMPTY)),
    JACK_BROWN_BAD_NO_ACCOUNT_TYPE(JACK_BROWN_VALID_ROW, fieldOverride(RowField.BANK_ACCOUNT_TYPE, StringUtils.EMPTY)),

    BAD_USER("1122111", "laa111", "Doe1", "John", "Directly Deposited", "0",
            "01/01/2016", "First Bank", "109876543", "44333222111", "Loan"),
    JOHN_DOE_BAD_MULTI_EMPTY_FIELDS(ACHPersonPayeeFixture.JOHN_DOE.employeeId, ACHPersonPayeeFixture.JOHN_DOE.principalName,
            ACHPersonPayeeFixture.JOHN_DOE.lastName, ACHPersonPayeeFixture.JOHN_DOE.firstName,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY),
    JANE_DOE_VALID_FORMATTED_ACCOUNT_NUMBER(JANE_DOE_VALID_ROW,
            fieldOverride(RowField.BANK_ACCOUNT_NUMBER, "66 5-55-444-3--3 3")),
    MARY_SMITH_BAD_ROUTING_NUMBER(MARY_SMITH_VALID_ROW, fieldOverride(RowField.BANK_ROUTING_NUMBER, "A66777888")),
    ROBERT_SMITH_BAD_ACCOUNT_NUMBER(ROBERT_SMITH_VALID_ROW, fieldOverride(RowField.BANK_ACCOUNT_NUMBER, "A2345554321")),
    JACK_BROWN_VALID_ALT_BANK_NAME(JACK_BROWN_VALID_ROW, fieldOverride(RowField.BANK_NAME, "Second Bank & Trust")),

    JANE_DOE_BAD_LENGTHY_ACCOUNT_NUMBER(JANE_DOE_VALID_ROW,
            fieldOverride(RowField.BANK_ACCOUNT_NUMBER, "66555444333222111000")),

    MARY_SMITH_VALID_MATCHING_SAVED_ROW(MARY_SMITH_MATCHING_ROW,
            "03/12/2020", PayeeAchAccountExtractStatuses.OPEN, 0),

    JOHN_DOE_VALID_PROCESSED_SAVED_ROW(JOHN_DOE_VALID_ROW,
            "03/13/2020", PayeeAchAccountExtractStatuses.PROCESSED, 1),
    JACK_BROWN_BAD_CANCELED_SAVED_ROW(JACK_BROWN_BAD_ACCOUNT_TYPE,
            "03/13/2020", PayeeAchAccountExtractStatuses.CANCELED, CUPdpTestConstants.MAX_ACCOUNT_EXTRACT_RETRY),

    JOHN_DOE_VALID_SAVED_ROW_SECOND_RETRY(JOHN_DOE_VALID_ROW,
            "03/14/2020", PayeeAchAccountExtractStatuses.OPEN, 1),
    MARY_SMITH_BAD_SAVED_ROW_NEAR_MAX_RETRY(MARY_SMITH_BAD_BALANCE_ACCOUNT,
            "03/14/2020", PayeeAchAccountExtractStatuses.OPEN, CUPdpTestConstants.MAX_ACCOUNT_EXTRACT_RETRY - 1),
    ROBERT_SMITH_BAD_SAVED_ROW_MAX_RETRY(ROBERT_SMITH_BAD_ROUTING_NUMBER,
            "03/14/2020", PayeeAchAccountExtractStatuses.OPEN, CUPdpTestConstants.MAX_ACCOUNT_EXTRACT_RETRY),

    JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY(JOHN_DOE_VALID_ROW,
            "03/15/2020", PayeeAchAccountExtractStatuses.OPEN, 0),
    JANE_DOE_VALID_SAVED_ROW_SECOND_RETRY(JANE_DOE_VALID_ROW,
            "03/15/2020", PayeeAchAccountExtractStatuses.OPEN, 1),
    MARY_SMITH_VALID_SAVED_ROW_NEAR_MAX_RETRY(MARY_SMITH_VALID_ROW,
            "03/15/2020", PayeeAchAccountExtractStatuses.OPEN, CUPdpTestConstants.MAX_ACCOUNT_EXTRACT_RETRY - 1),
    ROBERT_SMITH_VALID_SAVED_ROW_MAX_RETRY(ROBERT_SMITH_VALID_ROW,
            "03/15/2020", PayeeAchAccountExtractStatuses.OPEN, CUPdpTestConstants.MAX_ACCOUNT_EXTRACT_RETRY),
    JACK_BROWN_VALID_SAVED_ROW_BEYOND_MAX_RETRY(JACK_BROWN_VALID_ROW,
            "03/15/2020", PayeeAchAccountExtractStatuses.OPEN, CUPdpTestConstants.MAX_ACCOUNT_EXTRACT_RETRY + 1);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("MM/dd/yyyy");

    public final Optional<String> createDate;
    public final Optional<String> status;
    public final Optional<Integer> retryCount;
    public final String employeeId;
    public final String netId;
    public final String lastName;
    public final String firstName;
    public final String paymentType;
    public final String balanceAccount;
    public final String completedDate;
    public final String bankName;
    public final String bankRoutingNumber;
    public final String bankAccountNumber;
    public final String bankAccountType;

    private ACHRowFixture(PayeeACHAccountFixture account, String completedDate) {
        this(account.payeeFixture.employeeId, account.payeeFixture.principalName, account.payeeFixture.lastName,
                account.payeeFixture.firstName, CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE,
                CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_BALANCE_ACCOUNT_YES_INDICATOR, completedDate,
                account.bank.bankName, account.bank.bankRoutingNumber, account.bankAccountNumber,
                account.getBankAccountTypeForACHExtractDetail());
    }

    private ACHRowFixture(ACHRowFixture baseRow, String createDate, String status, Integer retryCount) {
        this(Optional.of(createDate), Optional.of(status), Optional.of(retryCount),
                baseRow.employeeId, baseRow.netId, baseRow.lastName, baseRow.firstName, baseRow.paymentType,
                baseRow.balanceAccount, baseRow.completedDate, baseRow.bankName, baseRow.bankRoutingNumber,
                baseRow.bankAccountNumber, baseRow.bankAccountType);
    }

    @SafeVarargs
    private ACHRowFixture(ACHRowFixture baseRow, Pair<RowField, String>... fieldOverrides) {
        this(baseRow, baseRow.createDate, baseRow.status, baseRow.retryCount,
                buildFieldOverrideMap(fieldOverrides));
    }

    @SafeVarargs
    private ACHRowFixture(ACHRowFixture baseRow, String createDate, String status, Integer retryCount,
            Pair<RowField, String>... fieldOverrides) {
        this(baseRow, Optional.of(createDate), Optional.of(status), Optional.of(retryCount),
                buildFieldOverrideMap(fieldOverrides));
    }

    private ACHRowFixture(ACHRowFixture baseRow, Optional<String> createDate, Optional<String> status,
            Optional<Integer> retryCount, EnumMap<RowField, String> overridesMap) {
        this(createDate, status, retryCount,
                overridesMap.getOrDefault(RowField.EMPLOYEE_ID, baseRow.employeeId),
                overridesMap.getOrDefault(RowField.NET_ID, baseRow.netId),
                overridesMap.getOrDefault(RowField.LAST_NAME, baseRow.lastName),
                overridesMap.getOrDefault(RowField.FIRST_NAME, baseRow.firstName),
                overridesMap.getOrDefault(RowField.PAYMENT_TYPE, baseRow.paymentType),
                overridesMap.getOrDefault(RowField.BALANCE_ACCOUNT, baseRow.balanceAccount),
                overridesMap.getOrDefault(RowField.COMPLETED_DATE, baseRow.completedDate),
                overridesMap.getOrDefault(RowField.BANK_NAME, baseRow.bankName),
                overridesMap.getOrDefault(RowField.BANK_ROUTING_NUMBER, baseRow.bankRoutingNumber),
                overridesMap.getOrDefault(RowField.BANK_ACCOUNT_NUMBER, baseRow.bankAccountNumber),
                overridesMap.getOrDefault(RowField.BANK_ACCOUNT_TYPE, baseRow.bankAccountType));
    }

    private ACHRowFixture(String employeeId, String netId, String lastName, String firstName, String paymentType,
            String balanceAccount, String completedDate, String bankName, String bankRoutingNumber,
            String bankAccountNumber, String bankAccountType) {
        this(Optional.empty(), Optional.empty(), Optional.empty(), employeeId, netId, lastName, firstName, paymentType,
                balanceAccount, completedDate, bankName, bankRoutingNumber, bankAccountNumber, bankAccountType);
    }

    private ACHRowFixture(Optional<String> createDate, Optional<String> status, Optional<Integer> retryCount,
            String employeeId, String netId, String lastName, String firstName, String paymentType,
            String balanceAccount, String completedDate, String bankName, String bankRoutingNumber,
            String bankAccountNumber, String bankAccountType) {
        this.createDate = createDate;
        this.status = status;
        this.retryCount = retryCount;
        this.employeeId = employeeId;
        this.netId = netId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.paymentType = paymentType;
        this.balanceAccount = balanceAccount;
        this.completedDate = completedDate;
        this.bankName = bankName;
        this.bankRoutingNumber = bankRoutingNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.bankAccountType = bankAccountType;
    }

    public PayeeACHAccountExtractDetail toPayeeACHAccountExtractDetailWithoutId() {
        PayeeACHAccountExtractDetail achDetail = new PayeeACHAccountExtractDetail();
        
        if (!createDate.isPresent() || !status.isPresent() || !retryCount.isPresent()) {
            throw new IllegalStateException(
                    "Fixture must define a createDate, status and retryCount when used for testing row reprocessing");
        }
        
        achDetail.setEmployeeID(employeeId);
        achDetail.setNetID(netId);
        achDetail.setFirstName(firstName);
        achDetail.setLastName(lastName);
        achDetail.setPaymentType(paymentType);
        achDetail.setBalanceAccount(balanceAccount);
        achDetail.setBankName(bankName);
        achDetail.setBankRoutingNumber(bankRoutingNumber);
        achDetail.setBankAccountNumber(bankAccountNumber);
        achDetail.setBankAccountType(bankAccountType);
        achDetail.setRetryCount(retryCount.get());
        achDetail.setCreateDate(getCreateDateAsSqlDate());
        achDetail.setStatus(status.get());
        achDetail.setRetryCount(retryCount.get());
        
        return achDetail;
    }

    public Date getCreateDateAsSqlDate() {
        return createDate.map(ACHRowFixture::convertDateStringToSqlDate)
                .orElse(null);
    }

    private static Date convertDateStringToSqlDate(String dateString) {
        long dateAsMilliseconds = DATE_FORMATTER.parseDateTime(dateString).getMillis();
        return new Date(dateAsMilliseconds);
    }

    private static Pair<RowField, String> fieldOverride(RowField fieldKey, String value) {
        return Pair.of(fieldKey, value);
    }

    @SafeVarargs
    private static EnumMap<RowField, String> buildFieldOverrideMap(Pair<RowField, String>... fieldOverrides) {
        return Stream.of(fieldOverrides)
                .collect(Collectors.toMap(
                        fieldOverride -> fieldOverride.getLeft(),
                        fieldOverride -> fieldOverride.getRight(),
                        (value1, value2) -> value2,
                        () -> new EnumMap<>(RowField.class)));
    }

    public enum RowField {
        EMPLOYEE_ID,
        NET_ID,
        LAST_NAME,
        FIRST_NAME,
        PAYMENT_TYPE,
        BALANCE_ACCOUNT,
        COMPLETED_DATE,
        BANK_NAME,
        BANK_ROUTING_NUMBER,
        BANK_ACCOUNT_NUMBER,
        BANK_ACCOUNT_TYPE;
    }

}
