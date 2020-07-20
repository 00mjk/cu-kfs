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
package org.kuali.kfs.sys.document.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.GeneralLedgerTransferDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemDocument;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocument;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.RouteHelper;
import org.kuali.rice.kew.engine.node.SplitNode;
import org.kuali.rice.kew.engine.node.SplitResult;

import java.util.ArrayList;
import java.util.List;

public class SimpleBooleanSplitNode implements SplitNode {
    
    private static final Logger LOG = LogManager.getLogger(SimpleBooleanSplitNode.class);

    /**
     * @see org.kuali.rice.kew.engine.node.SimpleNode#process(org.kuali.rice.kew.engine.RouteContext, org.kuali.rice.kew.engine.RouteHelper)
     */
    @Override
    public SplitResult process(RouteContext context, RouteHelper helper) throws Exception {
        SplitResult result;
        String documentID = context.getDocument().getDocumentId();
        Document document = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(documentID);
        LOG.info("SimpleBooleanSplitNode.process:::  CLASS is =" 
                + (ObjectUtils.isNull(document) ? "null document" 
                                                : (ObjectUtils.isNull(document.getClass()) ? "null document.getClass" 
                                                                                           :  (ObjectUtils.isNull(document.getClass().getName()) ? "null document.getClass().getName()" 
                                                                                                                                                 : document.getClass().getName()))) + "=    ^^^^^^^^^^^^^^^^^^^^^");
        if (document instanceof FinancialSystemDocument) {
            String nodeName = context.getNodeInstance().getRouteNode().getRouteNodeName();
            LOG.info("SimpleBooleanSplitNode.process::: DETECTED FinancialSystemDocument   nodeName=" + (ObjectUtils.isNull(nodeName) ? "NULL node Name" : nodeName) + "=    ^^^^^^^^^^^^^^^^^^^^^^^");
            boolean ret = ((FinancialSystemDocument) document).answerSplitNodeQuestion(nodeName);
            result = booleanToSplitResult(ret);
        } else {
            LOG.info("SimpleBooleanSplitNode.process::: DID NOTTTTTTTTTTTT detect FinancialSystemDocument   ^^^^^^^^^^^^^^^^^^^^^");
            throw new IllegalArgumentException("Document " + document.getDocumentTitle() + " with id " + documentID +
                    " is not an instance of " + FinancialSystemMaintenanceDocument.class + " or " +
                    FinancialSystemTransactionalDocument.class);
        }

        return result;
    }

    /**
     * Converts a boolean value to SplitResult where the branch name is "True" or "False" based on the value of the
     * given boolean
     *
     * @param b a boolean to convert to a SplitResult
     * @return the converted SplitResult
     */
    protected SplitResult booleanToSplitResult(boolean b) {
        List<String> branches = new ArrayList<>();
        final String branchName = b ? "True" : "False";
        branches.add(branchName);
        LOG.info("SimpleBooleanSplitNode.booleanToSplitResult::::::  inputParam=" +b+ "=     branchName" +branchName+ "= *********************************");
        return new SplitResult(branches);
    }

}
