package com.example.mynumbercardidp.keycloak.network.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.UserModel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PlatformAuthenticationResponseTest {

    private AutoCloseable closeable;
    PlatformAuthenticationResponse platformAuthenticationResponse = new PlatformAuthenticationResponse();
    private PlatformAuthenticationResponse.IdentityInfo identityInfo = new PlatformAuthenticationResponse.IdentityInfo();
    @Mock
    UserModel user;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetHttpStatusCode() throws Exception {
        Field field = platformAuthenticationResponse.getClass().getDeclaredField("httpStatusCode");
        field.setAccessible(true);
        field.set(platformAuthenticationResponse, 400);
        assertEquals(400, platformAuthenticationResponse.getHttpStatusCode());
    }

    @Test
    public void testToUserModelAttributes() {
        platformAuthenticationResponse.toUserModelAttributes(user);
        assertNull(user.getFirstAttribute("uniqueiId"));
        assertNull(user.getFirstAttribute("name"));
        assertNull(user.getFirstAttribute("gender_code"));
        assertNull(user.getFirstAttribute("user_address"));
        assertNull(user.getFirstAttribute("birth_date"));
    }

    @Test
    public void testSetHttpStatusCode() {
        platformAuthenticationResponse.setHttpStatusCode(400);
        assertEquals(400, platformAuthenticationResponse.getHttpStatusCode());
    }

    @Test
    public void testGetUniqueId() {
        assertNull(platformAuthenticationResponse.getUniqueId());
    }

    @Test
    public void testGetIdentityInfo() throws Exception {
        assertNotNull(platformAuthenticationResponse.getIdentityInfo());
    }
        
    @Test
    public void testGetIndentityInfoUniqueId() throws Exception {
        Field field = identityInfo.getClass().getDeclaredField("uniqueId");
        field.setAccessible(true);
        field.set(identityInfo, "c610e161-90ce-4a31-ab84-9429dd484e83");
        assertEquals("c610e161-90ce-4a31-ab84-9429dd484e83", identityInfo.getUniqueId());
    }

    @Test
    public void testGetName() throws Exception {
        Field field = identityInfo.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(identityInfo, "name");
        assertEquals("name", identityInfo.getName());
    }

    @Test
    public void testGetDateOfBirth() throws Exception {
        Field field = identityInfo.getClass().getDeclaredField("dateOfBirth");
        field.setAccessible(true);
        field.set(identityInfo, "1999-01-01");
        assertEquals("1999-01-01", identityInfo.getDateOfBirth());
    }

    @Test
    public void testGetGender() throws Exception {
        Field field = identityInfo.getClass().getDeclaredField("gender");
        field.setAccessible(true);
        field.set(identityInfo, "0");
        assertEquals("0", identityInfo.getGender());
    }

    @Test
    public void testGetAddress() throws Exception {
        Field field = identityInfo.getClass().getDeclaredField("address");
        field.setAccessible(true);
        field.set(identityInfo, "address");
        assertEquals("address", identityInfo.getAddress());
    }
}
