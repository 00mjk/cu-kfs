/*
 * Copyright 2008 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.cr.document.service;

import java.sql.Date;
import java.util.List;

import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliationReport;

/**
 * Check Reconciliation Report Service
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public interface CheckReconciliationReportService {

    /**
     * Build Reports
     * 
     * @param startDate
     * @param endDate
     * 
     * @return List
     */
    public List<CheckReconciliationReport> buildReports(Date startDate, Date endDate);
}
