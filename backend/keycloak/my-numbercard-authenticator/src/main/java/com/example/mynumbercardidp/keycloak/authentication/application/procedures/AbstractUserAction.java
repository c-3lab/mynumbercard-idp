package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.network.platform.UserRequestModel;
import com.example.mynumbercardidp.keycloak.core.authentication.application.procedures.ApplicationProcedure;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.InvalidKeyException;
import java.util.Base64;
import javax.ws.rs.core.Response;

/**
 * ユーザーが希望する操作の抽象クラスです。
 *
 * 認証、登録、登録情報の変更などで実行される処理のうち、共通の処理を定義します。
 */
public abstract class AbstractUserAction implements ApplicationProcedure {

    private static Logger consoleLogger = Logger.getLogger(AbstractUserAction.class);

    @Override
    public void execute(AuthenticationFlowContext context, PlatformApiClientImpl platform) {

    }

    /**
     * 事前処理として、署名された文字列がNonceをハッシュ化した文字列かどうか検証します。
     * 
     * @param constext 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void preExecute(AuthenticationFlowContext context, PlatformApiClientImpl platform) {
        UserRequestModel user = (UserRequestModel) platform.getUserRequest();
        user.ensureHasValues();

        // ユーザーが送ってきたNonceをハッシュ化した値は信用しない。
        String nonce = getNonce(context);
        consoleLogger.debug("Nonce: " + nonce);
        String nonceHash = toHashString(nonce);
        consoleLogger.debug("Nonce hash: " + nonceHash);

        String applicantData = user.getApplicantData();
        consoleLogger.debug("Applicant data: " + applicantData);
        String applicantDataLower = applicantData.toLowerCase();
        String applicantDataUpper = applicantData.toUpperCase();

        if (!nonceHash.equals(applicantDataLower) && !nonceHash.equals(applicantDataUpper)) {
            String message ="Applicant data is not equals a nonce hash.";
            if (!isDebugMode(context)) {
                throw new IllegalArgumentException(message);
            }
            consoleLogger.info("Debug mode is enabled. " + message);
        }

        String certificate = user.getCertificate();
        String sign = user.getSign();
        if (!isDebugMode(context)) {
            if (!validateSignature(sign, certificate, nonceHash.toLowerCase()) ||
                !validateSignature(sign, certificate, nonceHash.toUpperCase())) {
                    throw new IllegalArgumentException("The signature is not equals a nonce hash.");
            }
        }
        validateSignature(sign, certificate, nonceHash, nonce, applicantData);
    }

    /**
     * 公開鍵とnonceを利用して、署名した値が文字列と一致するかを検証します。
     *
     * 検査例外が発生した場合、非検査例外にラップし送出します。
     *
     * @param signature X.509に準拠する鍵で文字列に署名した結果
     * @param certificateBase64Content 公開鍵を基本型Base64でエンコードした値
     * @param str 署名された文字列
     * @return 検証された場合はtrue、そうでない場合はfalse
     * @exception UncheckedIOException 公開鍵の値が空値の場合
     * @exception IllegalArgumentException 署名の検証中に例外が発生した場合
     */
    private boolean validateSignature(String signature, String certificateBase64Content, String str) {
        String charset = "utf-8";
        String certType = "X.509";
        try {
            byte[] certificateBinary = Base64.getDecoder().decode(certificateBase64Content.getBytes(charset));
            Certificate certificate = null;
            try (InputStream inputStream = new ByteArrayInputStream(certificateBinary)) {
                certificate = CertificateFactory.getInstance(certType).generateCertificate(inputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            String signAlgorithm = "SHA256withRSA";
            Signature engine = Signature.getInstance(signAlgorithm);
            engine.initVerify(certificate);
            engine.update(str.getBytes(charset));


            return engine.verify(Base64.getDecoder().decode(signature.getBytes(charset)));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | CertificateException |
                 InvalidKeyException | SignatureException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 公開鍵とnonceを利用して、署名した値が文字列と一致するかを検証します。
     *
     * nonceをハッシュ化した値、nonce、ユーザーが自己申告した値の順で検証します。
     *
     * @param signature X.509に準拠する鍵で文字列に署名した結果
     * @param certificateBase64Content 公開鍵を基本型Base64でエンコードした値
     * @param nonceHash nonceをハッシュ化した文字列
     * @param nonce ランダムに生成された文字列
     * @param applicantData ユーザーが自己申告した文字列
     */
    private void validateSignature(String signature, String certificateBase64Content, String nonceHash, String nonce, String applicantData) {
        if (validateSignature(signature, certificateBase64Content, nonceHash.toLowerCase()) ||
            validateSignature(signature, certificateBase64Content, nonceHash.toUpperCase())) {
            return;
        }

        String consoleMessage = "Failed validate signature. The signed value was not a nonce hash. Retry, verifies that the signed value is a nonce.";
        consoleLogger.info("Debug mode is enabled. " + consoleMessage);
        if (validateSignature(signature, certificateBase64Content, nonce)) {
            return;
        }

        consoleMessage = "Failed validate signature. The signed value was not a nonce.";
        consoleLogger.info(consoleMessage);
        if (validateSignature(signature, certificateBase64Content, applicantData)) {
            return;
        }
        throw new IllegalArgumentException("The signature is not equals a applicant data.");
    }

    /**
     * 認証ユーザーのセッション情報からNonce文字列を取得します。
     * 
     * AuthNote名のnonceはActionHandler呼び出し前に上書きされるため、
     * AuthNoteからverifyNonceを取得します。
     * @param constext 認証フローのコンテキスト
     * @return Nonce文字列
     */
    private String getNonce(AuthenticationFlowContext context) {
       return context.getAuthenticationSession().getAuthNote("verifyNonce");
    }

    /**
     * 指定された文字列をSHA256アルゴリズムでハッシュ化し、その文字列を返します。
     * 
     * @param str SHA256アルゴリズムでハッシュ化する文字列
     * @return SHA256アルゴリズムでハッシュ化された文字列
     */
    private String toHashString(String str) {
        return DigestUtils.sha256Hex(str);
    }

    /**
     * プラットフォームが返したユニークIDからKeycloak内のユーザーを返します。
     * 
     * @param constext 認証フローのコンテキスト
     * @param uniqueId プラットフォームが識別したユーザーを特定する一意の文字列
     * @param str 署名された文字列
     * @return ユーザーのデータ構造 Keycloak内のユーザーが見つかった場合はユーザーデータ構造、そうでない場合はNull
     */
    protected UserModel findUser(AuthenticationFlowContext context, String uniqueId) {
        try {
            return UserIdentityToModelMapperBuilder.fromString("uniqueId").find(context, uniqueId);
        } catch (Exception e) {
            /*
             * 報告された例外はExceptionクラスで詳細な判別がつかない。
             * IllegalArgumentExceptionでラップする。
             */
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * デバッグモードの状態を返します。
     *
     * @param constext 認証フローのコンテキスト
     * @return 有効の場合はtrue、そうでない場合はfalse
     */ 
    protected boolean isDebugMode(AuthenticationFlowContext context) {
        String debugMode = SpiConfigProperty.DebugMode.CONFIG.getName();
        String debugModeValue = CurrentConfig.getValue(context, debugMode).toLowerCase();
        debugModeValue = StringUtil.isStringEmpty(debugModeValue) ? "false" : debugModeValue.toLowerCase();
        return Boolean.valueOf(debugModeValue);
    }

    /**
     *  登録画面を初期表示とした画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     */
    protected void actionRegistrationChallenge(AuthenticationFlowContext context) {
        actionReChallenge(context, "registration", Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     *  認証画面を初期表示とした画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     */
    protected void actionLoginChallenge(AuthenticationFlowContext context) {
        actionReChallenge(context, "login", Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     *  指定された処理の画面を初期表示とした画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param actionName 遷移先処理の種類
     * @param status プラットフォームのHTTPステータスコード
     */
    protected void actionReChallenge(AuthenticationFlowContext context, String actionName, int status) {
        Response response = ResponseCreater.createChallengePage(context, actionName, status);
        ResponseCreater.setFlowStepChallenge(context, response);
    }

    /**
     *  ユーザー属性項目と値の組み合わせからユーザーを返す処理の定義です。
     */
    private static class UserIdentityToModelMapperBuilder {

        static UserIdentityToModelMapper fromUniqueId() {
            return fromString("uniqueId");
        }

        static UserIdentityToModelMapper fromString(String attributeName) {

            UserIdentityToModelMapper mapper = UserIdentityToModelMapper.getUserIdentityToCustomAttributeMapper(attributeName);
            return mapper;
        }
    }
}
