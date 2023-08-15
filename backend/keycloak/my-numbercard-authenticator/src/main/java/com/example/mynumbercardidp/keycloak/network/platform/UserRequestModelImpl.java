package com.example.mynumbercardidp.keycloak.network.platform;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容の構造体を表すクラスが実装するメソッド定義です。
 */
public interface UserRequestModelImpl {
    String getActionMode();
    UserRequestModelImpl setActionMode(String mode);
}
