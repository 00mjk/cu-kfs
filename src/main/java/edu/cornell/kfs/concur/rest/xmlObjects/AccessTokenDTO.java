package edu.cornell.kfs.concur.rest.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Access_Token")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccessTokenDTO {

    @XmlElement(name = "Instance_Url")
    protected String instanceURL;
    @XmlElement(name = "Token")
    protected String token;
    @XmlElement(name = "Expiration_date")
    protected String expirationDate;
    @XmlElement(name = "Refresh_Token")
    protected String refreshToken;
    
    public String getInstanceURL() {
        return instanceURL;
    }
    public void setInstanceURL(String instanceURL) {
        this.instanceURL = instanceURL;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
