package edu.cornell.kfs.pdp.batch.service.impl;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractDetailResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractResultCode;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailSubResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractGroupResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReport;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractReportService;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PayeeACHAccountExtractReportServiceImpl implements PayeeACHAccountExtractReportService {

    private static final Logger LOG = LogManager.getLogger(PayeeACHAccountExtractReportServiceImpl.class);

    protected String reportFileNamePrefixFormat;
    protected ParameterService parameterService;
    protected ReportWriterService reportWriterService;
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;

    @Override
    public void writeBatchJobReports(PayeeACHAccountExtractReport achReport) {
        LOG.info("writeBatchJobReports: Start writing ACH Extract batch job reports for each file");
        
        PayeeACHAccountExtractGroupResult reprocessedResults = achReport.getReprocessedRowResults();
        writeBatchJobReport(reprocessedResults);
        for (PayeeACHAccountExtractGroupResult fileResult : achReport.getFileResults()) {
            writeBatchJobReport(fileResult);
        }
        
        LOG.info("writeBatchJobReports: Finished writing ACH Extract batch job reports for each file");
    }

    private void writeBatchJobReport(PayeeACHAccountExtractGroupResult groupResult) {
        String baseName = groupResult.isFileProcessingResult()
                ? getFileNameWithoutPathOrExtension(groupResult.getFileName())
                : CUPdpConstants.ACH_EXTRACT_REPROCESSED_ROWS_BASE_FILENAME;
        String logMessageSuffix = groupResult.isFileProcessingResult()
                ? "for file: " + baseName : "for reprocessed rows";
        LOG.info("writeBatchJobReport: Start writing ACH Extract report " + logMessageSuffix);
        
        initializeNewReport(baseName);
        writeReportSummary(groupResult);
        writeReportDetails(groupResult);
        finalizeReportForCurrentFile();
        
        LOG.info("writeBatchJobReport: Finished writing ACH Extract report " + logMessageSuffix);
    }

    private String getFileNameWithoutPathOrExtension(String fileName) {
        String result = CuBatchFileUtils.getFileNameWithoutPath(fileName);
        result = StringUtils.substringBefore(result, KFSConstants.DELIMITER);
        return result;
    }

    private void initializeNewReport(String baseName) {
        String fileNamePrefix = MessageFormat.format(reportFileNamePrefixFormat, baseName);
        reportWriterService.setFileNamePrefix(fileNamePrefix);
        reportWriterService.initialize();
    }

    private void writeReportSummary(PayeeACHAccountExtractGroupResult groupResult) {
        if (groupResult.isFileProcessingResult()) {
            reportWriterService.writeFormattedMessageLine("Report for ACH File: %s",
                    CuBatchFileUtils.getFileNameWithoutPath(groupResult.getFileName()));
        } else {
            reportWriterService.writeFormattedMessageLine("Report for Reprocessed ACH Rows");
        }
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Summary ***");
        reportWriterService.writeNewLines(1);
        writeResultCountSummariesToReport(groupResult);
        writeSubResultCountSummariesToReport(groupResult);
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** End of Report Summary ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeResultCountSummariesToReport(PayeeACHAccountExtractGroupResult groupResult) {
        writeResultCountSummaryToReportByResultCode(groupResult, ACHExtractDetailResultCode.SUCCESS);
        if (!groupResult.isFileProcessingResult()) {
            writeResultCountSummaryToReportByResultCode(groupResult, ACHExtractDetailResultCode.SKIPPED_MAX_RETRY);
        }
        writeResultCountSummaryToReportByResultCode(groupResult, ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID);
        writeResultCountSummaryToReportByResultCode(groupResult, ACHExtractDetailResultCode.ERROR);
    }

    private void writeResultCountSummaryToReportByResultCode(PayeeACHAccountExtractGroupResult groupResult, ACHExtractDetailResultCode resultCode) {
        long resultCount = groupResult.getDetailResults().stream()
                .filter(result -> resultCode.equals(result.getResultCode()))
                .count();
        
        String resultMessage;
        switch (resultCode) {
            case SUCCESS :
                resultMessage = "successfully processed";
                break;
            case SKIPPED_MAX_RETRY :
                resultMessage = "that were skipped due to exceeding the maximum number of retry attempts";
                break;
            case SKIPPED_MULTI_NET_ID :
                resultMessage = "that were skipped because of user duplication";
                break;
            case ERROR :
                resultMessage = "that resulted in error";
                break;
            default :
                resultMessage = StringUtils.EMPTY;
                break;
        }
        
        reportWriterService.writeFormattedMessageLine("Number of ACH rows %s: %s", resultMessage, resultCount);
    }

    private void writeSubResultCountSummariesToReport(PayeeACHAccountExtractGroupResult groupResult) {
        List<String> payeeTypes = Arrays.asList(PayeeIdTypeCodes.ENTITY, PayeeIdTypeCodes.EMPLOYEE);
        List<PayeeACHAccountExtractDetailSubResult> subResults = groupResult.getDetailResults().stream()
                .flatMap(detailResult -> detailResult.getSubResults().stream())
                .collect(Collectors.toList());
        for (String payeeType : payeeTypes) {
            writeSubResultCountSummaryToReportByResultCodeAndPayeeType(subResults, ACHExtractResultCode.SUCCESS_NEW, payeeType);
            writeSubResultCountSummaryToReportByResultCodeAndPayeeType(subResults, ACHExtractResultCode.SUCCESS_EDIT, payeeType);
            writeSubResultCountSummaryToReportByResultCodeAndPayeeType(subResults, ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE, payeeType);
            writeSubResultCountSummaryToReportByResultCodeAndPayeeType(subResults, ACHExtractResultCode.ERROR, payeeType);
        }
    }

    private void writeSubResultCountSummaryToReportByResultCodeAndPayeeType(List<PayeeACHAccountExtractDetailSubResult> subResults,
            ACHExtractResultCode resultCode, String payeeType) {
        long resultCount = subResults.stream()
                .filter(subResult -> resultCode.equals(subResult.getResultCode()) && StringUtils.equals(payeeType, subResult.getPayeeType()))
                .count();
        String payeeTypeLabel = getPayeeTypeLabel(payeeType);
        
        String resultMessage;
        switch (resultCode) {
            case SUCCESS_NEW :
                resultMessage = "successfully submitted for creation";
                break;
            case SUCCESS_EDIT :
                resultMessage = "successfully submitted for update";
                break;
            case SKIPPED_NO_DATA_CHANGE :
                resultMessage = "that were skipped because no data updates were made";
                break;
            case ERROR :
                resultMessage = "that resulted in error";
                break;
            default :
                resultMessage = StringUtils.EMPTY;
                break;
        }
        
        reportWriterService.writeFormattedMessageLine("Number of ACH %s account changes %s: %s",
                payeeTypeLabel, resultMessage, resultCount);
    }

    private void writeReportDetails(PayeeACHAccountExtractGroupResult groupResult) {
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Details ***");
        reportWriterService.writeNewLines(1);
        if (CollectionUtils.isNotEmpty(groupResult.getDetailResults())) {
            writeDetailLinesToReport(groupResult);
            reportWriterService.writeNewLines(1);
        }
        if (CollectionUtils.isNotEmpty(groupResult.getGroupMessages())) {
            writeGroupLevelMessagesToReport(groupResult);
            reportWriterService.writeNewLines(1);
        }
        reportWriterService.writeSubTitle("*** End of Report Details ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeGroupLevelMessagesToReport(PayeeACHAccountExtractGroupResult groupResult) {
        if (CollectionUtils.isNotEmpty(groupResult.getDetailResults())) {
            reportWriterService.writeFormattedMessageLine("*** Miscellaneous Details ***");
            reportWriterService.writeNewLines(1);
        }
        writeMessagesToReport(groupResult.getGroupMessages(), StringUtils.EMPTY);
    }

    private void writeDetailLinesToReport(PayeeACHAccountExtractGroupResult groupResult) {
        List<PayeeACHAccountExtractDetailResult> detailResults = groupResult.getDetailResults();
        writeDetailLinesToReportByResultCode(detailResults, ACHExtractDetailResultCode.SUCCESS);
        writeDetailLinesToReportByResultCode(detailResults, ACHExtractDetailResultCode.SKIPPED_MAX_RETRY);
        writeDetailLinesToReportByResultCode(detailResults, ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID);
        writeDetailLinesToReportByResultCode(detailResults, ACHExtractDetailResultCode.ERROR);
    }

    private void writeDetailLinesToReportByResultCode(
            List<PayeeACHAccountExtractDetailResult> detailResults, ACHExtractDetailResultCode resultCode) {
        List<PayeeACHAccountExtractDetailResult> filteredResults = detailResults.stream()
                .filter(detailResult -> resultCode.equals(detailResult.getResultCode()))
                .collect(Collectors.toList());
        
        writeHeaderForDetailLineResultsByResultCode(resultCode);
        reportWriterService.writeNewLines(1);
        
        for (PayeeACHAccountExtractDetailResult detailResult : filteredResults) {
            writeDetailLineToReport(detailResult);
        }
    }

    private void writeHeaderForDetailLineResultsByResultCode(ACHExtractDetailResultCode resultCode) {
        String header;
        switch (resultCode) {
            case SUCCESS:
                header = "*** ACH rows successfully processed ***";
                break;
            case SKIPPED_MAX_RETRY:
                header = "*** ACH rows exceeding maximum retry attempts ***";
                break;
            case SKIPPED_MULTI_NET_ID:
                header = "*** ACH rows duplicating other users in same load ***";
                break;
            case ERROR:
                header = "*** ACH rows with processing errors ***";
                break;
            default:
                header = StringUtils.EMPTY;
                break;
        }
        reportWriterService.writeNewLines(1);
        reportWriterService.writeFormattedMessageLine(header);
    }

    private void writeDetailLineToReport(PayeeACHAccountExtractDetailResult detailResult) {
        String netId = detailResult.getNetId().orElse("UNKNOWN");
        String originalDate = detailResult.getOriginalProcessingDate()
                .map(dateTimeService::toDateString)
                .orElse("UNKNOWN");
        reportWriterService.writeFormattedMessageLine("Payee with NetID %s from date %s", netId, originalDate);
        if (CollectionUtils.isNotEmpty(detailResult.getMessages())) {
            writeMessagesToReport(detailResult.getMessages(), "- ");
        }
        for (PayeeACHAccountExtractDetailSubResult subResult : detailResult.getSubResults()) {
            writeSubResultToReport(subResult, netId);
        }
    }

    private void writeSubResultToReport(PayeeACHAccountExtractDetailSubResult subResult, String netId) {
        String documentNumber = subResult.getDocumentNumber().orElse("UNKNOWN");
        String payeeTypeLabel = getPayeeTypeLabel(subResult.getPayeeType());
        String headerLineSuffix;
        switch (subResult.getResultCode()) {
            case SUCCESS_NEW:
                headerLineSuffix = "was submitted for creation by document #: " + documentNumber;
                break;
            case SUCCESS_EDIT:
                headerLineSuffix = "was submitted for update by document #: " + documentNumber;
                break;
            case SKIPPED_NO_DATA_CHANGE:
                headerLineSuffix = "was left unchanged";
                break;
            case ERROR:
                headerLineSuffix = "encountered a processing error";
                break;
            default:
                headerLineSuffix = StringUtils.EMPTY;
                break;
        }
        
        reportWriterService.writeFormattedMessageLine("* KFS ACH record for %s ID %s and user %s %s",
                payeeTypeLabel, subResult.getPayeeId(), netId, headerLineSuffix);
        if (CollectionUtils.isNotEmpty(subResult.getMessages())) {
            writeMessagesToReport(subResult.getMessages(), "*- ");
        }
    }

    private void writeMessagesToReport(List<String> messages, String messagePrefix) {
        for (String message : messages) {
            reportWriterService.writeFormattedMessageLine(messagePrefix + message);
        }
    }

    private void finalizeReportForCurrentFile() {
        reportWriterService.destroy();
    }

    private String getPayeeTypeLabel(String payeeType) {
        if (StringUtils.equals(PayeeIdTypeCodes.ENTITY, payeeType)) {
            return "Entity";
        } else if (StringUtils.equals(PayeeIdTypeCodes.EMPLOYEE, payeeType)) {
            return "Employee";
        } else {
            return StringUtils.EMPTY;
        }
    }

    public String getReportFileNamePrefixFormat() {
        return reportFileNamePrefixFormat;
    }

    public void setReportFileNamePrefixFormat(String reportFileNamePrefixFormat) {
        this.reportFileNamePrefixFormat = reportFileNamePrefixFormat;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
