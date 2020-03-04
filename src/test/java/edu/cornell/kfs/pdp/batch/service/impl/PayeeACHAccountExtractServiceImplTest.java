package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GeneralLedgerConstants.BatchFileSystem;
import org.kuali.kfs.kns.datadictionary.control.SelectControlDefinition;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.datadictionary.mask.MaskFormatterLiteral;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.impl.EmailServiceImpl;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractDetailResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractGroupResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.ACHExtractResultCode;
import edu.cornell.kfs.pdp.CUPdpConstants.PayeeAchAccountExtractStatuses;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.pdp.CUPdpTestConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsv;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsvInputFileType;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractDetailSubResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractGroupResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReport;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractStep;
import edu.cornell.kfs.pdp.batch.fixture.ACHBankFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHPersonPayeeFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHRowFixture;
import edu.cornell.kfs.pdp.batch.fixture.PayeeACHAccountFixture;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractReportService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.pdp.businessobject.options.TestPayeeAchIdTypeValuesFinder;
import edu.cornell.kfs.pdp.document.CuPayeeACHAccountMaintainableImpl;
import edu.cornell.kfs.pdp.service.CuAchService;
import edu.cornell.kfs.pdp.service.impl.CuAchServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.sys.util.MockPersonUtil;

@SuppressWarnings("deprecation")
public class PayeeACHAccountExtractServiceImplTest {

    private static final String ACH_SOURCE_FILE_PATH = "src/test/resources/edu/cornell/kfs/pdp/batch/service/fixture";
    private static final String ACH_TESTING_FILE_PATH = "test/pdp/payeeACHAccountExtract";
    private static final String ACH_TESTING_DIRECTORY = ACH_TESTING_FILE_PATH + "/";
    private static final String FILE_PATH_FORMAT = "{0}/{1}{2}";
    private static final String LITERAL_MASK_VALUE = "********";
    private static final Integer DOCUMENT_DESCRIPTION_MAX_LENGTH = Integer.valueOf(40);
    private static final String BANK_ACCOUNT_NUMBER_MAX_LENGTH_ERROR_MESSAGE = "Bank account number is too long";
    private static final String BUSINESS_RULES_FAILURE_MESSAGE = "Business rule validation failed";
    private static final String GLOBAL_ERROR_FORMAT = "{0}";
    private static final String ROW_RETRY_INDEX = "ROW_RETRY";
    private static final int BANK_ACCOUNT_NUMBER_MAX_LENGTH_FOR_ROUTING_VALIDATION = 17;
    private static final int INITIAL_DOCUMENT_ID = 1000;
    private static final long INITIAL_ACH_ACCOUNT_ID = 5000L;

    private static final String UNRESOLVED_EMAIL_STRING = "Payment $ \\n\\ for [payeeIdentifierTypeCode] of [payeeIdNumber]"
            + " will go to [bankAccountTypeCode] account at [bankRouting.bankName].[bankAccountNumber]";
    private static final String EMAIL_STRING_AS_FORMAT = "Payment $ \n\\ for {0} of {1} will go to {2} account at {3}.";
    private static final String PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL = "Personal Savings";

    private TestPayeeACHAccountExtractService payeeACHAccountExtractService;
    private TestDateTimeServiceImpl dateTimeService;
    private Map<String, MaintenanceDocument> documentsMap;
    private Map<KualiInteger, PayeeACHAccountExtractDetail> savedAchDetailsMap;
    private PayeeACHAccountExtractReport achReport;
    private AtomicInteger documentIdCounter;
    private AtomicLong achAccountIdCounter;
    private Iterator<MaintenanceDocument> documentIterator;
    private Iterator<PayeeACHAccountExtractDetail> savedAchDetailIterator;

    @Before
    public void setUp() throws Exception {
        BusinessObjectService businessObjectService = createMockBusinessObjectService();
        dateTimeService = new TestDateTimeServiceImpl();
        dateTimeService.afterPropertiesSet();
        
        documentsMap = new LinkedHashMap<>();
        savedAchDetailsMap = new LinkedHashMap<>();
        documentIdCounter = new AtomicInteger(INITIAL_DOCUMENT_ID);
        achAccountIdCounter = new AtomicLong(INITIAL_ACH_ACCOUNT_ID);
        
        payeeACHAccountExtractService = new TestPayeeACHAccountExtractService();
        payeeACHAccountExtractService.setBatchInputFileService(new BatchInputFileServiceImpl());
        payeeACHAccountExtractService.setBatchInputFileTypes(
                Collections.singletonList(createACHBatchInputFileType()));
        payeeACHAccountExtractService.setParameterService(createMockParameterService());
        payeeACHAccountExtractService.setEmailService(new TestEmailService());
        payeeACHAccountExtractService.setDocumentService(createMockDocumentService());
        payeeACHAccountExtractService.setDataDictionaryService(createMockDataDictionaryService());
        payeeACHAccountExtractService.setPersonService(createMockPersonService());
        payeeACHAccountExtractService.setSequenceAccessorService(createMockSequenceAccessorService());
        payeeACHAccountExtractService.setAchService(createAchService(businessObjectService));
        payeeACHAccountExtractService.setAchBankService(createMockAchBankService());
        payeeACHAccountExtractService.setConfigurationService(createMockConfigurationService());
        payeeACHAccountExtractService.setBusinessObjectService(businessObjectService);
        payeeACHAccountExtractService.setDateTimeService(dateTimeService);
        payeeACHAccountExtractService.setPayeeACHAccountExtractReportService(
                createMockPayeeACHAccountExtractReportServiceThatRecordsReport(this::setAchReport));
    }

    @After
    public void cleanUp() throws Exception {
        removeTestFilesAndDirectories();
    }

    @Test
    public void testRunJobWithoutReprocessedRowsOrDataFiles() throws Exception {
        assertACHAccountExtractionHasCorrectResults(emptyReprocessingResult());
    }

