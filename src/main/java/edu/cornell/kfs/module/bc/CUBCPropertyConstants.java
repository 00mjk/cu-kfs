package edu.cornell.kfs.module.bc;

import org.kuali.kfs.module.bc.BCPropertyConstants;

public class CUBCPropertyConstants extends BCPropertyConstants {

    public static final class PendingBudgetConstructionGeneralLedgerProperties {

        public static final String DOC_NBR = "documentNumber";
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_CD = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FINANCIAL_OBJECT_CODE = "financialObjectCode";
        public static final String FINANCIAL_SUB_OBJECT_CODE = "financialSubObjectCode";
        public static final String FINANCIAL_BALANCE_TYP_CD = "financialBalanceTypeCode";
        public static final String FINANCIAL_OBJ_TYP_CD = "financialObjectTypeCode";
        public static final String ACC_LINE_ANN_BAL_AMT = "accountLineAnnualBalanceAmount";
        public static final String FIN_BEG_BAL_LINE_AMT = "financialBeginningBalanceLineAmount";
    }

    public static final class PendingBudgetConstructionAppointmentFundingProperties {
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_CD = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FINANCIAL_OBJECT_CODE = "financialObjectCode";
        public static final String FINANCIAL_SUB_OBJECT_CODE = "financialSubObjectCode";
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String APPT_FUND_DURATION_CD = "appointmentFundingDurationCode";
        public static final String APPT_REQ_CSF_AMT = "appointmentRequestedCsfAmount";
        public static final String APPT_REQ_CSF_FTE_QUANTITY = "appointmentRequestedCsfFteQuantity";
        public static final String APPT_TOTAL_INTENDED_AMT = "appointmentTotalIntendedAmount";
        public static final String APPT_TOTAL_INTENDED_FTE_QUANTITY = "appointmentTotalIntendedFteQuantity";
        public static final String APPT_REQ_AMT = "appointmentRequestedAmount";
        public static final String APPT_REQ_TIME_PERCENT = "appointmentRequestedTimePercent";
        public static final String APPT_REQ_FTE_QUANTITY = "appointmentRequestedFteQuantity";
        public static final String APPT_REQ_PAY_RATE = "appointmentRequestedPayRate";
        public static final String APPT_FUNDING_DELETE_IND = "appointmentFundingDeleteIndicator";
        public static final String APPT_FUNDING_MONTH = "appointmentFundingMonth";
    }

    public static final class BudgetConstructionPositionProperties {
        public static final String POSITION_DESC = "positionDescription";
        public static final String SETID_SALARY = "setidSalary";
        public static final String POSITION_SALARY_PLAN_DEFAULT = "positionSalaryPlanDefault";
        public static final String POSITION_GRADE_DEFAULT = "positionGradeDefault";
        public static final String NORMAL_WORK_MONTHS = "iuNormalWorkMonths";
        public static final String PAY_MONTHS = "iuPayMonths";
    }

    public static final class BudgetConstructionAccountReportsProperties {
        public static final String REPORTS_TO_ORG_CD = "reportsToOrganizationCode";
    }

    public static class BudgetConstructionIntendedIncumbentProperties {
        public static final String NAME = "name";
        public static final String CLASSIFICATION_LEVEL = "iuClassificationLevel";
    }

    public static final class BudgetConstructionAdministrativePostProperties {
        public static final String ADMINISTRATIVE_POST = "administrativePost";
    }

    public static final class BudgetConstructionCalculatedSalaryFoundationTrackerProperties {
        public static final String CSF_AMT = "csfAmount";
        public static final String CSF_FULL_TIME_EMPL_QUANTITY = "csfFullTimeEmploymentQuantity";
        public static final String CSF_TIME_PERCENT = "csfTimePercent";
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_OF_ACCOUNTS = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FIN_OBJ_CD = "financialObjectCode";
        public static final String FIN_SUB_OBJ_CD = "financialSubObjectCode";
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String CSF_FUNDING_STATUS_CD = "csfFundingStatusCode";
    }

    public static final class BudgetConstructionAppointmentFundingReasonProperties {
        public static final String APPT_FUNDING_REASON_CD = "appointmentFundingReasonCode";
    }

    public static final class BudgetConstructionOrganizationReportsProperties {
        public static final String RESP_CENTER_CD = "responsibilityCenterCode";
    }

    public static final class BudgetConstructionMonthlyProperties {
        public static final String DOC_NBR = "documentNumber";
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_CD = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FINANCIAL_OBJ_CD = "financialObjectCode";
        public static final String FINANCIAL_SUB_OBJ_CD = "financialSubObjectCode";
        public static final String FINANCIAL_BAL_TYP_CD = "financialBalanceTypeCode";
        public static final String FINANCIAL_OBJ_TYP_CD = "financialBalanceTypeCode";

