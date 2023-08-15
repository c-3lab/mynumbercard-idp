package com.example.mynumbercardidp.keycloak.core.network.platform;

/**
 * プラットフォームが扱うユーザーリクエストデータ構造のインタフェースです。
 */
public interface UserRequestModelImpl {
    /**
     * ユーザーが希望する処理の種類を返します。
     *
     * @return ユーザーが希望する処理の種類  Nullを返す場合があります。
     */
    String getActionMode();
}
