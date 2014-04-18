/*
 * Copyright 2005-2006 The Kuali Foundation
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
package org.kuali.kfs.fp.document.web.struts;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.document.CapitalAssetEditable;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;

import edu.cornell.kfs.fp.document.service.CULegacyTravelService;

/**
 * This class is the action form for Distribution of Income and Expense.
 */
public class DistributionOfIncomeAndExpenseForm extends KualiAccountingDocumentFormBase implements CapitalAssetEditable{
    
    protected CapitalAssetInformation capitalAssetInformation;
    
    /**
     * Constructs a DistributionOfIncomeAndExpenseForm.java.
     */
    public DistributionOfIncomeAndExpenseForm() {
        super();
        this.setCapitalAssetInformation(new CapitalAssetInformation());
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "DI";
    }
    
    /**
     * @return Returns the DistributionOfIncomeAndExpenseDocument.
     */
    public DistributionOfIncomeAndExpenseDocument getDistributionOfIncomeAndExpenseDocument() {
        return (DistributionOfIncomeAndExpenseDocument) getDocument();
    }

    /**
     * @param distributionOfIncomeAndExpenseDocument
     */
    public void setDistributionOfIncomeAndExpenseDocument(DistributionOfIncomeAndExpenseDocument distributionOfIncomeAndExpenseDocument) {
        setDocument(distributionOfIncomeAndExpenseDocument);
    }

    /**
     * @see org.kuali.kfs.fp.document.CapitalAssetEditable#getCapitalAssetInformation()
     */
    public CapitalAssetInformation getCapitalAssetInformation() {
        return capitalAssetInformation;
    }

    /**
     * @see org.kuali.kfs.fp.document.CapitalAssetEditable#setCapitalAssetInformation(org.kuali.kfs.fp.businessobject.CapitalAssetInformation)
     */
    public void setCapitalAssetInformation(CapitalAssetInformation capitalAssetInformation) {
        this.capitalAssetInformation = capitalAssetInformation;
    }

    /**
     * determines if the DI document is a travel DI and therefore should display the associated Trip #
     * 
     * @return true if the DI document is a travel DI; otherwise, return false
     */
    public boolean getCanViewTrip() {
    	//boolean canViewTrip = SpringContext.getBean(CULegacyTravelService.class).isLegacyTravelGeneratedKfsDocument(this.getDocId());;
    	DisbursementVoucherDocument disbursementVoucherDocument = (DisbursementVoucherDocument)this.getDocument();
    	boolean canViewTrip = SpringContext.getBean(CULegacyTravelService.class).isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(disbursementVoucherDocument);
    	return canViewTrip;
    }

    /**
     * 
     * @param tripID
     * @return
     */
    public String getTripUrl() {
    	String tripID = SpringContext.getBean(CULegacyTravelService.class).getLegacyTripID(this.getDocId());
    	LOG.info("getTripUrl() called");
    	StringBuffer url = new StringBuffer();
    	url.append(SpringContext.getBean(CULegacyTravelService.class).getTravelUrl());
        url.append("/navigation?form_action=0&tripid=").append(tripID).append("&link=true");
    	return url.toString();
    }
    
    /**
     * 
     * @return
     */
    public String getTripID() {
    	return SpringContext.getBean(CULegacyTravelService.class).getLegacyTripID(this.getDocId());
    }
    

}
