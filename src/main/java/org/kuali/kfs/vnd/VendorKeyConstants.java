/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.vnd;

/**
 * Holds error key constants for Vendor.
 */
public class VendorKeyConstants {

    // Vendor Maintenance
    public static final String ERROR_VENDOR_TYPE_REQUIRES_TAX_NUMBER = "error.vendorMaint.VendorTypeRequiresTaxNumber";
    public static final String ERROR_VENDOR_TAX_TYPE_AND_NUMBER_COMBO_EXISTS = "error.vendorMaint.addVendor.vendor.exists";
    public static final String ERROR_VENDOR_NAME_REQUIRED = "error.vendorMaint.vendorName.required";
    public static final String ERROR_VENDOR_BOTH_NAME_REQUIRED = "error.vendorMaint.bothNameRequired";
    public static final String ERROR_VENDOR_NAME_INVALID = "error.vendorMaint.nameInvalid";
    public static final String VENDOR_SOLD_TO_NUMBER_INVALID = "error.vendorSoldToNumber.invalid";
    public static final String ERROR_VENDOR_MAX_MIN_ORDER_AMOUNT = "error.vendorMaint.minimumAmt.invalid";
    public static final String ERROR_VENDOR_TAX_TYPE_CANNOT_BE_BLANK = "error.vendorMaint.tax.type.cannot.be.blank";
    public static final String ERROR_VENDOR_TAX_TYPE_CANNOT_BE_SET = "error.vendorMaint.tax.type.cannot.be.set";
    public static final String ERROR_VENDOR_PARENT_NEEDS_CHANGED = "error.vendorMaint.vendorParent.needs.changed";
    public static final String ERROR_TAX_NUMBER_INVALID = "error.vendorMaint.taxNumber.invalid";
    public static final String ERROR_TAX_NUMBER_NOT_ALLOWED = "error.vendorMaint.taxNumber.notAllowed";
    public static final String ERROR_OWNERSHIP_CATEGORY_CODE_NOT_ALLOWED = "error.vendorMaint.ownershipCategoryCode.notAllowed";
    public static final String ERROR_OWNERSHIP_TYPE_CODE_NOT_ALLOWED = "error.vendorMaint.ownershipTypeCode.notAllowed";
    public static final String ERROR_INACTIVE_REASON_REQUIRED = "error.vendorMaint.inactiveReason.required";
    public static final String ERROR_INACTIVE_REASON_NOT_ALLOWED = "error.vendorMaint.inactiveReason.notAllowed";
    public static final String ERROR_RESTRICTED_REASON_REQUIRED = "error.vendorMaint.restrictedReason.required";
    public static final String ERROR_VENDOR_TAX_BEGIN_DATE_AFTER_END = "error.vendorTax.beginDateAfterEnd";
    public static final String ERROR_VENDOR_W9_AND_W8_RECEIVED_INDICATOR_BOTH_TRUE = "error.vendor.w9Andw8.receivedIndicator.BothTrue";
    public static final String MESSAGE_VENDOR_PARENT_TO_DIVISION = "message.vendorMaint.parent.to.division";
    public static final String CONFIRM_VENDOR_CHANGE_TO_PARENT = "message.vendorMaint.confirm.change.to.parent";
    
    // Vendor Lookup
    public static final String ERROR_VENDOR_LOOKUP_NAME_TOO_SHORT = "error.vendorLookup.name.too.short";
    public static final String ERROR_VENDOR_LOOKUP_TAX_NUM_INVALID = "error.vendorLookup.taxNum.invalid";
    public static final String ERROR_VENDOR_LOOKUP_VNDR_NUM_TOO_MANY_DASHES = "error.vendorLookup.vndrNum.dashes.tooMany";
    public static final String ERROR_VENDOR_LOOKUP_VNDR_NUM_DASHES_ONLY = "error.vendorLookup.vndrNum.dashes.only";
    public static final String ERROR_VENDOR_LOOKUP_VNDR_NUM_NUMERIC_DASH_SEPARATED = "error.vendorLookup.vndrNum.numeric.dash.separated";

