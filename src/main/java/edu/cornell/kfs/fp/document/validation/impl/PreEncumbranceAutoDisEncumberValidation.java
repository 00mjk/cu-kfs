package edu.cornell.kfs.fp.document.validation.impl;

import static org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TreeMap;

import org.kuali.kfs.fp.document.PreEncumbranceDocument;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.AccountingDocumentRuleHelperService;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.PreEncumbranceAccountingLineUtil;
import edu.cornell.kfs.fp.businessobject.PreEncumbranceSourceAccountingLine;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class PreEncumbranceAutoDisEncumberValidation extends GenericValidation {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PreEncumbranceAutoDisEncumberValidation.class);

    private AccountingDocumentRuleHelperService accountingDocumentRuleHelperService;
    private PreEncumbranceDocument accountingDocumentForValidation;
    public static final String TRANSACTION_DATE_FORMAT_STRING = "yyyy-MM-dd";
    private Date annualClosingDate;
	
	public boolean validate(AttributedDocumentEvent event) {
		boolean success = true;
		ParameterService ps = SpringContext.getBean(ParameterService.class);
        try {
            DateFormat transactionDateFormat = new SimpleDateFormat(TRANSACTION_DATE_FORMAT_STRING);
            annualClosingDate = new Date(transactionDateFormat.parse(ps.getParameterValue(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_TRANSACTION_DATE_PARM)).getTime());
            // this needs to be changed
            annualClosingDate.setYear(annualClosingDate.getYear()+1);
            LOG.info("Annual closing Date of: " + annualClosingDate);
        }
        catch (ParseException e) {
            LOG.error("PreEncumbrance validation nnable to parse transaction date", e);
            throw new IllegalArgumentException("Unable to parse transaction date");
        }

		PreEncumbranceDocument ped = (PreEncumbranceDocument) getAccountingDocumentForValidation();
		Iterator<PreEncumbranceSourceAccountingLine> it = ped.getSourceAccountingLines().iterator();
		while (it.hasNext()) {
			PreEncumbranceSourceAccountingLine pesal = it.next();
			success &=checkMinimumRequirements(pesal);
			success &=checkDates(pesal);
			success &=checkGenerationValidity(pesal);
		}
		return success;
	}

	private boolean checkMinimumRequirements(PreEncumbranceSourceAccountingLine sourceAccountingLine) {
		boolean success = true;
		if (ObjectUtils.isNotNull(sourceAccountingLine.getAutoDisEncumberType())) {
			if (ObjectUtils.isNull(sourceAccountingLine.getStartDate())) {
				GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceAccountingLine.getSequenceNumber() + "].startDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_NEEDS_START_DATE);
				success = false;
			}
			if (sourceAccountingLine.getAutoDisEncumberType().equals("semiMonthly") && (ObjectUtils.isNull(sourceAccountingLine.getEndDate()) && ObjectUtils.isNull(sourceAccountingLine.getPartialTransactionCount()) )) {
				GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceAccountingLine.getSequenceNumber() + "].startDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_NEEDS_END_OR_COUNT);
				success = false;
			}
			if (ObjectUtils.isNull(sourceAccountingLine.getPartialAmount())) {
				GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceAccountingLine.getSequenceNumber() + "].startDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_NEEDS_AMOUNT_SPECIFIED);
				success = false;
			}
		}
		return success;
	}
	
	private boolean checkDates(PreEncumbranceSourceAccountingLine sourceLine) {
		boolean success = true;
		boolean isCGAccount = sourceLine.getAccount().getSubFundGroup().getFundGroupCode().equals("CG");
		if (ObjectUtils.isNotNull(sourceLine.getStartDate()) && ObjectUtils.isNotNull(sourceLine.getEndDate())) {
			if (sourceLine.getStartDate().after(sourceLine.getEndDate())) {
				GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceLine.getSequenceNumber() + "].startDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_CONFLICTING_START_END);
				success = false;
			}
		}
        java.sql.Date today = SpringContext.getBean(DateTimeService.class).getCurrentSqlDateMidnight();
        if (ObjectUtils.isNotNull(sourceLine.getStartDate())) {
        	if (sourceLine.getStartDate().before(today)) {
        		GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceLine.getSequenceNumber() + "].startDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_INVALID_START);
				success = false;        		
        	}
        	if (sourceLine.getStartDate().after(annualClosingDate) && !isCGAccount) {
        		GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceLine.getSequenceNumber() + "].startDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_DATE_PAST_YEAR_END);
        		success = false;
        	}
        	if (ObjectUtils.isNotNull(accountingDocumentForValidation.getReversalDate())) {
        		GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceLine.getSequenceNumber() + "].startDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_BOTH_REV_DATES_USED);
        		success = false;
        	}
        }
        if (ObjectUtils.isNotNull(sourceLine.getEndDate())) {
        	if (sourceLine.getEndDate().before(today)) {
        		GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceLine.getSequenceNumber() + "].endDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_INVALID_END);
				success = false;
        	}
        	if (sourceLine.getEndDate().after(annualClosingDate) && !isCGAccount) {
        		GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceLine.getSequenceNumber() + "].endDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_DATE_PAST_YEAR_END);
        		success = false;
        	}
       	}        
		return success;
	}
	
	private boolean checkGenerationValidity(PreEncumbranceSourceAccountingLine sourceLine) {
		boolean success = true;
		boolean isCGAccount = sourceLine.getAccount().getSubFundGroup().getFundGroupCode().equals("CG");

		// this will take the various information and ensure that the GL pending entries
		// that have yet to be generated will be within a valid time frame (not span into next FY)

		TreeMap<Date,KualiDecimal> datesAndValues = PreEncumbranceAccountingLineUtil.generateDatesAndAmounts(sourceLine.getAutoDisEncumberType(), 
				sourceLine.getStartDate(), sourceLine.getEndDate(), Integer.parseInt(sourceLine.getPartialTransactionCount()), 
				sourceLine.getAmount(), sourceLine.getPartialAmount());
		
		Date finalReversalDate = datesAndValues.pollLastEntry().getKey();
		if (finalReversalDate.after(annualClosingDate) && !isCGAccount) {
    		GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + sourceLine.getSequenceNumber() + "].endDate", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_GENERATED_ENTRIES_SPAN_FY);
    		success = false;
		}
		
		return success;
	}
	
	
    /**
     * Gets the accountingDocumentRuleHelperService attribute. 
     * @return Returns the accountingDocumentRuleHelperService.
     */
    public AccountingDocumentRuleHelperService getAccountingDocumentRuleHelperService() {
        return accountingDocumentRuleHelperService;
    }

    /**
     * Sets the accountingDocumentRuleHelperService attribute value.
     * @param accountingDocumentRuleHelperService The accountingDocumentRuleHelperService to set.
     */
    public void setAccountingDocumentRuleHelperService(AccountingDocumentRuleHelperService accountingDocumentRuleHelperService) {
        this.accountingDocumentRuleHelperService = accountingDocumentRuleHelperService;
    }

    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public PreEncumbranceDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(PreEncumbranceDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

}
