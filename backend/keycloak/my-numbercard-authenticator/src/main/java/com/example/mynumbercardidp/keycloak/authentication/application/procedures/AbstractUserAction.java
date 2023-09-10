package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformAuthenticationResponseStructure;
import com.example.mynumbercardidp.keycloak.util.Encryption;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import org.jboss.logging.Logger;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.Key;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

/**
 * ユーザーが希望する操作の抽象クラスです。
 *
 * 認証、登録、登録情報の変更などで実行される処理のうち、共通の処理を定義します。
 */
public abstract class AbstractUserAction {

    private static Logger consoleLogger = Logger.getLogger(AbstractUserAction.class);

    /**
     * 認証ユーザーのセッション情報からNonce文字列を取得します。
     *
     * @param context 認証フローのコンテキスト
     * @return Nonce文字列
     */
    private static String getSessionNonce(final AuthenticationFlowContext context) {
        String authNoteName = "nonce";
        return context.getAuthenticationSession().getAuthNote(authNoteName);
    }

    /**
     * プラットフォームが返したユニークIDからKeycloak内のユーザーを返します。
     * 
     * @param context 認証フローのコンテキスト
     * @param uniqueId プラットフォームが識別したユーザーを特定する一意の文字列
     * @return ユーザーのデータ構造 Keycloak内のユーザーが見つかった場合はユーザーデータ構造、そうでない場合はNull
     * @exception IllegalArgumentException Keycloak内のユーザーを検索中に例外が発生した場合
     */
    protected UserModel findUser(final AuthenticationFlowContext context, final String uniqueId) {
        try {
            return UserIdentityToModelMapperBuilder.fromUniqueId().find(context, uniqueId);
        } catch (Exception e) {
            // 報告された例外は全ての例外である。
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * デバッグモードの状態を返します。
     *
     * @param context 認証フローのコンテキスト
     * @return 有効の場合はtrue、そうでない場合はfalse
     */
    protected boolean isDebugMode(final AuthenticationFlowContext context) {
        String key = SpiConfigProperty.DebugMode.CONFIG.getName();
        String value = CurrentConfig.getValue(context, key).toLowerCase();
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * プラットフォームのレスポンスデータからユーザーのユニークIDを取得します。
     *
     * @param response プラットフォームのレスポンス
     * @return ユーザーの一意のID
     * @exception IllegalStateException ユニークIDの値がnullまたは空の場合
     */
    protected String tryExtractUniqueId(final PlatformAuthenticationResponseStructure response) {
        String uniqueId = response.getUniqueId();
        if (StringUtil.isEmpty(uniqueId)) {
            throw new IllegalStateException("The unique id in the platform response was empty.");
        }
        return uniqueId;
    }

    /**
     * ユーザーリクエストから公開鍵とnonceを利用して、署名した値が文字列と一致するかを検証します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォームAPIクライアント
     * @return 検証された場合はtrue、そうでない場合はfalse
     */
    // Keycloakが発行したnonce値とユーザーが自己申告したnonce値は異なる可能性がある。
    protected boolean validateSignature(final AuthenticationFlowContext context,
            final PlatformApiClientInterface platform) {
        String nonce = AbstractUserAction.getSessionNonce(context);
        AbstractUserAction.consoleLogger.debug("Nonce: " + nonce);

        AuthenticationRequest userRequest = platform.getUserRequest();
        userRequest.validateHasValues();
        String applicantData = userRequest.getApplicantData();
        AbstractUserAction.consoleLogger.debug("Applicant data: " + applicantData);

        if (!nonce.equals(applicantData)) {
            return false;
        }

        String jweCertificate = platform.getUserRequest().getCertificate();
        String sign = platform.getUserRequest().getSign();

        try {
            RealmModel realm = context.getRealm();
            Key privateKey = context.getSession().keys().getActiveKey(realm, KeyUse.ENC, "RSA-OAEP-256").getPrivateKey();
            String certificateContent = Encryption.decrypt(jweCertificate, privateKey).get("claim").asText();
            return validateSignature(sign, certificateContent, nonce);
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * 公開鍵とnonceを利用して、署名した値が文字列と一致するかを検証します。
     *
     * 例外が発生した場合は握り潰し、falseを返します。
     *
     * @param signature          X.509に準拠する鍵で文字列に署名した結果
     * @param certificateContent 公開鍵
     * @param nonce              Nonce文字列
     * @return 検証された場合はtrue、そうでない場合はfalse
     */
    private boolean validateSignature(final String signature, final String certificateContent, final String nonce) {
        try {
            Certificate certificate = null;
            try (InputStream inputStream = new ByteArrayInputStream(certificateContent.getBytes("utf-8"))) {
                certificate = CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            Signature engine = Signature.getInstance("SHA256withRSA");
            engine.initVerify(certificate);
            engine.update(nonce.getBytes());
            return engine.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            // 例外を握り潰す。
            AbstractUserAction.consoleLogger.warn("Caught exception at method validateSignature." + e.getMessage(), e);
            return false;
        }
    }

    /**
     * ユーザー属性項目と値の組み合わせからユーザーを返す処理の定義です。
     */
    private static class UserIdentityToModelMapperBuilder {

        private static UserIdentityToModelMapper fromUniqueId() {
            String attributeName = "uniqueId";
            return fromString(attributeName);
        }

        private static UserIdentityToModelMapper fromString(final String attributeName) {
            UserIdentityToModelMapper mapper = UserIdentityToModelMapper
                    .getUserIdentityToCustomAttributeMapper(attributeName);
            return mapper;
        }
    }
}
