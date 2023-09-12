package com.example.mynumbercardidp.keycloak.core.network;

import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest.Filed;
import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class AuthenticationRequestTest {

    AuthenticationRequest authenticationRequest = new AuthenticationRequest();

    @Test
    public void getCertificateType() {
        assertNull(authenticationRequest.getCertificateType());
    }

    @Test
    public void getCertificate() {
        assertNull(authenticationRequest.getCertificate());
    }

    @Test
    public void getApplicantData() {
        assertNull(authenticationRequest.getApplicantData());
    }

    @Test
    public void getSign() {
        assertNull(authenticationRequest.getSign());
    }

    @Test
    public void setCertificateType() {
        authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
        assertEquals(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE, authenticationRequest.getCertificateType());
        assertEquals("encryptedDigitalSignatureCertificate", authenticationRequest.getCertificateType().getName());
        authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
        assertEquals(CertificateType.ENCRYPTED_USER_AUTHENTICATION, authenticationRequest.getCertificateType());
        assertEquals("encryptedUserAuthenticationCertificate", authenticationRequest.getCertificateType().getName());
    }

    @Test
    public void setCertificate() {
        authenticationRequest.setCertificate(null);
        assertNull(authenticationRequest.getCertificate());
        authenticationRequest.setCertificate("");
        assertEquals("", authenticationRequest.getCertificate());
        authenticationRequest.setCertificate("abc123");
        assertEquals("abc123", authenticationRequest.getCertificate());
    }

    @Test
    public void setApplicantData() {
        authenticationRequest.setApplicantData(null);
        assertNull(authenticationRequest.getApplicantData());
        authenticationRequest.setApplicantData("");
        assertEquals("", authenticationRequest.getApplicantData());
        authenticationRequest.setApplicantData("abc123");
        assertEquals("abc123", authenticationRequest.getApplicantData());
    }

    @Test
    public void setSign() {
        authenticationRequest.setSign(null);
        assertNull(authenticationRequest.getSign());
        authenticationRequest.setSign("");
        assertEquals("", authenticationRequest.getSign());
        authenticationRequest.setSign("abc123");
        assertEquals("abc123", authenticationRequest.getSign());
    }

    @Test
    public void getActionMode() {
        assertNull(authenticationRequest.getActionMode());
    }

    @Test
    public void setActionMode() {
        authenticationRequest.setActionMode(null);
        assertNull(authenticationRequest.getActionMode());
        authenticationRequest.setActionMode("login");
        assertEquals("login", authenticationRequest.getActionMode());
    }

    @Test
    public void validateHasValues() {
        assertDoesNotThrow(() -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
            authenticationRequest.setCertificate("abc123");
            authenticationRequest.setApplicantData("abc123");
            authenticationRequest.setSign("abc123");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void validateHasValuesWithoutCertificateWithoutCertificateType() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(null);
            authenticationRequest.setCertificate("abc123");
            authenticationRequest.setApplicantData("abc123");
            authenticationRequest.setSign("abc123");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void validateHasValuesWithoutCertificate() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("");
            authenticationRequest.setApplicantData("abc123");
            authenticationRequest.setSign("abc123");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void validateHasValuesWithoutApplicantData() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("abc123");
            authenticationRequest.setApplicantData("");
            authenticationRequest.setSign("abc123");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void validateHasValuesWithoutSign() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("abc123");
            authenticationRequest.setApplicantData("abc123");
            authenticationRequest.setSign("");
            authenticationRequest.setActionMode("login");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void validateHasValuesWithoutMode() {
        assertThrows(IllegalStateException.class, () -> {
            authenticationRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
            authenticationRequest.setCertificate("abc123");
            authenticationRequest.setApplicantData("abc123");
            authenticationRequest.setSign("abc123");
            authenticationRequest.setActionMode("");
            authenticationRequest.validateHasValues();
        });
    }

    @Test
    public void getName() {
        assertNotNull(Filed.ACTION_MODE.getName());
    }
}
