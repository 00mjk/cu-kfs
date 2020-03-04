package edu.cornell.kfs.pdp.batch.service.impl;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;

public class PdpMockServiceFactory {

    public EmailService buildMockEmailService() throws Exception {
        return Mockito.mock(EmailService.class);
    }

    public ParameterService buildMockParameterService() throws Exception {
        return new MockParameterServiceImpl();
    }

    public ConfigurationService buildMockConfigurationService() throws Exception {
        return Mockito.mock(ConfigurationService.class);
    }

}
