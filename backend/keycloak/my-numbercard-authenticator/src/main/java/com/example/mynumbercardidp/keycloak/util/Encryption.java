package com.example.mynumbercardidp.keycloak.util;

import java.security.Key;
import java.security.PublicKey;
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
     * @param privateKey 秘密鍵
     * @return 復号化された証明書データJSONオブジェクト
     */
     public static JsonNode decrypt(String encryptedJWE, Key privateKey) throws Exception {
        JWE jwe = new JWE(encryptedJWE);

        // 復号用の鍵を定義
        JWEKeyStorage keyStorage = jwe.getKeyStorage();
        keyStorage.setDecryptionKey(privateKey);

        //JsonNode jweData = null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jweData = objectMapper.readTree(new String(jwe.verifyAndDecodeJwe().getContent()));

        return jweData;
    }
}
