package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractDetailResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractGroupResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractResultCode;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailSubResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractGroupResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReport;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractStep;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractReportService;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.pdp.service.CuAchService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class PayeeACHAccountExtractServiceImpl implements PayeeACHAccountExtractService {
	private static final Logger LOG = LogManager.getLogger(PayeeACHAccountExtractServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private List<BatchInputFileType> batchInputFileTypes;
    private ConfigurationService configurationService;
    private ParameterService parameterService;
    private EmailService emailService;
    private DocumentService documentService;
    private DataDictionaryService dataDictionaryService;
    private PersonService personService;
    private SequenceAccessorService sequenceAccessorService;
    private CuAchService achService;
    private AchBankService achBankService;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private PayeeACHAccountExtractReportService payeeACHAccountExtractReportService;

    // Portions of this method are based on code and logic from CustomerLoadServiceImpl.
    @Transactional
    @Override
    public boolean processACHBatchDetails() {
        LOG.info("processACHBatchDetails: Beginning processing of ACH input files");
        
        List<PayeeACHAccountExtractGroupResult> fileResults = new ArrayList<>();
        PayeeACHProcessingTracker tracker = new PayeeACHProcessingTracker();

        Map<String,BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("processACHBatchDetails: Found " + fileNamesToLoad.size() + " file(s) to process.");

        Collection<PayeeACHAccountExtractDetail> detailsToReprocess = findAllOpenACHDetails();
        LOG.info("processACHBatchDetails: Found " + detailsToReprocess.size() + " previously-failed ACH rows to reprocess.");
        PayeeACHAccountExtractGroupResult reprocessResult = reprocessFailedRowsFromPreviousRun(detailsToReprocess, tracker);
        LOG.info("processACHBatchDetails: Finished reprocessing of previously-failed ACH rows.");

        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("processACHBatchDetails: Beginning processing of filename: " + inputFileName);
            processedFiles.add(inputFileName);
            
            PayeeACHAccountExtractGroupResult fileResult = loadACHBatchDetailFile(inputFileName, fileNamesToLoad.get(inputFileName), tracker);
            fileResults.add(fileResult);
            
            LOG.info("processACHBatchDetails: Finished processing of filename: " + inputFileName);
        }

        removeDoneFiles(processedFiles);

        LOG.info("processACHBatchDetails: Writing reports");
        PayeeACHAccountExtractReport achReport = new PayeeACHAccountExtractReport(reprocessResult, fileResults);
        payeeACHAccountExtractReportService.writeBatchJobReports(achReport);
        LOG.info("processACHBatchDetails: Finished writing reports");

        // For now, return true even if files or rows did not load successfully. Functionals will address the failed rows/files accordingly.
        return true;
    }

    protected Collection<PayeeACHAccountExtractDetail> findAllOpenACHDetails() {
        try {
            Map<String, Object> criteria = Collections.singletonMap(
                    CUPdpPropertyConstants.STATUS, CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN);
            return businessObjectService.findMatchingOrderBy(
                    PayeeACHAccountExtractDetail.class, criteria, KRADPropertyConstants.ID, true);
        } catch (Exception e) {
            LOG.error("findAllOpenACHDetails: Unexpected error retrieving existing ACH details", e);
            throw new RuntimeException("Could not retrieve ACH details that failed on a previous run, this should NEVER happen! Message: "
                    + e.getMessage());
        }
    }

    /**
     * Create a collection of the files to process with the mapped value of the BatchInputFileType
     */
    // This is a slightly-tweaked copy of a method from CustomerLoadServiceImpl.
    protected Map<String,BatchInputFileType> getListOfFilesToProcess() {
        Map<String,BatchInputFileType> inputFileTypeMap = new LinkedHashMap<String, BatchInputFileType>();

        for (BatchInputFileType batchInputFileType : batchInputFileTypes) {

            List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
            if (inputFileNames == null) {
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifier()
                        + ") returned NULL which should never happen.");
            } else {
                // update the file name mapping
                for (String inputFileName : inputFileNames) {

                    // filenames returned should never be blank/empty/null
                    if (StringUtils.isBlank(inputFileName)) {
                        criticalError("One of the file names returned as ready to process [" + inputFileName
                                + "] was blank.  This should not happen, so throwing an error to investigate.");
                    }

                    inputFileTypeMap.put(inputFileName, batchInputFileType);
                }
            }
        }

        return inputFileTypeMap;
    }

    protected PayeeACHAccountExtractGroupResult reprocessFailedRowsFromPreviousRun(
            Collection<PayeeACHAccountExtractDetail> achDetails, PayeeACHProcessingTracker tracker) {
        try {
            int maxRetryCount = getMaximumACHDetailRetryCount();
        
            if (CollectionUtils.isEmpty(achDetails)) {
                LOG.info("reprocessFailedRowsFromPreviousRun: Did not find any unresolved ACH rows that failed on a previous run; "
                        + "skipping the reprocessing phase.");
                return new PayeeACHAccountExtractGroupResult(
                        ACHExtractGroupResultCode.SKIPPED, tracker.getCurrentProcessingResults(),
                        "There were no unresolved ACH rows from previous runs that needed to be processed.");
            }
        
            LOG.info("reprocessFailedRowsFromPreviousRun: Attempting to reprocess " + achDetails.size()
                    + " unresolved ACH rows that failed on the previous run");
        
            for (PayeeACHAccountExtractDetail achDetail : achDetails) {
                PayeeACHAccountExtractDetailResult processingResult = reprocessFailedRow(achDetail, tracker, maxRetryCount);
                tracker.addProcessingResult(processingResult);
            }
            
            return new PayeeACHAccountExtractGroupResult(
                    ACHExtractGroupResultCode.SUCCESS, tracker.getCurrentProcessingResults());
        } catch (Exception e) {
            LOG.error("reprocessFailedRowsFromPreviousRun: An unexpected error occurred", e);
            return new PayeeACHAccountExtractGroupResult(
                    ACHExtractGroupResultCode.ERROR, tracker.getCurrentProcessingResults(),
                    "Unexpected error: " + e.getMessage());
        } finally {
            tracker.clearAccumulatedProcessingResults();
        }
    }

    protected PayeeACHAccountExtractDetailResult reprocessFailedRow(
            PayeeACHAccountExtractDetail achDetail, PayeeACHProcessingTracker tracker, int maxRetryCount) {
        if (achDetail.getRetryCount() >= maxRetryCount) {
            return new PayeeACHAccountExtractDetailResult(
                    ACHExtractDetailResultCode.SKIPPED_MAX_RETRY, achDetail,
                    buildGenericSkipOrFailurePrefix(achDetail)
                            + " because it has exceeded the maximum number of retry attempts; this ACH detail will be skipped.");
        }
        
        PayeeACHAccountExtractDetailResult processingResult = processACHBatchDetail(achDetail, tracker);
        if (rowWasSkippedDueToMultiplesOfSameUserInSingleRun(processingResult)) {
            return processingResult;
        }
        
        boolean reprocessingNeeded = isRowReprocessingNeeded(processingResult);
        
        if (!reprocessingNeeded) {
            Integer oldRetryCount = achDetail.getRetryCount();
            String saveErrorMessage = updateACHDetailThatSucceededOnRetry(achDetail);
            if (StringUtils.isNotBlank(saveErrorMessage)) {
                achDetail.setRetryCount(oldRetryCount);
                processingResult = new PayeeACHAccountExtractDetailResult(
                        processingResult, ACHExtractDetailResultCode.ERROR, saveErrorMessage);
                reprocessingNeeded = true;
            }
        }
        
        if (reprocessingNeeded) {
            String saveErrorMessage = updateACHDetailThatFailedOnRetry(achDetail);
            if (StringUtils.isNotBlank(saveErrorMessage)) {
                processingResult = new PayeeACHAccountExtractDetailResult(
                        processingResult, ACHExtractDetailResultCode.ERROR, saveErrorMessage);
            }
        }
        
        return processingResult;
    }

    // Portions of this method are based on the code and logic from CustomerLoadServiceImpl.loadFile
    protected PayeeACHAccountExtractGroupResult loadACHBatchDetailFile(String inputFileName, BatchInputFileType batchInputFileType, PayeeACHProcessingTracker tracker) {
        try {
            List<PayeeACHAccountExtractDetail> achDetails = loadAndParseBatchFile(inputFileName, batchInputFileType);
            
            for (PayeeACHAccountExtractDetail achDetail : achDetails) {
                cleanPayeeACHAccountExtractDetail(achDetail);
                PayeeACHAccountExtractDetailResult processingResult = processACHDetailFromFile(achDetail, tracker);
                tracker.addProcessingResult(processingResult);
            }
            
            return new PayeeACHAccountExtractGroupResult(
                    ACHExtractGroupResultCode.SUCCESS, inputFileName, tracker.getCurrentProcessingResults());
        } catch (Exception e) {
            LOG.error("loadACHBatchDetailFile: Unexpected file processing error", e);
            String errorMessage = MessageFormat.format(
                    "Unexpected error encountered when processing file {0} -- {1}",
                    inputFileName, e.getMessage());
            return new PayeeACHAccountExtractGroupResult(
                    ACHExtractGroupResultCode.ERROR, inputFileName, tracker.getCurrentProcessingResults(), errorMessage);
        } finally {
            tracker.clearAccumulatedProcessingResults();
        }
    }
    
    // Portions of this method are based on the code and logic from CustomerLoadServiceImpl.loadFile
    @SuppressWarnings("unchecked")
    protected List<PayeeACHAccountExtractDetail> loadAndParseBatchFile(String inputFileName, BatchInputFileType batchInputFileType) {
        byte[] fileByteContent = LoadFileUtils.safelyLoadFileBytes(inputFileName);
        
        LOG.info("loadAndParseBatchFile: Attempting to parse the file.");
        
        Object parsedObject = null;
        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        } catch (ParseException e) {
            String errorMessage = "loadAndParseBatchFile: Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        if (!(parsedObject instanceof List)) {
            String errorMessage = "loadAndParseBatchFile: Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }
        
        return (List<PayeeACHAccountExtractDetail>) parsedObject;
    }
    
    protected void cleanPayeeACHAccountExtractDetail(PayeeACHAccountExtractDetail detail) {
        if (!StringUtils.isNumeric(detail.getBankAccountNumber())) {
            String logMessageStarter = "cleanPayeeACHAccountExtractDetail, the bank account for " + detail.getNetID();
            String bankAccountNumber = detail.getBankAccountNumber();
            if (StringUtils.contains(bankAccountNumber, KFSConstants.DASH)) {
                LOG.info(logMessageStarter + " contains dashes, so removing them");
                bankAccountNumber = StringUtils.remove(bankAccountNumber, KFSConstants.DASH);
            }
            if (StringUtils.contains(bankAccountNumber, StringUtils.SPACE)) {
                LOG.info(logMessageStarter + " contains spaces, so removing them");
                bankAccountNumber = StringUtils.remove(bankAccountNumber,  StringUtils.SPACE);
            }
            
            detail.setBankAccountNumber(bankAccountNumber);
            
            if (!StringUtils.isNumeric(detail.getBankAccountNumber())) {
                LOG.error(logMessageStarter + " is not numeric after cleaning");
            }
        }
    }

    protected PayeeACHAccountExtractDetailResult processACHDetailFromFile(
            PayeeACHAccountExtractDetail achDetail, PayeeACHProcessingTracker tracker) {
        PayeeACHAccountExtractDetailResult processingResult = processACHBatchDetail(achDetail, tracker);
        
        if (isRowReprocessingNeeded(processingResult)) {
            String saveErrorMessage = saveNewFailedACHDetail(achDetail);
            if (StringUtils.isNotBlank(saveErrorMessage)) {
                processingResult = new PayeeACHAccountExtractDetailResult(
                        processingResult, ACHExtractDetailResultCode.ERROR, saveErrorMessage);
            }
        }
        
        return processingResult;
    }

    protected boolean isRowReprocessingNeeded(PayeeACHAccountExtractDetailResult detailResult) {
        return !ACHExtractDetailResultCode.SUCCESS.equals(detailResult.getResultCode());
    }

    protected boolean rowWasSkippedDueToMultiplesOfSameUserInSingleRun(PayeeACHAccountExtractDetailResult detailResult) {
        return ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID.equals(detailResult.getResultCode());
    }

    protected PayeeACHAccountExtractDetailResult processACHBatchDetail(
            PayeeACHAccountExtractDetail achDetail, PayeeACHProcessingTracker tracker) {
        LOG.info("processACHBatchDetail: Started processing detail for: " + achDetail.getLogData());

        if (tracker.netIdWasProcessedAlready(achDetail.getNetID())) {
            return new PayeeACHAccountExtractDetailResult(
                    ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID, achDetail,
                    buildGenericSkipOrFailurePrefix(achDetail) + "because another detail for the same payee was also encountered on this run; "
                            + " this ACH detail will be reprocessed on the next run.");
        } else if (StringUtils.isNotBlank(achDetail.getNetID())) {
            tracker.addProcessedNetId(achDetail.getNetID());
        }

        Person payee = personService.getPersonByPrincipalName(achDetail.getNetID());
        List<String> validationErrors = validateACHBatchDetail(achDetail, payee);
        if (CollectionUtils.isNotEmpty(validationErrors)) {
            return new PayeeACHAccountExtractDetailResult(
                    ACHExtractDetailResultCode.ERROR, achDetail, validationErrors);
        }

        List<PayeeACHAccountExtractDetailSubResult> subResults = new ArrayList<>();
        boolean processingFailed = false;
        
        PayeeACHAccountExtractDetailSubResult entityResult = addOrUpdateACHAccountIfNecessary(
                payee, achDetail, PayeeIdTypeCodes.ENTITY, payee.getEntityId());
        subResults.add(entityResult);
        processingFailed = entityResult.didProcessingFail();
        
        if (!processingFailed) {
            PayeeACHAccountExtractDetailSubResult employeeResult = addOrUpdateACHAccountIfNecessary(
                    payee, achDetail, PayeeIdTypeCodes.EMPLOYEE, payee.getEmployeeId());
            subResults.add(employeeResult);
            processingFailed = employeeResult.didProcessingFail();
        }
        
        if (processingFailed) {
            String errorMessage = subResults.size() <= 1
                    ? "Changes to ACH accounts failed" : "Changes to ACH accounts partially failed";
            return new PayeeACHAccountExtractDetailResult(
                    ACHExtractDetailResultCode.ERROR, achDetail, subResults, errorMessage);
        } else {
            return new PayeeACHAccountExtractDetailResult(
                    ACHExtractDetailResultCode.SUCCESS, achDetail, subResults);
        }
    }

    protected PayeeACHAccountExtractDetailSubResult addOrUpdateACHAccountIfNecessary(
            Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType, String payeeIdNumber) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        PayeeACHAccount achAccount = achService.getAchInformationIncludingInactive(
                payeeType, payeeIdNumber, getDirectDepositTransactionType());
        
        if (ObjectUtils.isNull(achAccount)) {
            return addACHAccount(payee, achDetail, payeeType);
        } else {
            return updateACHAccountIfNecessary(payee, achDetail, achAccount);
        }
    }

    protected List<String> validateACHBatchDetail(PayeeACHAccountExtractDetail achDetail, Person payee) {
        List<String> errors = new ArrayList<>();
        int errorNumber = 0;
        
        if (ObjectUtils.isNull(payee) || StringUtils.isBlank(payee.getEntityId())) {
            errors.add(buildNumberedMessage(++errorNumber, "Payee does not exist in KFS."));
        } else {
            if (!StringUtils.equals(achDetail.getEmployeeID(), payee.getEmployeeId())) {
                String errorMessage = MessageFormat.format(
                        "Payee has an employee ID of \"{0}\" in input file, but has an employee ID of \"{1}\" in KFS.",
                        achDetail.getEmployeeID(), payee.getEmployeeId());
                errors.add(buildNumberedMessage(++errorNumber, errorMessage));
            }
            if (StringUtils.isBlank(payee.getEmailAddressUnmasked())) {
                errors.add(buildNumberedMessage(++errorNumber,
                        "Payee has no email address defined in KFS. No notification emails will be sent for this user."));
            }
        }

        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE.equalsIgnoreCase(achDetail.getPaymentType())) {
            String errorMessage = MessageFormat.format("Payment type is \"{0}\" instead of \"{1}\".",
                    achDetail.getPaymentType(), CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE);
            errors.add(buildNumberedMessage(++errorNumber, errorMessage));
        }
        
        if (StringUtils.isBlank(achDetail.getBankName())) {
            errors.add(buildNumberedMessage(++errorNumber, "Bank Name was not supplied."));
        }

        if (StringUtils.isBlank(achDetail.getBankRoutingNumber())) {
            errors.add(buildNumberedMessage(++errorNumber, "Bank routing number was not supplied."));
        } else if (!StringUtils.isNumeric(achDetail.getBankRoutingNumber())) {
            errors.add(buildNumberedMessage(++errorNumber, "Bank routing number must only contain digits."));
        } else if (ObjectUtils.isNull(achBankService.getByPrimaryId(achDetail.getBankRoutingNumber()))) {
            String errorMessage = MessageFormat.format(
                    "Could not find bank \"{0}\" under the given routing number.", achDetail.getBankName());
            errors.add(buildNumberedMessage(++errorNumber, errorMessage));
        }

        if (StringUtils.isBlank(achDetail.getBankAccountNumber())) {
            errors.add(buildNumberedMessage(++errorNumber, "Bank account number was not supplied."));
        } else if (!StringUtils.isNumeric(achDetail.getBankAccountNumber())) {
            errors.add(buildNumberedMessage(++errorNumber, "Bank account number must only contain digits."));
        }

        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_BALANCE_ACCOUNT_YES_INDICATOR.equals(achDetail.getBalanceAccount())) {
            errors.add(buildNumberedMessage(++errorNumber, "Account is not a balance account."));
        }
        
        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE.equalsIgnoreCase(achDetail.getBankAccountType())
                && !CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE.equalsIgnoreCase(achDetail.getBankAccountType())) {
            errors.add(buildNumberedMessage(++errorNumber, "Account is not a checking or savings account."));
        }

        if (!errors.isEmpty()) {
            errors.add(0, buildValidationFailurePrefix(achDetail));
            String messagesForLog = errors.stream()
                    .collect(Collectors.joining(KFSConstants.NEWLINE));
            LOG.error("validateACHBatchDetail: " + achDetail.getLogData() + " -- " + messagesForLog);
        } else {
            LOG.info("validateACHBatchDetail: Validation succeeded for detail " + achDetail.getLogData());
        }
        
        return errors;
    }

    protected String buildNumberedMessage(int number, String message) {
        return MessageFormat.format("[{0}] {1}", number, message);
    }

    protected String buildValidationFailurePrefix(PayeeACHAccountExtractDetail achDetail) {
        return buildGenericSkipOrFailurePrefix(achDetail) + " for the following reasons:";
    }

    protected String buildGenericSkipOrFailurePrefix(PayeeACHAccountExtractDetail achDetail) {
        Date originalAchDate = ObjectUtils.isNotNull(achDetail.getId())
                ? achDetail.getCreateDate() : dateTimeService.getCurrentSqlDate();
        return MessageFormat.format(
                "ACH Detail from {0} for payee \"{1}\" could not be processed",
                dateTimeService.toDateString(originalAchDate), achDetail.getNetID());
    }

    protected PayeeACHAccountExtractDetailSubResult addACHAccount(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType) {
        PayeeACHData achData = new PayeeACHData(payee, achDetail, payeeType);
        return createAndRoutePayeeACHAccountDocument(achData, this::setupDocumentForACHCreate);
    }

    protected void setupDocumentForACHCreate(MaintenanceDocument paatDocument, PayeeACHData achData) {
        Person payee = achData.getPayee();
        PayeeACHAccountExtractDetail achDetail = achData.getAchDetail();
        String payeeType = achData.getPayeeType();
        
        PayeeACHAccountMaintainableImpl maintainable = (PayeeACHAccountMaintainableImpl) paatDocument.getNewMaintainableObject();
        maintainable.setMaintenanceAction(KFSConstants.MAINTENANCE_NEW_ACTION);
        PayeeACHAccount achAccount = (PayeeACHAccount) maintainable.getDataObject();
        String payeeId = getPayeeIdForType(payee, payeeType);
        achAccount.setPayeeIdNumber(payeeId);
        achAccount.setPayeeIdentifierTypeCode(payeeType);
        
        Long newId = sequenceAccessorService.getNextAvailableSequenceNumber(PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME);
        achAccount.setAchAccountGeneratedIdentifier(new KualiInteger(newId));
        achAccount.setAchTransactionType(getDirectDepositTransactionType());
        achAccount.setBankRoutingNumber(achDetail.getBankRoutingNumber());
        achAccount.setBankAccountNumber(achDetail.getBankAccountNumber());
        achAccount.setBankAccountTypeCode(getACHTransactionCode(achDetail.getBankAccountType()));
        if (StringUtils.isNotBlank(payee.getNameUnmasked())) {
            achAccount.setPayeeName(payee.getNameUnmasked());
        }
        if (StringUtils.isNotBlank(payee.getEmailAddressUnmasked())) {
            achAccount.setPayeeEmailAddress(payee.getEmailAddressUnmasked());
        }
        achAccount.setActive(true);
    }

    protected PayeeACHAccountExtractDetailSubResult updateACHAccountIfNecessary(
            Person payee, PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount) {
        if (accountHasChanged(achDetail, achAccount)) {
            PayeeACHData achData = new PayeeACHData(payee, achDetail, achAccount.getPayeeIdentifierTypeCode(), achAccount);
            return createAndRoutePayeeACHAccountDocument(achData, this::setupDocumentForACHUpdate);
        } else {
            LOG.info("updateACHAccountIfNecessary: Input file's account information for payee of type '" + achAccount.getPayeeIdentifierTypeCode()
                    + "' matches what is already in KFS; no updates will be made for this entry.");
            return new PayeeACHAccountExtractDetailSubResult(
                    ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE, achAccount, "Accounting information for payee type "
                            + achAccount.getPayeeIdentifierTypeCode() + " matches what is already in KFS; update was NOT performed.");
        }
    }

    protected boolean accountHasChanged(PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount) {
        return !StringUtils.equals(achDetail.getBankRoutingNumber(), achAccount.getBankRoutingNumber())
                || !StringUtils.equals(achDetail.getBankAccountNumber(), achAccount.getBankAccountNumber())
                || !StringUtils.equals(
                        getACHTransactionCode(achDetail.getBankAccountType()), achAccount.getBankAccountTypeCode());
    }

    protected void setupDocumentForACHUpdate(MaintenanceDocument paatDocument, PayeeACHData achData) {
        if (!achData.hasOldAccount()) {
            throw new RuntimeException("An existing ACH account should have been present");
        }
        PayeeACHAccountExtractDetail achDetail = achData.getAchDetail();
        PayeeACHAccount oldAccount = achData.getOldAccount();
        PayeeACHAccount newAccount = (PayeeACHAccount) ObjectUtils.deepCopy(oldAccount);
        
        newAccount.setBankRoutingNumber(achDetail.getBankRoutingNumber());
        newAccount.setBankAccountNumber(achDetail.getBankAccountNumber());
        newAccount.setBankAccountTypeCode(getACHTransactionCode(achDetail.getBankAccountType()));
        newAccount.setActive(true);
        
        paatDocument.getOldMaintainableObject().setDataObject(oldAccount);
        paatDocument.getNewMaintainableObject().setDataObject(newAccount);
        paatDocument.getNewMaintainableObject().setMaintenanceAction(KFSConstants.MAINTENANCE_EDIT_ACTION);
    }

    protected PayeeACHAccountExtractDetailSubResult createAndRoutePayeeACHAccountDocument(
            PayeeACHData achData, BiConsumer<MaintenanceDocument, PayeeACHData> documentConfigurer) {
        try {
            MaintenanceDocument paatDocument = (MaintenanceDocument) documentService.getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE);
            documentConfigurer.accept(paatDocument, achData);
            
            Person payee = achData.getPayee();
            PayeeACHAccount achAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
            paatDocument.getDocumentHeader().setDocumentDescription(
                    buildDocumentDescription(payee, achAccount, paatDocument));
            
            addNote(paatDocument, getPayeeACHAccountExtractParameter(CUPdpParameterConstants.GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT));

            paatDocument = (MaintenanceDocument) documentService.routeDocument(
                    paatDocument, CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_ROUTE_ANNOTATION, null);
            
            achAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
            sendPayeeACHAccountAddOrUpdateEmail((PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject(), payee, paatDocument);
            LOG.info("createAndRoutePayeeACHAccountDocument: " + getSuccessMessageStart(paatDocument) + "ACH Account of type "
                    + achAccount.getPayeeIdentifierTypeCode() + " for payee " + payee.getPrincipalName());
            
            ACHExtractResultCode resultCode = isDocumentCreatingNewAccount(paatDocument)
                    ? ACHExtractResultCode.SUCCESS_NEW : ACHExtractResultCode.SUCCESS_EDIT;
            return new PayeeACHAccountExtractDetailSubResult(resultCode, achAccount, paatDocument);
        } catch (Exception e) {
            String errorMessage = getFailRequestMessage(e);
            LOG.error("createAndRoutePayeeACHAccountDocument: " + errorMessage, e);
            String payeeId = getPayeeIdForTypeIfValid(achData.getPayee(), achData.getPayeeType());
            return new PayeeACHAccountExtractDetailSubResult(ACHExtractResultCode.ERROR, payeeId, achData.getPayeeType(), errorMessage);
        }
    }

    private String getSuccessMessageStart(MaintenanceDocument document) {
        return isDocumentCreatingNewAccount(document) ? "Created new " : "Updated existing ";
    }

    private String getFailRequestMessage(Exception e) {
        if (e instanceof ValidationException) {
            return "Failed request : "+ e.getMessage() + " - " +  getValidationErrorMessage();
        } else {
            return "Failed request : " + e.getCause() + " - " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
    }

    private String getValidationErrorMessage() {
        StringBuilder validationError = new StringBuilder();
        for (String errorProperty : GlobalVariables.getMessageMap().getAllPropertiesWithErrors()) {
            for (Object errorMessage : GlobalVariables.getMessageMap().getMessages(errorProperty)) {
                String errorMsg = configurationService.getPropertyValueAsString(((ErrorMessage) errorMessage).getErrorKey());
                if (errorMsg == null) {
                    throw new RuntimeException("Cannot find message for error key: " + ((ErrorMessage) errorMessage).getErrorKey());
                }
                else {
                    Object[] arguments = (Object[]) ((ErrorMessage) errorMessage).getMessageParameters();
                    if (arguments != null && arguments.length != 0) {
                        errorMsg = MessageFormat.format(errorMsg, arguments);
                    }
                }
                validationError.append(errorMsg + KFSConstants.NEWLINE);
            }
        }
        return validationError.toString();
    }

    private String getPayeeIdForTypeIfValid(Person payee, String payeeType) {
        try {
            return getPayeeIdForType(payee, payeeType);
        } catch (Exception e) {
            LOG.error("getPayeeIdForTypeIfValid: Could not get payee ID", e);
            return KFSConstants.QUESTION_MARK;
        }
    }

    private String getPayeeIdForType(Person payee, String payeeType) {
        if (StringUtils.equals(PayeeIdTypeCodes.ENTITY, payeeType)) {
            return payee.getEntityId();
        } else if (StringUtils.equals(PayeeIdTypeCodes.EMPLOYEE, payeeType)) {
            return payee.getEmployeeId();
        } else {
            throw new RuntimeException("Invalid payee ID type: " + payeeType);
        }
    }

    /**
     * Returns the ACH transaction code for the given input file transaction type.
     * The default implementation expects the input file type to be "Checking" or "Savings",
     * and uses parameters initially set to map those types to the "22PPD" (Personal Checking)
     * and "32PPD" (Personal Savings) ACH transaction codes, respectively.
     */
    protected String getACHTransactionCode(String workdayAccountType) {
        if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_PERSONAL_CHECKING_TRANSACTION_CODE);
        } else if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_PERSONAL_SAVINGS_TRANSACTION_CODE);
        } else {
            throw new IllegalArgumentException("Unrecognized account type from file: " + workdayAccountType);
        }
    }

    protected String getDirectDepositTransactionType() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE);
    }

    protected String getEmailSubjectForNewPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT);
    }

    protected String getEmailSubjectForUpdatedPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT);
    }

    protected String getUnresolvedEmailBodyForNewPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY);
    }

    protected String getUnresolvedEmailBodyForUpdatedPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_BODY);
    }

    protected String getPayeeACHAccountExtractParameter(String parameterName) {
        return parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, parameterName);
    }

    protected void sendPayeeACHAccountAddOrUpdateEmail(PayeeACHAccount achAccount, Person payee, MaintenanceDocument document) {
        if (isDocumentCreatingNewAccount(document)) {
            sendPayeeACHAccountAddOrUpdateEmail(achAccount, payee,
                    getEmailSubjectForNewPayeeACHAccount(), getUnresolvedEmailBodyForNewPayeeACHAccount());
        } else {
            sendPayeeACHAccountAddOrUpdateEmail(achAccount, payee,
                    getEmailSubjectForUpdatedPayeeACHAccount(), getUnresolvedEmailBodyForUpdatedPayeeACHAccount());
        }
    }

    /**
     * Sends an ACH Account add/update notification email to the given payee.
     * 
     * @param achAccount The Payee ACH Account that was added or updated.
     * @param payee The person affected by the change.
     * @param emailSubject The email subject line.
     * @param emailBody The email body; may contain "[propertyName]"-style placeholders to substitute in ACH Account values, as well as newlines.
     */
    protected void sendPayeeACHAccountAddOrUpdateEmail(PayeeACHAccount achAccount, Person payee, String emailSubject, String emailBody) {
        if (StringUtils.isBlank(payee.getEmailAddressUnmasked())) {
            LOG.warn("Payee " + payee.getPrincipalName() + " has no email address defined in KFS. No notification emails will be sent for this user.");
            return;
        }
        
        // Construct mail message, and replace property placeholders and literal "\n" strings in the email body accordingly.
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARAM_NM));
        message.setSubject(emailSubject);
        message.setMessage(getResolvedEmailBody(achAccount, emailBody));
        message.addToAddress(payee.getEmailAddressUnmasked());
        
        // Send the message.
        emailService.sendMessage(message, false);        
    }

    /**
     * Helper method for replacing "[propertyName]"-style placeholders in the email body
     * with actual property values from the given Payee ACH Account, in addition to
     * replacing literal "\n" with newline characters accordingly. Potentially sensitive
     * placeholders will be replaced with empty text except for the bank name.
     * Placeholders referencing properties with value finders will print the matching key/value
     * label instead. Any placeholders that could not be resolved successfully will be replaced
     * with empty text.
     */
    protected String getResolvedEmailBody(PayeeACHAccount achAccount, String emailBody) {
        Pattern placeholderPattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher emailMatcher = placeholderPattern.matcher(emailBody.replace("\\n", "\n"));
        // Use a StringBuffer here, due to the Matcher class not supporting StringBuilder for appending operations.
        StringBuffer resolvedEmailBody = new StringBuffer(emailBody.length());
        
        // Replace all placeholders one by one. The pattern has a single group in it to help with retrieving just the property name and not the brackets.
        while (emailMatcher.find()) {
            String propertyName = emailMatcher.group(1);
            AttributeDefinition attDefinition = dataDictionaryService.getAttributeDefinition(PayeeACHAccount.class.getName(), propertyName);
            
            String replacement;
            // Make sure property exists in data dictionary and is either not potentially sensitive or is the safe-to-use bank name property.
            if (attDefinition != null) {
                AttributeSecurity attSecurity = attDefinition.getAttributeSecurity();
                if (attSecurity != null && (attSecurity.isHide() || attSecurity.isMask() || attSecurity.isPartialMask())
                        && !CUPdpPropertyConstants.PAYEE_ACH_BANK_NAME.equals(propertyName)) {
                    // Replace potentially-sensitive placeholders with an empty string.
                    replacement = StringUtils.EMPTY;
                } else {
                    // Replace the placeholder with the property value, or with an empty string if null or invalid.
                    try {
                        Object propertyValue = ObjectPropertyUtils.getPropertyValue(achAccount, propertyName);
                        replacement = ObjectUtils.isNotNull(propertyValue) ? propertyValue.toString() : StringUtils.EMPTY;
                        // If a values finder is defined, use the label from the matching key/value pair instead.
                        if (attDefinition.getControl() != null && ObjectUtils.isNotNull(attDefinition.getControl().getValuesFinder())) {
                            KeyValuesFinder valuesFinder = attDefinition.getControl().getValuesFinder();
                            String key = replacement;
                            replacement = valuesFinder.getKeyLabel(key);
                            // If the key is in the label, then remove it from the label.
                            if (attDefinition.getControl().getIncludeKeyInLabel() != null
                                    && attDefinition.getControl().getIncludeKeyInLabel().booleanValue()) {
                                // Check for key-and-dash or key-in-parentheses, and remove them if found.
                                // (Former can come from BO values finders, latter is only for custom values finders that append the keys as such.)
                                String keyAndDashPrefix = key + " - ";
                                String keyInParenSuffix = " (" + key + ")";
                                replacement = replacement.startsWith(keyAndDashPrefix) ? StringUtils.substringAfter(replacement, keyAndDashPrefix)
                                        : (replacement.endsWith(keyInParenSuffix)
                                                ? StringUtils.substringBeforeLast(replacement, keyInParenSuffix) : replacement);
                            }
                        }
                        // Because of the way that Matcher.appendReplacement() works, escape the special replacement characters accordingly.
                        if (replacement.indexOf('\\') != -1) {
                            replacement = replacement.replace("\\", "\\\\");
                        }
                        if (replacement.indexOf('$') != -1) {
                            replacement = replacement.replace("$", "\\$");
                        }
                        
                    } catch (RuntimeException e) {
                        replacement = StringUtils.EMPTY;
                    }
                }
            } else {
                // Replace non-data-dictionary-defined property placeholders with an empty string.
                replacement = StringUtils.EMPTY;
            }
            
            emailMatcher.appendReplacement(resolvedEmailBody, replacement);
        }
        
        emailMatcher.appendTail(resolvedEmailBody);
        return resolvedEmailBody.toString();
    }

    /*
     * Copied this method from VendorBatchServiceImpl and tweaked accordingly.
     */
    protected void addNote(Document document, String noteText) {
        Note note = createEmptyNote();

        note.setNoteText(noteText);
        note.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
        note.setNotePostedTimestampToCurrent();
        document.addNote(note);
    }

    /*
     * This has been separated into its own method for unit testing convenience.
     */
    protected Note createEmptyNote() {
        return new Note();
    }

    /*
     * Copied this method from VendorBatchServiceImpl.
     */
    private Person getSystemUser() {
        return personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
    }

    /**
     * Clears out associated .done files for the processed data files.
     * 
     * @param dataFileNames
     */
    // Copied this method from CustomerLoadServiceImpl.
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    protected String buildDocumentDescription(Person payee, PayeeACHAccount achAccount, MaintenanceDocument document) {
        boolean isNewAccount = isDocumentCreatingNewAccount(document);
        boolean isEntityId = StringUtils.equalsIgnoreCase(PayeeIdTypeCodes.ENTITY, achAccount.getPayeeIdentifierTypeCode());
        int descMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), KRADPropertyConstants.DOCUMENT_DESCRIPTION).intValue();
        StringBuilder docDescription = new StringBuilder(descMaxLength);
        
        docDescription.append(payee.getPrincipalName());
        docDescription.append(isNewAccount ? " -- New " : " -- Edit ");
        docDescription.append(isEntityId ? "Entity" : "Employee");
        docDescription.append(" Account");
        
        return StringUtils.left(docDescription.toString(), descMaxLength);
    }

    protected boolean isDocumentCreatingNewAccount(MaintenanceDocument document) {
        return StringUtils.equalsIgnoreCase(KFSConstants.MAINTENANCE_NEW_ACTION, document.getNewMaintainableObject().getMaintenanceAction());
    }

    protected String saveNewFailedACHDetail(PayeeACHAccountExtractDetail achDetail) {
        try {
            achDetail.setCreateDate(dateTimeService.getCurrentSqlDate());
            achDetail.setStatus(CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN);
            achDetail.setRetryCount(0);
            businessObjectService.save(achDetail);
            return StringUtils.EMPTY;
        } catch (Exception e) {
            LOG.error("saveNewFailedACHDetail: Could not save newly-failed ACH detail to database", e);
            
            return MessageFormat.format("Could not save newly-failed ACH detail for payee {0}: {1} - {2}",
                    achDetail.getNetID(), e.getCause(), e.getMessage());
        }
    }

    protected String updateACHDetailThatFailedOnRetry(PayeeACHAccountExtractDetail achDetail) {
        try {
            achDetail.setRetryCount(achDetail.getRetryCount() + 1);
            businessObjectService.save(achDetail);
            if (achDetail.getRetryCount() >= getMaximumACHDetailRetryCount()) {
                return MessageFormat.format("The maximum number of retries has been reached on ACH detail for payee {0}; "
                        + "no further retries will be attempted for it", achDetail.getNetID());
            } else {
                return StringUtils.EMPTY;
            }
        } catch (Exception e) {
            LOG.error("updateACHDetailThatFailedOnRetry: Could not update ACH detail that failed on retry", e);
            return MessageFormat.format("Could not update ACH detail for payee {0} that failed on retry: {1} - {2}",
                    achDetail.getNetID(), e.getCause(), e.getMessage());
        }
    }

    protected String updateACHDetailThatSucceededOnRetry(PayeeACHAccountExtractDetail achDetail) {
        try {
            achDetail.setRetryCount(achDetail.getRetryCount() + 1);
            achDetail.setStatus(CUPdpConstants.PayeeAchAccountExtractStatuses.PROCESSED);
            businessObjectService.save(achDetail);
            return StringUtils.EMPTY;
        } catch (Exception e) {
            LOG.error("updateACHDetailThatSucceededOnRetry: Could not update ACH detail that succeeded on retry", e);
            return MessageFormat.format("Could not update ACH detail for payee {0} that succeeded on retry: {1} - {2}",
                    achDetail.getNetID(), e.getCause(), e.getMessage());
        }
    }

    protected int getMaximumACHDetailRetryCount() {
        try {
            String retryCountString = getPayeeACHAccountExtractParameter(CUPdpParameterConstants.MAX_ACH_ACCT_EXTRACT_RETRY);
            int retryCount = Integer.parseInt(retryCountString);
            if (retryCount < 1) {
                throw new IllegalStateException("Maximum retry count was not a positive integer");
            }
            return retryCount;
        } catch (Exception e) {
            LOG.error("getMaximumACHDetailRetryCount: Could not find or parse max-retry-count parameter value", e);
            throw new IllegalStateException("The parameter for configuring the maximum number of ACH Detail retries "
                    + "has not been set to a valid integer value");
        }
    }

    /**
     * LOG error and throw RuntimeException
     * 
     * @param errorMessage
     */
    private void criticalError(String errorMessage) {
        LOG.error("criticalError: " + errorMessage);
        throw new RuntimeException(errorMessage);
    }    

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setBatchInputFileTypes(List<BatchInputFileType> batchInputFileTypes) {
        this.batchInputFileTypes = batchInputFileTypes;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService) {
        this.sequenceAccessorService = sequenceAccessorService;
    }

    public void setAchService(CuAchService achService) {
        this.achService = achService;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPayeeACHAccountExtractReportService(PayeeACHAccountExtractReportService payeeACHAccountExtractReportService) {
        this.payeeACHAccountExtractReportService = payeeACHAccountExtractReportService;
    }

    protected static class PayeeACHData {
        private Person payee;
        private PayeeACHAccountExtractDetail achDetail;
        private String payeeType;
        private Optional<PayeeACHAccount> oldAccount;
        
        public PayeeACHData(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType) {
            this(payee, achDetail, payeeType, null);
        }
        
        public PayeeACHData(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType, PayeeACHAccount oldAccount) {
            this.payee = payee;
            this.achDetail = achDetail;
            this.payeeType = payeeType;
            this.oldAccount = Optional.ofNullable(oldAccount);
        }
        
        public Person getPayee() {
            return payee;
        }
        
        public PayeeACHAccountExtractDetail getAchDetail() {
            return achDetail;
        }
        
        public String getPayeeType() {
            return payeeType;
        }
        
        public boolean hasOldAccount() {
            return oldAccount.isPresent();
        }
        
        public PayeeACHAccount getOldAccount() {
            return oldAccount.get();
        }
    }

    protected static class PayeeACHProcessingTracker {
        private List<PayeeACHAccountExtractDetailResult> processingResults;
        private Set<String> processedNetIds;
        
        public PayeeACHProcessingTracker() {
            this.processingResults = new ArrayList<>();
            this.processedNetIds = new HashSet<>();
        }
        
        public void addProcessingResult(PayeeACHAccountExtractDetailResult subResult) {
            processingResults.add(subResult);
        }
        
        public List<PayeeACHAccountExtractDetailResult> getCurrentProcessingResults() {
            return processingResults;
        }
        
        public void clearAccumulatedProcessingResults() {
            processingResults.clear();
        }
        
        public void addProcessedNetId(String netId) {
            processedNetIds.add(netId);
        }
        
        public boolean netIdWasProcessedAlready(String netId) {
            return processedNetIds.contains(netId);
        }
        
        public Set<String> getProcessedNetIds() {
            return processedNetIds;
        }
    }

}
