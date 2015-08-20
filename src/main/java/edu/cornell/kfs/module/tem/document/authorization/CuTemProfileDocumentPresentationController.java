/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2015 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.module.tem.document.authorization;

import java.util.Set;

import org.kuali.kfs.module.tem.TemPropertyConstants;
import org.kuali.kfs.module.tem.document.authorization.TemProfileDocumentPresentationController;
import org.kuali.rice.kns.document.MaintenanceDocument;

@SuppressWarnings("deprecation")
public class CuTemProfileDocumentPresentationController extends TemProfileDocumentPresentationController {

  private static final long serialVersionUID = 1L;

    @Override
    public Set<String> getConditionallyReadOnlyPropertyNames(MaintenanceDocument document) {
        Set<String> readOnlyPropertyNames = super.getConditionallyReadOnlyPropertyNames(document);

        // KFSPTS-4421 wants us to make the country code editable.
        if(readOnlyPropertyNames.contains(TemPropertyConstants.TemProfileProperties.COUNTRY_CODE)){
            readOnlyPropertyNames.remove(TemPropertyConstants.TemProfileProperties.COUNTRY_CODE);
        }

        return readOnlyPropertyNames;
    }

}