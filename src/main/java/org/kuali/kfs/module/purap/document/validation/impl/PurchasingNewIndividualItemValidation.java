/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.integration.cam.CapitalAssetManagementModuleService;
import org.kuali.kfs.kns.service.BusinessObjectDictionaryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

public class PurchasingNewIndividualItemValidation extends PurchasingAccountsPayableNewIndividualItemValidation {

    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private BusinessObjectService businessObjectService;
    private CapitalAssetManagementModuleService capitalAssetManagementModuleService;
    private PurchasingUnitOfMeasureValidation unitOfMeasureValidation;
    private PurchasingItemUnitPriceValidation itemUnitPriceValidation;
    private PurchasingItemDescriptionValidation itemDescriptionValidation;
    private PurchasingItemQuantityValidation itemQuantityValidation;
    private PurchasingBelowTheLineItemNoUnitCostValidation belowTheLineItemNoUnitCostValidation;
    private PurchasingCommodityCodeValidation commodityCodeValidation;
                                              
    public static final String UNORDERED_ITEM_DEFAULT_COMMODITY_CODE = "UNORDERED_ITEM_DEFAULT_COMMODITY_CODE";
    
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = super.validate(event);
        String recurringPaymentTypeCode = ((PurchasingDocument) event.getDocument()).getRecurringPaymentTypeCode();
        //Capital asset validations are only done on line items (not additional charge items).
        if (!getItemForValidation().getItemType().isAdditionalChargeIndicator()) {
            valid &= capitalAssetManagementModuleService.validateItemCapitalAssetWithErrors(recurringPaymentTypeCode,
                    getItemForValidation(), false);
        }
        unitOfMeasureValidation.setItemForValidation(getItemForValidation());
        valid &= unitOfMeasureValidation.validate(event);

        if (getItemForValidation().getItemType().isLineItemIndicator()) {
            itemUnitPriceValidation.setItemForValidation(getItemForValidation());
            valid &= itemUnitPriceValidation.validate(event);

            itemDescriptionValidation.setItemForValidation(getItemForValidation());
            valid &= itemDescriptionValidation.validate(event);

            itemQuantityValidation.setItemForValidation(getItemForValidation());
            valid &= itemQuantityValidation.validate(event);
            
            commodityCodeValidation.setItemForValidation(getItemForValidation());
            valid &= commodityCodeValidation.validate(event);    
        }
        else {
            // No accounts can be entered on below-the-line items that have no unit cost.
            belowTheLineItemNoUnitCostValidation.setItemForValidation(getItemForValidation());
            valid &= belowTheLineItemNoUnitCostValidation.validate(event);                        
        }
        return valid;
    }
    

    /**
     * Validates whether the commodity code existed on the item, and if existed, whether the commodity code on the
     * item existed in the database, and if so, whether the commodity code is active. Display error if any of these 3
     * conditions are not met.
     *
     * @param item The PurApItem containing the commodity code to be validated.
     * @return boolean false if the validation fails and true otherwise.
     */
    
    public void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }
            
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setCapitalAssetManagementModuleService(
            CapitalAssetManagementModuleService capitalAssetManagementModuleService) {
        this.capitalAssetManagementModuleService = capitalAssetManagementModuleService;
    }

    public void setUnitOfMeasureValidation(PurchasingUnitOfMeasureValidation unitOfMeasureValidation) {
        this.unitOfMeasureValidation = unitOfMeasureValidation;
    }

    public void setItemUnitPriceValidation(PurchasingItemUnitPriceValidation itemUnitPriceValidation) {
        this.itemUnitPriceValidation = itemUnitPriceValidation;
    }

    public void setItemDescriptionValidation(PurchasingItemDescriptionValidation itemDescriptionValidation) {
        this.itemDescriptionValidation = itemDescriptionValidation;
    }

    public void setItemQuantityValidation(PurchasingItemQuantityValidation itemQuantityValidation) {
        this.itemQuantityValidation = itemQuantityValidation;
    }

    public void setBelowTheLineItemNoUnitCostValidation(
            PurchasingBelowTheLineItemNoUnitCostValidation belowTheLineItemNoUnitCostValidation) {
        this.belowTheLineItemNoUnitCostValidation = belowTheLineItemNoUnitCostValidation;
    }
    
    public PurchasingCommodityCodeValidation getCommodityCodeValidation() {
               return commodityCodeValidation;
     }
    public void setCommodityCodeValidation(PurchasingCommodityCodeValidation commodityCodeValidation) {
                 this.commodityCodeValidation = commodityCodeValidation;
     }
    

}
