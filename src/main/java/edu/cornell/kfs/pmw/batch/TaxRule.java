package edu.cornell.kfs.pmw.batch;

import org.apache.commons.lang3.StringUtils;

public enum TaxRule {
    INDIVIDUAL_US_SSN(PaymentWorksConstants.PaymentWorksTinType.SSN.getKfsTaxTypeCodeAsString(),
            PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode(), true, true, false), 
    INDIVIDUAL_US_EIN(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(),
            PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode(), true, false, true), 
    NOT_INDIVIDUAL_US(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(), StringUtils.EMPTY, true, false, true),
    FOREIGN_INDIVIDUAL(StringUtils.EMPTY, StringUtils.EMPTY, false, false, false),
    FOREIGN_ENTITY(StringUtils.EMPTY, StringUtils.EMPTY, false, false, false),
    OTHER(StringUtils.EMPTY, StringUtils.EMPTY, false, false, false);

    public final String taxTypeCode;
    public final String ownershipTypeCode;
    public final boolean populateW9Attributes;
    public final boolean populateFirstLastLegalName;
    public final boolean populateBusinessLegalName;

    private TaxRule(String taxTypeCode, String ownershipTypeCode, boolean populateW9Attributes, boolean populateFirstLastLegalName, boolean populateBusinessLegalName) {
        this.taxTypeCode = taxTypeCode;
        this.ownershipTypeCode = ownershipTypeCode;
        this.populateW9Attributes = populateW9Attributes;
        this.populateFirstLastLegalName = populateFirstLastLegalName;
        this.populateBusinessLegalName = populateBusinessLegalName;
    }
    

}
