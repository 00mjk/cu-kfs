package edu.cornell.kfs.fp.dataaccess;

import java.sql.Date;

import org.kuali.kfs.fp.document.ProcurementCardDocument;

public interface ProcurementCardDocumentDao {    
    public List<ProcurementCardDocument> getDocumentByCarhdHolderAmountDateVendor(String cardHolder, String amount, Date transactionDate);
}
