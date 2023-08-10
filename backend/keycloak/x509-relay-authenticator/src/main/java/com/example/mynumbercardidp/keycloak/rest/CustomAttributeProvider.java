package com.example.mynumbercardidp.keycloak.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Time;
import org.keycloak.events.EventBuilder;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.util.DefaultClientSessionContext;

public class CustomAttributeProvider implements RealmResourceProvider {

    private KeycloakSession session;
    private AccessToken token;
    private UserModel user;
    private ClientModel client;
    private String sessionId;
    private static final String ATTRIBUTE_SUFFIX = "_user_attributes";
    private static final Integer MAX_SECONDS = 10;

    public CustomAttributeProvider(KeycloakSession session) {
        this.session = session;
        this.token = null;
        this.user = null;
        this.client = null;
        this.sessionId = null;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @POST
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/assign")
    @NoCache
    // RP側のユーザーに関連した情報をIdP側のユーザーに紐づける
    public Response setAttributes(String requestBody) {
        KeycloakContext context = this.session.getContext();

        // ユーザー認証を実施する
        try {
            this.user = authorization(context.getRequestHeaders());
        } catch (JWSInputException e) {
            return ResponseMessage.getErrorResponse(ResponseMessage.ERROR_TYPE.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseMessage.getErrorResponse(ResponseMessage.ERROR_TYPE.UNAUTHORIZED);
        }

        // 送られてきたデータをJSON形式へ変換する
        JsonNode requestData = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            requestData = objectMapper.readTree(requestBody);
        } catch (IOException e) {
            return ResponseMessage.getErrorResponse(ResponseMessage.ERROR_TYPE.BAD_REQUEST);
        }

        // user_attributesの値がJSONオブジェクトであるかを確認する
        String jsonValue = requestData.get("user_attributes").toString();
        if (!verifyJsonFormat(jsonValue)) {
            return ResponseMessage.getErrorResponse(ResponseMessage.ERROR_TYPE.BAD_REQUEST);
        }

        // クライアント名はアクセストークンから取得する。
        String clientId = this.client.getClientId();

        // ユーザー属性へ値を書き込む
        this.user.setSingleAttribute(clientId + ATTRIBUTE_SUFFIX, jsonValue);

        // アクセストークンの再発行
        UserSessionModel userSession = this.session.sessions().getUserSession(context.getRealm(), this.sessionId);
        AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(this.client.getId());
        ClientSessionContext clientSessionCtx = DefaultClientSessionContext.fromClientSessionScopeParameter(clientSession, this.session);

        ClientConnection connection = this.session.getContext().getConnection();
        EventBuilder event = new EventBuilder(context.getRealm(), this.session, connection);

        TokenManager tokenManager = new TokenManager();
        AccessTokenResponse accessTokenResponseBuilder = tokenManager.responseBuilder(context.getRealm(), this.client, event, this.session, userSession, clientSessionCtx)
                    .generateAccessToken().generateIDToken().generateRefreshToken().build();

        String newTokens = null;
        try {
            String[] strRemoveKeys = {
                "error",
                "error_description",
                "error_uri"
            };
            Collection<String> removeKeys = Arrays.asList(strRemoveKeys);

            ObjectNode tokens = (ObjectNode)objectMapper.readTree(objectMapper.writeValueAsString(accessTokenResponseBuilder));
            newTokens = objectMapper.writeValueAsString(tokens.remove(removeKeys));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseMessage.getErrorResponse(ResponseMessage.ERROR_TYPE.INTERNAL_SERVER_ERROR);
        }

        // 古いアクセストークンを失効させる
        revokeAccessToken(this.token);

        return Response.ok(newTokens, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public void close() {
    }

    // 送られてきたデータが正しいJSON形式になっているか確認
    private boolean verifyJsonFormat(String content) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(content);

            return jsonNode.isObject();
        } catch (Exception e) {
            return false;
        }
    }

    // アクセストークン認証
    private UserModel authorization(HttpHeaders headers) throws Exception {
        String tokenString = AppAuthManager.extractAuthorizationHeaderToken(headers);
        
        try {
            JWSInput input = new JWSInput(tokenString);
            this.token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            e.printStackTrace();
            throw e;
        }

        String realmName = this.token.getIssuer().substring(this.token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(this.session);
        RealmModel realm = realmManager.getRealmByName(realmName);

        if (realm == null) {
            throw new Exception();
        }

        ClientConnection connection = this.session.getContext().getConnection();
        AuthenticationManager.AuthResult authResult =
            authenticateBearerToken(
                tokenString,
                this.session,
                realm,
                this.session.getContext().getUri(),
                connection,
                headers
           );

        if (authResult == null) {
            throw new Exception();
        }

        this.client = realm.getClientByClientId(this.token.getIssuedFor());
        this.sessionId = this.token.getSessionId();

        return authResult.getUser();
    }

    private AuthenticationManager.AuthResult authenticateBearerToken(
        String tokenString,
        KeycloakSession session,
        RealmModel realm,
        UriInfo uriInfo,
        ClientConnection connection,
        HttpHeaders headers) {
      return new AppAuthManager.BearerTokenAuthenticator(this.session)
          .setRealm(realm)
          .setUriInfo(uriInfo)
          .setTokenString(tokenString)
          .setConnection(connection)
          .setHeaders(headers)
          .authenticate();
    }

    // 古いアクセストークンを失効させる
    private void revokeAccessToken(AccessToken token) {
        SingleUseObjectProvider singleUseStore = this.session.getProvider(SingleUseObjectProvider.class);
        int currentTime = Time.currentTime();
        long lifespanInSecs = Math.max(this.token.getExp() - currentTime, MAX_SECONDS);
        singleUseStore.put(this.token.getId() + SingleUseObjectProvider.REVOKED_KEY, lifespanInSecs, Collections.emptyMap());
    }

    // エラー情報定義
    private static class ResponseMessage {

        static enum ERROR_TYPE {
            BAD_REQUEST,
            UNAUTHORIZED,
            INTERNAL_SERVER_ERROR
        }

        public static Response getErrorResponse(ERROR_TYPE type) {
            switch (type) {
                case BAD_REQUEST:
                    return error("リクエストに問題が発生しました。", Response.Status.BAD_REQUEST);
                case UNAUTHORIZED:
                    return error("トークン認証に問題が発生しました。", Response.Status.UNAUTHORIZED);
                case INTERNAL_SERVER_ERROR:
                    return error("内部サーバーエラーが発生しました。", Response.Status.INTERNAL_SERVER_ERROR);
                default:
                    throw new IllegalArgumentException("エラーが発生しました。");
            }
        }

        private static Response error(String message, Response.Status status) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode responseBody = objectMapper.createObjectNode();
            responseBody.put("error", status.getStatusCode() + " " + status);
            responseBody.put("error_description", message);
            return Response.status(status).type(MediaType.APPLICATION_JSON)
                       .entity(responseBody.toString())
                       .build();
        }

    }
}
