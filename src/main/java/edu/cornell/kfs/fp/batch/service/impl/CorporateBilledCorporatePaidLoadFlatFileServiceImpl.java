package edu.cornell.kfs.fp.batch.service.impl;

import java.util.HashMap;

import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransaction;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionExtendedAttribute;

public class CorporateBilledCorporatePaidLoadFlatFileServiceImpl extends ProcurementCardLoadFlatTransactionsServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidLoadFlatFileServiceImpl.class);
    
    @Override
    public void cleanTransactionsTable() {
        LOG.info("clearning CorporateBilledCorporatePaidTransactionExtendedAttribute and CorporateBilledCorporatePaidTransaction");
        businessObjectService.deleteMatching(CorporateBilledCorporatePaidTransactionExtendedAttribute.class, new HashMap<String, Object>());
        businessObjectService.deleteMatching(CorporateBilledCorporatePaidTransaction.class, new HashMap<String, Object>());
    }
    
    @Override
    public boolean loadProcurementCardFile(String fileName) {
        LOG.info("loadProcurementCardFile, loading: " + fileName);
        return super.loadProcurementCardFile(fileName);
    }

}
