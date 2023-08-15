package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.AbstractUserAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformResponseModel;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformResponseModelImpl;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;

import java.util.Objects;
import javax.ws.rs.core.Response;

/**
 * 個人番号カードの公的個人認証部分を利用したプラットフォームからの応答を元に登録処理をし、ユーザーへのレスポンスを設定する定義です。
 */
public class RegistrationAction extends AbstractUserAction {
    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からユーザーをKeycloakに登録します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void onAction(AuthenticationFlowContext context, PlatformApiClientImpl platform) { 
        PlatformResponseModelImpl response = platform.getPlatformResponse();
        int platformStatusCode = response.getHttpStatusCode();
        if (! new RegistrationFlowTransition().canAction(context, Response.Status.fromStatusCode(platformStatusCode))) {
            return;
        }

        /*
         * Keycloak内にユーザーが存在する場合は認証画面を表示する。
         */
        String uniqueId = super.tryExtractUniqueId(response);
        UserModel user = super.findUser(context, uniqueId);
        if (Objects.nonNull(user)) {
            ResponseCreater.actionLoginChallenge(context);
            return;
        }

        // ユーザーを作成する。
        user = context.getSession().users().addUser(context.getRealm(), uniqueId);
        user = response.toUserModelAttributes(user);
        user.setEnabled(true);
        context.setUser(user);
        context.success();
    } 
}
