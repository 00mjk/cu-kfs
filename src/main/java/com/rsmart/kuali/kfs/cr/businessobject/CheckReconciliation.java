/*
 * Copyright 2008 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.cr.businessobject;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.KualiInteger;

/**
 * Check Reconciliation BO
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class CheckReconciliation extends PersistableBusinessObjectBase implements Serializable {

    private Integer id; // CR_ID
    
    private KualiInteger checkNumber; // CHECK_NBR

    private Date checkDate; // CHECK_DT

    private KualiDecimal amount; // AMOUNT

    private String status; // STATUS

    private Timestamp lastUpdate; // LST_UPDT_TS
    
    private String bankAccountNumber; // BANK_ACCOUNT_NBR
    
    private Boolean glTransIndicator = Boolean.FALSE;  // GL_TRANS_IND
    
    private String sourceCode; // SRC_CD
    
    private String bankCode; // BNK_CD
    private Date issueDate; //ISSUE_DT
    
    private String payeeId;
    private String payeeName;
    private String payeeType;
    
    
    private Bank bank;
    
    /**
     * Constructs a CheckBase business object.
     */
    public CheckReconciliation() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public KualiInteger getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(KualiInteger checkNumber) {
        this.checkNumber = checkNumber;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(KualiDecimal amount) {
        this.amount = amount;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Boolean getGlTransIndicator() {
        return glTransIndicator;
    }

    public void setGlTransIndicator(Boolean glTransIndicator) {
        this.glTransIndicator = glTransIndicator;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    /**
     * @see org.kuali.core.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings("unchecked")
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put("bankAccountNumber", getBankAccountNumber());
        m.put("checkNumber", getCheckNumber());
        m.put("checkDate", getCheckNumber());
        m.put("amount", getAmount());
        m.put("sourceCd", getSourceCode());
        m.put("issueDate",getIssueDate());
        
        return m;
    }

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public String getPayeeId() {
		return payeeId;
	}

	public void setPayeeId(String payeeId) {
		this.payeeId = payeeId;
	}

	public String getPayeeName() {
		return payeeName;
	}

	public void setPayeeName(String payeeName) {
		this.payeeName = payeeName;
	}

	public String getPayeeType() {
		return payeeType;
	}

	public void setPayeeType(String payeeType) {
		this.payeeType = payeeType;
	}

}
