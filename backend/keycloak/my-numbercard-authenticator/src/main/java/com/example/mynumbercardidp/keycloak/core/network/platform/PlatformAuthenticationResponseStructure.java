package com.example.mynumbercardidp.keycloak.core.network.platform;

import org.keycloak.models.UserModel;

public interface PlatformAuthenticationResponseStructure {
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
     * プラットフォームのレスポンスからKeycloakのユーザー属性を追加、更新します。
     *
     * @param user Keycloak ユーザーのデータ構造体インスタンス
     */
    UserModel toUserModelAttributes(final UserModel user);
}
