package edu.cornell.kfs.concur.service.impl;

import java.text.ParseException;

import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.rest.xmlObjects.ConcurEventNotificationDTO;
import edu.cornell.kfs.concur.service.ConcurEventNotificationConversionService;

public class ConcurEventNotificationConversionServiceImpl implements ConcurEventNotificationConversionService {
    protected DateTimeService dateTimeService;

    @Override
    public ConcurEventNotification convertConcurEventNotification(ConcurEventNotificationDTO concurEventNotificationDTO) throws ParseException {
        ConcurEventNotification concurEventNotification = new ConcurEventNotification();
        concurEventNotification.setContext(concurEventNotificationDTO.getContext());
        concurEventNotification.setEventDateTime(dateTimeService.convertToSqlDate(concurEventNotificationDTO.getEventDateTime()));
        concurEventNotification.setEventType(concurEventNotificationDTO.getEventType());
        concurEventNotification.setObjectType(concurEventNotificationDTO.getObjectType());
        concurEventNotification.setObjectURI(concurEventNotificationDTO.getObjectURI());
        return concurEventNotification;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
