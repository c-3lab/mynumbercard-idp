package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * AbstractMyNumberCardAuthenticatorは、個人番号カードの公的個人認証部分を利用する認証処理で共通して実行する定義を表す抽象クラスです。
 */
public abstract class AbstractMyNumberCardAuthenticator extends AbstractFormAuthenticator {

    /** Keycloakイベントロガー */
    private static final Logger logger = Logger.getLogger(AbstractMyNumberCardAuthenticator.class);

    /** Keycloakサービスロガー */
    protected static ServicesLogger serviceLogger = ServicesLogger.LOGGER;

    public static final String REGISTRATION_FORM_ACTION = "registration_form"; // テンプレートファイルとの互換性維持
    public static final String ATTEMPTED_USERNAME = "ATTEMPTED_USERNAME"; // テンプレートファイルとの互換性維持

    @Override
    public void authenticate(AuthenticationFlowContext context) {

    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // 一度も呼ばれない
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // 一度も呼ばれない
    }

    @Override
    public void close() {

    }

    /**
     * HTTPフォームデータを取得する。
     *
     * @param context 認証フローのコンテキスト
     * @return デコード済みのHTTPフォームデータ配列
     */
    protected MultivaluedMap<String, String> getFormData(AuthenticationFlowContext context) {
        return context.getHttpRequest().getDecodedFormParameters();
    }

    /**
     * 現在のSPI設定を取得するユーティリティクラスです。
     */
    protected static class CurrentConfig {
        CurrentConfig() {}
        /**
         * 現在のSPI設定の値を返します。
         *
         * @param context 認証フローのコンテキスト
         * @param SPI設定名
         * @return SPI設定の値
         */
        public static String getValue(AuthenticationFlowContext context, String configName) {
            String config = context.getAuthenticatorConfig()
                .getConfig()
                .get(configName);
            return java.util.Objects.nonNull(config) ? config.toString() : "";
        }
    }
}
