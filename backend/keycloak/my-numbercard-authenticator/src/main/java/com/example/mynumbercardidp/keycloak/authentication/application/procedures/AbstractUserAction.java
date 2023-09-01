package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformAuthenticationResponseStructure;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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

    private static final String MESSAGE_DEBUG_MODE_ENABLED = "Debug mode is enabled.";
    private static Logger consoleLogger = Logger.getLogger(AbstractUserAction.class);

    /**
     * 認証ユーザーのセッション情報からNonceをハッシュ化した文字列を取得します。
     *
     * @param constext 認証フローのコンテキスト
     * @return Nonceをハッシュ化した文字列
     */
    private static String getSessionNonceHash(final AuthenticationFlowContext context) {
        String authNoteName = "nonce";
        String nonce = context.getAuthenticationSession().getAuthNote(authNoteName);
        return DigestUtils.sha256Hex(nonce);
    }

    /**
     * プラットフォームが返したユニークIDからKeycloak内のユーザーを返します。
     * 
     * @param constext 認証フローのコンテキスト
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
     * @param constext 認証フローのコンテキスト
     * @return 有効の場合はtrue、そうでない場合はfalse
     */
    protected boolean isDebugMode(final AuthenticationFlowContext context) {
        String key = SpiConfigProperty.DebugMode.CONFIG.getName();
        String value = CurrentConfig.getValue(context, key).toLowerCase();
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        return Boolean.parseBoolean(value.toLowerCase());
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
     * @param constext 認証フローのコンテキスト
     * @param platform プラットフォームAPIクライアント
     * @return 検証された場合はtrue、そうでない場合はfalse
     */
    // Keycloakが発行したnonceをハッシュ化した値とユーザーが自己申告したnonceをハッシュ化した値は異なる可能性がある。
    // デバッグモードが無効の場合、ユーザーが送ってきたnonceをハッシュ化した値は信用しない。
    // デバッグモードが有効の場合、検証に失敗した場合はユーザーが送ってきたnonceをハッシュ化した値も比較する。
    protected boolean validateSignature(final AuthenticationFlowContext context,
            final PlatformApiClientInterface platform) {
        String nonceHash = AbstractUserAction.getSessionNonceHash(context);
        AbstractUserAction.consoleLogger.debug("Nonce hash: " + nonceHash);

        AuthenticationRequest userRequest = platform.getUserRequest();
        userRequest.validateHasValues();
        String applicantData = userRequest.getApplicantData();
        AbstractUserAction.consoleLogger.debug("Applicant data: " + applicantData);

        if (!nonceHash.toLowerCase().equals(applicantData.toLowerCase())) {
            if (!isDebugMode(context)) {
                return false;
            }
            String message = "Applicant data does not equal the nonce hash.";
            AbstractUserAction.consoleLogger.info(MESSAGE_DEBUG_MODE_ENABLED + " " + message);
        }

        String certificate = platform.getUserRequest().getCertificate();
        String sign = platform.getUserRequest().getSign();
        if (!isDebugMode(context)) {
            return validateSignature(sign, certificate, nonceHash.toLowerCase()) ||
                    validateSignature(sign, certificate, nonceHash.toUpperCase());
        } else {
            return validateSignature(sign, certificate, nonceHash.toLowerCase()) ||
                    validateSignature(sign, certificate, nonceHash.toUpperCase()) ||
                    validateSignature(sign, certificate, applicantData);
        }
    }

    /**
     * 公開鍵とnonceを利用して、署名した値が文字列と一致するかを検証します。
     *
     * 例外が発生した場合は握り潰し、falseを返します。
     *
     * @param signature                X.509に準拠する鍵で文字列に署名した結果
     * @param certificateBase64Content 公開鍵を基本型Base64でエンコードした値
     * @param nonceHash                Nonceをハッシュ化した文字列
     * @return 検証された場合はtrue、そうでない場合はfalse
     */
    // [NOTE] 署名用証明書はJWE形式のデータ、利用者用証明書は生のデータで受け取るため、このメソッドは置き換えられる可能性がある。
    private boolean validateSignature(final String signature, final String certificateBase64Content,
            final String nonceHash) {
        try {
            byte[] certificateBinary = Base64.getDecoder().decode(certificateBase64Content.getBytes("utf-8"));
            Certificate certificate = null;
            try (InputStream inputStream = new ByteArrayInputStream(certificateBinary)) {
                certificate = CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            Signature engine = Signature.getInstance("SHA256withRSA");
            engine.initVerify(certificate);
            engine.update(nonceHash.getBytes("utf-8"));
            return engine.verify(Base64.getDecoder().decode(signature.getBytes("utf-8")));
        } catch (Exception e) {
            // 例外を握り潰す。
            AbstractUserAction.consoleLogger.debug("Catched exception at method validateSignature. " + e.getMessage());
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
