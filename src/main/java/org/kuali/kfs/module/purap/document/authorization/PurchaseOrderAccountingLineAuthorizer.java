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
package org.kuali.kfs.module.purap.document.authorization;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.RequisitionService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.RoleManagementService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

/**
 * Accounting line authorizer for Requisition document which allows adding accounting lines at specified nodes
 */
public class PurchaseOrderAccountingLineAuthorizer extends PurapAccountingLineAuthorizer {
    private static final String NEW_UNORDERED_ITEMS_NODE = "NewUnorderedItems";

    /**
     * Allow new lines to be rendered at NewUnorderedItems node
     * @see org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase#renderNewLine(org.kuali.kfs.sys.document.AccountingDocument, java.lang.String)
     */
    @Override
    public boolean renderNewLine(AccountingDocument accountingDocument, String accountingGroupProperty) {
        KualiWorkflowDocument workflowDoc = accountingDocument.getDocumentHeader().getWorkflowDocument();
        String currentRouteNodeName = workflowDoc.getCurrentRouteNodeNames();
        
        //  if its in the NEW_UNORDERED_ITEMS node, then allow the new line to be drawn
        if (PurchaseOrderAccountingLineAuthorizer.NEW_UNORDERED_ITEMS_NODE.equals(currentRouteNodeName)) {
            return true;
        }
        
        if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_AMENDMENT_DOCUMENT.equals(workflowDoc.getDocumentType()) && StringUtils.isNotBlank(accountingGroupProperty) && accountingGroupProperty.contains(PurapPropertyConstants.ITEM)) {
            int itemNumber = determineItemNumberFromGroupProperty(accountingGroupProperty);
            PurchaseOrderAmendmentDocument poaDoc = (PurchaseOrderAmendmentDocument) accountingDocument;
            PurchaseOrderItem item = (PurchaseOrderItem) poaDoc.getItem(itemNumber);
            return item.isNewItemForAmendment() || item.getSourceAccountingLines().size() == 0;
        }
        
        return super.renderNewLine(accountingDocument, accountingGroupProperty) || (accountingDocument instanceof PurchaseOrderAmendmentDocument && accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentRouteNodeNames().equals(RequisitionStatuses.NODE_ACCOUNT) 
				&& isFiscalOfficersForAllAcctLines((PurchaseOrderAmendmentDocument)accountingDocument));
    }
    
    private int determineItemNumberFromGroupProperty(String accountingGroupProperty) {
        int openBracketPos = accountingGroupProperty.indexOf("[");
        int closeBracketPos = accountingGroupProperty.indexOf("]");
        String itemNumberString = accountingGroupProperty.substring(openBracketPos + 1, closeBracketPos);
        int itemNumber = new Integer(itemNumberString).intValue();
        return itemNumber;
    }
    
    @Override
    protected boolean allowAccountingLinesAreEditable(AccountingDocument accountingDocument,
            AccountingLine accountingLine){
        PurApAccountingLine purapAccount = (PurApAccountingLine)accountingLine;
        PurchaseOrderItem poItem = (PurchaseOrderItem)purapAccount.getPurapItem();
        PurchaseOrderDocument po = (PurchaseOrderDocument)accountingDocument;
        
        
        if (poItem != null && !poItem.getItemType().isAdditionalChargeIndicator()) {
            if (!poItem.isItemActiveIndicator()) {
                return false;
            }

            // if total amount has a value and is non-zero
            if (poItem.getItemInvoicedTotalAmount() != null && poItem.getItemInvoicedTotalAmount().compareTo(new KualiDecimal(0)) != 0) {
                return false;
            }
            
            if (po.getContainsUnpaidPaymentRequestsOrCreditMemos() && !poItem.isNewItemForAmendment()) {
                return false;
            }
            
        }
        return super.allowAccountingLinesAreEditable(accountingDocument, accountingLine);
    }

    @Override
    public boolean determineEditPermissionOnLine(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, boolean currentUserIsDocumentInitiator, boolean pageIsEditable) {
     // the fields in a new line should be always editable
        if (accountingLine.getSequenceNumber() == null) {
            return true;
        }
        
        // check the initiation permission on the document if it is in the state of preroute, but only if
        // the PO status is not In Process.
        KualiWorkflowDocument workflowDocument = accountingDocument.getDocumentHeader().getWorkflowDocument();
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument)accountingDocument;
        if (!poDocument.getStatusCode().equals(PurapConstants.PurchaseOrderStatuses.IN_PROCESS) && (workflowDocument.stateIsInitiated() || workflowDocument.stateIsSaved())) {
            if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_AMENDMENT_DOCUMENT.equals(workflowDocument.getDocumentType())) {
                PurApAccountingLine purapAccount = (PurApAccountingLine)accountingLine;
                PurchaseOrderItem item = (PurchaseOrderItem)purapAccount.getPurapItem();
                return item.isNewItemForAmendment() || item.getSourceAccountingLines().size() == 0;
            }
            else {
                return currentUserIsDocumentInitiator;
            }
        }
        else {
            return true;
        }
    }
    
    // KFSPTS-1768
	private boolean isFiscalOfficersForAllAcctLines(PurchaseOrderAmendmentDocument document) {

		boolean isFoForAcctLines = true;
		String personId = GlobalVariables.getUserSession().getPrincipalId();
		for (SourceAccountingLine accountingLine : (List<SourceAccountingLine>)document.getSourceAccountingLines()) {
			List<String> fiscalOfficers = new ArrayList<String>();
			AttributeSet roleQualifier = new AttributeSet();
			roleQualifier.put(KfsKimAttributes.DOCUMENT_NUMBER,document.getDocumentNumber());
			roleQualifier.put(KfsKimAttributes.DOCUMENT_TYPE_NAME, document.getDocumentHeader().getWorkflowDocument().getDocumentType());
			roleQualifier.put(KfsKimAttributes.FINANCIAL_DOCUMENT_TOTAL_AMOUNT,document.getDocumentHeader().getFinancialDocumentTotalAmount().toString());
			roleQualifier.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE,accountingLine.getChartOfAccountsCode());
			roleQualifier.put(KfsKimAttributes.ACCOUNT_NUMBER,accountingLine.getAccountNumber());
			fiscalOfficers.addAll(SpringContext.getBean(RoleManagementService.class).getRoleMemberPrincipalIds(KFSConstants.ParameterNamespaces.KFS,
					KFSConstants.SysKimConstants.FISCAL_OFFICER_KIM_ROLE_NAME,roleQualifier));
			if (!fiscalOfficers.contains(personId)) {
				fiscalOfficers.addAll(SpringContext.getBean(RoleManagementService.class).getRoleMemberPrincipalIds(
										KFSConstants.ParameterNamespaces.KFS,KFSConstants.SysKimConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME,
										roleQualifier));
			}
			if (!fiscalOfficers.contains(personId)) {
				fiscalOfficers.addAll(SpringContext.getBean(RoleManagementService.class).getRoleMemberPrincipalIds(KFSConstants.ParameterNamespaces.KFS,
										KFSConstants.SysKimConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME,roleQualifier));
			}
			if (!fiscalOfficers.contains(personId)) {
				isFoForAcctLines = false;
				break;
			}
		}

		return isFoForAcctLines;
	}

}
