package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformAuthenticationRequest.RequestInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PlatformAuthenticationRequestTest {
    private PlatformAuthenticationRequest platformAuthenticationRequest = new PlatformAuthenticationRequest("ID123");
    private PlatformAuthenticationRequest.RequestInfo requestInfo = new RequestInfo("ID123");

    @Test
    public void getCertificateType() {
        assertNull(platformAuthenticationRequest.getCertificateType());
    }

    @Test
    public void getCertificate() {
        assertNull(platformAuthenticationRequest.getCertificate());
    }

    @Test
    public void getApplicantData() {
        assertNull(platformAuthenticationRequest.getApplicantData());
    }

    @Test
    public void getSign() {
        assertNull(platformAuthenticationRequest.getSign());
    }

    @Test
    public void getRequestInfo() {
        assertNotNull(platformAuthenticationRequest.getRequestInfo());
    }

    @Test
    public void setCertificateType() {
        platformAuthenticationRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
        assertEquals(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE, platformAuthenticationRequest.getCertificateType());
        assertEquals("encryptedDigitalSignatureCertificate", platformAuthenticationRequest.getCertificateType().getName());
        platformAuthenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
        assertEquals(CertificateType.ENCRYPTED_USER_AUTHENTICATION, platformAuthenticationRequest.getCertificateType());
        assertEquals("encryptedUserAuthenticationCertificate", platformAuthenticationRequest.getCertificateType().getName());
    }

    @Test
    public void setCertificate() {
        platformAuthenticationRequest.setCertificate(null);
        assertNull(platformAuthenticationRequest.getCertificate());
        platformAuthenticationRequest.setCertificate("");
        assertEquals("", platformAuthenticationRequest.getCertificate());
        platformAuthenticationRequest.setCertificate("abc123");
        assertEquals("abc123", platformAuthenticationRequest.getCertificate());
    }

    @Test
    public void setApplicantData() {
        platformAuthenticationRequest.setApplicantData(null);
        assertNull(platformAuthenticationRequest.getApplicantData());
        platformAuthenticationRequest.setApplicantData("");
        assertEquals("", platformAuthenticationRequest.getApplicantData());
        platformAuthenticationRequest.setApplicantData("abc123");
        assertEquals("abc123", platformAuthenticationRequest.getApplicantData());
    }

    @Test
    public void setSign() {
        platformAuthenticationRequest.setSign(null);
        assertNull(platformAuthenticationRequest.getSign());
        platformAuthenticationRequest.setSign("");
        assertEquals("", platformAuthenticationRequest.getSign());
        platformAuthenticationRequest.setSign("abc123");
        assertEquals("abc123", platformAuthenticationRequest.getSign());
    }

    @Test
    public void setRequestInfo() {
        platformAuthenticationRequest.setRequestInfo(requestInfo);
        assertNotNull(platformAuthenticationRequest.getRequestInfo());
    }

    @Test
    public void getTransactionId() {
        assertNotNull(requestInfo.getTransactionId());
    }

    @Test
    public void getRecipient() {
        assertNotNull(requestInfo.getRecipient());
    }

    @Test
    public void getSender() {
        assertNotNull(requestInfo.getSender());
    }

    @Test
    public void getTimeStamp() {
        assertNotNull(requestInfo.getTimeStamp());
    }

    @Test
    public void setSender() {
        requestInfo.setSender("ID123");
        assertNotNull(requestInfo.getSender());
    }
}
