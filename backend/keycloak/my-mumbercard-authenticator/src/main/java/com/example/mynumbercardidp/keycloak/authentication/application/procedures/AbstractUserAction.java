package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.network.platform.RequestBuilder;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClientImpl;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.InvalidKeyException;
import java.util.Base64;

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
        // ユーザーが送ってきたNonceをハッシュ化した値は信用しない。
        String nonceHash = toHashString(getNonce(context));
        String certificate = platform.getUserRequest().getCertificate();
        String sign = platform.getUserRequest().getSign();
        if (!validateSignature(sign, certificate, nonceHash)) {
            // 署名検証はしたが、失敗した場合
            throw new IllegalArgumentException("The signature not equals nonce hash.");
        }     
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
     * 認証ユーザーのセッション情報からNonce文字列を取得します。
     * 
     * @param constext 認証フローのコンテキスト
     * @return Nonce文字列
     */
    private String getNonce(AuthenticationFlowContext context) {
       return context.getAuthenticationSession().getAuthNote("nonce");
    }

    /**
     * 指定された文字列をSHA256アルゴリズムでハッシュ化し、その文字列を返します。
     * 
     * @param str SHA256アルゴリズムでハッシュ化する文字列
     * @return SHA256アルゴリズムでハッシュ化された文字列
     */
    private String toHashString(String str) {
        return toHashString(str);
    }

    /**
     * 指定された文字列を指定されたメッセージダイジェストアルゴリズムでハッシュ化し、その文字列を返します。
     * 
     * @param mdAlg メッセージダイジェストアルゴリズムの種類
     * @param str 指定されたメッセージダイジェストアルゴリズムでハッシュ化する文字列
     * @return 指定されたメッセージダイジェストアルゴリズムでハッシュ化された文字列
     * @exception NoSuchAlgorithmException 指定されたアルゴリズムが存在しない場合
     */
    private String toHashString(String mdAlg, String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(mdAlg);
        byte[] hash = md.digest(str.getBytes());
        return new String(hash);
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
