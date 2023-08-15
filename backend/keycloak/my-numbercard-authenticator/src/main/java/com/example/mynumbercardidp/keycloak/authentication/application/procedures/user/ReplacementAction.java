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
 * 個人番号カードの公的個人認証部分を利用したプラットフォームからの応答を元にユーザー情報を更新する定義です。
 */
public class ReplacementAction extends AbstractUserAction {
    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からKeycloak内のユーザー情報を更新します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void onAction(AuthenticationFlowContext context, PlatformApiClientImpl platform) { 
        PlatformResponseModelImpl response = platform.getPlatformResponse();
        int platformStatusCode = response.getHttpStatusCode();
        if (! new ReplacementFlowTransition().canAction(context, Response.Status.fromStatusCode(platformStatusCode))) {
            return;
        }

        /*
         * ユニークIDからKeycloak内のユーザーを探す。
         * Keycloak内にユーザーが存在しない場合は登録画面を表示する。
         */
        String uniqueId = super.tryExtractUniqueId(response);
        UserModel user = super.findUser(context, uniqueId);
        if (Objects.isNull(user)) {
            ResponseCreater.actionRegistrationChallenge(context);
            return;
        }
        context.setUser(user);

        // ユーザーの情報を更新する。
        response.toUserModelAttributes(context.getUser());
        context.success();
    } 
}