        public static final String FINANCIAL_DOC_MONTH_1_LINE_AMT = "financialDocumentMonth1LineAmount";
        public static final String FINANCIAL_DOC_MONTH_2_LINE_AMT = "financialDocumentMonth2LineAmount";
        public static final String FINANCIAL_DOC_MONTH_3_LINE_AMT = "financialDocumentMonth3LineAmount";
        public static final String FINANCIAL_DOC_MONTH_4_LINE_AMT = "financialDocumentMonth4LineAmount";
        public static final String FINANCIAL_DOC_MONTH_5_LINE_AMT = "financialDocumentMonth5LineAmount";
        public static final String FINANCIAL_DOC_MONTH_6_LINE_AMT = "financialDocumentMonth6LineAmount";
        public static final String FINANCIAL_DOC_MONTH_7_LINE_AMT = "financialDocumentMonth7LineAmount";
        public static final String FINANCIAL_DOC_MONTH_8_LINE_AMT = "financialDocumentMonth8LineAmount";
        public static final String FINANCIAL_DOC_MONTH_9_LINE_AMT = "financialDocumentMonth9LineAmount";
        public static final String FINANCIAL_DOC_MONTH_10_LINE_AMT = "financialDocumentMonth10LineAmount";
        public static final String FINANCIAL_DOC_MONTH_11_LINE_AMT = "financialDocumentMonth11LineAmount";
        public static final String FINANCIAL_DOC_MONTH_12_LINE_AMT = "financialDocumentMonth12LineAmount";
    }

    public static final class CalculateSalaryFoundationTrackerProperties {
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_OF_ACCOUNTS = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FIN_OBJ_CD = "financialObjectCode";
        public static final String FIN_SUB_OBJ_CD = "financialSubObjectCode";
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String CREATE_TIMESTAMP = "csfCreateTimestamp";
    }

    public static final class PSPositionInfoProperties {
        public static final String POSITION_NBR = "positionNumber";
    }

    public static final class PSJobDataProperties {
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String EMPL_TYP = "employeeType";
    }

    public static final class PSJobCodeProperties {
        public static final String JOB_CD = "jobCode";
    }
    
    public static final class BudgetConstructionSIPExportProperties {
        public static final String UNITID = "UnitID";
        public static final String HRDEPT = "HR Dept Id";
        public static final String DEPTID = "DeptID";
        public static final String DEPT_NAME = "Department Name";
        public static final String POSITION_NBR = "Position Nbr";
        public static final String POSITION_DESC = "Position Description";
        public static final String EMPLID = "EmplID";
        public static final String PERSON_NAME = "Person Name";
        public static final String SIP_ELIGIBILITY = "SIP Eligible?";
        public static final String EMPLOYEE_TYPE = "SIP Employee Type";
        public static final String EMPL_RCD = "Employee RCD";
        public static final String JOBCODE = "Job Code";
        public static final String JOBCODE_SHORT_DESC = "Job Code Short Desc";
        public static final String JOB_FAMILY = "Job Family";
        public static final String CU_PLANNED_FTE = "CU Planned FTE";
        public static final String POS_GRADE_DFLT = "Grade";
        public static final String CU_STATE_CERT = "CU State Cert?";
        public static final String ANNL_RT = "Annual Rate";
        public static final String COMP_RT = "Comp Rate";
        public static final String COMP_FREQ = "Comp Frequency";
        public static final String JOB_STD_HRS = "Job Standard Hours";
        public static final String WORK_MONTHS = "Work Months";
        public static final String JOB_FUNC = "Job Function";
        public static final String JOB_FUNC_DESC = "Job Function Description";
        public static final String INCREASE_TO_MINIMUM = "Increase to Minimum";
        public static final String EQUITY = "Equity";
        public static final String MERIT = "Merit";
        public static final String NOTE = "Note";
        public static final String DEFERRED = "Deferred";
        public static final String CU_ABBR_FLAG = "CU ABBR Flag";
        public static final String APPT_TOT_INTND_AMT = "Job Planned Commit";
        public static final String APPT_RQST_FTE_QTY = "Job Planned FTE";
        public static final String LEAVE_CODE = "Leave Code";
        public static final String LEAVE_DESC = "Leave Description";
        public static final String LEAVE_AMT = "Leave Amount";
        public static final String BGP_FLSA = "BGP FLSA";
    }

}
