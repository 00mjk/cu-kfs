package edu.cornell.kfs.pdp.batch.service;

import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReport;

public interface PayeeACHAccountExtractReportService {

    void writeBatchJobReports(PayeeACHAccountExtractReport achReport);

}
