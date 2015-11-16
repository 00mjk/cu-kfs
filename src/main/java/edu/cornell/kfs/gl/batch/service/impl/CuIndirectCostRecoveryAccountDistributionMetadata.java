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
package edu.cornell.kfs.gl.batch.service.impl;

import java.text.MessageFormat;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.gl.batch.service.impl.IndirectCostRecoveryAccountDistributionMetadata;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuIndirectCostRecoveryAccountDistributionMetadata extends IndirectCostRecoveryAccountDistributionMetadata {

    /**
     * @param icrAccount
     * @throws Exception
     */
    public CuIndirectCostRecoveryAccountDistributionMetadata(IndirectCostRecoveryAccount icrAccount) throws Exception {
      super(icrAccount);

      Account a = icrAccount.getIndirectCostRecoveryAccount();
      int i = 0;

      while(i < 10) {
        if((a != null) && a.isClosed()) {
          a = a.getContinuationAccount();
          i++;
        } else {
          break;
        }
      }

      // If the ICR Account is still closed after searching this far down, raise an Exception.
      if((a != null) && a.isClosed()) {
        String errorText = SpringContext.getBean(ConfigurationService.class)
                                        .getPropertyValueAsString(CUKFSKeyConstants.ERROR_ICRACCOUNT_CONTINUATION_ACCOUNT_CLOSED);
        Object[] args = {
            icrAccount.getIndirectCostRecoveryFinCoaCode(),
            icrAccount.getIndirectCostRecoveryAccountNumber()
          };
        errorText = MessageFormat.format(errorText, args);

        throw new UnsupportedOperationException(errorText);
      }

      this.setIndirectCostRecoveryFinCoaCode(a.getContinuationFinChrtOfAcctCd());
      this.setIndirectCostRecoveryAccountNumber(a.getContinuationAccountNumber());
    }

}
