package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;

public enum GroupLevelFixture {
    DEPT_1_COST_CENTER_1(CloudCheckrFixtureConstants.ACCOUNT_NONE, CloudCheckrFixtureConstants.DEPT1_COSTCENTER2_COST),
    DEPT_1_COST_CENTER_2(CloudCheckrFixtureConstants.ACCOUNT_U353901, CloudCheckrFixtureConstants.DEPT1_COSTCENTER5_COST),
    DEPT_1_COST_CENTER_3(CloudCheckrFixtureConstants.ACCOUNT_U353803, CloudCheckrFixtureConstants.DEPT1_COSTCENTER1_COST),
    DEPT_1_COST_CENTER_4(CloudCheckrFixtureConstants.ACCOUNT_U353805, CloudCheckrFixtureConstants.DEPT1_COSTCENTER3_COST),
    DEPT_1_COST_CENTER_5(CloudCheckrFixtureConstants.ACCOUNT_1503307, CloudCheckrFixtureConstants.DEPT1_COSTCENTER4_COST),
    DEPT_2_COST_CENTER_1(CloudCheckrFixtureConstants.ACCOUNT_NONE, CloudCheckrFixtureConstants.DEPT2_COSTCENTER1_COST),
    DEPT_3_COST_CENTER_1(CloudCheckrFixtureConstants.ACCOUNT_NONE, CloudCheckrFixtureConstants.DEPT3_COSTCENTER1_COST),
    CLOUDCHECKR_EXAMPLE_COST_CENTER_MIKEB(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_NAME_CREATE_BY, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VALUE_IAM_MIKEB, StringUtils.EMPTY, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_MIKEB_COST, 0),
    CLOUDCHECKR_EXAMPLE_COST_CENTER_VANW(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_NAME_CREATE_BY, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VALUE_IAM_VANW, StringUtils.EMPTY, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VANW_COST, 0),
    
    DEPT1_ACCOUNT_GROUP(CloudCheckrFixtureConstants.DEPARTMENT_1_GROUP_VALUE, CloudCheckrFixtureConstants.DEPARTMENT_1_FRIENDLY_NAME, 
            CloudCheckrFixtureConstants.DEPARTMENT_1_COST,
            groupLevelFixtureArrayBuilder(DEPT_1_COST_CENTER_1, DEPT_1_COST_CENTER_2, 
                    DEPT_1_COST_CENTER_3, DEPT_1_COST_CENTER_4, DEPT_1_COST_CENTER_5)),
    DEPT2_ACCOUNT_GROUP(CloudCheckrFixtureConstants.DEPARTMENT_2_GROUP_VALUE, CloudCheckrFixtureConstants.DEPARTMENT_2_FRIENDLY_NAME, 
            CloudCheckrFixtureConstants.DEPARTMENT_2_COST,  groupLevelFixtureArrayBuilder(DEPT_2_COST_CENTER_1)),
    DEPT3_ACCOUNT_GROUP(CloudCheckrFixtureConstants.DEPARTMENT_3_GROUP_VALUE, CloudCheckrFixtureConstants.DEPARTMENT_3_FRIENDLY_NAME, 
            CloudCheckrFixtureConstants.DEPARTMENT_3_COST, groupLevelFixtureArrayBuilder(DEPT_3_COST_CENTER_1)),
    CLOUDCHECKR_GROUP_1(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_NAME_SERVICE, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VALUE_S3, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_FRIENDLY_NAME_S3,
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_S3_COST, 0,
            groupLevelFixtureArrayBuilder(CLOUDCHECKR_EXAMPLE_COST_CENTER_MIKEB)),
    CLOUDCHECKR_GROUP_2(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_NAME_SERVICE, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VALUE_EC2, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_FRIENDLY_NAME_EC2,
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_EC2_COST, 0,
            groupLevelFixtureArrayBuilder(CLOUDCHECKR_EXAMPLE_COST_CENTER_VANW)),


