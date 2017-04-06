package edu.cornell.kfs.concur.batch.report;

import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public class ConcurBatchReportMissingObjectCodeItem extends ConcurBatchReportLineValidationErrorItem {
     private String policyName;
     private String expenseTypeName;
     
     public ConcurBatchReportMissingObjectCodeItem() {
         super();
         this.policyName = KFSConstants.EMPTY_STRING;
         this.expenseTypeName = KFSConstants.EMPTY_STRING;
     }
     
     public ConcurBatchReportMissingObjectCodeItem(String reportId, String employeeId, String lastName, String firstName, String middleInitial, List<ValidationResult> itemErrorResults, String policyName,  String expenseTypeName) {
         super(reportId, employeeId, lastName, firstName, middleInitial, itemErrorResults);
         this.policyName = policyName;
         this.expenseTypeName = expenseTypeName;
     }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getExpenseTypeName() {
        return expenseTypeName;
    }

    public void setExpenseTypeName(String expenseTypeName) {
        this.expenseTypeName = expenseTypeName;
    }
     
}