/**
 * 
 */
package edu.cornell.kfs.module.ezra.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
/**
 * @author kwk43
 *
 */
public class EzraProposalAward extends PersistableBusinessObjectBase {

	private static final long serialVersionUID = 1L;
	private String projectId; //PROJ_ID
	private String awardProposalId; //AWARD_PROP_ID
	private String projectTitle; //TITLE
	private Long sponsorNumber; //SPONSOR_ID
	private String sponsorProjectId; //SPONSOR_PROJ_ID
	private String cfdaNumber; //CFDA_NMBR
	private String status; //STATUS_CD
	private String purpose; //PROJ_FUNCTION_CD
	private String awardDescriptionCode; //AWARD_DESC_CD
	private Date startDate; //AWD_PROP_START_DT
	private Date stopDate; //AWD_PROP_END_DT
	private KualiDecimal totalAmt; //AWD_PROP_TOTAL
	private Date budgetStartDate; //BUDG_START_DT
	private Date budgetStopDate; //BUDG_END_DT
	private KualiDecimal budgetAmt; //BUDG_TOTAL
	private KualiDecimal csVolCntr;
	private KualiDecimal csVolDept;
	private KualiDecimal csVolClg;
	private KualiDecimal csVolUniv;
	private KualiDecimal csVolExt;
	private KualiDecimal csMandCntr;
	private KualiDecimal csMandDept;
	private KualiDecimal csMandClg;
	private KualiDecimal csMandUniv;
	private KualiDecimal csMandExt;
	private String federalPassThrough; //FED_FLOW_THROUGH
	private Long federalPassThroughAgencyNumber; //FLOWTHROUGH_SPONSOR_ID
	private String departmentId; //DEPT_ID
	private Date lastUpdated;
	
	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}


	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}


	/**
	 * @return the awardProposalId
	 */
	public String getAwardProposalId() {
		return awardProposalId;
	}


	/**
	 * @param awardProposalId the awardProposalId to set
	 */
	public void setAwardProposalId(String awardProposalId) {
		this.awardProposalId = awardProposalId;
	}


	/**
	 * @return the projectTitle
	 */
	public String getProjectTitle() {
		return projectTitle;
	}


	/**
	 * @param projectTitle the projectTitle to set
	 */
	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}


	/**
	 * @return the sponsorNumber
	 */
	public Long getSponsorNumber() {
		return sponsorNumber;
	}


	/**
	 * @param sponsorNumber the sponsorNumber to set
	 */
	public void setSponsorNumber(Long sponsorNumber) {
		this.sponsorNumber = sponsorNumber;
	}


	/**
	 * @return the sponsorProjectId
	 */
	public String getSponsorProjectId() {
		return sponsorProjectId;
	}


	/**
	 * @param sponsorProjectId the sponsorProjectId to set
	 */
	public void setSponsorProjectId(String sponsorProjectId) {
		this.sponsorProjectId = sponsorProjectId;
	}


	/**
	 * @return the cfdaNumber
	 */
	public String getCfdaNumber() {
		return cfdaNumber;
	}


	/**
	 * @param cfdaNumber the cfdaNumber to set
	 */
	public void setCfdaNumber(String cfdaNumber) {
		this.cfdaNumber = cfdaNumber;
	}


	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}


	/**
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}


	/**
	 * @param purpose the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}


	/**
	 * @return the awardDescriptionCode
	 */
	public String getAwardDescriptionCode() {
		return awardDescriptionCode;
	}


	/**
	 * @param awardDescriptionCode the awardDescriptionCode to set
	 */
	public void setAwardDescriptionCode(String awardDescriptionCode) {
		this.awardDescriptionCode = awardDescriptionCode;
	}


	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}


	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}


	/**
	 * @return the stopDate
	 */
	public Date getStopDate() {
		return stopDate;
	}


	/**
	 * @param stopDate the stopDate to set
	 */
	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}


	/**
	 * @return the totalAmt
	 */
	public KualiDecimal getTotalAmt() {
		return totalAmt;
	}


	/**
	 * @param totalAmt the totalAmt to set
	 */
	public void setTotalAmt(KualiDecimal totalAmt) {
		this.totalAmt = totalAmt;
	}


	public Date getBudgetStartDate() {
		return budgetStartDate;
	}


	public void setBudgetStartDate(Date budgetStartDate) {
		this.budgetStartDate = budgetStartDate;
	}


	public Date getBudgetStopDate() {
		return budgetStopDate;
	}


	public void setBudgetStopDate(Date budgetStopDate) {
		this.budgetStopDate = budgetStopDate;
	}


	/**
	 * @return the budgetAmt
	 */
	public KualiDecimal getBudgetAmt() {
		return budgetAmt;
	}


	/**
	 * @param budgetAmt the budgetAmt to set
	 */
	public void setBudgetAmt(KualiDecimal budgetAmt) {
		this.budgetAmt = budgetAmt;
	}

	
	public KualiDecimal getCsVolCntr() {
		return csVolCntr;
	}


	public void setCsVolCntr(KualiDecimal csVolCntr) {
		this.csVolCntr = csVolCntr;
	}


	public KualiDecimal getCsVolDept() {
		return csVolDept;
	}


	public void setCsVolDept(KualiDecimal csVolDept) {
		this.csVolDept = csVolDept;
	}


	public KualiDecimal getCsVolClg() {
		return csVolClg;
	}


	public void setCsVolClg(KualiDecimal csVolClg) {
		this.csVolClg = csVolClg;
	}


	public KualiDecimal getCsVolUniv() {
		return csVolUniv;
	}


	public void setCsVolUniv(KualiDecimal csVolUniv) {
		this.csVolUniv = csVolUniv;
	}


	public KualiDecimal getCsVolExt() {
		return csVolExt;
	}


	public void setCsVolExt(KualiDecimal csVolExt) {
		this.csVolExt = csVolExt;
	}


	/**
	 * @return the federalPassThrough
	 */
	public String getFederalPassThrough() {
		return federalPassThrough;
	}
	/**
	 * @return the federalPassThrough
	 */
	public boolean getFederalPassThroughBoolean() {
		if (federalPassThrough == null || federalPassThrough.equalsIgnoreCase("N")) {
			return false;
		} else
			return true;
	}


	/**
	 * @param federalPassThrough the federalPassThrough to set
	 */
	public void setFederalPassThrough(String federalPassThrough) {
		this.federalPassThrough = federalPassThrough;
	}


	/**
	 * @return the federalPassThroughAgencyNumber
	 */
	public Long getFederalPassThroughAgencyNumber() {
		return federalPassThroughAgencyNumber;
	}


	/**
	 * @param federalPassThroughAgencyNumber the federalPassThroughAgencyNumber to set
	 */
	public void setFederalPassThroughAgencyNumber(
			Long federalPassThroughAgencyNumber) {
		this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
	}

	/**
	 * @return the departmentId
	 */
	public String getDepartmentId() {
		return departmentId;
	}


	/**
	 * @param departmentId the departmentId to set
	 */
	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}


	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public KualiDecimal getCsMandCntr() {
		return csMandCntr;
	}


	public void setCsMandCntr(KualiDecimal csMandCntr) {
		this.csMandCntr = csMandCntr;
	}


	public KualiDecimal getCsMandDept() {
		return csMandDept;
	}


	public void setCsMandDept(KualiDecimal csMandDept) {
		this.csMandDept = csMandDept;
	}


	public KualiDecimal getCsMandClg() {
		return csMandClg;
	}


	public void setCsMandClg(KualiDecimal csMandClg) {
		this.csMandClg = csMandClg;
	}


	public KualiDecimal getCsMandUniv() {
		return csMandUniv;
	}


	public void setCsMandUniv(KualiDecimal csMandUniv) {
		this.csMandUniv = csMandUniv;
	}


	public KualiDecimal getCsMandExt() {
		return csMandExt;
	}


	public void setCsMandExt(KualiDecimal csMandExt) {
		this.csMandExt = csMandExt;
	}


	/* (non-Javadoc)
	 * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();

        m.put("projectId", projectId);
        m.put("awardProposalId", awardProposalId);
        return m;
	}

}