    ACCT_NONE_COST_1(CloudCheckrFixtureConstants.ACCOUNT_NONE, 1.00),
    ACCT_165833X_COST_2(CloudCheckrFixtureConstants.ACCOUNT_165833X, 2.00),
    ACCT_165833X_COST_3(CloudCheckrFixtureConstants.ACCOUNT_165833X, 3.00),
    ACCT_R583805_70710_COST_4(CloudCheckrFixtureConstants.ACCOUNT_STRING_R583805_70170, 4.00),
    ACCT_R583805_533X_COST_5(CloudCheckrFixtureConstants.ACCOUNT_STRING_R583805_533X, 5.00),
    ACCT_R589966_STAR_VALID_COST_6(CloudCheckrFixtureConstants.ACCOUNT_STRING_IT_R589966_NONCA_1000, 6.00),
    ACCT_1023715_STAR_VALID_COST_7(CloudCheckrFixtureConstants.ACCOUNT_STRING_IT_1023715_97601_4020, 7.00),
    ACCT_1023715_STAR_INVALID_SUB_OBJ_COST_8(CloudCheckrFixtureConstants.ACCOUNT_STRING_IT_1023715_97601_4020_10X, 8.00),
    ACCT_R589966_STAR_PART_VALID_COST_9(CloudCheckrFixtureConstants.ACCOUNT_STRING_IT_R589966_NONCX_1000, 9.00),
    ACCT_J801000_COST_10(CloudCheckrFixtureConstants.ACCOUNT_STRING_CS_J801000_SHAN_6600, 10.00),
    ACCT_J80100X_COST_11(CloudCheckrFixtureConstants.ACCOUNT_J80100X, 11.00),
    ACCT_NONE_COST_12(CloudCheckrFixtureConstants.ACCOUNT_NONE, 12.00),
    CHART_CS_ACCT_J80100_COST_50(CloudCheckrFixtureConstants.ACCOUNT_STRING_CS_J801000, 50.00),
    ACCT_1023715_COST_13(CloudCheckrFixtureConstants.ACCOUNT_STRING_IT_1023715_97601_4020_109, 13);

    public final String groupName;
    public final String groupValue;
    public final String friendlyName;
    public final KualiDecimal cost;
    public final Double usageQuantity;
    public final List<GroupLevelFixture> nextLevel;
    
    private GroupLevelFixture(String groupName, String groupValue, String friendlyName, double cost, double usageQuantity, 
            GroupLevelFixture[] nextLevel) {
        this.groupName = groupName;
        this.groupValue = groupValue;
        this.friendlyName = friendlyName;
        this.cost = new KualiDecimal(cost);
        this.usageQuantity = new Double(usageQuantity);
        this.nextLevel = XmlDocumentFixtureUtils.toImmutableList(nextLevel);
    }
    
    private GroupLevelFixture(String groupValue, String friendlyName, double cost, GroupLevelFixture[] nextLevel) {
        this(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_AWS_ACCOUNT, groupValue, friendlyName, cost, 0, nextLevel);
    }
    
    private GroupLevelFixture(String groupName, String groupValue, String friendlyName, double cost, double usageQuantity) {
        this(groupName, groupValue, friendlyName, cost, usageQuantity, new GroupLevelFixture[0]);
    }
    
    private GroupLevelFixture(String groupValue, double cost) {
        this(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, groupValue, StringUtils.EMPTY, cost, 0);
    }
    
    public GroupLevel toGroupLevel() {
        GroupLevel gl = new GroupLevel();
        gl.setCost(cost);
        gl.setFriendlyName(friendlyName);
        gl.setGroupName(groupName);
        gl.setGroupValue(groupValue);
        gl.setUsageQuantity(usageQuantity);
        nextLevel.stream().forEach(level -> gl.getNextLevel().add(level.toGroupLevel()));
        return gl;
    }
    
    private static GroupLevelFixture[] groupLevelFixtureArrayBuilder(GroupLevelFixture...fixtures) {
        return fixtures;
    }

}
