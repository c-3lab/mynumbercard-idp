package com.example.mynumbercardidp.keycloak.util.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;

import java.util.Optional;

/**
 * 現在のSPI設定を取得するユーティリティクラスです。
 */
public class CurrentConfig {
    private CurrentConfig() {
    }

    /**
     * 現在のSPI設定の値を返します。
     *
     * @param context 認証フローのコンテキスト
     * @param SPI設定名
     * @return SPI設定の値
     */
    public static String getValue(final AuthenticationFlowContext context, final String configName) {
        String config = context.getAuthenticatorConfig()
                .getConfig()
                .get(configName);
        return Optional.ofNullable(config).orElse("");
    }
}
