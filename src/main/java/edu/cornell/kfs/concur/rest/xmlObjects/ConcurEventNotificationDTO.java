package edu.cornell.kfs.concur.rest.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Notification")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConcurEventNotificationDTO {
    @XmlElement(name = "Context")
    private String context;
    @XmlElement(name = "EventDateTime")
    private String eventDateTime;
    @XmlElement(name = "EventType")
    private String eventType;
    @XmlElement(name = "ObjectType")
    private String objectType;
    @XmlElement(name = "ObjectURI")
    private String objectURI;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(String eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectURI() {
        return objectURI;
    }

    public void setObjectURI(String objectURI) {
        this.objectURI = objectURI;
    }
}
