package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformAuthenticationRequest.RequestInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

public class PlatformAuthenticationRequestTest {
    private PlatformAuthenticationRequest platformAuthenticationRequest = new PlatformAuthenticationRequest("sender");
    private PlatformAuthenticationRequest.RequestInfo requestInfo = new RequestInfo("sender");

    public PlatformAuthenticationRequest setTestValues(CertificateType certificateType, String certificate, String applicantData, String sign) {
        PlatformAuthenticationRequest expected = new PlatformAuthenticationRequest("sender") {
            {
                setCertificateType(certificateType);
                setCertificate(certificate);
                setApplicantData(applicantData);
                setSign(sign);
            }
        };
        return expected;
    }

    @Test
    public void testGetCertificateType() throws Exception {
        Field field = platformAuthenticationRequest.getClass().getDeclaredField("certificateType");
        field.setAccessible(true);
        field.set(platformAuthenticationRequest, CertificateType.ENCRYPTED_USER_AUTHENTICATION);
        assertEquals(CertificateType.ENCRYPTED_USER_AUTHENTICATION, platformAuthenticationRequest.getCertificateType());
    }

    @Test
    public void testGetCertificate() throws Exception {
        Field field = platformAuthenticationRequest.getClass().getDeclaredField("certificate");
        field.setAccessible(true);
        field.set(platformAuthenticationRequest, "certificate");
        assertEquals("certificate", platformAuthenticationRequest.getCertificate());
    }

    @Test
    public void testGetApplicantData() throws Exception {
        Field field = platformAuthenticationRequest.getClass().getDeclaredField("applicantData");
        field.setAccessible(true);
        field.set(platformAuthenticationRequest, "applicantData");
        assertEquals("applicantData", platformAuthenticationRequest.getApplicantData());
    }

    @Test
    public void testGetNonceData() throws Exception {
        Field field = platformAuthenticationRequest.getClass().getDeclaredField("nonceData");
        field.setAccessible(true);
        field.set(platformAuthenticationRequest, "nonceData");
        assertEquals("nonceData", platformAuthenticationRequest.getNonceData());
    }

    @Test
    public void testGetSign() throws Exception {
        Field field = platformAuthenticationRequest.getClass().getDeclaredField("sign");
        field.setAccessible(true);
        field.set(platformAuthenticationRequest, "sign");
        assertEquals("sign", platformAuthenticationRequest.getSign());
    }

    @Test
    public void testGetNonceSign() throws Exception {
        Field field = platformAuthenticationRequest.getClass().getDeclaredField("nonceSign");
        field.setAccessible(true);
        field.set(platformAuthenticationRequest, "nonceSign");
        assertEquals("nonceSign", platformAuthenticationRequest.getNonceSign());
    }

    @Test
    public void testGetRequestInfo() {
        assertNotNull(platformAuthenticationRequest.getRequestInfo());
    }

    @Test
    public void testSetCertificateType() {
        PlatformAuthenticationRequest expected = setTestValues(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE, null, null, null);
        PlatformAuthenticationRequest result = platformAuthenticationRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getSign(), result.getSign());
    }

    @Test
    public void testSetCertificate() {
        PlatformAuthenticationRequest expected = setTestValues(null, "certificate", null, null);
        PlatformAuthenticationRequest result = platformAuthenticationRequest.setCertificate("certificate");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getSign(), result.getSign());
    }

    @Test
    public void testSetApplicantData() {
        PlatformAuthenticationRequest expected = setTestValues(null, null, "applicantData", null);
        PlatformAuthenticationRequest result = platformAuthenticationRequest.setApplicantData("applicantData");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getSign(), result.getSign());
    }

    @Test
    public void testSetSign() {
        PlatformAuthenticationRequest expected = setTestValues(null, null, null, "sign");
        PlatformAuthenticationRequest result = platformAuthenticationRequest.setSign("sign");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getSign(), result.getSign());
    }

    @Test
    public void testSetRequestInfo() throws Exception {
        platformAuthenticationRequest.setRequestInfo(requestInfo);
        assertEquals(requestInfo, platformAuthenticationRequest.getRequestInfo());
    }

    @Test
    public void testGetTransactionId() throws Exception {
        Field field = requestInfo.getClass().getDeclaredField("transactionId");
        field.setAccessible(true);
        field.set(requestInfo, "c610e161-90ce-4a31-ab84-9429dd484e83");
        assertEquals("c610e161-90ce-4a31-ab84-9429dd484e83", requestInfo.getTransactionId());
    }

    @Test
    public void testGetRecipient() throws Exception {
        Field field = requestInfo.getClass().getDeclaredField("recipient");
        field.setAccessible(true);
        field.set(requestInfo, "recipient");
        assertEquals("recipient", requestInfo.getRecipient());
    }

    @Test
    public void testGetSender() throws Exception {
        Field field = requestInfo.getClass().getDeclaredField("sender");
        field.setAccessible(true);
        field.set(requestInfo, "sender");
        assertEquals("sender", requestInfo.getSender());
    }

    @Test
    public void testGetTimeStamp() throws Exception {
        Field field = requestInfo.getClass().getDeclaredField("timeStamp");
        field.setAccessible(true);
        field.set(requestInfo, "2023-09-13 11:11:11.123");
        assertEquals("2023-09-13 11:11:11.123", requestInfo.getTimeStamp());
    }

    @Test
    public void testSetSender() {
        requestInfo.setSender("sender");
        assertEquals("sender", requestInfo.getSender());
    }
}
