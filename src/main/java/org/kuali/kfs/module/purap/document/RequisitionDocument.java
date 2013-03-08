/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.kfs.module.purap.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.purap.CUPurapParameterConstants;
import org.kuali.kfs.module.purap.CUPurapWorkflowConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants.NodeDetails;
import org.kuali.kfs.module.purap.PurapWorkflowConstants.RequisitionDocument.NodeDetailEnum;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.businessobject.DefaultPrincipalAddress;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseRequisitionItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionCapitalAssetItem;
import org.kuali.kfs.module.purap.businessobject.RequisitionCapitalAssetSystem;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.PurchasingDocumentSpecificService;
import org.kuali.kfs.module.purap.document.service.PurchasingService;
import org.kuali.kfs.module.purap.document.service.RequisitionService;
import org.kuali.kfs.module.purap.util.PurapAccountingLineComparator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AddAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.DeleteAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.ReviewAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.UpdateAccountingLineEvent;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.rice.kew.dto.DocumentRouteLevelChangeDTO;
import org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO;
import org.kuali.rice.kew.dto.ReportCriteriaDTO;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.Copyable;
import org.kuali.rice.kns.document.TransactionalDocument;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.service.PersistenceService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowInfo;
import org.kuali.rice.kns.workflow.service.WorkflowDocumentService;

/**
 * Document class for the Requisition.
 */
