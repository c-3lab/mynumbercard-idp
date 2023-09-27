package com.example.mynumbercardidp.keycloak.core.network;

import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest.Filed;
import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AuthenticationRequestTest {

    AuthenticationRequest authenticationRequest = new AuthenticationRequest();

    public AuthenticationRequest setTestValues(CertificateType certificateType, String certificate, String applicantData, String nonceData, String sign, String nonceSign, String actionMode) {
        AuthenticationRequest expected = new AuthenticationRequest() {
            {
                setCertificateType(certificateType);
                setCertificate(certificate);
                setApplicantData(applicantData);
                setNonceData(nonceData);
                setSign(sign);
                setNonceSign(nonceSign);
                setActionMode(actionMode);
            }
        };
        return expected;
    }

    @Test
    public void testGetCertificateType() throws Exception {
        Field field = authenticationRequest.getClass().getDeclaredField("certificateType");
        field.setAccessible(true);
        field.set(authenticationRequest, CertificateType.ENCRYPTED_USER_AUTHENTICATION);
        assertEquals(CertificateType.ENCRYPTED_USER_AUTHENTICATION, authenticationRequest.getCertificateType());
    }

    @Test
    public void testGetCertificate() throws Exception  {
        Field field = authenticationRequest.getClass().getDeclaredField("certificate");
        field.setAccessible(true);
        field.set(authenticationRequest, "certificate");
        assertEquals("certificate", authenticationRequest.getCertificate());
    }

    @Test
    public void testGetApplicantData() throws Exception {
        Field field = authenticationRequest.getClass().getDeclaredField("applicantData");
        field.setAccessible(true);
        field.set(authenticationRequest, "applicantData");
        assertEquals("applicantData", authenticationRequest.getApplicantData());
    }

    @Test
    public void testGetNonceData() throws Exception {
        Field field = authenticationRequest.getClass().getDeclaredField("nonceData");
        field.setAccessible(true);
        field.set(authenticationRequest, "nonceData");
        assertEquals("nonceData", authenticationRequest.getNonceData());
    }

    @Test
    public void testGetSign() throws Exception {
        Field field = authenticationRequest.getClass().getDeclaredField("sign");
        field.setAccessible(true);
        field.set(authenticationRequest, "sign");
        assertEquals("sign", authenticationRequest.getSign());
    }

    @Test
    public void testGetNonceSign() throws Exception {
        Field field = authenticationRequest.getClass().getDeclaredField("nonceSign");
        field.setAccessible(true);
        field.set(authenticationRequest, "nonceSign");
        assertEquals("nonceSign", authenticationRequest.getNonceSign());
    }

    @Test
    public void testSetCertificateType() {
        AuthenticationRequest expected = setTestValues(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE, null, null, null, null , null, null);
        AuthenticationRequest result = authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getNonceData(), result.getNonceData());
        assertEquals(expected.getSign(), result.getSign());
        assertEquals(expected.getNonceSign(), result.getNonceSign());
        assertEquals(expected.getActionMode(), result.getActionMode());
    }

    @Test
    public void testSetCertificate() {
        AuthenticationRequest expected = setTestValues(null, "certificate", null, null, null, null, null);
        AuthenticationRequest result = authenticationRequest.setCertificate("certificate");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getNonceData(), result.getNonceData());
        assertEquals(expected.getSign(), result.getSign());
        assertEquals(expected.getNonceSign(), result.getNonceSign());
        assertEquals(expected.getActionMode(), result.getActionMode());
    }

    @Test
    public void testSetApplicantData() {
        AuthenticationRequest expected = setTestValues(null, null, "applicantData", null, null, null, null);
        AuthenticationRequest result = authenticationRequest.setApplicantData("applicantData");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getNonceData(), result.getNonceData());
        assertEquals(expected.getSign(), result.getSign());
        assertEquals(expected.getNonceSign(), result.getNonceSign());
        assertEquals(expected.getActionMode(), result.getActionMode());
    }

    @Test
    public void testSetNonceData() {
        AuthenticationRequest expected = setTestValues(null, null, null, "nonceData", null, null, null);
        AuthenticationRequest result = authenticationRequest.setNonceData("nonceData");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getNonceData(), result.getNonceData());
        assertEquals(expected.getSign(), result.getSign());
        assertEquals(expected.getNonceSign(), result.getNonceSign());
        assertEquals(expected.getActionMode(), result.getActionMode());
    }


    @Test
    public void testSetSign() {
        AuthenticationRequest expected = setTestValues(null, null, null, null, "sign", null, null);
        AuthenticationRequest result = authenticationRequest.setSign("sign");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getNonceData(), result.getNonceData());
        assertEquals(expected.getSign(), result.getSign());
        assertEquals(expected.getNonceSign(), result.getNonceSign());
        assertEquals(expected.getActionMode(), result.getActionMode());
    }

    @Test
    public void testSetNonceSign() {
        AuthenticationRequest expected = setTestValues(null, null, null, null, null, "nonceSign", null);
        AuthenticationRequest result = authenticationRequest.setNonceSign("nonceSign");
        assertEquals(expected.getCertificateType(), result.getCertificateType());
        assertEquals(expected.getCertificate(), result.getCertificate());
        assertEquals(expected.getApplicantData(), result.getApplicantData());
        assertEquals(expected.getNonceData(), result.getNonceData());
        assertEquals(expected.getSign(), result.getSign());
        assertEquals(expected.getNonceSign(), result.getNonceSign());
        assertEquals(expected.getActionMode(), result.getActionMode());
    }

    @Test
    public void testGetActionMode() throws Exception {
        Field field = authenticationRequest.getClass().getDeclaredField("actionMode");
        field.setAccessible(true);
        field.set(authenticationRequest, "actionMode");
        assertEquals("actionMode", authenticationRequest.getActionMode());
    }

    @Test
    public void testSetActionMode() {
        authenticationRequest.setActionMode(null);
        assertNull(authenticationRequest.getActionMode());
        authenticationRequest.setActionMode("login");
        assertEquals("login", authenticationRequest.getActionMode());
    }

    @Test
    public void testValidateHasValues() {
        assertDoesNotThrow(() -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutCertificateWithoutCertificateType() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(null);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutCertificate() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutApplicantDataAndNonceData() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("");
            authenticationRequest.setNonceData("");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutApplicantData() {
        assertDoesNotThrow(() -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutNonceData() {
        assertDoesNotThrow(() -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutSignAndNonceSign() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("");
            authenticationRequest.setNonceSign("");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutSign() {
        assertDoesNotThrow(() -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutNonceSign() {
        assertDoesNotThrow(() -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testValidateHasValuesWithoutMode() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("certificate");
            authenticationRequest.setApplicantData("applicantData");
            authenticationRequest.setNonceData("nonceData");
            authenticationRequest.setSign("sign");
            authenticationRequest.setNonceSign("nonceSign");
            authenticationRequest.setActionMode("");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void testGetName() {
        assertEquals("mode", Filed.ACTION_MODE.getName());
    }
}
