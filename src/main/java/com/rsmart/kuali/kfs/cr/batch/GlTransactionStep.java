/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.cr.batch;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.service.PendingTransactionService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.bo.KualiCode;
import org.kuali.rice.kns.service.BusinessObjectService;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.document.service.GlTransactionService;

/**
 * GlTransactionStep
 * 
 * @author Derek Helbert
 */
public class GlTransactionStep extends AbstractStep {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GlTransactionStep.class);
    
    private GlTransactionService glTransactionService;
    
    private BusinessObjectService businessObjectService;
    
    /**
     * Execute
     * 
     * @param jobName Job Name
     * @param jobRunDate Job Date
     * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("Started GlTransactionStep @ " + (new Date()).toString());
        
        LOG.info("Get Bank List");
        Collection<Bank> banks = businessObjectService.findAll(Bank.class);
        
        List<String> bankCodes = null;
        Collection<PaymentGroup> paymentGroups = null;
        Map<Object,Object> fieldValues = null;
        Collection<CheckReconciliation> records = null;
        
        // Stop payments
        fieldValues = new HashMap<Object,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.STOP);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        
        records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
            
            if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                    for (PaymentGroup paymentGroup : paymentGroups) {
                    	
                    	//Create cancellation offsets for STOPed checks. KFSPTS-1741
                        PendingTransactionService glPendingTransactionService = SpringContext.getBean(PendingTransactionService.class);
                        glPendingTransactionService.generateStopGeneralLedgerPendingEntry(paymentGroup);
                    	
                        
                        //glTransactionService.generateGlPendingTransactionStop(paymentGroup);
                        
                        KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                        if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                            paymentGroup.setPaymentStatus((PaymentStatus) code);
                        }
                        paymentGroup.setLastUpdate(new Timestamp((new java.util.Date()).getTime()));
                        businessObjectService.save(paymentGroup);
                    
                        // Update status
                        cr.setGlTransIndicator(Boolean.TRUE);
                        businessObjectService.save(cr);
                    
                        LOG.info("Generated Stop GL Pending Transacation");        
                    }
                }
            }
        }
        
        // Canceled payments
        fieldValues = new HashMap<Object,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.CANCELLED);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        
        records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
            
            if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                    for (PaymentGroup paymentGroup : paymentGroups) {
                        glTransactionService.generateGlPendingTransactionCancel(paymentGroup);
                    
                        KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                        if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                            paymentGroup.setPaymentStatus((PaymentStatus) code);
                        }
                        paymentGroup.setLastUpdate(new Timestamp((new java.util.Date()).getTime()));
                        businessObjectService.save(paymentGroup);
                    
                        // Update status
                        cr.setGlTransIndicator(Boolean.TRUE);
                    
                        businessObjectService.save(cr);
                        LOG.info("Generated Cancelled GL Pending Transacation");        
                    }
                }
            }
        }

        // VOID payments
        fieldValues = new HashMap<Object,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.VOIDED);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        
        records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
    
           if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                    for (PaymentGroup paymentGroup : paymentGroups) {
                        //Do not generate GL tarsactions for VIODED trasactions 

//                        glTransactionService.generateGlPendingTransactionStop(paymentGroup);
                    
                        KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                        if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                            paymentGroup.setPaymentStatus((PaymentStatus) code);
                        }
                        paymentGroup.setLastUpdate(new Timestamp((new java.util.Date()).getTime()));
                        businessObjectService.save(paymentGroup);
                    
                        // Update status
                        cr.setGlTransIndicator(Boolean.TRUE);
                        businessObjectService.save(cr);
                    
                        LOG.info("Generated VOID GL Pending Transacation");        
                    }
                }
            }
        }
        

        
        
        
        // Stale payments
        fieldValues = new HashMap<Object,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.STALE);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        
        records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
            
            if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                     for (PaymentGroup paymentGroup : paymentGroups) {
                         glTransactionService.generateGlPendingTransactionStale(paymentGroup);
                    
                         KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                         if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                             paymentGroup.setPaymentStatus((PaymentStatus) code);
                         }
                         paymentGroup.setLastUpdate(new Timestamp((new java.util.Date()).getTime()));
                         businessObjectService.save(paymentGroup);
                    
                         // Update status
                         cr.setGlTransIndicator(Boolean.TRUE);
                         businessObjectService.save(cr);
                    
                         LOG.info("Generated Stale GL Pending Transacation");        
                     }
                }
            }
        }
        
        LOG.info("Completed GlTransactionStep @ " + (new Date()).toString());

        return true;
    }

    /**
     * Set Bank Codes List
     * 
     * @param banks
     * @param cr
     * @param bankCodes
     */
    private void setBankCodes(Collection<Bank> banks, CheckReconciliation cr, List<String> bankCodes) {
        for( Bank bank : banks ) {
            if( bank.getBankAccountNumber().equals(cr.getBankAccountNumber()) ) {
                bankCodes.add(bank.getBankCode());
            }
        }
    }

    /**
     * Get GlTransactionService
     * 
     * @return GlTransactionService
     */
    public GlTransactionService getGlTransactionService() {
        return glTransactionService;
    }

    /**
     * Set GlTransactionService
     * 
     * @param glTransactionService
     */
    public void setGlTransactionService(GlTransactionService glTransactionService) {
        this.glTransactionService = glTransactionService;
    }

    /**
     * Get BusinessObjectService
     * 
     * @return BusinessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Set BusinessObjectService
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
}
