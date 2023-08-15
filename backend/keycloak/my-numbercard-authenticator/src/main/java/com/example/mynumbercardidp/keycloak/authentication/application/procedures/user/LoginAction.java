package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.AbstractUserAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformResponseModel;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;

import java.net.http.HttpTimeoutException;
import java.net.URISyntaxException;
import java.util.Objects;
import javax.ws.rs.core.Response;

/**
 * 個人番号カードの公的個人認証部分を利用したプラットフォームからの応答を元に認証処理をし、ユーザーへのレスポンスを設定する定義です。
 */
public class LoginAction extends AbstractUserAction {

    private static final String ACTION_NAME = "login";
    private static Logger consoleLogger = Logger.getLogger(LoginAction.class);
   
    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からユーザーを認証します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void execute(AuthenticationFlowContext context, PlatformApiClientImpl platform) { 
        // プラットフォームへデータを送信する。
        platform.action();
        PlatformResponseModel platformResponse = (PlatformResponseModel) platform.getPlatformResponse();
        int platformStatusCode = platformResponse.getHttpStatusCode();
        if (! new FlowTransition().canAction(context, platformStatusCode)) {
            return;
        }

        /*
         * ユニークIDからKeycloak内のユーザーを探す。
         * Keycloak内にユーザーが存在しない場合は登録画面を表示する。
         */
        platform.ensureHasUniqueId();
        String uniqueId = platformResponse.getUniqueId();
        UserModel user = findUser(context, uniqueId);
        if (Objects.isNull(user)) {
            actionRegistrationChallenge(context);
            return;
        }

        context.setUser(user);
        context.success();
    } 

    /**
     * プラットフォームが応答したステータスコードによる処理の遷移を定義するクラスです。
     */
    protected class FlowTransition extends CommonFlowTransition {
        @Override
        protected boolean canAction(AuthenticationFlowContext context, int status) {
            platformStatusCode = status;
            if (super.canAction(context, status)) {
                return true;
            }
            if (status == Response.Status.NOT_FOUND.getStatusCode()) {
                actionRegistrationChallenge(context);
                return false;
            }
            if (status == Response.Status.UNAUTHORIZED.getStatusCode()) {
                actionUnauthorized(context);
                return false;
            }

            if (status == Response.Status.GONE.getStatusCode()) {
                actionReChallenge(context, "replacement", platformStatusCode);
                return false;
            }
            actionUndefinedFlow(ACTION_NAME);
            return false;
        }
    }
}
