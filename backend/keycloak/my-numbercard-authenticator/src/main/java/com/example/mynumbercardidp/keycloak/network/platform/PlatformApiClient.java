package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.AbstractPlatformApiClient;
import com.example.mynumbercardidp.keycloak.core.network.platform.RequestAndResponseDataManager;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import javax.ws.rs.core.MultivaluedMap;

public class PlatformApiClient extends AbstractPlatformApiClient {
    private static final ContentType REQUEST_CONTENT_TYPE = ContentType.APPLICATION_JSON;
    private static final String API_URI_PATH = "/verify/";
    private static Logger consoleLogger = Logger.getLogger(PlatformApiClient.class);

    {
        super.setHttpRequestContentType(PlatformApiClient.REQUEST_CONTENT_TYPE);
        super.setDefaultCharset(Charset.forName("UTF-8"));
    }

    @Override
    public void sendRequest() {
        DataModelManager data = (DataModelManager) super.getDataModelManager();
        HttpEntity requsetEntity = new ByteArrayEntity(
                data.convertPlatformRequestToJson().getBytes(super.getDefaultCharset()),
                PlatformApiClient.REQUEST_CONTENT_TYPE);
        URI apiUri = createApiUri();
        Header[] headers = {};
        PlatformApiClient.consoleLogger.debug("Platform API URI: " + apiUri);
        super.sendEntity(apiUri, headers, requsetEntity);
    }

    @Override
    protected RequestAndResponseDataManager createDataManager(final MultivaluedMap<String, String> formData) {
        RequestAndResponseDataManager data = new DataModelManager();
        data.setUserFormData(formData);
        return data;
    }

    private URI createApiUri() {
        try {
            String rootUri = super.getApiRootUri().toString();
            String action = super.getUserRequest().getActionMode();
            return new URI(rootUri + PlatformApiClient.API_URI_PATH + action);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setContextForDataManager(AuthenticationFlowContext context) {
        DataModelManager dataModelManager = (DataModelManager) super.getDataModelManager();
        dataModelManager.setContext(context);
    }
}
