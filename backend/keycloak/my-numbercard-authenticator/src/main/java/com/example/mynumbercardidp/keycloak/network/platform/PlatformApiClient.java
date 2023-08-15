package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.AbstractPlatformApiClient;
import com.example.mynumbercardidp.keycloak.core.network.platform.DataModelManagerImpl;
import org.apache.http.entity.ContentType;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MultivaluedMap;

public class PlatformApiClient extends AbstractPlatformApiClient {
    private static final ContentType REQUEST_CONTENT_TYPE = ContentType.APPLICATION_JSON;
    private static final String API_URI_PATH = "/verify/";
    private static Logger consoleLogger = Logger.getLogger(PlatformApiClient.class);
    private String platformRequestSender = "";

    {
        super.setHttpRequestContentType(PlatformApiClient.REQUEST_CONTENT_TYPE);
    }

    @Override
    public void action() {
        DataModelManager data = (DataModelManager) super.getDataModelManager();
        HttpEntity requsetEntity = super.createHttpEntity(data.convertPlatformRequestToJson());
        URI apiUri = createApiUri();
        Header[] headers = {};
        PlatformApiClient.consoleLogger.debug("Platform API URI: " + apiUri);
        super.post(apiUri, headers, requsetEntity);
    }

    @Override
    protected DataModelManagerImpl createDataManager(final MultivaluedMap<String, String> formData) {
        DataModelManagerImpl data = new DataModelManager();
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
}
