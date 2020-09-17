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
package org.kuali.kfs.fp.document.dataaccess.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.fp.document.GeneralLedgerTransferDocument;
import org.kuali.kfs.fp.document.dataaccess.GeneralLedgerTransferDocumentDao;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.ArrayList;
import java.util.List;

public class GeneralLedgerTransferDocumentDaoOjb extends PlatformAwareDaoBaseOjb implements
        GeneralLedgerTransferDocumentDao {

    @Override
    public List<GeneralLedgerTransferDocument> findDocumentsByNullBatchProcessedDateAndApproved() {
        Criteria criteria = new Criteria();
        criteria.addIsNull(KFSPropertyConstants.BATCH_PROCESSED_DATE);
        criteria.addEqualTo(KFSPropertyConstants.DOCUMENT_HEADER + "." +
            KFSPropertyConstants.FINANCIAL_DOCUMENT_STATUS_CODE, KFSConstants.DocumentStatusCodes.APPROVED);

        QueryByCriteria queryByCriteria = QueryFactory.newQuery(GeneralLedgerTransferDocument.class, criteria);
        return new ArrayList<GeneralLedgerTransferDocument>(getPersistenceBrokerTemplate().getCollectionByQuery(queryByCriteria));
    }
}