public class RequisitionDocument extends PurchasingDocumentBase implements Copyable {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RequisitionDocument.class);

    protected String requisitionOrganizationReference1Text;
    protected String requisitionOrganizationReference2Text;
    protected String requisitionOrganizationReference3Text;
    protected String alternate1VendorName;
    protected String alternate2VendorName;
    protected String alternate3VendorName;
    protected String alternate4VendorName;
    protected String alternate5VendorName;
    protected KualiDecimal organizationAutomaticPurchaseOrderLimit;
    
    // non-persistent property used for controlling validation for accounting lines when doc is request for blanket approve.
    protected boolean isBlanketApproveRequest = false;
    
    /**
     * Default constructor.
     */
    public RequisitionDocument() {
        super();
    }

    @Override
    public PurchasingDocumentSpecificService getDocumentSpecificService() {
        return SpringContext.getBean(RequisitionService.class);
    }
    /**
     * @see org.kuali.rice.kns.bo.PersistableBusinessObjectBase#isBoNotesSupport()
     */
    @Override
    public boolean isBoNotesSupport() {
        return true;
    }

    /**
     * Provides answers to the following splits:
     * AmountRequiresSeparationOfDutiesReview
     * 
     * @see org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.HAS_ACCOUNTING_LINES)) return !isMissingAccountingLines();
        if (nodeName.equals(PurapWorkflowConstants.AMOUNT_REQUIRES_SEPARATION_OF_DUTIES_REVIEW_SPLIT)) return isSeparationOfDutiesReviewRequired();
        if (nodeName.equals(PurapWorkflowConstants.AWARD_REVIEW_REQUIRED)) return isAwardReviewRequired();
        if (nodeName.equals(CUPurapWorkflowConstants.B2B_AUTO_PURCHASE_ORDER)){ 
        	boolean isB2BAutoPurchaseOrder =  isB2BAutoPurchaseOrder();
        	if(isB2BAutoPurchaseOrder) this.paymentRequestPositiveApprovalIndicator=true;
        	return isB2BAutoPurchaseOrder;
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");
    }

    protected boolean isB2BAutoPurchaseOrder(){
    	boolean returnValue = false;
 
    	VendorDetail vendorDetail = SpringContext.getBean(VendorService.class).getVendorDetail(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier());
    	if (vendorDetail != null) {
	        if (vendorDetail.isB2BVendor())
	        	returnValue = isB2BTotalAmountForAutoPO();
	        else
	        	returnValue =  false;
	        
	        return returnValue;
    	}
    	return false;
    }
    
    protected boolean isB2BTotalAmountForAutoPO(){
    	boolean returnValue = false;
    	
    	ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        KualiDecimal autoPOAmount = new KualiDecimal(parameterService.getParameterValue(RequisitionDocument.class, CUPurapParameterConstants.B2B_TOTAL_AMOUNT_FOR_AUTO_PO));
        KualiDecimal totalAmount = documentHeader.getFinancialDocumentTotalAmount();
        if (ObjectUtils.isNotNull(autoPOAmount) && ObjectUtils.isNotNull(totalAmount) && (autoPOAmount.compareTo(totalAmount) >= 0)) 
        	returnValue = true;
        else 
        	returnValue =  false;
       
        return returnValue;
    }
    
    
    protected boolean isMissingAccountingLines() {
        for (Iterator iterator = getItems().iterator(); iterator.hasNext();) {
            RequisitionItem item = (RequisitionItem) iterator.next();
            if (item.isConsideredEntered() && item.isAccountListEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    protected boolean isSeparationOfDutiesReviewRequired() {
        try {
            Set<Person> priorApprovers = getDocumentHeader().getWorkflowDocument().getAllPriorApprovers();
            // if there are more than 0 prior approvers which means there had been at least another approver than the current approver
            // then no need for separation of duties
            if (priorApprovers.size() > 0) {
                return false;
            }
        }catch (WorkflowException we) {
            LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
        }
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        KualiDecimal maxAllowedAmount = new KualiDecimal(parameterService.getParameterValue(RequisitionDocument.class, PurapParameterConstants.SEPARATION_OF_DUTIES_DOLLAR_AMOUNT));
        // if app param amount is greater than or equal to documentTotalAmount... no need for separation of duties
        KualiDecimal totalAmount = documentHeader.getFinancialDocumentTotalAmount();
        if (ObjectUtils.isNotNull(maxAllowedAmount) && ObjectUtils.isNotNull(totalAmount) && (maxAllowedAmount.compareTo(totalAmount) >= 0)) {
            return false;
        }
        else {
            return true;
        }
    }

    
    /**
     * Overrides the method in PurchasingAccountsPayableDocumentBase to add the criteria
     * specific to Requisition Document.
     * 
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#isInquiryRendered()
     */
    @Override
    public boolean isInquiryRendered() {
        if ( isPostingYearPrior() && 
             ( getStatusCode().equals(PurapConstants.RequisitionStatuses.CLOSED) || 
               getStatusCode().equals(PurapConstants.RequisitionStatuses.CANCELLED) ) )  {
               return false;            
        }
        else {
            return true;
        }
    }
    
    /**
     * Performs logic needed to initiate Requisition Document.
     */
    public void initiateDocument() {
        this.setRequisitionSourceCode(PurapConstants.RequisitionSources.STANDARD_ORDER);
        this.setStatusCode(PurapConstants.RequisitionStatuses.IN_PROCESS);
        this.setPurchaseOrderCostSourceCode(PurapConstants.POCostSources.ESTIMATE);
        this.setPurchaseOrderTransmissionMethodCode(determinePurchaseOrderTransmissionMethod());
        this.setDocumentFundingSourceCode(SpringContext.getBean(ParameterService.class).getParameterValue(RequisitionDocument.class, PurapParameterConstants.DEFAULT_FUNDING_SOURCE));
        this.setUseTaxIndicator(SpringContext.getBean(PurchasingService.class).getDefaultUseTaxIndicatorValue(this));
            
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        ChartOrgHolder purapChartOrg = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, PurapConstants.PURAP_NAMESPACE);
        if (ObjectUtils.isNotNull(purapChartOrg)) {
        this.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
        this.setOrganizationCode(purapChartOrg.getOrganizationCode());
        }
        this.setDeliveryCampusCode(currentUser.getCampusCode());
        this.setDeliveryToName(currentUser.getName());
        this.setDeliveryToEmailAddress(currentUser.getEmailAddressUnmasked());
        this.setDeliveryToPhoneNumber(SpringContext.getBean(PhoneNumberService.class).formatNumberIfPossible(currentUser.getPhoneNumber()));
        this.setRequestorPersonName(currentUser.getName());
        this.setRequestorPersonEmailAddress(currentUser.getEmailAddressUnmasked());
        this.setRequestorPersonPhoneNumber(SpringContext.getBean(PhoneNumberService.class).formatNumberIfPossible(currentUser.getPhoneNumber()));

        DefaultPrincipalAddress defaultPrincipalAddress = new DefaultPrincipalAddress(currentUser.getPrincipalId());
        Map addressKeys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(defaultPrincipalAddress);
        defaultPrincipalAddress = (DefaultPrincipalAddress) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(DefaultPrincipalAddress.class, addressKeys);
        if (ObjectUtils.isNotNull(defaultPrincipalAddress) && ObjectUtils.isNotNull(defaultPrincipalAddress.getBuilding())) {
            if (defaultPrincipalAddress.getBuilding().isActive()) {
                this.setDeliveryCampusCode(defaultPrincipalAddress.getCampusCode());
                this.templateBuildingToDeliveryAddress(defaultPrincipalAddress.getBuilding());
                this.setDeliveryBuildingRoomNumber(defaultPrincipalAddress.getBuildingRoomNumber());
            }
            else {
                //since building is now inactive, delete default building record
                SpringContext.getBean(BusinessObjectService.class).delete(defaultPrincipalAddress);
            }
        }
        
        // set the APO limit
        this.setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class).getApoLimit(this.getVendorContractGeneratedIdentifier(), this.getChartOfAccountsCode(), this.getOrganizationCode()));

        // populate billing address
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setBillingCampusCode(this.getDeliveryCampusCode());
        Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(billingAddress);
        billingAddress = (BillingAddress) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(BillingAddress.class, keys);
        this.templateBillingAddress(billingAddress);
        
        // populate receiving address with the default one for the chart/org
        loadReceivingAddress();
        
        SpringContext.getBean(PurapService.class).addBelowLineItems(this);
        this.refreshNonUpdateableReferences();
    }

    public void templateBuildingToDeliveryAddress(Building building) {
        if (ObjectUtils.isNotNull(building)) {
            setDeliveryBuildingCode(building.getBuildingCode());
            setDeliveryBuildingName(building.getBuildingName());
            setDeliveryBuildingLine1Address(building.getBuildingStreetAddress());
            setDeliveryCityName(building.getBuildingAddressCityName());
            setDeliveryStateCode(building.getBuildingAddressStateCode());
            setDeliveryPostalCode(building.getBuildingAddressZipCode());
            setDeliveryCountryCode(building.getBuildingAddressCountryCode());
        }
    }

    /**
     * Determines what PO transmission method to use.
     * 
     * @return the PO PO transmission method to use.
     */
    protected String determinePurchaseOrderTransmissionMethod() {
        
        return SpringContext.getBean(ParameterService.class).getParameterValue(RequisitionDocument.class, PurapParameterConstants.PURAP_DEFAULT_PO_TRANSMISSION_CODE);
    }

    /**
     * Checks whether copying of this document should be allowed. Copying is not allowed if this is a B2B requistion, and more than
     * a set number of days have passed since the document's creation.
     * 
     * @return True if copying of this requisition is allowed.
     * @see org.kuali.rice.kns.document.Document#getAllowsCopy()
     */
    @Override
    public boolean getAllowsCopy() {
        boolean allowsCopy = super.getAllowsCopy();
        if (PurapConstants.RequisitionSources.B2B.equals(getRequisitionSourceCode())) {
            DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
            Calendar c = Calendar.getInstance();

            // The allowed copy date is the document creation date plus a set number of days.
            Date createDate = getDocumentHeader().getWorkflowDocument().getCreateDate();
            c.setTime(createDate);
            String allowedCopyDays = SpringContext.getBean(ParameterService.class).getParameterValue(RequisitionDocument.class, PurapParameterConstants.B2B_ALLOW_COPY_DAYS);
            c.add(Calendar.DATE, Integer.parseInt(allowedCopyDays));
            Date allowedCopyDate = c.getTime();
            Date currentDate = dateTimeService.getCurrentDate();

            // Return true if the current time is before the allowed copy date.
            allowsCopy = (dateTimeService.dateDiff(currentDate, allowedCopyDate, false) > 0);
        }
        return allowsCopy;
    }

    /**
     * Performs logic needed to copy Requisition Document.
     * 
     * @see org.kuali.rice.kns.document.Document#toCopy()
     */
    @Override
    public void toCopy() throws WorkflowException, ValidationException {
        super.toCopy();

        // Clear related views
        this.setAccountsPayablePurchasingDocumentLinkIdentifier(null);
        this.setRelatedViews(null);
        
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        ChartOrgHolder purapChartOrg = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, PurapConstants.PURAP_NAMESPACE);
        this.setPurapDocumentIdentifier(null);

        // Set req status to INPR.
        this.setStatusCode(PurapConstants.RequisitionStatuses.IN_PROCESS);

        // Set fields from the user.
        if (ObjectUtils.isNotNull(purapChartOrg)) {
        this.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
        this.setOrganizationCode(purapChartOrg.getOrganizationCode());
        }
        this.setPostingYear(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        boolean activeVendor = true;
        boolean activeContract = true;
        Date today = SpringContext.getBean(DateTimeService.class).getCurrentDate();
        VendorContract vendorContract = new VendorContract();
        vendorContract.setVendorContractGeneratedIdentifier(this.getVendorContractGeneratedIdentifier());
        Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(vendorContract);
        vendorContract = (VendorContract) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(VendorContract.class, keys);
        if (!(vendorContract != null && today.after(vendorContract.getVendorContractBeginningDate()) && today.before(vendorContract.getVendorContractEndDate()))) {
            activeContract = false;
        }

        VendorDetail vendorDetail = SpringContext.getBean(VendorService.class).getVendorDetail(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier());
        if (!(vendorDetail != null && vendorDetail.isActiveIndicator())) {
            activeVendor = false;
        }

//KFSPTS-1458: Removed this code because saved values were getting overwritten when they shouldn't have been.
//        //KFSPTS-916 : need vendor address key for business rules and only way to get it is to retrieve the default PO address for the vendor.
//        if (vendorDetail != null) {        	
//        	VendorAddress vendorAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier(), VendorConstants.AddressTypes.PURCHASE_ORDER, "");
//        	if (vendorAddress != null) {
//        		super.templateVendorAddress(vendorAddress);   
//        	}
//        }

        // B2B - only copy if contract and vendor are both active (throw separate errors to print to screen)
        if (this.getRequisitionSourceCode().equals(PurapConstants.RequisitionSources.B2B)) {
            if (!activeContract) {
                throw new ValidationException(PurapKeyConstants.ERROR_REQ_COPY_EXPIRED_CONTRACT);
            }
            if (!activeVendor) {
                throw new ValidationException(PurapKeyConstants.ERROR_REQ_COPY_INACTIVE_VENDOR);
            }
        }

        if (!activeVendor) {
            this.setVendorContractGeneratedIdentifier(null);
        }
        if (!activeContract) {
            this.setVendorContractGeneratedIdentifier(null);
        }

        // These fields should not be set in this method; force to be null
        this.setOrganizationAutomaticPurchaseOrderLimit(null);
        this.setPurchaseOrderAutomaticIndicator(false);

        // Fill the BO Notes with an empty List.
        this.setBoNotes(new ArrayList());

        for (Iterator iter = this.getItems().iterator(); iter.hasNext();) {
            RequisitionItem item = (RequisitionItem) iter.next();
            item.setPurapDocumentIdentifier(null);
            item.setItemIdentifier(null);
            for (Iterator acctIter = item.getSourceAccountingLines().iterator(); acctIter.hasNext();) {
                RequisitionAccount account = (RequisitionAccount) acctIter.next();
                account.setAccountIdentifier(null);
                account.setItemIdentifier(null);
            }
        }

        if (!PurapConstants.RequisitionSources.B2B.equals(this.getRequisitionSourceCode())) {
            SpringContext.getBean(PurapService.class).addBelowLineItems(this);
        }
        this.setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class).getApoLimit(this.getVendorContractGeneratedIdentifier(), this.getChartOfAccountsCode(), this.getOrganizationCode()));
        clearCapitalAssetFields();
        SpringContext.getBean(PurapService.class).clearTax(this, this.isUseTaxIndicator());
        
        this.refreshNonUpdateableReferences();
    }
    
   
    
    /**
     * toCopyFromGateway
     */
    
    public void toCopyFromGateway() throws WorkflowException, ValidationException {
       //no validation for the KFS copy requisition rules:
       //  if (!this.getAllowsCopy()) {
       //      throw new IllegalStateException(this.getClass().getName() + " does not support document-level copying");
       // }
       
    	String sourceDocumentHeaderId = getDocumentNumber();
        setNewDocumentHeader();
        
        getDocumentBusinessObject().getBoNotes();
        
        getDocumentHeader().setDocumentTemplateNumber(sourceDocumentHeaderId);

        addCopyErrorDocumentNote("copied from document " + sourceDocumentHeaderId);   
        
       //--- LedgerPostingDocumentBase:
        setAccountingPeriod(retrieveCurrentAccountingPeriod());
        //--GeneralLedgerPostingDocumentBase:
        getGeneralLedgerPendingEntries().clear();
        //--AccountingDocumentBase:
        copyAccountingLines(false);
        updatePostingYearForAccountingLines(getSourceAccountingLines());
        updatePostingYearForAccountingLines(getTargetAccountingLines());
        
        //--RequisitionDocument:
        
        // Clear related views
        this.setAccountsPayablePurchasingDocumentLinkIdentifier(null);
        this.setRelatedViews(null);
        
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        ChartOrgHolder purapChartOrg = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, PurapConstants.PURAP_NAMESPACE);
        this.setPurapDocumentIdentifier(null);

        // Set req status to INPR.
        this.setStatusCode(PurapConstants.RequisitionStatuses.IN_PROCESS);

        // Set fields from the user.
        if (ObjectUtils.isNotNull(purapChartOrg)) {
        this.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
        this.setOrganizationCode(purapChartOrg.getOrganizationCode());
        }
        this.setPostingYear(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        boolean activeVendor = true;
        boolean activeContract = true;
        Date today = SpringContext.getBean(DateTimeService.class).getCurrentDate();
        VendorContract vendorContract = new VendorContract();
        vendorContract.setVendorContractGeneratedIdentifier(this.getVendorContractGeneratedIdentifier());
        Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(vendorContract);
        vendorContract = (VendorContract) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(VendorContract.class, keys);
        if (!(vendorContract != null && today.after(vendorContract.getVendorContractBeginningDate()) && today.before(vendorContract.getVendorContractEndDate()))) {
            activeContract = false;
        }

        VendorDetail vendorDetail = SpringContext.getBean(VendorService.class).getVendorDetail(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier());
        if (!(vendorDetail != null && vendorDetail.isActiveIndicator())) {
            activeVendor = false;
        }

        //KFSPTS-916 : need vendor address key for business rules and only way to get it is to retrieve the default PO address for the vendor.
        if (vendorDetail != null) {        	
        	VendorAddress vendorAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier(), VendorConstants.AddressTypes.PURCHASE_ORDER, "");
        	if (vendorAddress != null) {
        		super.templateVendorAddress(vendorAddress);   
        	}
        }

        // B2B - only copy if contract and vendor are both active (throw separate errors to print to screen)
        if (this.getRequisitionSourceCode().equals(PurapConstants.RequisitionSources.B2B)) {
            if (!activeContract) {
              //--  throw new ValidationException(PurapKeyConstants.ERROR_REQ_COPY_EXPIRED_CONTRACT);
            }
            if (!activeVendor) {
               //-- throw new ValidationException(PurapKeyConstants.ERROR_REQ_COPY_INACTIVE_VENDOR);
            }
        }

        if (!activeVendor) {
            this.setVendorContractGeneratedIdentifier(null);
        }
        if (!activeContract) {
            this.setVendorContractGeneratedIdentifier(null);
        }

        // These fields should not be set in this method; force to be null
        this.setOrganizationAutomaticPurchaseOrderLimit(null);
        this.setPurchaseOrderAutomaticIndicator(false);

        // Fill the BO Notes with an empty List.
        this.setBoNotes(new ArrayList());

        for (Iterator iter = this.getItems().iterator(); iter.hasNext();) {
            RequisitionItem item = (RequisitionItem) iter.next();
            item.setPurapDocumentIdentifier(null);
            item.setItemIdentifier(null);
            for (Iterator acctIter = item.getSourceAccountingLines().iterator(); acctIter.hasNext();) {
                RequisitionAccount account = (RequisitionAccount) acctIter.next();
                account.setAccountIdentifier(null);
                account.setItemIdentifier(null);
            }
        }

        if (!PurapConstants.RequisitionSources.B2B.equals(this.getRequisitionSourceCode())) {
            SpringContext.getBean(PurapService.class).addBelowLineItems(this);
        }
        this.setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class).getApoLimit(this.getVendorContractGeneratedIdentifier(), this.getChartOfAccountsCode(), this.getOrganizationCode()));
        clearCapitalAssetFields();
        SpringContext.getBean(PurapService.class).clearTax(this, this.isUseTaxIndicator());
        
        this.refreshNonUpdateableReferences();
    }
    
    /**
     * Updates status of this document and saves it.
     * 
     * @param statusCode the status code of the current status.
     */
    protected void updateStatusAndSave(String statusCode) {
        SpringContext.getBean(PurapService.class).updateStatus(this, statusCode);
        SpringContext.getBean(PurapService.class).saveDocumentNoValidation(this);
    }

    @Override
    public List<Long> getWorkflowEngineDocumentIdsToLock() {
        List<String> docIdStrings = new ArrayList<String>();
        docIdStrings.add(getDocumentNumber());
        
        //  PROCESSED
        if (getDocumentHeader().getWorkflowDocument().stateIsProcessed()) {
            // creates a new PO but no way to know what the docID will be ahead of time
        }
        
        //  convert our easy to use List<String> to a Long[]
        List<Long> docIds = new ArrayList<Long>();
        for (int i = 0; i < docIdStrings.size(); i++) {
            docIds.add(new Long(docIdStrings.get(i)));
        }
        return docIds;
    }

    /**
     * @see org.kuali.rice.kns.document.DocumentBase#doRouteStatusChange()
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChangeDTO statusChangeEvent) {
        LOG.debug("doRouteStatusChange() started");
        super.doRouteStatusChange(statusChangeEvent);
        try {
            // DOCUMENT PROCESSED
            if (this.getDocumentHeader().getWorkflowDocument().stateIsProcessed()) {
                String newRequisitionStatus = PurapConstants.RequisitionStatuses.AWAIT_CONTRACT_MANAGER_ASSGN;
                if (SpringContext.getBean(RequisitionService.class).isAutomaticPurchaseOrderAllowed(this)) {
                    newRequisitionStatus = PurapConstants.RequisitionStatuses.CLOSED;
                    SpringContext.getBean(PurchaseOrderService.class).createAutomaticPurchaseOrderDocument(this);
                }
                updateStatusAndSave(newRequisitionStatus);
            }
            // DOCUMENT DISAPPROVED
            else if (this.getDocumentHeader().getWorkflowDocument().stateIsDisapproved()) {
                String nodeName = SpringContext.getBean(WorkflowDocumentService.class).getCurrentRouteLevelName(getDocumentHeader().getWorkflowDocument());
                NodeDetails currentNode = NodeDetailEnum.getNodeDetailEnumByName(nodeName);
                if (ObjectUtils.isNotNull(currentNode)) {
                    if (StringUtils.isNotBlank(currentNode.getDisapprovedStatusCode())) {
                        updateStatusAndSave(currentNode.getDisapprovedStatusCode());
                        return;
                    }
                }
              //--  logAndThrowRuntimeException("No status found to set for document being disapproved in node '" + nodeName + "'");
            }
            // DOCUMENT CANCELED
            else if (this.getDocumentHeader().getWorkflowDocument().stateIsCanceled()) {
                updateStatusAndSave(RequisitionStatuses.CANCELLED);
            }
        }
        catch (WorkflowException e) {
          //--  logAndThrowRuntimeException("Error saving routing data while saving document with id " + getDocumentNumber(), e);
        }
        LOG.debug("doRouteStatusChange() ending");
    }

    /**
     * @see org.kuali.rice.kns.document.DocumentBase#handleRouteLevelChange(org.kuali.rice.kew.clientapp.vo.DocumentRouteLevelChangeDTO)
     */
    @Override
    public void doRouteLevelChange(DocumentRouteLevelChangeDTO change) {
        LOG.debug("handleRouteLevelChange() started");
        super.doRouteLevelChange(change);
        try {
            String newNodeName = change.getNewNodeName();
            if (StringUtils.isNotBlank(newNodeName)) {
                ReportCriteriaDTO reportCriteriaDTO = new ReportCriteriaDTO(Long.valueOf(getDocumentNumber()));
                reportCriteriaDTO.setTargetNodeName(newNodeName);
                if (SpringContext.getBean(KualiWorkflowInfo.class).documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[] { KEWConstants.ACTION_REQUEST_APPROVE_REQ, KEWConstants.ACTION_REQUEST_COMPLETE_REQ }, false)) {
                    NodeDetails currentNode = NodeDetailEnum.getNodeDetailEnumByName(newNodeName);
                    if (ObjectUtils.isNotNull(currentNode)) {
                        if (StringUtils.isNotBlank(currentNode.getAwaitingStatusCode())) {
                            updateStatusAndSave(currentNode.getAwaitingStatusCode());
                        }
                    }
                }
                else {
                    LOG.debug("Document with id " + getDocumentNumber() + " will not stop in route node '" + newNodeName + "'");
                }
            }
        }
        catch (WorkflowException e) {
            String errorMsg = "Workflow Error found checking actions requests on document with id " + getDocumentNumber() + ". *** WILL NOT UPDATE PURAP STATUS ***";
            LOG.warn(errorMsg, e);
        }
    }

    /**
     * @see org.kuali.kfs.sys.document.AccountingDocument#getSourceAccountingLineClass()
     */
    @Override
    public Class getSourceAccountingLineClass() {
      //NOTE: do not do anything with this method as it is used by routing etc!
        return super.getSourceAccountingLineClass();
    } 
    
    public String getRequisitionOrganizationReference1Text() {
        return requisitionOrganizationReference1Text;
    }

    public void setRequisitionOrganizationReference1Text(String requisitionOrganizationReference1Text) {
        this.requisitionOrganizationReference1Text = requisitionOrganizationReference1Text;
    }

    public String getRequisitionOrganizationReference2Text() {
        return requisitionOrganizationReference2Text;
    }

    public void setRequisitionOrganizationReference2Text(String requisitionOrganizationReference2Text) {
        this.requisitionOrganizationReference2Text = requisitionOrganizationReference2Text;
    }

    public String getRequisitionOrganizationReference3Text() {
        return requisitionOrganizationReference3Text;
    }

    public void setRequisitionOrganizationReference3Text(String requisitionOrganizationReference3Text) {
        this.requisitionOrganizationReference3Text = requisitionOrganizationReference3Text;
    }

    public String getAlternate1VendorName() {
        return alternate1VendorName;
    }

    public void setAlternate1VendorName(String alternate1VendorName) {
        this.alternate1VendorName = alternate1VendorName;
    }

    public String getAlternate2VendorName() {
        return alternate2VendorName;
    }

    public void setAlternate2VendorName(String alternate2VendorName) {
        this.alternate2VendorName = alternate2VendorName;
    }

    public String getAlternate3VendorName() {
        return alternate3VendorName;
    }

    public void setAlternate3VendorName(String alternate3VendorName) {
        this.alternate3VendorName = alternate3VendorName;
    }

    public String getAlternate4VendorName() {
        return alternate4VendorName;
    }

    public void setAlternate4VendorName(String alternate4VendorName) {
        this.alternate4VendorName = alternate4VendorName;
    }

    public String getAlternate5VendorName() {
        return alternate5VendorName;
    }

    public void setAlternate5VendorName(String alternate5VendorName) {
        this.alternate5VendorName = alternate5VendorName;
    }

    public KualiDecimal getOrganizationAutomaticPurchaseOrderLimit() {
        return organizationAutomaticPurchaseOrderLimit;
    }

    public void setOrganizationAutomaticPurchaseOrderLimit(KualiDecimal organizationAutomaticPurchaseOrderLimit) {
        this.organizationAutomaticPurchaseOrderLimit = organizationAutomaticPurchaseOrderLimit;
    }

    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#getItemClass()
     */
    @Override
    public Class getItemClass() {
        return RequisitionItem.class;
    }

    @Override
    public Class getItemUseTaxClass() {
        return PurchaseRequisitionItemUseTax.class;
    }

    /**
     * Returns null as requistion has no source document.
     * 
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#getPurApSourceDocumentIfPossible()
     */
    @Override
    public PurchasingAccountsPayableDocument getPurApSourceDocumentIfPossible() {
        return null;
    }

    /**
     * Returns null as requistion has no source document.
     * 
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#getPurApSourceDocumentLabelIfPossible()
     */
    @Override
    public String getPurApSourceDocumentLabelIfPossible() {
        return null;
    }

    /**
     * @see org.kuali.rice.kns.document.Document#getDocumentTitle()
     */
    @Override
    public String getDocumentTitle() {
        String title = "";
        if (SpringContext.getBean(ParameterService.class).getIndicatorParameter(RequisitionDocument.class, PurapParameterConstants.PURAP_OVERRIDE_REQ_DOC_TITLE)) {
            String docIdStr = "";
            if ((this.getPurapDocumentIdentifier() != null) && (StringUtils.isNotBlank(this.getPurapDocumentIdentifier().toString()))) {
                docIdStr = "Requisition: " + this.getPurapDocumentIdentifier().toString();
            }
            String chartAcct = this.getFirstChartAccount();
            String chartAcctStr = (chartAcct == null ? "" : " - Account Number:  " + chartAcct);
            title = docIdStr + chartAcctStr;
        }
        else {
            title = super.getDocumentTitle();
        }
        return title;
    }

    /**
     * Gets this requisition's Chart/Account of the first accounting line from the first item.
     * 
     * @return The first Chart and Account, or an empty string if there is none.
     */
    protected String getFirstChartAccount() {
        String chartAcct = null;
        RequisitionItem item = (RequisitionItem) this.getItem(0);
        if (ObjectUtils.isNotNull(item)) {
            if (item.getSourceAccountingLines().size() > 0) {
            PurApAccountingLine accountLine = item.getSourceAccountingLine(0);
            if (ObjectUtils.isNotNull(accountLine) && ObjectUtils.isNotNull(accountLine.getChartOfAccountsCode()) && ObjectUtils.isNotNull(accountLine.getAccountNumber())) {
                chartAcct = accountLine.getChartOfAccountsCode() + "-" + accountLine.getAccountNumber();
                }
            }
        }
        return chartAcct;
    }

    public Date getCreateDate() {
        return this.getDocumentHeader().getWorkflowDocument().getCreateDate();
    }

    public String getUrl() {
        return SpringContext.getBean(KualiConfigurationService.class).getPropertyString(KFSConstants.WORKFLOW_URL_KEY) + "/DocHandler.do?docId=" + getDocumentNumber() + "&command=displayDocSearchView";
    }

    /**
     * Used for routing only.
     * 
     * @deprecated
     */
    public String getStatusDescription() {
        return "";
    }

    /**
     * Used for routing only.
     * 
     * @deprecated
     */
    public void setStatusDescription(String statusDescription) {
    }
    
    /**
     * This is a "do nothing" version of the method - it just won't create GLPEs
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#generateGeneralLedgerPendingEntries(org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail, org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper)
     */
    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        return true;
    }

    @Override
    public Class getPurchasingCapitalAssetItemClass() {
        return RequisitionCapitalAssetItem.class;
    }
    
    @Override
    public Class getPurchasingCapitalAssetSystemClass() {
        return RequisitionCapitalAssetSystem.class;
    }
    
    @Override
    public boolean shouldGiveErrorForEmptyAccountsProration() {
        if (isDocumentStoppedInRouteNode(NodeDetailEnum.CONTENT_REVIEW) ||
            getStatusCode().equals(PurapConstants.RequisitionStatuses.IN_PROCESS)) {
            return false;
        }
        return true;
    }
    
    public Date getCreateDateForResult() {
        return this.getDocumentHeader().getWorkflowDocument().getCreateDate();
    }

    /**
     * Gets the isBlanketApproveRequest attribute. 
     * @return Returns the isBlanketApproveRequest.
     */
    public boolean isBlanketApproveRequest() {
        return isBlanketApproveRequest;
    }

    /**
     * Sets the isBlanketApproveRequest attribute value.
     * @param isBlanketApproveRequest The isBlanketApproveRequest to set.
     */
    public void setBlanketApproveRequest(boolean isBlanketApproveRequest) {
        this.isBlanketApproveRequest = isBlanketApproveRequest;
    }
    
    @Override
    public boolean isSensitive() {
    	boolean isSensitive =  super.isSensitive();

        for (PurchasingItemBase item : (List<PurchasingItemBase>)this.getItems()) {
            if (item.getCommodityCode() != null 
            		&& item.getCommodityCode().getSensitiveDataCode() != null 
            		&& item.getCommodityCode().getSensitiveDataCode().length() != 0) {
                isSensitive |= true;
            }
        }
        return isSensitive;
    }
    
	public static final String DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW = "DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW";

    protected boolean isAwardReviewRequired() {
        boolean requiresAwardReview = false;
    	
    	this.getAccountsForRouting();
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        
        // If the award amount is less than the threshold, then there's no reason to review the object codes, return false
        if(!isAwardAmountGreaterThanThreshold()) {
        	return requiresAwardReview;
        }
        
        for (PurApItem item : (List<PurApItem>) this.getItems()) {
            for (PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                
                requiresAwardReview = isObjectCodeAllowedForAwardRouting(accountingLine, parameterService);
                // We should return true as soon as we have at least one objectCodeAllowed=true so that the PO will stop at Award
                // level.
                if (requiresAwardReview) {
                    return requiresAwardReview;
                }

            }
        }
        return requiresAwardReview;        
    }

    protected boolean isObjectCodeAllowedForAwardRouting(PurApAccountingLine accountingLine, ParameterService parameterService) {
        if (ObjectUtils.isNull(accountingLine.getObjectCode())) {
            return false;
        }

        // make sure object code is active
        if (!accountingLine.getObjectCode().isFinancialObjectActiveCode()) {
            return false;
        }

        String chartCode = accountingLine.getChartOfAccountsCode();
        // check object level is in permitted list for award routing
        boolean objectCodeAllowed = parameterService.getParameterEvaluator(PurchaseOrderDocument.class, PurapParameterConstants.CG_ROUTE_OBJECT_LEVELS_BY_CHART, PurapParameterConstants.NO_CG_ROUTE_OBJECT_LEVELS_BY_CHART, chartCode, accountingLine.getObjectCode().getFinancialObjectLevelCode()).evaluationSucceeds();

        if (!objectCodeAllowed) {
            // If the object level is not permitting for award routing, then we need to also
            // check object code is in permitted list for award routing
            objectCodeAllowed = parameterService.getParameterEvaluator(PurchaseOrderDocument.class, PurapParameterConstants.CG_ROUTE_OBJECT_CODES_BY_CHART, PurapParameterConstants.NO_CG_ROUTE_OBJECT_CODES_BY_CHART, chartCode, accountingLine.getFinancialObjectCode()).evaluationSucceeds();
        }
        return objectCodeAllowed;
    }

    protected boolean isAwardAmountGreaterThanThreshold() {
		ParameterService parameterService = SpringContext.getBean(ParameterService.class);
		String dollarThreshold = parameterService.getParameterValue("KFS-PURAP", "Requisition", DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW);
		KualiDecimal dollarThresholdDecimal = new KualiDecimal(dollarThreshold);
		if ( this.getTotalPreTaxDollarAmount().isGreaterEqual(dollarThresholdDecimal)) {
			return true;
		}				
		return false;
    }
    
    public List<Account> getAccountsForAwardRouting() {
        List<Account> accounts = new ArrayList<Account>();
        
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        for (PurApItem item : (List<PurApItem>) this.getItems()) {
            for (PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                if (isObjectCodeAllowedForAwardRouting(accountingLine, parameterService)) {
                    if (ObjectUtils.isNull(accountingLine.getAccount())) {
                        accountingLine.refreshReferenceObject("account");
                    }
                    if (accountingLine.getAccount() != null && !accounts.contains(accountingLine.getAccount())) {
                        accounts.add(accountingLine.getAccount());
                    }
                }
            }
        }
        return accounts;
    }

    // KFSPTS-1273 fix an existing issue
    // this change in accountingbase will have wider impact.  Not familiar with the whole impact, so implement for the requisition first.
    // a complete solution for all document types and for all events need more work
    protected List generateEvents(List persistedLines, List currentLines, String errorPathPrefix, TransactionalDocument document) {
        List addEvents = new ArrayList();
        List updateEvents = new ArrayList();
        List reviewEvents = new ArrayList();
        List deleteEvents = new ArrayList();
        
        errorPathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + ".item["; 
        //KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME
        //
        // generate events
        Map persistedLineMap = buildAccountingLineMap(persistedLines);

        // (iterate through current lines to detect additions and updates, removing affected lines from persistedLineMap as we go
        // so deletions can be detected by looking at whatever remains in persistedLineMap)
        int index = 0;
        for (Iterator i = currentLines.iterator(); i.hasNext(); index++) {
            AccountingLine currentLine = (AccountingLine) i.next();
            String indexedErrorPathPrefix = getIndexedErrorPathPrefix(errorPathPrefix, currentLine);
            Integer key = currentLine.getSequenceNumber();

            AccountingLine persistedLine = (AccountingLine) persistedLineMap.get(key);
            // if line is both current and persisted...
            if (persistedLine != null) {
                // ...check for updates
                if (!currentLine.isLike(persistedLine)) {
                    UpdateAccountingLineEvent updateEvent = new UpdateAccountingLineEvent(indexedErrorPathPrefix, document, persistedLine, currentLine);
                    updateEvents.add(updateEvent);
                }
                else {
                    ReviewAccountingLineEvent reviewEvent = new ReviewAccountingLineEvent(indexedErrorPathPrefix, document, currentLine);
                    reviewEvents.add(reviewEvent);
                }

                persistedLineMap.remove(key);
            }
            else {
                // it must be a new addition
                AddAccountingLineEvent addEvent = new AddAccountingLineEvent(indexedErrorPathPrefix, document, currentLine);
                addEvents.add(addEvent);
            }
        }

        // detect deletions
        for (Iterator i = persistedLineMap.entrySet().iterator(); i.hasNext();) {
            // the deleted line is not displayed on the page, so associate the error with the whole group
            String groupErrorPathPrefix = errorPathPrefix + KFSConstants.ACCOUNTING_LINE_GROUP_SUFFIX;
            Map.Entry e = (Map.Entry) i.next();
            AccountingLine persistedLine = (AccountingLine) e.getValue();
            DeleteAccountingLineEvent deleteEvent = new DeleteAccountingLineEvent(groupErrorPathPrefix, document, persistedLine, true);
            deleteEvents.add(deleteEvent);
        }


        //
        // merge the lists
        List lineEvents = new ArrayList();
        lineEvents.addAll(reviewEvents);
        lineEvents.addAll(updateEvents);
        lineEvents.addAll(addEvents);
        lineEvents.addAll(deleteEvents);

        return lineEvents;
    }

    private String getIndexedErrorPathPrefix(String errorPathPrefix, AccountingLine currentLine) {
    	int idx = 0;
    	int i = 0;
        for (PurApItem item : (List<PurApItem>)this.getItems()) {
        	int j = 0;
        	// KFSPTS-1273 Note : The accountinglinegrouptag will sort this before the acctlines are rendered.  If we don't sort here, then
        	// the error icon may be placed in wrong line.
        	if (CollectionUtils.isNotEmpty(item.getSourceAccountingLines())) {
                Collections.sort(item.getSourceAccountingLines(), new PurapAccountingLineComparator());
        	}
            for (PurApAccountingLine acctLine : item.getSourceAccountingLines()) {
            	if (acctLine == currentLine) {
            		return errorPathPrefix +  i + "]."+ KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME + "["+j+"]";
            	} else {
            		j++;
            	}
            }
            i++;
        }
		return errorPathPrefix +  0 + "]."+ KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME + "["+0+"]";
            
    }

}

