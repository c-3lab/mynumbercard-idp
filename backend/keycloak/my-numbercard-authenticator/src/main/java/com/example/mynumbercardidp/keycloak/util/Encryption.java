package com.example.mynumbercardidp.keycloak.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.JWKSHttpUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *  証明書データのJWE暗号化・復号化するユーティリティクラスです。
 */
public class Encryption {

    /**
     * 証明書のコンテンツをJWE暗号化します。
     *
     * @param session Keycloakのセッション
     * @param content 証明書のコンテンツ
     * @param jwksUrl JSONWebKeySetのURL
     * @return 暗号化された証明書データJWE
     */
    public static String encrypt(KeycloakSession session, String content, String jwksUrl) throws Exception {
        // ヘッダー定義
        JWE jwe = new JWE();
        JWEHeader header = new JWEHeader("RSA-OAEP-256", "A128CBC-HS256", "DEF");
        jwe.header(header);

        // コンテンツ定義
        jwe.content(content.getBytes());

        // 暗号化に利用されるRSA公開鍵を取得
        JSONWebKeySet jwks = JWKSHttpUtils.sendJwksRequest(session, jwksUrl);
        PublicKey publicKey = new JWKParser(jwks.getKeys()[0]).toPublicKey();

        // 暗号化鍵定義
        JWEKeyStorage keyStorage = jwe.getKeyStorage();
        keyStorage.setEncryptionKey(publicKey);

        // JWEを暗号化
        return jwe.encodeJwe();
    }

    /**
     * 暗号化された証明書データを復号します。
     *
     * @param encryptedJWE 暗号化された証明書データJWE
     * @param privateKeyFileName 秘密鍵の公開パス
     * @return 復号化された証明書データJSONオブジェクト
     */
     public static JsonNode decrypt(String encryptedJWE, String privateKeyFileName) throws Exception {
        JWE jwe = new JWE(encryptedJWE);

        // リソースから秘密鍵を取得
        PrivateKey privateKey = KeyFactory.getInstance("RSA")
            .generatePrivate(new PKCS8EncodedKeySpec(readKey(privateKeyFileName)));

        // 復号用の鍵を定義
        JWEKeyStorage keyStorage = jwe.getKeyStorage();
        keyStorage.setDecryptionKey(privateKey);

        //JsonNode jweData = null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jweData = objectMapper.readTree(new String(jwe.verifyAndDecodeJwe().getContent()));

        return jweData;
    }

    /**
     * 秘密鍵をPKCS8として読み込みます。
     * Original: https://at-sushi.com/pukiwiki/index.php?JavaSE%20RSA%B0%C5%B9%E6%20Java8
     * 
     * @param fileName 秘密鍵のファイル名
     * @return 復号化された証明書データ
     */
    public static byte[] readKey(final String fileName) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream keyStream = loader.getResourceAsStream(fileName);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(keyStream))) {
            String line;
            StringBuilder sb = new StringBuilder();
            boolean isContents = false;

            while ((line = br.readLine()) != null) {
                if (line.matches("[-]+BEGIN[ A-Z]+[-]+")) {
                    isContents = true;
                } else if (line.matches("[-]+END[ A-Z]+[-]+")) {
                    break;
                } else if (isContents) {
                    sb.append(line);
                }
            }

            return Base64.getDecoder().decode(sb.toString());
        } catch (FileNotFoundException e) {
            throw new Exception("File not found.", e);
        } catch (IOException e) {
            throw new Exception("Could not read the PEM file.", e);
        }
    }
}
