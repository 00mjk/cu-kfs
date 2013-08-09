/*
 * Copyright 2009 The Kuali Foundation
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
package edu.cornell.kfs.fp.document.authorization;

import java.util.List;
import java.util.Set;

import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.AccountingDocumentPresentationControllerBase;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;

import edu.cornell.kfs.fp.document.service.CULegacyTravelService;
import edu.cornell.kfs.sys.CUKFSAuthorizationConstants;

@SuppressWarnings("serial")
public class DisbursementVoucherDocumentPresentationController extends AccountingDocumentPresentationControllerBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherDocumentPresentationController.class);
	
    /**
     * @see org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase#canBlanketApprove(org.kuali.rice.kns.document.Document)
     */
    @Override
	public boolean canBlanketApprove(Document document) {
        return false;
    }

    /**
     * @see org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase#getEditModes(org.kuali.rice.kns.document.Document)
     */
    @Override
    public Set<String> getEditModes(Document document) {
		LOG.info("Checking presentation permissions for DV.");
        Set<String> editModes = super.getEditModes(document);

        editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TAX_ENTRY);
        editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.FRN_ENTRY);
        editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.WIRE_ENTRY);

        addFullEntryEntryMode(document, editModes);
        addPayeeEditEntryMode(document, editModes);
        addTravelEntryMode(document, editModes);
        addTravelSystemGeneratedEntryMode(document, editModes);
        addPaymentHandlingEntryMode(document, editModes);
        addVoucherDeadlineEntryMode(document, editModes);
        addSpecialHandlingChagingEntryMode(document, editModes);

        return editModes;
    }

    protected void addPayeeEditEntryMode(Document document, Set<String> editModes) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        if ((workflowDocument.isInitiated() || workflowDocument.isSaved())) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYEE_ENTRY);
        }
        else if (workflowDocument.isEnroute()) {
            List<String> currentRouteLevels = getCurrentRouteLevels(workflowDocument);
            if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.ACCOUNT) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TAX) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.AWARD) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.CAMPUS) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TRAVEL)) {
                editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYEE_ENTRY);
            }
        }
    }
    
    protected void addFullEntryEntryMode(Document document, Set<String> editModes) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        if ((workflowDocument.isInitiated() || workflowDocument.isSaved())) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.FULL_ENTRY);
        }
    }
    
    /**
     * If at a proper route node, adds the ability to edit payment handling fields
     * @param document the disbursement voucher document authorization is being sought on
     * @param editModes the edit modes so far, which can be added to
     */
    protected void addPaymentHandlingEntryMode(Document document, Set<String> editModes) {
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        if ((workflowDocument.isInitiated() || workflowDocument.isSaved())) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYMENT_HANDLING_ENTRY);
        }
        final List<String> currentRouteLevels = getCurrentRouteLevels(workflowDocument);
        if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.ACCOUNT) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.CAMPUS) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TRAVEL) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TAX)) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYMENT_HANDLING_ENTRY);
        }
    }
    
    /**
     * If at a proper route node, adds the ability to edit the due date for the voucher
     * @param document the disbursement voucher document authorization is being sought on
     * @param editModes the edit modes so far, which can be added to
     */
    protected void addVoucherDeadlineEntryMode(Document document, Set<String> editModes) {
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        if ((workflowDocument.isInitiated() || workflowDocument.isSaved())) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.VOUCHER_DEADLINE_ENTRY);
        }
        final List<String> currentRouteLevels = getCurrentRouteLevels(workflowDocument);
        if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.ACCOUNT) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.CAMPUS) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TAX) || currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TRAVEL)) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.VOUCHER_DEADLINE_ENTRY);
        }
    }
    
    /**
     * If at a proper route node, adds the ability to edit the travel information on the disbursement voucher
     * @param document the disbursement voucher document authorization is being sought on
     * @param editModes the edit modes so far, which can be added to
     */
    protected void addTravelEntryMode(Document document, Set<String> editModes) {
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        final List<String> currentRouteLevels = getCurrentRouteLevels(workflowDocument);
        if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.ACCOUNT)) {  //FO?
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
        } else if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TAX)) { //tax manager? Then only allow this if we're going to route to travel node anyway
            if (((DisbursementVoucherDocument)document).isTravelReviewRequired()) {
               editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
            }
        } else if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.PAYMENT_METHOD) && ((DisbursementVoucherDocument)document).getDisbVchrPaymentMethodCode().equals(DisbursementVoucherConstants.PAYMENT_METHOD_DRAFT)) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
        } else {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY); // we're not FO? Then always add it, as KIM permissions will take it out if we shouldn't have it
        }
    }
    
    /**
     * If the DV was generated from a connection with the Cornell Travel System, enforces special edit rules regarding information on the disbursement voucher.
     * 
     * Added condition that DV is not in the Payment Method Reviewers' queue, as the check amount needs to be editable for that circustance.
     * 
     * @param document the disbursement voucher document authorization is being sought on
     * @param editModes the edit modes so far, which can be added to
     */
    protected void addTravelSystemGeneratedEntryMode(Document document, Set<String> editModes) {
    	final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument)document;
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        final List<String> currentRouteLevels = getCurrentRouteLevels(workflowDocument);

    	if(SpringContext.getBean(CULegacyTravelService.class).isLegacyTravelGeneratedKfsDocument(dvDocument.getDocumentNumber()) &&
    	   !currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.PAYMENT_METHOD)) {
    		LOG.info("Checking travel system generated entry permissions.");
        	editModes.add(CUKFSAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_SYSTEM_GENERATED_ENTRY); 
        }
    }
    
    /**
     * If at a proper route node, adds the ability to edit whether special handling is needed on the disbursement voucher
     * @param document the disbursement voucher document authorization is being sought on
     * @param editModes the edit modes so far, which can be added to
     */
    protected void addSpecialHandlingChagingEntryMode(Document document, Set<String> editModes) {
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        final List<String> currentRouteLevels = getCurrentRouteLevels(workflowDocument);
        
        if (!currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.PURCHASING)) {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.SPECIAL_HANDLING_CHANGING_ENTRY);
        }
    }
}
