package edu.cornell.kfs.pdp;

import org.kuali.kfs.pdp.PdpConstants;

public class CUPdpConstants extends PdpConstants {
	
    public static class PdpDocumentTypes {
    	public static String DISBURSEMENT_VOUCHER = "DVCA";
    	public static String PAYMENT_REQUEST = "PREQ";
    	public static String CAMPUS_LIFE = "APCL";
    	public static String CORNELL_STORE = "APCS";
    	public static String LIBRARY = "APLB";
    	public static String HOTEL = "APHT";
    	public static String CREDIT_MEMO = "CM";
    }
    
    public static class DivisionCodes {
        public static final int US_MAIL = 1;
        public static final int CU_MAIL_SERVICES = 16;
    }

    public static class PaymentDistributions {
        public static final String PROCESS_ACH_ONLY = "achOnly";
        public static final String PROCESS_CHECK_ONLY = "checkOnly";
        public static final String PROCESS_ALL = "all";
    }
    
    public static class CustomerProfilePrimaryKeyTags {
        public static final String CHART_OPEN = "<chart>";
        public static final String CHART_CLOSE = "</chart>";
        public static final String UNIT_OPEN = "<unit>";
        public static final String UNIT_CLOSE = "</unit>";
        public static final String SUBUNIT_OPEN = "<sub_unit>";
        public static final String SUBUNIT_CLOSE = "</sub_unit>";
    }
    
    public static final String PAYEE_ACH_ACCOUNT_EXTRACT_FILE_TYPE_ID = "payeeACHAccountExtract_kfs";
    public static final String PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE = "PAAT";
    public static final String PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE = "Direct Deposit";
    public static final String PAYEE_ACH_ACCOUNT_EXTRACT_BALANCE_ACCOUNT_YES_INDICATOR = "1";
    public static final String PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE = "Checking";
    public static final String PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE = "Savings";
    public static final String PAYEE_ACH_ACCOUNT_EXTRACT_BANK_NAME_PROPERTY = "bankRouting.bankName";
    
    public static final String PAYEE_TYPE_CODE_VENDOR = "V";
    public static final String PAYEE_TYPE_CODE_EMPLOYEE = "E";
    public static final String PAYEE_TYPE_CODE_ALUMNI = "A";
    public static final String PAYEE_TYPE_CODE_STUDENT = "S";
}
