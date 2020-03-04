package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractDetailResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractGroupResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractResultCode;
import edu.cornell.kfs.pdp.CUPdpTestConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailSubResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractGroupResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReport;
import edu.cornell.kfs.pdp.batch.fixture.ACHRowFixture;
import edu.cornell.kfs.pdp.batch.fixture.PayeeACHAccountFixture;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@LoadSpringFile("edu/cornell/kfs/pdp/batch/cu-spring-payee-ach-report-test.xml")
public class PayeeACHAccountExtractReportServiceImplTest extends SpringEnabledMicroTestBase {

    private static final String EXPECTED_RESULTS_DIRECTORY_PATH = "src/test/resources/edu/cornell/kfs/pdp/reports/";
    private static final String TEST_REPORTS_DIRECTORY_PATH = "test/pdp/";

    private static final String REPROCESS_EMPTY_FILE = "reprocessedRows-empty";
    private static final String EMPTY_INPUT_FILE = "empty-input";

    private PayeeACHAccountExtractReportServiceImpl achExtractReportService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        achExtractReportService = springContext.getBean(
                CUPdpTestConstants.ACH_REPORT_SERVICE_BEAN_NAME, PayeeACHAccountExtractReportServiceImpl.class);
        buildReportOutputDirectory();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        achExtractReportService = null;
        removeGeneratedReportFilesAndDirectories();
    }

    private void buildReportOutputDirectory() throws Exception {
        File rassReportOutputDirectory = getReportsDirectoryAsFile();
        FileUtils.forceMkdir(rassReportOutputDirectory);
    }

    private void removeGeneratedReportFilesAndDirectories() throws Exception {
        File rassReportOutputDirectory = getReportsDirectoryAsFile();
        if (rassReportOutputDirectory.exists() && rassReportOutputDirectory.isDirectory()) {
            FileUtils.forceDelete(rassReportOutputDirectory.getAbsoluteFile());
        }
    }

    private File getReportsDirectoryAsFile() {
        return new File(TEST_REPORTS_DIRECTORY_PATH);
    }

    @Test
    public void testReportForZeroFilesAndZeroReprocessedRows() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(REPROCESS_EMPTY_FILE),
                achReport(emptyReprocessingResult()));
    }

    @Test
    public void testReportForEmptyFile() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(REPROCESS_EMPTY_FILE, EMPTY_INPUT_FILE),
                achReport(
                        emptyReprocessingResult(),
                        emptyFileResult()
                        ));
    }

    private void assertReportServiceGeneratesCorrectReportOutput(
            List<String> expectedOutputFiles, PayeeACHAccountExtractReport achReport) throws Exception {
        achExtractReportService.writeBatchJobReports(achReport);
        Collection<File> actualOutputFiles = FileUtils.listFiles(getReportsDirectoryAsFile(), null, false);
        assertEquals("Wrong number of report files generated", expectedOutputFiles.size(), actualOutputFiles.size());
        
        for (String expectedOutputFile : expectedOutputFiles) {
            File expectedFile = new File(EXPECTED_RESULTS_DIRECTORY_PATH + expectedOutputFile);
            File actualFile = findOutputFile(expectedOutputFile, actualOutputFiles);
            assertTrue("Could not find expected-results file " + expectedOutputFile, expectedFile.exists());
            
            String expectedContent = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
            String actualContent = getContentWithoutInitialPageMarkerLines(
                    FileUtils.readFileToString(actualFile, StandardCharsets.UTF_8));
            assertEquals("Wrong file contents for " + actualFile.getName(), expectedContent, actualContent);
        }
    }

    private File findOutputFile(String expectedOutputFile, Collection<File> actualOutputFiles) {
        String expectedOutputFileNamePrefixFragment = getOutputFileNamePrefixFragment(expectedOutputFile);
        String expectedFilePrefix = MessageFormat.format(
                achExtractReportService.getReportFileNamePrefixFormat(), expectedOutputFileNamePrefixFragment);
        File[] matchingFiles = actualOutputFiles.stream()
                .filter(file -> StringUtils.startsWith(file.getName(), expectedFilePrefix))
                .toArray(File[]::new);
        assertEquals("Wrong number of output files found for " + expectedOutputFile, 1, matchingFiles.length);
        return matchingFiles[0];
    }

    private String getOutputFileNamePrefixFragment(String expectedOutputFile) {
        if (expectedOutputFileIsForReprocessedResults(expectedOutputFile)) {
            return CUPdpConstants.ACH_EXTRACT_REPROCESSED_ROWS_BASE_FILENAME;
        } else {
            return StringUtils.substringBeforeLast(expectedOutputFile, KFSConstants.DELIMITER);
        }
    }

    private boolean expectedOutputFileIsForReprocessedResults(String expectedOutputFile) {
        return StringUtils.startsWith(expectedOutputFile, CUPdpConstants.ACH_EXTRACT_REPROCESSED_ROWS_BASE_FILENAME);
    }

    private String getContentWithoutInitialPageMarkerLines(String fileContent) {
        String result = StringUtils.substringAfter(fileContent, KFSConstants.NEWLINE);
        result = StringUtils.substringAfter(result, KFSConstants.NEWLINE);
        return result;
    }

    private String addTextExtensionToBaseFileName(String fileName) {
        return fileName + CuGeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION;
    }

    private String addCsvExtensionToBaseFileName(String fileName) {
        return fileName + CUPdpTestConstants.CSV_FILE_EXTENSION;
    }

    private List<String> expectedFiles(String... expectedFiles) {
        return Stream.of(expectedFiles)
                .map(this::addTextExtensionToBaseFileName)
                .collect(Collectors.toList());
    }

    private PayeeACHAccountExtractGroupResult emptyFileResult() {
        return fileResult(ACHExtractGroupResultCode.SUCCESS, EMPTY_INPUT_FILE, messages());
    }

    private PayeeACHAccountExtractReport achReport(PayeeACHAccountExtractGroupResult reprocessingResult,
            PayeeACHAccountExtractGroupResult... fileResults) {
        return new PayeeACHAccountExtractReport(reprocessingResult, Arrays.asList(fileResults));
    }

    private PayeeACHAccountExtractGroupResult emptyReprocessingResult() {
        return reprocessingResult(ACHExtractGroupResultCode.SKIPPED,
                messages("There were no unresolved ACH rows from previous runs that needed to be processed."));
    }

    private PayeeACHAccountExtractGroupResult reprocessingResult(ACHExtractGroupResultCode resultCode,
            String[] messages, PayeeACHAccountExtractDetailResult... detailResults) {
        return new PayeeACHAccountExtractGroupResult(resultCode, Arrays.asList(detailResults), messages);
    }

    private PayeeACHAccountExtractGroupResult fileResult(ACHExtractGroupResultCode resultCode,
            String fileName, String[] messages, PayeeACHAccountExtractDetailResult... detailResults) {
        String csvFileName = addCsvExtensionToBaseFileName(fileName);
        return new PayeeACHAccountExtractGroupResult(resultCode, csvFileName, Arrays.asList(detailResults), messages);
    }

    private PayeeACHAccountExtractDetailResult detailResult(ACHExtractDetailResultCode resultCode,
            ACHRowFixture rowFixture, PayeeACHAccountExtractDetailSubResult... subResults) {
        return detailResult(resultCode, rowFixture, Arrays.asList(subResults), Collections.emptyList());
    }

    private PayeeACHAccountExtractDetailResult detailResult(ACHExtractDetailResultCode resultCode,
            ACHRowFixture rowFixture, String... messages) {
        return detailResult(resultCode, rowFixture, Collections.emptyList(), Arrays.asList(messages));
    }

    private PayeeACHAccountExtractDetailResult detailResult(ACHExtractDetailResultCode resultCode,
            ACHRowFixture rowFixture, String[] messages, PayeeACHAccountExtractDetailSubResult... subResults) {
        return detailResult(resultCode, rowFixture, Arrays.asList(subResults), Arrays.asList(messages));
    }

    private PayeeACHAccountExtractDetailResult detailResult(ACHExtractDetailResultCode resultCode,
            ACHRowFixture rowFixture, List<PayeeACHAccountExtractDetailSubResult> subResults, List<String> messages) {
        return new PayeeACHAccountExtractDetailResult(resultCode, rowFixture.netId, rowFixture.getCreateDateAsSqlDate(),
                subResults, messages);
    }

    private PayeeACHAccountExtractDetailSubResult subResult(ACHExtractResultCode resultCode,
            PayeeACHAccountFixture accountFixture, String... messages) {
        return subResult(resultCode, null, accountFixture, messages);
    }

    private PayeeACHAccountExtractDetailSubResult subResult(ACHExtractResultCode resultCode,
            String documentNumber, PayeeACHAccountFixture accountFixture, String... messages) {
        return new PayeeACHAccountExtractDetailSubResult(resultCode, accountFixture.getPayeeIdNumber(),
                accountFixture.payeeIdentifierTypeCode, documentNumber, Arrays.asList(messages));
    }

    private String[] messages(String... messages) {
        return messages;
    }

}
