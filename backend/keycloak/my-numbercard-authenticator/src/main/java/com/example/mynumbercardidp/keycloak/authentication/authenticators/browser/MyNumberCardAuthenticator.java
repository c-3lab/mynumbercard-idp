package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ActionResolver;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.core.authentication.authenticators.browser.AbstractMyNumberCardAuthenticator;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.forms.login.LoginFormsProvider;

import javax.ws.rs.core.Response;

/**
 * このクラスは個人番号カードの公的個人認証部分を利用する認証SPIです。
 *
 * Keycloakのコンソールログは英語出力であるため、それに倣い、英語で出力します。
 * Keycloakのコンソールへ出力するログレベルはKeycloakが定義しているロギングの構成に倣います。
 * @see <a href="https://www.keycloak.org/server/logging">ロギングの構成</a>
 */
public class MyNumberCardAuthenticator extends AbstractMyNumberCardAuthenticator {
    /**
     * プラットフォームへ公的個人認証部分を送信し、その結果からログインや登録、登録情報の変更処理を呼び出します。
     *
     * ユーザーリクエストの構造はプラットフォームのAPIに依存しています。
     * プラットフォームAPIクライアント（コネクタ）でユーザーリクエスト構造の解析をします。
     * @param context 認証フローのコンテキスト
     */
    @Override
    public void action(final AuthenticationFlowContext context) {
        SpiConfigProperty.initFreeMarkerJavaTemplateVariablesIfNeeded(context);

        /*
         * 認証を試行するユーザーが希望している動作で処理をします。
         * ActionHandlerクラスが持つメソッドの戻り値はvoid型かつ、
         * publicアクセス修飾子のメソッドはexecuteのみであるため、インスタンスを変数へ格納しません。
         */
        new ActionResolver().action(context);

        /*
         * 認証試行ユーザーのセッション情報から認証フローの結果を取得します。
         * 認証フローの結果が存在している場合はAuthenticatorの処理を終了し、ユーザーへHTTPレスポンスを返します。
         */
        super.ensureHasAuthFlowStatus(context);
    }

    /**
     * 個人番号カードの公的個人認証部分を送信するアプリが起動できるフォームを返します。
     *
     * @param context 認証フローのコンテキスト
     */
    @Override
    public void authenticate(final AuthenticationFlowContext context) {
        SpiConfigProperty.initFreeMarkerJavaTemplateVariablesIfNeeded(context);

        ResponseCreater.setLoginFormAttributes(context);

        String initialView = context.getAuthenticationSession().getAuthNote("initialView");
        if (StringUtil.isEmpty(initialView)) {
            initialView = "";
        }
        Response response = ResponseCreater.createChallengePage(context, initialView);
        ResponseCreater.setFlowStepChallenge(context, response);
    }
}
