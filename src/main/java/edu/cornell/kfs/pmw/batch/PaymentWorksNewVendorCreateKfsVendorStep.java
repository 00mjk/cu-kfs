package edu.cornell.kfs.pmw.batch;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsService;

public class PaymentWorksNewVendorCreateKfsVendorStep extends AbstractStep {
    
	private static final Logger LOG = LogManager.getLogger(PaymentWorksNewVendorCreateKfsVendorStep.class);
    protected PaymentWorksNewVendorRequestsService paymentWorksNewVendorRequestsService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected PaymentWorksFormModeService paymentWorksFormModeService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("execute, PMW form mode is: " + paymentWorksFormModeService.getFormModeDescription());
        if (getPaymentWorksBatchUtilityService().isPaymentWorksIntegrationProcessingEnabled()) {
            getPaymentWorksNewVendorRequestsService().createKfsVendorsFromPmwNewVendorRequests();
        } else {
            LOG.info("execute: KFS System Parameter '" + PaymentWorksParameterConstants.PMW_INTEGRATION_IS_ACTIVE_IND
                     + "' is NOT active. The value of this KFS System parameter must be changed to turn on the batch jobs integration to PaymentWorks.");
        }
        return true;
    }

    public PaymentWorksNewVendorRequestsService getPaymentWorksNewVendorRequestsService() {
        return paymentWorksNewVendorRequestsService;
    }

    public void setPaymentWorksNewVendorRequestsService(PaymentWorksNewVendorRequestsService paymentWorksNewVendorRequestsService) {
        this.paymentWorksNewVendorRequestsService = paymentWorksNewVendorRequestsService;
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksFormModeService(PaymentWorksFormModeService paymentWorksFormModeService) {
        this.paymentWorksFormModeService = paymentWorksFormModeService;
    }

}
