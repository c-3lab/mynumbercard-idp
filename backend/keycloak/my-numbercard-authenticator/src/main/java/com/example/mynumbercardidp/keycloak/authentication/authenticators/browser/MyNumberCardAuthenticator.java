package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ActionResolver;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.core.authentication.application.procedures.ApplicationResolverInterface;
import com.example.mynumbercardidp.keycloak.core.authentication.authenticators.browser.AbstractMyNumberCardAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.util.Optional;
import javax.ws.rs.core.Response;

/**
 * このクラスは個人番号カードの公的個人認証部分を利用する認証SPIです。
 *
 * Keycloakのコンソールログは英語出力であるため、それに倣い、英語で出力します。
 * Keycloakのコンソールへ出力するログレベルはKeycloakが定義しているロギングの構成に倣います。
 *
 * @see <a href="https://www.keycloak.org/server/logging">ロギングの構成</a>
 */
public class MyNumberCardAuthenticator extends AbstractMyNumberCardAuthenticator {
    private ApplicationResolverInterface actionResolver = new ActionResolver();

    /**
     * プラットフォームへ公的個人認証部分を送信し、その結果からログインや登録、登録情報の変更処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     */
    @Override
    public void action(final AuthenticationFlowContext context) {
        SpiConfigProperty.initFreeMarkerJavaTemplateVariables(context);
        this.actionResolver.executeUserAction(context);
        AbstractMyNumberCardAuthenticator.validateHasAuthFlowStatus(context);
    }

    /**
     * 個人番号カードの公的個人認証部分を送信するアプリが起動できるフォームを返します。
     *
     * @param context 認証フローのコンテキスト
     */
    @Override
    public void authenticate(final AuthenticationFlowContext context) {
        SpiConfigProperty.initFreeMarkerJavaTemplateVariables(context);
        ResponseCreater.setLoginFormAttributes(context);
        String initialView = Optional.ofNullable(
                context.getAuthenticationSession().getAuthNote("initialView")).orElse("");
        Response response = ResponseCreater.createChallengePage(context, initialView);
        context.challenge(response);
    }
}
