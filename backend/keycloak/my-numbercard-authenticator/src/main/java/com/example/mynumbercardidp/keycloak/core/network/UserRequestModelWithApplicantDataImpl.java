package com.example.mynumbercardidp.keycloak.core.network;

/**
 * nonceをハッシュ化した値を含むユーザーリクエストのデータ構造インタフェースです。
 */
public interface UserRequestModelWithApplicantDataImpl extends UserRequestModelImpl {
    /**
     * nonceをハッシュ化した値を返します。
     *
     * @return nonceをハッシュ化した値  Nullを返す場合があります。
     */
    String getApplicantData();
}