    @Test
    public void testLoadValidFileWithSingleDataRow() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_good",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testLoadFileWithInvalidFormat() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.ERROR, "pdp_ach_test_badHeaders"));
    }

    @Test
    public void testLoadFileWithoutAccountRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_headerOnly"));
    }

    @Test
    public void testLoadFileWithAllInvalidRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_badLines",
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JOHN_DOE_BAD_EMPLOYEE_ID),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JOHN_DOE_BAD_NET_ID),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JANE_DOE_BAD_PAYMENT_TYPE),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.MARY_SMITH_BAD_BALANCE_ACCOUNT),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.ROBERT_SMITH_BAD_ROUTING_NUMBER),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JACK_BROWN_BAD_ACCOUNT_TYPE),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.SYSTEM_USER_BAD_MULTI_FIELDS)));
    }

    @Test
    public void testLoadFileWithSomeInvalidRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_mixGoodBadLines",
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.BAD_USER),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JOHN_DOE_BAD_MULTI_EMPTY_FIELDS),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JANE_DOE_VALID_FORMATTED_ACCOUNT_NUMBER,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.MARY_SMITH_BAD_ROUTING_NUMBER),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.ROBERT_SMITH_BAD_ACCOUNT_NUMBER),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JACK_BROWN_VALID_ALT_BANK_NAME,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JACK_BROWN_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JACK_BROWN_SAVINGS_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testLoadFirstFileWithMissingFieldData() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_missingData1",
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.BAD_EMPTY_ROW),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JOHN_DOE_BAD_NO_EMPLOYEE_ID),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JANE_DOE_BAD_NO_NET_ID),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JANE_DOE_VALID_NO_LAST_NAME,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_NO_FIRST_NAME,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.ROBERT_SMITH_BAD_NO_PAYMENT_TYPE),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JACK_BROWN_BAD_NO_BALANCE_ACCOUNT)
                        ));
    }

    @Test
    public void testLoadSecondFileWithMissingFieldData() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_missingData2",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_NO_COMPLETED_DATE,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JANE_DOE_BAD_NO_BANK_NAME),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.MARY_SMITH_BAD_NO_ROUTING_NUMBER),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.ROBERT_SMITH_BAD_NO_ACCOUNT_NUMBER),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JACK_BROWN_BAD_NO_ACCOUNT_TYPE)
                        ));
    }

    @Test
    public void testLoadFileWithLineMatchingExistingAccounts() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_matching",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_MATCHING_ROW,
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD),
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD))
                        ));
    }

    @Test
    public void testLoadFileWithMultipleValidRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_multiGood",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JANE_DOE_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.ROBERT_SMITH_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JACK_BROWN_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JACK_BROWN_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JACK_BROWN_SAVINGS_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testLoadFileWithLinesPartiallyOrFullyMatchingExistingAccounts() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_multiMatching",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.ROBERT_SMITH_VALID_ALT_ROW,
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE,
                                        PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_EMPLOYEE_ALT_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JANE_DOE_VALID_ALT_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_ALT_NEW),
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_MATCHING_ROW,
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD),
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD))
                        ));
    }

    @Test
    public void testLoadFileWithValidRowsPlusOneRulesFailureRow() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_mixGoodBadWithRulesFailure",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JANE_DOE_BAD_LENGTHY_ACCOUNT_NUMBER,
                                subResult(ACHExtractResultCode.ERROR, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.ROBERT_SMITH_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testLoadFileWithUserDuplication() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_duplicateUser",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID, ACHRowFixture.JOHN_DOE_VALID_ROW)
                        ));
    }

    @Test
    public void testLoadMultipleFiles() throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                emptyReprocessingResult(),
                fileResult(ACHExtractGroupResultCode.ERROR, "pdp_ach_test_badHeaders"),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_good",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW))
                        ),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_mixGoodBadWithRulesFailure",
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID, ACHRowFixture.JOHN_DOE_VALID_ROW),
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.JANE_DOE_BAD_LENGTHY_ACCOUNT_NUMBER,
                                subResult(ACHExtractResultCode.ERROR, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.ROBERT_SMITH_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testInitialReprocessOfSingleRow() throws Exception {
        saveAchDetails(ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testReprocessOfSingleRowJustBelowMaxRetryLimit() throws Exception {
        saveAchDetails(ACHRowFixture.MARY_SMITH_VALID_SAVED_ROW_NEAR_MAX_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_SAVED_ROW_NEAR_MAX_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testReprocessOfSingleRowAtMaxRetryLimit() throws Exception {
        saveAchDetails(ACHRowFixture.ROBERT_SMITH_VALID_SAVED_ROW_MAX_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MAX_RETRY, ACHRowFixture.ROBERT_SMITH_VALID_SAVED_ROW_MAX_RETRY)
                        ));
    }

    @Test
    public void testReprocessOfMultipleValidRowsWithVaryingRetryLimits() throws Exception {
        saveAchDetails(ACHRowFixture.MARY_SMITH_VALID_SAVED_ROW_NEAR_MAX_RETRY, ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY,
                ACHRowFixture.JACK_BROWN_VALID_SAVED_ROW_BEYOND_MAX_RETRY, ACHRowFixture.JANE_DOE_VALID_SAVED_ROW_SECOND_RETRY,
                ACHRowFixture.ROBERT_SMITH_VALID_SAVED_ROW_MAX_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_SAVED_ROW_NEAR_MAX_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MAX_RETRY, ACHRowFixture.JACK_BROWN_VALID_SAVED_ROW_BEYOND_MAX_RETRY),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JANE_DOE_VALID_SAVED_ROW_SECOND_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MAX_RETRY, ACHRowFixture.ROBERT_SMITH_VALID_SAVED_ROW_MAX_RETRY)
                        ));
    }

    @Test
    public void testSkipReprocessingOfNonOpenRows() throws Exception {
        saveAchDetails(ACHRowFixture.JOHN_DOE_VALID_PROCESSED_SAVED_ROW, ACHRowFixture.JACK_BROWN_BAD_CANCELED_SAVED_ROW);
        
        assertACHAccountExtractionHasCorrectResults(emptyReprocessingResult());
    }

    @Test
    public void testSkipDuplicateUserReprocessing() throws Exception {
        saveAchDetails(ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_SECOND_RETRY, ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_SECOND_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID, ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY)
                        ));
    }

    @Test
    public void testAttemptedReprocessingOfInvalidRow() throws Exception {
        saveAchDetails(ACHRowFixture.MARY_SMITH_BAD_SAVED_ROW_NEAR_MAX_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.MARY_SMITH_BAD_SAVED_ROW_NEAR_MAX_RETRY)
                        ));
    }

    @Test
    public void testReprocessSingleRowMatchingExistingAccounts() throws Exception {
        saveAchDetails(ACHRowFixture.MARY_SMITH_VALID_MATCHING_SAVED_ROW);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_MATCHING_SAVED_ROW,
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD),
                                subResult(ACHExtractResultCode.SKIPPED_NO_DATA_CHANGE,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD))
                        ));
    }

    @Test
    public void testReprocessSingleRowAndLoadSingleFile() throws Exception {
        saveAchDetails(ACHRowFixture.MARY_SMITH_VALID_SAVED_ROW_NEAR_MAX_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.MARY_SMITH_VALID_SAVED_ROW_NEAR_MAX_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW))
                        ),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_good",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testProcessingOfMultipleSavedRowsAndMultipleFiles() throws Exception {
        saveAchDetails(ACHRowFixture.JOHN_DOE_VALID_PROCESSED_SAVED_ROW, ACHRowFixture.MARY_SMITH_BAD_SAVED_ROW_NEAR_MAX_RETRY,
                ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY, ACHRowFixture.ROBERT_SMITH_VALID_SAVED_ROW_MAX_RETRY);
        
        assertACHAccountExtractionHasCorrectResults(
                reprocessingResult(ACHExtractGroupResultCode.SUCCESS,
                        detailResult(ACHExtractDetailResultCode.ERROR, ACHRowFixture.MARY_SMITH_BAD_SAVED_ROW_NEAR_MAX_RETRY),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JOHN_DOE_VALID_SAVED_ROW_FIRST_RETRY,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MAX_RETRY, ACHRowFixture.ROBERT_SMITH_VALID_SAVED_ROW_MAX_RETRY)
                        ),
                fileResult(ACHExtractGroupResultCode.ERROR, "pdp_ach_test_badHeaders"),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_good",
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID, ACHRowFixture.JOHN_DOE_VALID_ROW)
                        ),
                fileResult(ACHExtractGroupResultCode.SUCCESS, "pdp_ach_test_multiGood",
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JANE_DOE_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD,
                                        PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID, ACHRowFixture.MARY_SMITH_VALID_ROW),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.ROBERT_SMITH_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_EDIT, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                                        PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_EMPLOYEE_NEW)),
                        detailResult(ACHExtractDetailResultCode.SUCCESS, ACHRowFixture.JACK_BROWN_VALID_ROW,
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JACK_BROWN_SAVINGS_ACCOUNT_ENTITY_NEW),
                                subResult(ACHExtractResultCode.SUCCESS_NEW, PayeeACHAccountFixture.JACK_BROWN_SAVINGS_ACCOUNT_EMPLOYEE_NEW))
                        ));
    }

    @Test
    public void testEmailBodyPlaceholderResolution() throws Exception {
        // NOTE: We expect that all potentially-sensitive placeholders except bank name will be replaced with empty text.
        TestPayeeAchIdTypeValuesFinder payeeIdTypeKeyValues = new TestPayeeAchIdTypeValuesFinder();
        PayeeACHAccount achAccount = PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW.toPayeeACHAccount();
        String expectedPayeeIdTypeLabel = payeeIdTypeKeyValues.getKeyLabel(achAccount.getPayeeIdentifierTypeCode());
        String expectedBody = MessageFormat.format(EMAIL_STRING_AS_FORMAT, expectedPayeeIdTypeLabel,
                achAccount.getPayeeIdNumber(), PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL, achAccount.getBankRouting().getBankName());
        String actualBody = payeeACHAccountExtractService.getResolvedEmailBody(achAccount, UNRESOLVED_EMAIL_STRING);
        
        assertEquals("Email body placeholders and special characters were not resolved properly", expectedBody, actualBody);
    }

    private void assertACHAccountExtractionHasCorrectResults(ExpectedACHExtractGroupResult expectedReprocessedRowsResult,
            ExpectedACHExtractGroupResult... expectedFileResults) throws Exception {
        assertACHAccountExtractionHasCorrectResults(
                expectedReport(expectedReprocessedRowsResult, expectedFileResults));
    }

    private void assertACHAccountExtractionHasCorrectResults(ExpectedACHExtractReport expectedReport) throws Exception {
        String[] fileNames = expectedReport.getExpectedFileResults().stream()
                .map(ExpectedACHExtractGroupResult::getBaseFileName)
                .toArray(String[]::new);
        List<KualiInteger> initialProcessableSavedDetailIds = getIdsOfInitialSavedOpenAchDetails();
        int oldDetailCount = savedAchDetailsMap.size();
        
        copyInputFilesAndGenerateDoneFiles(fileNames);
        processACHBatchDetailsInNewUserSession();
        documentIterator = documentsMap.values().iterator();
        savedAchDetailIterator = getIteratorOverSavedAchDetailsThatWereExpectedToBeProcessed(
                initialProcessableSavedDetailIds, oldDetailCount);
        
        assertTrue("A non-null ACH report should have been generated and recorded", ObjectUtils.isNotNull(achReport));
        assertAchExtractHasExpectedResults(expectedReport, achReport);
        assertDoneFilesWereDeleted(fileNames);
        assertFalse("Some extra unexpected documents were created by the ACH extract process", documentIterator.hasNext());
        assertFalse("Some extra unexpected ACH details were saved by the ACH extract process", savedAchDetailIterator.hasNext());
    }

    private boolean processACHBatchDetailsInNewUserSession() throws Exception {
        Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        UserSession systemUserSession = MockPersonUtil.createMockUserSession(systemUser);
        return GlobalVariables.doInNewGlobalVariables(systemUserSession, payeeACHAccountExtractService::processACHBatchDetails);
    }

    private void assertAchExtractHasExpectedResults(
            ExpectedACHExtractReport expectedReport, PayeeACHAccountExtractReport actualReport) throws Exception {
        ExpectedACHExtractGroupResult expectedReprocessResult = expectedReport.getExpectedReprocessedRowsResult();
        PayeeACHAccountExtractGroupResult actualReprocessResult = actualReport.getReprocessedRowResults();
        String reprocessedResultsIndexString = buildInitialIndexString(ROW_RETRY_INDEX);
        
        assertTrue("Result grouping for reprocessed rows should not be null", ObjectUtils.isNotNull(actualReprocessResult));
        assertAchGroupingHasExpectedResults(reprocessedResultsIndexString, expectedReprocessResult, actualReprocessResult);
        
        int expectedFileResultCount = expectedReport.getExpectedFileResults().size();
        assertEquals("Wrong number of file result groupings", expectedFileResultCount, actualReport.getFileResults().size());
        for (int i = 0; i < expectedFileResultCount; i++) {
            ExpectedACHExtractGroupResult expectedGroupResult = expectedReport.getExpectedFileResults().get(i);
            PayeeACHAccountExtractGroupResult actualGroupResult = actualReport.getFileResults().get(i);
            String groupIndexString = buildInitialIndexString(expectedGroupResult.getExpectedFileName());
            assertAchGroupingHasExpectedResults(groupIndexString, expectedGroupResult, actualGroupResult);
        }
    }

    private void assertAchGroupingHasExpectedResults(String indexStr,
            ExpectedACHExtractGroupResult expectedResult, PayeeACHAccountExtractGroupResult actualResult) throws Exception {
        if (expectedResult.isFileResult()) {
            assertTrue("Result grouping should have represented a file result group at index " + indexStr,
                    actualResult.isFileProcessingResult());
            assertEquals(
                    "Wrong filename for result group at index " + indexStr
                            + " (the service might be processing files in alphabetical order)",
                    expectedResult.getExpectedFileName(),
                    CuBatchFileUtils.getFileNameWithoutPath(actualResult.getFileName()));
        } else {
            assertFalse("Result grouping should have represented a row-reprocessing group at index " + indexStr,
                    actualResult.isFileProcessingResult());
        }

        assertEquals("Wrong group result code at index " + indexStr, expectedResult.getResultCode(), actualResult.getResultCode());
        if (ACHExtractGroupResultCode.SUCCESS.equals(expectedResult.getResultCode())) {
            assertEquals("No extra messages should have been recorded for successful result group at index " + indexStr,
                    0, actualResult.getGroupMessages().size());
        } else {
            assertFalse("There should have been one or more messages recorded for result group at index " + indexStr,
                    actualResult.getGroupMessages().isEmpty());
        }
        
        int expectedAchRowCount = expectedResult.getExpectedDetailResults().size();
        assertEquals("Wrong number of processed ACH rows at index " + indexStr,
                expectedAchRowCount, actualResult.getDetailResults().size());
        for (int i = 0; i < expectedAchRowCount; i++) {
            String detailIndexString = buildAppendedIndexString(indexStr, i);
            ExpectedACHExtractDetailResult expectedDetailResult = expectedResult.getExpectedDetailResults().get(i);
            PayeeACHAccountExtractDetailResult actualDetailResult = actualResult.getDetailResults().get(i);
            assertAchRowHasExpectedResult(detailIndexString, expectedDetailResult, actualDetailResult);
        }
    }

    private void assertAchRowHasExpectedResult(String indexStr,
            ExpectedACHExtractDetailResult expectedResult, PayeeACHAccountExtractDetailResult actualResult) throws Exception {
        assertEquals("Wrong result code at index " + indexStr, expectedResult.getResultCode(), actualResult.getResultCode());
        
        if (ACHExtractDetailResultCode.SUCCESS.equals(expectedResult.getResultCode())) {
            assertEquals("No extra messages should have been recorded for sucessful detail result at index " + indexStr,
                    0, actualResult.getMessages().size());
        } else {
            assertFalse("There should have been one or more messages recorded for detail result at index " + indexStr,
                    actualResult.getMessages().isEmpty());
        }
        
        if (StringUtils.isNotBlank(expectedResult.getAchRow().netId)) {
            assertTrue("Result detail should have recorded what NetID was used at index " + indexStr,
                    actualResult.getNetId().isPresent());
            assertEquals("Wrong result detail NetID at index " + indexStr,
                    expectedResult.getAchRow().netId, actualResult.getNetId().get());
        } else {
            assertFalse("Result detail should not have been able to record what NetID was used at index " + indexStr,
                    actualResult.getNetId().isPresent());
        }
        
        if (expectedResult.getAchRow().createDate.isPresent()) {
            assertTrue("Result detail should have had a create/original-processing date at index " + indexStr,
                    actualResult.getOriginalProcessingDate().isPresent());
            assertEquals("Wrong create/original-processing date at index " + indexStr,
                    expectedResult.getAchRow().getCreateDateAsSqlDate(), actualResult.getOriginalProcessingDate().get());
        } else {
            assertFalse("Result detail should not have had a create/original-processing date at index " + indexStr,
                    actualResult.getOriginalProcessingDate().isPresent());
        }
        
        assertPersistedAchRowIsCorrectIfPresent(indexStr, expectedResult);
        
        int expectedSubResultCount = expectedResult.getExpectedSubResults().size();
        assertEquals("Wrong number of sub-results at index " + indexStr,
                expectedSubResultCount, actualResult.getSubResults().size());
        for (int i = 0; i < expectedSubResultCount; i++) {
            String subResultIndexString = buildAppendedIndexString(indexStr, i);
            ExpectedACHExtractSubResult expectedSubResult = expectedResult.getExpectedSubResults().get(i);
            PayeeACHAccountExtractDetailSubResult actualSubResult = actualResult.getSubResults().get(i);
            assertSubResultHasExpectedAccountUpdates(subResultIndexString, expectedSubResult, actualSubResult);
        }
    }

    private void assertPersistedAchRowIsCorrectIfPresent(String indexStr, ExpectedACHExtractDetailResult expectedResult) throws Exception {
        boolean isReprocessedRow = StringUtils.contains(indexStr, ROW_RETRY_INDEX);
        if (ACHExtractDetailResultCode.SUCCESS.equals(expectedResult.getResultCode()) && !isReprocessedRow) {
            return;
        }
        
        assertTrue("There should have been a saved ACH detail present for result at index " + indexStr,
                savedAchDetailIterator.hasNext());
        
        ACHRowFixture expectedRow = expectedResult.getAchRow();
        PayeeACHAccountExtractDetail actualRow = savedAchDetailIterator.next();
        Date expectedCreateDate;
        String expectedStatus;
        Integer expectedRetryCount;
        
        if (isReprocessedRow) {
            boolean updateExpectedRetryCount = !ACHExtractDetailResultCode.SKIPPED_MAX_RETRY.equals(expectedResult.getResultCode())
                    && !ACHExtractDetailResultCode.SKIPPED_MULTI_NET_ID.equals(expectedResult.getResultCode());
            expectedCreateDate = expectedRow.getCreateDateAsSqlDate();
            expectedStatus = ACHExtractDetailResultCode.SUCCESS.equals(expectedResult.getResultCode())
                    ? PayeeAchAccountExtractStatuses.PROCESSED : expectedRow.status.get();
            expectedRetryCount = expectedRow.retryCount.get() + (updateExpectedRetryCount ? 1 : 0);
        } else {
            expectedCreateDate = dateTimeService.getCurrentSqlDate();
            expectedStatus = PayeeAchAccountExtractStatuses.OPEN;
            expectedRetryCount = 0;
        }
        
        assertEqualsForDates("Wrong saved create date for result at index " + indexStr,
                expectedCreateDate, actualRow.getCreateDate());
        assertEquals("Wrong saved status for result at index " + indexStr,
                expectedStatus, actualRow.getStatus());
        assertEquals("Wrong saved retry count for result at index " + indexStr,
                expectedRetryCount, actualRow.getRetryCount());
        assertEqualsOrBlank("Wrong saved employee ID for result at index " + indexStr,
                expectedRow.employeeId, actualRow.getEmployeeID());
        assertEqualsOrBlank("Wrong saved NetID for result at index " + indexStr,
                expectedRow.netId, actualRow.getNetID());
        assertEqualsOrBlank("Wrong saved first name for result at index " + indexStr,
                expectedRow.firstName, actualRow.getFirstName());
        assertEqualsOrBlank("Wrong saved last name for result at index " + indexStr,
                expectedRow.lastName, actualRow.getLastName());
        assertEqualsOrBlank("Wrong saved payment type for result at index " + indexStr,
                expectedRow.paymentType, actualRow.getPaymentType());
        assertEqualsOrBlank("Wrong saved balance account for result at index " + indexStr,
                expectedRow.balanceAccount, actualRow.getBalanceAccount());
        assertEqualsOrBlank("Wrong saved bank name for result at index " + indexStr,
                expectedRow.bankName, actualRow.getBankName());
        assertEqualsOrBlank("Wrong saved bank routing number for result at index " + indexStr,
                expectedRow.bankRoutingNumber, actualRow.getBankRoutingNumber());
        assertEqualsOrBlank("Wrong saved bank account number for result at index " + indexStr,
                expectedRow.bankAccountNumber, actualRow.getBankAccountNumber());
        assertEqualsOrBlank("Wrong saved bank account type for result at index " + indexStr,
                expectedRow.bankAccountType, actualRow.getBankAccountType());
    }

    private void assertSubResultHasExpectedAccountUpdates(String indexStr,
            ExpectedACHExtractSubResult expectedSubResult, PayeeACHAccountExtractDetailSubResult actualSubResult) throws Exception {
        assertEquals("Wrong sub-result code at index " + indexStr, expectedSubResult.getResultCode(), actualSubResult.getResultCode());
        assertEquals("Wrong payee type for sub-result at index " + indexStr,
                expectedSubResult.getAchAccount().payeeIdentifierTypeCode, actualSubResult.getPayeeType());
        assertEquals("Wrong payee ID for sub-result at index " + indexStr,
                expectedSubResult.getAchAccount().getPayeeIdNumber(), actualSubResult.getPayeeId());
        
        boolean shouldDocumentBePresent = false;
        String expectedMaintenanceAction = null;
        
        switch (expectedSubResult.getResultCode()) {
            case SUCCESS_NEW:
                expectedMaintenanceAction = KFSConstants.MAINTENANCE_NEW_ACTION;
                shouldDocumentBePresent = true;
                break;
            case SUCCESS_EDIT:
                expectedMaintenanceAction = KFSConstants.MAINTENANCE_EDIT_ACTION;
                shouldDocumentBePresent = true;
                break;
            case SKIPPED_NO_DATA_CHANGE:
                shouldDocumentBePresent = false;
                break;
            case ERROR:
                shouldDocumentBePresent = false;
                break;
            default:
                throw new IllegalStateException("Sub-result at index " + indexStr
                        + " had an invalid result code, this should NEVER happen! Result code: "
                        + expectedSubResult.getResultCode());
        }
        
        if (!shouldDocumentBePresent) {
           assertFalse("There should not have been an update to the account for sub-result at index " + indexStr,
                   actualSubResult.getDocumentNumber().isPresent());
           assertFalse("There should have been one or more messages recorded for sub-result at index " + indexStr,
                   actualSubResult.getMessages().isEmpty());
           return;
        }
        
        assertTrue("There should have been an update to the account for sub-result at index " + indexStr,
                actualSubResult.getDocumentNumber().isPresent());
        assertEquals("No extra messages should have been recorded for successful sub-result at index " + indexStr,
                0, actualSubResult.getMessages().size());
        assertTrue("A document should have been created when modifying sub-result at index " + indexStr, documentIterator.hasNext());
        
        MaintenanceDocument document = documentsMap.get(actualSubResult.getDocumentNumber().get());
        MaintenanceDocument documentFromIterator = documentIterator.next();
        
        assertTrue("There should have been a document to modify the account for sub-result at index " + indexStr,
                ObjectUtils.isNotNull(document));
        assertEquals("Found the wrong document association for sub-result at index " + indexStr,
                document.getDocumentNumber(), documentFromIterator.getDocumentNumber());
        
        FinancialSystemMaintainable newMaintainable = (FinancialSystemMaintainable) document.getNewMaintainableObject();
        assertTrue("New maintainable should not be null at sub-result index " + indexStr, ObjectUtils.isNotNull(newMaintainable));
        assertEquals("Wrong data object class at sub-result index " + indexStr,
                PayeeACHAccount.class, newMaintainable.getDataObjectClass());
        assertEquals("Wrong maintenance action at sub-result index " + indexStr,
                expectedMaintenanceAction, newMaintainable.getMaintenanceAction());
        assertPayeeACHAccountIsCorrect(indexStr, expectedMaintenanceAction, expectedSubResult.getAchAccount(), newMaintainable);
        
        if (StringUtils.equals(KFSConstants.MAINTENANCE_EDIT_ACTION, expectedMaintenanceAction)) {
            FinancialSystemMaintainable oldMaintainable = (FinancialSystemMaintainable) document.getOldMaintainableObject();
            assertTrue("Old maintainable should not be null at sub-result index " + indexStr, ObjectUtils.isNotNull(oldMaintainable));
            assertEquals("Wrong data object class on old maintainable at sub-result index " + indexStr,
                    PayeeACHAccount.class, oldMaintainable.getDataObjectClass());
            assertTrue("An expected \"old\" account should have been set up in the test configuration at sub-result index " + indexStr,
                    expectedSubResult.hasOldAchAccount());
            assertPayeeACHAccountIsCorrect(indexStr, expectedMaintenanceAction, expectedSubResult.getOldAchAccount(), oldMaintainable);
        } else {
            assertFalse("An \"old\" account should not have been expected by the test configuration at sub-result index " + indexStr,
                    expectedSubResult.hasOldAchAccount());
        }
    }

    private void assertPayeeACHAccountIsCorrect(String indexStr,
            String expectedMaintenanceAction, PayeeACHAccountFixture expectedAccountFixture, FinancialSystemMaintainable maintainable) throws Exception {
        PayeeACHAccount expectedAccount = expectedAccountFixture.toPayeeACHAccount();
        PayeeACHAccount actualAccount = (PayeeACHAccount) maintainable.getDataObject();
        
        assertTrue("ACH account should not be null at sub-result index " + indexStr, ObjectUtils.isNotNull(actualAccount));
        assertTrue("ACH account generated ID should not be null at sub-result index " + indexStr,
                ObjectUtils.isNotNull(actualAccount.getAchAccountGeneratedIdentifier()));
        if (StringUtils.equals(KFSConstants.MAINTENANCE_EDIT_ACTION, expectedMaintenanceAction)) {
            assertEquals("ACH account generated ID should not have changed at sub-result index " + indexStr,
                    expectedAccount.getAchAccountGeneratedIdentifier(), actualAccount.getAchAccountGeneratedIdentifier());
        }
        assertEquals("Wrong payee ID type code at sub-result index " + indexStr,
                expectedAccount.getPayeeIdentifierTypeCode(), actualAccount.getPayeeIdentifierTypeCode());
        assertEquals("Wrong payee ID number at sub-result index " + indexStr,
                expectedAccount.getPayeeIdNumber(), actualAccount.getPayeeIdNumber());
        assertEquals("Wrong transaction type at sub-result index " + indexStr,
                expectedAccount.getAchTransactionType(), actualAccount.getAchTransactionType());
        assertEquals("Wrong bank routing number at sub-result index " + indexStr,
                expectedAccount.getBankRoutingNumber(), actualAccount.getBankRoutingNumber());
        assertEquals("Wrong bank account type at sub-result index " + indexStr,
                expectedAccount.getBankAccountTypeCode(), actualAccount.getBankAccountTypeCode());
        assertEquals("Wrong bank account number at sub-result index " + indexStr,
                expectedAccount.getBankAccountNumber(), actualAccount.getBankAccountNumber());
        assertEquals("Wrong active flag status at sub-result index " + indexStr,
                expectedAccount.isActive(), actualAccount.isActive());
    }

    private void assertEqualsForDates(String message, Date expected, Date actual) throws Exception {
        String expectedDateString = ObjectUtils.isNotNull(expected) ? dateTimeService.toDateString(expected) : null;
        String actualDateString = ObjectUtils.isNotNull(actual) ? dateTimeService.toDateString(actual) : null;
        assertEqualsOrBlank(message, expectedDateString, actualDateString);
    }

    private void assertEqualsOrBlank(String message, String expected, String actual) throws Exception {
        assertEquals(message, StringUtils.defaultIfBlank(expected, StringUtils.EMPTY),
                StringUtils.defaultIfBlank(actual, StringUtils.EMPTY));
    }

    private void assertDoneFilesWereDeleted(String... inputFileNames) throws Exception {
        for (String inputFileName : inputFileNames) {
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, BatchFileSystem.DONE_FILE_EXTENSION));
            assertFalse("There should not be a .done file for " + inputFileName, doneFile.exists());
        }
    }

    private String buildInitialIndexString(Object index) {
        return buildAppendedIndexString(StringUtils.EMPTY, index);
    }

    private String buildAppendedIndexString(String currentIndexString, Object subIndex) {
        return MessageFormat.format("{0}[{1}]", currentIndexString, subIndex);
    }

    protected void copyInputFilesAndGenerateDoneFiles(String... inputFileNames) throws IOException {
        File achDirectory = new File(ACH_TESTING_DIRECTORY);
        FileUtils.forceMkdir(achDirectory);
        for (String inputFileName : inputFileNames) {
            File sourceFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_SOURCE_FILE_PATH, inputFileName, CUPdpTestConstants.CSV_FILE_EXTENSION));
            File destFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, CUPdpTestConstants.CSV_FILE_EXTENSION));
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, BatchFileSystem.DONE_FILE_EXTENSION));
            FileUtils.copyFile(sourceFile, destFile);
            doneFile.createNewFile();
        }
    }

    protected void removeTestFilesAndDirectories() throws IOException {
        File achDirectory = new File(ACH_TESTING_DIRECTORY);
        if (achDirectory.exists() && achDirectory.isDirectory()) {
            for (File achFile : achDirectory.listFiles()) {
                achFile.delete();
            }
            achDirectory.delete();
            
            // Delete parent directories.
            int slashIndex = ACH_TESTING_DIRECTORY.lastIndexOf('/', ACH_TESTING_DIRECTORY.lastIndexOf('/') - 1);
            while (slashIndex != -1) {
                File tempDirectory = new File(ACH_TESTING_DIRECTORY.substring(0, slashIndex + 1));
                tempDirectory.delete();
                slashIndex = ACH_TESTING_DIRECTORY.lastIndexOf('/', slashIndex - 1);
            }
        }
    }

    private PayeeACHAccountExtractCsvInputFileType createACHBatchInputFileType() {
        PayeeACHAccountExtractCsvInputFileType fileType = new PayeeACHAccountExtractCsvInputFileType();
        fileType.setDirectoryPath(ACH_TESTING_FILE_PATH);
        fileType.setFileExtension(
                StringUtils.substringAfter(CUPdpTestConstants.CSV_FILE_EXTENSION, CUKFSConstants.DELIMITER));
        fileType.setCsvEnumClass(PayeeACHAccountExtractCsv.class);
        return fileType;
    }

    private ParameterService createMockParameterService() throws Exception {
        ParameterService parameterService = Mockito.mock(ParameterService.class);
        Stream<Pair<String, String>> parameterMappings = Stream.of(
                Pair.of(CUPdpParameterConstants.ACH_PERSONAL_CHECKING_TRANSACTION_CODE, CUPdpTestConstants.PERSONAL_CHECKING_CODE),
                Pair.of(CUPdpParameterConstants.ACH_PERSONAL_SAVINGS_TRANSACTION_CODE, CUPdpTestConstants.PERSONAL_SAVINGS_CODE),
                Pair.of(CUPdpParameterConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE),
                Pair.of(CUPdpParameterConstants.GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT, CUPdpTestConstants.GENERATED_PAAT_NOTE_TEXT),
                Pair.of(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT, CUPdpTestConstants.NEW_ACCOUNT_EMAIL_SUBJECT),
                Pair.of(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY, CUPdpTestConstants.NEW_ACCOUNT_EMAIL_BODY),
                Pair.of(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT, CUPdpTestConstants.UPDATED_ACCOUNT_EMAIL_SUBJECT),
                Pair.of(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_BODY, CUPdpTestConstants.UPDATED_ACCOUNT_EMAIL_BODY),
                Pair.of(CUPdpParameterConstants.MAX_ACH_ACCT_EXTRACT_RETRY, CUPdpTestConstants.MAX_ACCOUNT_EXTRACT_RETRY_STRING));
        
        parameterMappings.forEach((mapping) -> {
            Mockito.when(parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, mapping.getKey()))
                    .thenReturn(mapping.getValue());
        });
        
        Mockito.when(parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARAM_NM))
                .thenReturn(CUPdpTestConstants.ACH_EMAIL_FROM_ADDRESS);
        
        return parameterService;
    }

    private DocumentService createMockDocumentService() throws Exception {
        DocumentService mockDocumentService = Mockito.mock(DocumentService.class);
        
        Mockito.when(mockDocumentService.getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE))
                .then(this::createMockPAATDocument);
        
        Mockito.when(mockDocumentService.routeDocument(ArgumentMatchers.any(Document.class), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .then(this::recordAndReturnDocumentIfValid);
        
        return mockDocumentService;
    }

    private Document recordAndReturnDocumentIfValid(InvocationOnMock invocation) throws Exception {
        Document document = invocation.getArgument(0);
        validateBankAccountMaxLength(document);
        if (GlobalVariables.getMessageMap().hasErrors()) {
            throw new ValidationException(BUSINESS_RULES_FAILURE_MESSAGE);
        }
        setBankObjectOnPayeeACHAccountIfAbsent(document);
        documentsMap.put(document.getDocumentNumber(), (MaintenanceDocument) document);
        return document;
    }

    private void validateBankAccountMaxLength(Document document) {
        MaintenanceDocument paatDocument = (MaintenanceDocument) document;
        PayeeACHAccount newAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
        if (StringUtils.length(newAccount.getBankAccountNumber()) > BANK_ACCOUNT_NUMBER_MAX_LENGTH_FOR_ROUTING_VALIDATION) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.BANK_ACCOUNT_NUMBER, KFSConstants.GLOBAL_ERRORS,
                    BANK_ACCOUNT_NUMBER_MAX_LENGTH_ERROR_MESSAGE);
        }
    }

    private void setBankObjectOnPayeeACHAccountIfAbsent(Document document) {
        MaintenanceDocument paatDocument = (MaintenanceDocument) document;
        PayeeACHAccount achAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
        if (ObjectUtils.isNull(achAccount.getBankRouting())) {
            ACHBankFixture.findBankByRoutingNumber(achAccount.getBankRoutingNumber())
                    .ifPresent((bankFixture) -> achAccount.setBankRouting(bankFixture.toACHBank()));
        }
    }

    private MaintenanceDocument createMockPAATDocument(InvocationOnMock invocation) throws Exception {
        MaintenanceDocument paatDocument = Mockito.mock(FinancialSystemMaintenanceDocument.class, Mockito.CALLS_REAL_METHODS);
        
        paatDocument.setDocumentHeader(new FinancialSystemDocumentHeader());
        paatDocument.setAdHocRoutePersons(new ArrayList<>());
        paatDocument.setAdHocRouteWorkgroups(new ArrayList<>());
        paatDocument.setNotes(new ArrayList<>());
        paatDocument.setOldMaintainableObject(createNewMaintainableForPAAT());
        paatDocument.setNewMaintainableObject(createNewMaintainableForPAAT());
        
        int nextId = documentIdCounter.incrementAndGet();
        String documentNumber = String.valueOf(nextId);
        paatDocument.setDocumentNumber(documentNumber);
        paatDocument.getDocumentHeader().setDocumentNumber(documentNumber);
        
        return paatDocument;
    }

    private FinancialSystemMaintainable createNewMaintainableForPAAT() {
        FinancialSystemMaintainable maintainable = new CuPayeeACHAccountMaintainableImpl();
        maintainable.setDataObjectClass(PayeeACHAccount.class);
        maintainable.setDataObject(new PayeeACHAccount());
        return maintainable;
    }

    private DataDictionaryService createMockDataDictionaryService() throws Exception {
        final String PAYEE_ACH_ACCOUNT_CLASSNAME = PayeeACHAccount.class.getName();
        final String DOCUMENT_HEADER_CLASSNAME = DocumentHeader.class.getName();
        /*
         * Only a few specific attribute definitions should be masked or have values finders; the rest
         * should be plain. Also, we only care about the max lengths of a few specific properties.
         */
        DataDictionaryService ddService = Mockito.mock(DataDictionaryService.class);
        AttributeDefinition maskedAttribute = createMaskedAttributeDefinition();
        AttributeDefinition unmaskedAttribute = createUnmaskedAttributeDefinition();
        AttributeDefinition payeeIdTypeAttribute = createAttributeDefinitionWithValuesFinder(
                new TestPayeeAchIdTypeValuesFinder(), false);
        AttributeDefinition bankAccountTypeAttribute = createAttributeDefinitionWithValuesFinder(
                new CuCheckingSavingsValuesFinder(), true);
        
        Mockito.when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE))
                .thenReturn(payeeIdTypeAttribute);
        Mockito.when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, PdpPropertyConstants.PAYEE_ID_NUMBER))
                .thenReturn(unmaskedAttribute);
        Mockito.when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, CUPdpPropertyConstants.BANK_ACCOUNT_TYPE_CODE))
                .thenReturn(bankAccountTypeAttribute);
        Mockito.when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, CUPdpPropertyConstants.PAYEE_ACH_BANK_NAME))
                .thenReturn(maskedAttribute);
        Mockito.when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, KFSPropertyConstants.BANK_ACCOUNT_NUMBER))
                .thenReturn(maskedAttribute);
        
        Mockito.when(ddService.getAttributeMaxLength(DOCUMENT_HEADER_CLASSNAME, KRADPropertyConstants.DOCUMENT_DESCRIPTION))
                .thenReturn(DOCUMENT_DESCRIPTION_MAX_LENGTH);
        
        return ddService;
    }

    private AttributeDefinition createMaskedAttributeDefinition() {
        // We only care about the attribute being masked, not about any other setup like property name and max length.
        AttributeDefinition maskedDefinition = new AttributeDefinition();
        AttributeSecurity maskedSecurity = new AttributeSecurity();
        MaskFormatterLiteral literalMask = new MaskFormatterLiteral();
        maskedSecurity.setMask(true);
        literalMask.setLiteral(LITERAL_MASK_VALUE);
        maskedSecurity.setMaskFormatter(literalMask);
        maskedDefinition.setAttributeSecurity(maskedSecurity);
        return maskedDefinition;
    }

    private AttributeDefinition createUnmaskedAttributeDefinition() {
        // We only care about the attribute being unmasked, not about any other setup like property name and max length.
        return new AttributeDefinition();
    }

    private AttributeDefinition createAttributeDefinitionWithValuesFinder(KeyValuesFinder valuesFinder, boolean includeKeyInLabel) {
        // We only care about the values finder and include-key-in-label flag, not about any other setup like property name and max length.
        AttributeDefinition attrDefinition = new AttributeDefinition();
        SelectControlDefinition controlDefinition = new SelectControlDefinition();
        controlDefinition.setValuesFinder(valuesFinder);
        controlDefinition.setIncludeKeyInLabel(Boolean.valueOf(includeKeyInLabel));
        attrDefinition.setControl(controlDefinition);
        return attrDefinition;
    }

    private PersonService createMockPersonService() throws Exception {
        PersonService personService = Mockito.mock(PersonService.class);
        Stream<ACHPersonPayeeFixture> fixtures = Stream.of(
                ACHPersonPayeeFixture.JOHN_DOE, ACHPersonPayeeFixture.JANE_DOE, ACHPersonPayeeFixture.ROBERT_SMITH,
                ACHPersonPayeeFixture.MARY_SMITH, ACHPersonPayeeFixture.JACK_BROWN, ACHPersonPayeeFixture.KFS_SYSTEM_USER);
        
        fixtures.forEach((fixture) -> {
            Mockito.when(personService.getPersonByPrincipalName(fixture.principalName))
                    .thenReturn(fixture.toPerson());
        });
        
        return personService;
    }

    private SequenceAccessorService createMockSequenceAccessorService() throws Exception {
        SequenceAccessorService sequenceAccessorService = Mockito.mock(SequenceAccessorService.class);
        
        Mockito.when(sequenceAccessorService.getNextAvailableSequenceNumber(PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME))
                .then((invocation) -> Long.valueOf(achAccountIdCounter.incrementAndGet()));
        
        return sequenceAccessorService;
    }

    private CuAchService createAchService(BusinessObjectService businessObjectService) throws Exception {
        CuAchServiceImpl achService = new CuAchServiceImpl();
        achService.setBusinessObjectService(businessObjectService);
        return achService;
    }

    private BusinessObjectService createMockBusinessObjectService() throws Exception {
        BusinessObjectService businessObjectService = Mockito.mock(BusinessObjectService.class);
        
        Map<String, Object> persistedExtractDetailsCriteria = Collections.singletonMap(
                CUPdpPropertyConstants.STATUS, CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN);
        
        Stream<PayeeACHAccountFixture> achFixtures = Stream.of(
                PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD);
        
        achFixtures.forEach((achFixture) -> {
            Map<String, Object> mapForMatching = createPropertiesMapForMatching(achFixture);
            Mockito.when(businessObjectService.findMatching(PayeeACHAccount.class, mapForMatching))
                    .thenReturn(Collections.singletonList(achFixture.toPayeeACHAccount()));
        });
        
        Mockito.when(businessObjectService.save(Mockito.any(PayeeACHAccountExtractDetail.class)))
                .then(invocation -> saveAchDetail(invocation.getArgument(0)));
        
        Mockito.when(businessObjectService.findMatchingOrderBy(
                PayeeACHAccountExtractDetail.class, persistedExtractDetailsCriteria, KRADPropertyConstants.ID, true))
                .then(invocation -> getSavedOpenAchDetails());
        
        return businessObjectService;
    }

    private Map<String, Object> createPropertiesMapForMatching(PayeeACHAccountFixture achFixture) {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, achFixture.payeeIdentifierTypeCode);
        propertiesMap.put(PdpPropertyConstants.ACH_TRANSACTION_TYPE, achFixture.achTransactionType);
        propertiesMap.put(PdpPropertyConstants.PAYEE_ID_NUMBER, achFixture.getPayeeIdNumber());
        return propertiesMap;
    }

    private void saveAchDetails(ACHRowFixture... fixtures) {
        Stream.of(fixtures)
                .map(ACHRowFixture::toPayeeACHAccountExtractDetailWithoutId)
                .forEach(this::saveAchDetail);
    }

    private PayeeACHAccountExtractDetail saveAchDetail(PayeeACHAccountExtractDetail achDetail) {
        KualiInteger id = achDetail.getId();
        if (ObjectUtils.isNull(id)) {
            id = new KualiInteger(achAccountIdCounter.incrementAndGet());
            achDetail.setId(id);
        }
        savedAchDetailsMap.put(id, achDetail);
        return achDetail;
    }

    private List<KualiInteger> getIdsOfInitialSavedOpenAchDetails() throws Exception {
        return getSavedOpenAchDetails().stream()
                .map(PayeeACHAccountExtractDetail::getId)
                .collect(Collectors.toList());
    }

    private List<PayeeACHAccountExtractDetail> getSavedOpenAchDetails() throws Exception {
        return savedAchDetailsMap.values().stream()
                .filter(achDetail -> StringUtils.equals(CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN, achDetail.getStatus()))
                .collect(Collectors.toList());
    }

    private Iterator<PayeeACHAccountExtractDetail> getIteratorOverSavedAchDetailsThatWereExpectedToBeProcessed(
            List<KualiInteger> initialProcessableSavedDetailIds, int oldDetailCount) throws Exception {
        Stream<PayeeACHAccountExtractDetail> reprocessedAchDetailsStream = initialProcessableSavedDetailIds.stream()
                .map(this::getNonNullSavedAchDetail);
        Stream<PayeeACHAccountExtractDetail> newPersistedAchDetailsStream = savedAchDetailsMap.values().stream()
                .skip(oldDetailCount);
        return Stream.concat(reprocessedAchDetailsStream, newPersistedAchDetailsStream)
                .iterator();
    }

    private PayeeACHAccountExtractDetail getNonNullSavedAchDetail(KualiInteger id) {
        PayeeACHAccountExtractDetail achDetail = savedAchDetailsMap.get(id);
        if (ObjectUtils.isNull(achDetail)) {
            throw new NullPointerException("ACH detail with ID \"" + id + "\" should not be null");
        }
        return achDetail;
    }

    private AchBankService createMockAchBankService() throws Exception {
        AchBankService achBankService = Mockito.mock(AchBankService.class);
        Stream<ACHBankFixture> banks = Stream.of(ACHBankFixture.FIRST_BANK, ACHBankFixture.SECOND_BANK);
        
        banks.forEach((bankFixture) -> {
            Mockito.when(achBankService.getByPrimaryId(bankFixture.bankRoutingNumber))
                    .thenReturn(bankFixture.toACHBank());
        });
        
        return achBankService;
    }

    private ConfigurationService createMockConfigurationService() throws Exception {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        
        Mockito.when(configurationService.getPropertyValueAsString(KFSConstants.GLOBAL_ERRORS))
                .thenReturn(GLOBAL_ERROR_FORMAT);
        
        return configurationService;
    }

    private PayeeACHAccountExtractReportService createMockPayeeACHAccountExtractReportServiceThatRecordsReport(
            Consumer<PayeeACHAccountExtractReport> reportSetter) throws Exception {
        PayeeACHAccountExtractReportService reportService = Mockito.mock(PayeeACHAccountExtractReportService.class);
        
        Answer<Object> reportServiceHandler = invocation -> {
            reportSetter.accept(invocation.getArgument(0));
            return null;
        };
        Mockito.doAnswer(reportServiceHandler)
                .when(reportService).writeBatchJobReports(Mockito.any());
        
        return reportService;
    }

    private void setAchReport(PayeeACHAccountExtractReport achReport) {
        this.achReport = achReport;
    }

    private ExpectedACHExtractReport expectedReport(ExpectedACHExtractGroupResult expectedReprocessedRowsResult,
            ExpectedACHExtractGroupResult... expectedFileResults) {
        return new ExpectedACHExtractReport(expectedReprocessedRowsResult, expectedFileResults);
    }

    private ExpectedACHExtractGroupResult emptyReprocessingResult() {
        return new ExpectedACHExtractGroupResult(ACHExtractGroupResultCode.SKIPPED);
    }

    private ExpectedACHExtractGroupResult reprocessingResult(ACHExtractGroupResultCode resultCode,
            ExpectedACHExtractDetailResult... expectedDetailResults) {
        return new ExpectedACHExtractGroupResult(resultCode, expectedDetailResults);
    }

    private ExpectedACHExtractGroupResult fileResult(ACHExtractGroupResultCode resultCode,
            String fileName, ExpectedACHExtractDetailResult... expectedDetailResults) {
        return new ExpectedACHExtractGroupResult(resultCode, fileName, expectedDetailResults);
    }

    private ExpectedACHExtractDetailResult detailResult(ACHExtractDetailResultCode resultCode,
            ACHRowFixture achRow, ExpectedACHExtractSubResult... expectedSubResults) {
        return new ExpectedACHExtractDetailResult(resultCode, achRow, expectedSubResults);
    }

    private ExpectedACHExtractSubResult subResult(ACHExtractResultCode resultCode, PayeeACHAccountFixture achAccount) {
        return new ExpectedACHExtractSubResult(resultCode, achAccount);
    }

    private ExpectedACHExtractSubResult subResult(ACHExtractResultCode resultCode,
            PayeeACHAccountFixture oldAchAccount, PayeeACHAccountFixture achAccount) {
        return new ExpectedACHExtractSubResult(resultCode, oldAchAccount, achAccount);
    }

    private static class TestEmailService extends EmailServiceImpl {
        @Override
        protected String getMode() {
            return MODE_LOG;
        }
    }

    private static class TestPayeeACHAccountExtractService extends PayeeACHAccountExtractServiceImpl {
        
        @Override
        protected Note createEmptyNote() {
            Note note = Mockito.mock(Note.class, Mockito.CALLS_REAL_METHODS);
            Mockito.doNothing().when(note).setNotePostedTimestampToCurrent();
            note.setNoteText(StringUtils.EMPTY);
            note.setNoteTypeCode(NoteType.DOCUMENT_HEADER.getCode());
            return note;
        }
        
        // Increase visibility to "public" for testing convenience.
        @Override
        public String getResolvedEmailBody(PayeeACHAccount achAccount, String emailBody) {
            return super.getResolvedEmailBody(achAccount, emailBody);
        }
        
    }

}
