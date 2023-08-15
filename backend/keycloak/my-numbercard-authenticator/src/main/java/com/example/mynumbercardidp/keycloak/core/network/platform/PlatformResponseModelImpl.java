package com.example.mynumbercardidp.keycloak.core.network.platform;

import org.keycloak.models.UserModel;

public interface PlatformResponseModelImpl {
    /**
     * プラットフォームのHTTPレスポンスコードを返します。
     *
     * @return プラットフォームのHTTPレスポンスコード
     */
    int getHttpStatusCode();

    /**
     * ユーザーの一意なIDを返します。
     *
     * @return ユーザーの一意なID
     */
    String getUniqueId();

    /**
     * プラットフォームレスポンスにユーザーの一意なIDが存在することを保証します。
     *
     * 存在しない場合は IllegalStateException を送出します。
     *
     * @exception IllegalStateException プラットフォームレスポンスにユーザーの一意のIDが存在しない場合
     */
    void ensureHasUniqueId();

    /**
     * プラットフォームのレスポンスからKeycloakのユーザー属性を追加、更新します。
     *
     * @param user Keycloak ユーザーのデータ構造体インスタンス
     */
    UserModel toUserModelAttributes(final UserModel user);
}
