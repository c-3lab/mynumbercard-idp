package com.example.mynumbercardidp.keycloak.network.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.models.UserModel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PlatformAuthenticationResponseTest {

    PlatformAuthenticationResponse platformAuthenticationResponse = new PlatformAuthenticationResponse();
    private PlatformAuthenticationResponse.IdentityInfo identityInfo = new PlatformAuthenticationResponse.IdentityInfo();
    @Mock
    UserModel user;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getHttpStatusCode() {
        assertNotNull(platformAuthenticationResponse.getHttpStatusCode());
    }

    @Test
    public void toUserModelAttributes() {
        platformAuthenticationResponse.toUserModelAttributes(user);
        assertNull(user.getFirstAttribute("uniqueid"));
        assertNull(user.getFirstAttribute("name"));
        assertNull(user.getFirstAttribute("gender_code"));
        assertNull(user.getFirstAttribute("user_address"));
        assertNull(user.getFirstAttribute("birth_date"));
    }

    @Test
    public void setHttpStatusCode() {
        platformAuthenticationResponse.setHttpStatusCode(400);
        assertNotNull(platformAuthenticationResponse.getHttpStatusCode());
    }

    @Test
    public void getUniqueId() {
        assertNull(platformAuthenticationResponse.getUniqueId());
    }

    @Test
    public void getIdentityInfo() {
        assertNotNull(platformAuthenticationResponse.getIdentityInfo());
    }
        
    @Test
    public void getIndentityInfoUniqueId() {
        assertNull(identityInfo.getUniqueId());
    }

    @Test
    public void getName() {
        assertNull(identityInfo.getName());
    }

    @Test
    public void getDateOfBirth() {
        assertNull(identityInfo.getDateOfBirth());
    }

    @Test
    public void getGender() {
        assertNull(identityInfo.getGender());
    }

    @Test
    public void getAddress() {
        assertNull(identityInfo.getAddress());
    }
}