    // Vendor Maintenance Address
    public static final String ERROR_US_REQUIRES_STATE = "error.vendorMaint.vendorAddress.USRequiresStateAndZip";
    public static final String ERROR_US_REQUIRES_ZIP = "error.vendorMaint.vendorAddress.USRequiresStateAndZip";
    public static final String ERROR_POSTAL_CODE_INVALID = "error.vendorMaint.vendorAddress.postalCode.invalid";
    public static final String ERROR_FAX_NUMBER = "error.vendorMaint.vendorAddress.faxNumber";
    public static final String ERROR_ADDRESS_TYPE = "error.vendorMaint.vendorAddress.addressType";
    public static final String ERROR_ADDRESS_TYPE_DIVISIONS = "error.vendorMaint.vendorAddress.addressType.divisions";
    public static final String ERROR_ADDRESS_DEFAULT_CAMPUS = "error.vendorMaint.vendorDefaultAddress.campusCode";
    public static final String ERROR_ADDRESS_DEFAULT_INDICATOR = "error.vendorMaint.vendorAddress.vendorDefaultAddressIndicator";
    public static final String ERROR_ADDRESS_DEFAULT_CAMPUS_NOT_ALLOWED = "error.vendorMaint.vendorDefaultCampus.notAllowed";
    public static final String ERROR_ADDRESS_DEFAULT_ADDRESS_NOT_ALLOWED = "error.vendorMaint.vendorDefaultAddress.notAllowed";


    // Vendor Maintenance Contract
    public static final String ERROR_VENDOR_CONTRACT_NO_APO_LIMIT = "error.vendorContract.noApoLimit";
    public static final String ERROR_VENDOR_CONTRACT_BEGIN_DATE_AFTER_END = "error.vendorContract.beginDateAfterEnd";
    public static final String ERROR_VENDOR_CONTRACT_BEGIN_DATE_NO_END_DATE = "error.vendorContract.beginDateNoEndDate";
    public static final String ERROR_VENDOR_CONTRACT_END_DATE_NO_BEGIN_DATE = "error.vendorContract.endDateNoBeginDate";
    public static final String ERROR_VENDOR_CONTRACT_ORG_EXCLUDED_WITH_APO_LIMIT = "error.vendorContractOrg.excludedWithApoLimit";
    public static final String ERROR_VENDOR_CONTRACT_ORG_NOT_EXCLUDED_NO_APO_LIMIT = "error.vendorContractOrg.notExcludedNoApoLimit";
    public static final String ERROR_VENDOR_CONTRACT_NOT_ALLOWED = "error.vendorContract.notAllowed";

    // Vendor Maintenance Commodity Code
    public static final String ERROR_VENDOR_COMMODITY_CODE_REQUIRE_ONE_DEFAULT_IND = "error.vendorCommodityCode.require.one.defaultIndicator";
    public static final String ERROR_VENDOR_COMMODITY_CODE_IS_REQUIRED_FOR_THIS_VENDOR_TYPE = "error.vendorCommodityCode.is.required.for.vendorType";
 
    //Vendor Maintenance W9 Received Date
    public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_NOT_POPULATED = "error.document.vendor.w9ReceivedNotPopulatedButIndicatorIsTrue";
    public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_POPULATED_W_O_IND = "error.document.vendor.w9ReceivedPopulatedButIndicatorIsNull";
    
    //Vendor Maintenance Supplier Diversity Expiration Date
    public static final String ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_IN_PAST = "error.document.vendor.supplierDiversityExpirationDateIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_BLANK = "error.document.vendor.supplierDiversityExpirationDateCannotBeBlank";
    
    //Vendor Maintenance Insurance Tracking
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_EXPR_DATE_NEEDED = "error.document.vendor.generalLiabilityExpirationDateNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_COVERAGE_NEEDED = "error.document.vendor.generalLiabilityCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_EXPR_NEEDED = "error.document.vendor.automobileExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_COVERAGE_NEEDED = "error.document.vendor.automobileCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_EXPR_NEEDED = "error.document.vendor.workmansCompExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_COVERAGE_NEEDED = "error.document.vendor.workmansCompCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_EXPR_NEEDED = "error.document.vendor.umbrellaExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_COVERAGE_NEEDED = "error.document.vendor.umbrellaCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_EXPR_NEEDED = "error.document.vendor.healthLicenseExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_NEEDED = "error.document.vendor.healthLicenseNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_INSURANCE_REQUIRED_USED_WO_DATA = "error.document.vendor.insuranceReqIndicatedWOData";
    
    //Vendor Maintenance Credit Merchant
    
    public static final String ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_DUPLICATE = "error.document.vendor.creditMerchantNameDuplicate";
    public static final String ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_BLANK = "error.document.vendor.creditMerchantNameBlank";
    public static final String ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST = "error.document.vendor.dateInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_DATE_IN_FUTURE = "error.document.vendor.dateInFuture";
}
