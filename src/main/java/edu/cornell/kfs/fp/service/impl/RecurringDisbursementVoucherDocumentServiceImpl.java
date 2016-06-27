package edu.cornell.kfs.fp.service.impl;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonResidentAlienTax;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.bo.AdHocRouteRecipient;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherDetail;
import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;
import edu.cornell.kfs.gl.service.ScheduledAccountingLineService;

public class RecurringDisbursementVoucherDocumentServiceImpl implements RecurringDisbursementVoucherDocumentService, Serializable {
	
	private static final long serialVersionUID = -1775346783004136197L;
	protected static Log LOG = LogFactory.getLog(RecurringDisbursementVoucherDocumentServiceImpl.class);
	protected DocumentService documentService;
	protected ScheduledAccountingLineService scheduledAccountingLineService;
	protected BusinessObjectService businessObjectService;
	
	
	
	@Override
	public void updateRecurringDisbursementVoucherDetails(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument){
		if (recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails() == null || 
				recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails().isEmpty()){
			recurringDisbursementVoucherDocument.setRecurringDisbursementVoucherDetails(new ArrayList<RecurringDisbursementVoucherDetail>());
		} else {
			resetAmounts(recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails());
		}
		int rowId = 0;
		for (Object accountingLine : recurringDisbursementVoucherDocument.getSourceAccountingLines()) {
			ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine)accountingLine;
			TreeMap<Date, KualiDecimal> datesAndAmounts = getScheduledAccountingLineService().generateDatesAndAmounts(scheduledAccountingLine, rowId);
			
			for (Date date : datesAndAmounts.keySet()){
				KualiDecimal amount = datesAndAmounts.get(date);
				RecurringDisbursementVoucherDetail detail = getDetailtem(recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails(), date);
				detail.setDvCheckAmount(amount.add(detail.getDvCheckAmount()));
				detail.setRecurringDVDocumentNumber(recurringDisbursementVoucherDocument.getDocumentNumber());
				if (StringUtils.isEmpty(detail.getDvCheckStub())) {
					detail.setDvCheckStub(recurringDisbursementVoucherDocument.getDisbVchrCheckStubText());
				}
			}
			rowId++;
		}
		recurringDisbursementVoucherDocument.setRecurringDisbursementVoucherDetails(
				removeZeroAmounts(recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails()));
	}
	
	private void resetAmounts(List<RecurringDisbursementVoucherDetail> details) {
		for (RecurringDisbursementVoucherDetail detail : details) {
			detail.setDvCheckAmount(KualiDecimal.ZERO);
		}
	}
	
	private List<RecurringDisbursementVoucherDetail> removeZeroAmounts(List<RecurringDisbursementVoucherDetail> oldDetails) {
		List<RecurringDisbursementVoucherDetail> newDetails = new ArrayList<RecurringDisbursementVoucherDetail>();
		for (RecurringDisbursementVoucherDetail detail : oldDetails) {
			if (!detail.getDvCheckAmount().equals(KualiDecimal.ZERO)) {
				newDetails.add(detail);
			} else {
				getBusinessObjectService().delete(detail);
			}
		}
		return newDetails;
	}
	
	private RecurringDisbursementVoucherDetail getDetailtem(List<RecurringDisbursementVoucherDetail> details, Date date) {
		for (RecurringDisbursementVoucherDetail detail : details) {
			if (detail.getDvCheckDate().equals(date)) {
				return detail;
			}
		}
		RecurringDisbursementVoucherDetail detail = new RecurringDisbursementVoucherDetail(date, KualiDecimal.ZERO, "");
		details.add(detail);
		return detail;
	}

	@Override
	public List<DisbursementVoucherDocument> generateDisbursementDocumentsFromRecurringDV(RecurringDisbursementVoucherDocument recurringDV) throws WorkflowException {
		List<DisbursementVoucherDocument> generatedDVs = new ArrayList<DisbursementVoucherDocument>();
		int rowId = 0;
		for (Object accountingLine : recurringDV.getSourceAccountingLines()) {
			ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine)accountingLine;
			TreeMap<Date, KualiDecimal> datesAndAmounts = getScheduledAccountingLineService().generateDatesAndAmounts(scheduledAccountingLine, rowId);
			
			for (Date date : datesAndAmounts.keySet()){
				KualiDecimal amount = datesAndAmounts.get(date);
				
				CuDisbursementVoucherDocument disbursementVoucherDocument = getCuDisbursementVoucherDocument(generatedDVs, recurringDV, date);
				disbursementVoucherDocument.setDisbVchrCheckTotalAmount(calculateDVCheckAmount(amount, disbursementVoucherDocument));
				
				RecurringDisbursementVoucherDetail dvDetail = getDetailtem(recurringDV.getRecurringDisbursementVoucherDetails(), date);
				dvDetail.setDvDocumentNumber(disbursementVoucherDocument.getDocumentNumber());
				disbursementVoucherDocument.setDisbVchrCheckStubText(dvDetail.getDvCheckStub());
				
				SourceAccountingLine line = buildSourceAccountingLine(scheduledAccountingLine);
				line.setAmount(amount);
				disbursementVoucherDocument.addSourceAccountingLine(line);
			
			}
			rowId++;
		}
		saveRouteAndApproveDisbursementVouchers(generatedDVs, recurringDV);
		return generatedDVs;
	}

	private KualiDecimal calculateDVCheckAmount(KualiDecimal amount, CuDisbursementVoucherDocument disbursementVoucherDocument) {
		KualiDecimal calculatedAmount;
		if (disbursementVoucherDocument.getDisbVchrCheckTotalAmount() == null || disbursementVoucherDocument.getDisbVchrCheckTotalAmount().isZero()) {
			calculatedAmount = amount;
		} else {
			calculatedAmount = amount.add(disbursementVoucherDocument.getDisbVchrCheckTotalAmount());
		}
		return calculatedAmount;
	}
	
	private CuDisbursementVoucherDocument getCuDisbursementVoucherDocument(List<DisbursementVoucherDocument> generatedDVs, 
			RecurringDisbursementVoucherDocument recurringDV, Date dvCheckDate) throws WorkflowException {
		for (DisbursementVoucherDocument dv : generatedDVs) {
			if (dv.getDisbursementVoucherDueDate().equals(dvCheckDate)) {
				return (CuDisbursementVoucherDocument)dv;
			}
		}
		CuDisbursementVoucherDocument dv =  buildCuDisbursementVoucherDocument(recurringDV);
		dv.setDisbursementVoucherDueDate(dvCheckDate);
		dv.setDisbursementVoucherCheckDate(new Timestamp(dvCheckDate.getTime()));
		generatedDVs.add(dv);
		return dv;
	}
	
	private CuDisbursementVoucherDocument buildCuDisbursementVoucherDocument(RecurringDisbursementVoucherDocument recurringDV) throws WorkflowException {
		CuDisbursementVoucherDocument dv = (CuDisbursementVoucherDocument) getDocumentService().getNewDocument(CuDisbursementVoucherDocument.class);		
		
		dv.setDisbVchrContactPersonName(recurringDV.getDisbVchrContactPersonName());
		dv.setDisbVchrContactPhoneNumber(recurringDV.getDisbVchrContactPhoneNumber());
		dv.setDisbVchrContactEmailId(recurringDV.getDisbVchrContactEmailId());
		dv.setDisbVchrAttachmentCode(recurringDV.isDisbVchrAttachmentCode());
		dv.setDisbVchrSpecialHandlingCode(recurringDV.isDisbVchrSpecialHandlingCode());
		dv.setDisbVchrForeignCurrencyInd(recurringDV.isDisbVchrForeignCurrencyInd());
		dv.setDisbursementVoucherDocumentationLocationCode(recurringDV.getDisbursementVoucherDocumentationLocationCode());
		dv.setDisbVchrCheckStubText(recurringDV.getDisbVchrCheckStubText());
		dv.setDvCheckStubOverflowCode(recurringDV.getDvCheckStubOverflowCode());
		dv.setCampusCode(recurringDV.getCampusCode());
		dv.setDisbVchrPayeeTaxControlCode(recurringDV.getDisbVchrPayeeTaxControlCode());
		dv.setDisbVchrPayeeChangedInd(recurringDV.isDisbVchrPayeeChangedInd());
		dv.setExceptionIndicator(recurringDV.isExceptionIndicator());
		dv.setDisbursementVoucherPdpStatus(recurringDV.getDisbursementVoucherPdpStatus());
		dv.setDisbVchrBankCode(recurringDV.getDisbVchrBankCode());
		dv.setPayeeAssigned(recurringDV.isPayeeAssigned());
		dv.setEditW9W8BENbox(recurringDV.isEditW9W8BENbox());
		dv.setDisbVchrPdpBankCode(recurringDV.getDisbVchrPdpBankCode());
		dv.setImmediatePaymentIndicator(recurringDV.isImmediatePaymentIndicator());
		dv.setDisbExcptAttachedIndicator(recurringDV.isDisbExcptAttachedIndicator());
		dv.setDisbVchrPayeeW9CompleteCode(recurringDV.getDisbVchrPayeeW9CompleteCode());
		dv.setDisbVchrPaymentMethodCode(recurringDV.getDisbVchrPaymentMethodCode());
		
		CuDisbursementVoucherPayeeDetail payeeDetail = (CuDisbursementVoucherPayeeDetail) ObjectUtils.deepCopy(recurringDV.getDvPayeeDetail());
		payeeDetail.setDocumentNumber(dv.getDocumentNumber());
		dv.setDvPayeeDetail(payeeDetail);
		
		PaymentSourceWireTransfer wireTransfer = (PaymentSourceWireTransfer) ObjectUtils.deepCopy(recurringDV.getWireTransfer());
		wireTransfer.setDocumentNumber(dv.getDocumentNumber());
		dv.setWireTransfer(wireTransfer);
		
		DisbursementVoucherNonEmployeeTravel nonEmployeeTravel = (DisbursementVoucherNonEmployeeTravel) ObjectUtils.deepCopy(recurringDV.getDvNonEmployeeTravel());
		nonEmployeeTravel.setDocumentNumber(dv.getDocumentNumber());
		dv.setDvNonEmployeeTravel(nonEmployeeTravel);
		
		DisbursementVoucherNonResidentAlienTax nonAlienTax = (DisbursementVoucherNonResidentAlienTax) ObjectUtils.deepCopy(recurringDV.getDvNonResidentAlienTax());
		nonAlienTax.setDocumentNumber(dv.getDocumentNumber());
		dv.setDvNonResidentAlienTax(nonAlienTax);
		
 	 	DisbursementVoucherPreConferenceDetail conferenceDetail = (DisbursementVoucherPreConferenceDetail) ObjectUtils.deepCopy(recurringDV.getDvPreConferenceDetail());
 	 	conferenceDetail.setDocumentNumber(dv.getDocumentNumber());
		dv.setDvPreConferenceDetail(conferenceDetail);
		
		return dv;
	}
	
	private SourceAccountingLine buildSourceAccountingLine(ScheduledSourceAccountingLine scheduledAccountingLine) {
		SourceAccountingLine line = new SourceAccountingLine();
		line.setAccountExpiredOverride(scheduledAccountingLine.getAccountExpiredOverride());
		line.setAccountExpiredOverrideNeeded(scheduledAccountingLine.getAccountExpiredOverrideNeeded());
		line.setAccountNumber(scheduledAccountingLine.getAccountNumber());
		line.setChartOfAccountsCode(scheduledAccountingLine.getChartOfAccountsCode());
		line.setDebitCreditCode(scheduledAccountingLine.getDebitCreditCode());
		line.setEncumbranceUpdateCode(scheduledAccountingLine.getEncumbranceUpdateCode());
		line.setFinancialDocumentLineDescription(scheduledAccountingLine.getFinancialDocumentLineDescription());
		line.setFinancialDocumentLineTypeCode(scheduledAccountingLine.getFinancialDocumentLineTypeCode());
		line.setFinancialObjectCode(scheduledAccountingLine.getFinancialObjectCode());
		line.setFinancialSubObjectCode(scheduledAccountingLine.getFinancialSubObjectCode());
		line.setNonFringeAccountOverride(scheduledAccountingLine.getNonFringeAccountOverride());
		line.setNonFringeAccountOverrideNeeded(scheduledAccountingLine.getNonFringeAccountOverrideNeeded());
		line.setOrganizationReferenceId(scheduledAccountingLine.getOrganizationReferenceId());
		line.setPostingYear(scheduledAccountingLine.getPostingYear());
		line.setProjectCode(scheduledAccountingLine.getProjectCode());
		line.setReferenceNumber(scheduledAccountingLine.getReferenceNumber());
		line.setReferenceTypeCode(scheduledAccountingLine.getReferenceTypeCode());
		line.setSalesTaxRequired(scheduledAccountingLine.isSalesTaxRequired());
		line.setSubAccountNumber(scheduledAccountingLine.getSubAccountNumber());
		return line;
	}
	
	private void saveRouteAndApproveDisbursementVouchers(List<DisbursementVoucherDocument> dvs, RecurringDisbursementVoucherDocument recurringDV){
		for (DisbursementVoucherDocument dv : dvs) {
			try {
				dv.getDocumentHeader().setDocumentDescription(recurringDV.getDocumentHeader().getDocumentDescription());
				getDocumentService().saveDocument(dv);
				List<AdHocRouteRecipient> adHocRoutingRecipients = new ArrayList();
				getDocumentService().routeDocument(dv, "Disbursement Document created from recurring DV: " + recurringDV.getDocumentHeader().getDocumentNumber(), adHocRoutingRecipients);
				getDocumentService().blanketApproveDocument(dv, "Disbursement Document automatically approved by recurring DV", adHocRoutingRecipients);
				getBusinessObjectService().save(recurringDV.getRecurringDisbursementVoucherDetails());
			} catch (WorkflowException e) {
				LOG.error("There was an errorr trying to save our route the created Disbursement Voucher documents: ", e);
				throw new RuntimeException(e);
			}
		}
	}
	

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	public ScheduledAccountingLineService getScheduledAccountingLineService() {
		return scheduledAccountingLineService;
	}

	public void setScheduledAccountingLineService(ScheduledAccountingLineService scheduledAccountingLineService) {
		this.scheduledAccountingLineService = scheduledAccountingLineService;
	}

	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}
	
}
