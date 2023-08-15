package com.example.mynumbercardidp.keycloak.core.network;

/**
 * ユーザーリクエストのデータ構造インタフェースです。
 */
public interface UserRequestModelImpl {
    /**
     * ユーザーが希望する処理の種類を返します。
     *
     * @return ユーザーが希望する処理の種類  Nullを返す場合があります。
     */
    String getActionMode();

    /**
     * ユーザーが希望する処理を設定します。
     *
     * @param mode ユーザーが希望する処理の種類
     */
    void setActionMode(String mode);

    /**
     * Base64エンコードされた公開証明書を返します。
     *
     * @return Base64エンコードされた公開証明書  Nullを返す場合があります。
     */
    String getCertificate();

    /**
     * Base64エンコードされた、nonceのハッシュ値を署名した値を返します。
     *
     * @return Base64エンコードされた、nonceのハッシュ値を署名した値  Nullを返す場合があります。
     */
    String getSign();

    /**
     * すべてのフィールドに値が存在することを保証します。
     *
     * 1つ以上のフィールドでNullまたは許容されていない空値があった場合は例外を送出します。
     * @exception IllegalStateException 1つ以上のフィールドでNullまたは空値があった場合
     */
    void ensureHasValues();
}
