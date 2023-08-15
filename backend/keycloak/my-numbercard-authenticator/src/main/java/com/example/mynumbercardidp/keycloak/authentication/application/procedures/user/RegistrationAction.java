package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.AbstractUserAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.network.platform.CommonResponseModel;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClientImpl;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;

import java.net.http.HttpTimeoutException;
import java.net.URISyntaxException;
import java.util.Objects;
import javax.ws.rs.core.Response;

/**
 * 個人番号カードの公的個人認証部分を利用したプラットフォームからの応答を元に登録処理をし、ユーザーへのレスポンスを設定する定義です。
 */
public class RegistrationAction extends AbstractUserAction {

    private static final String ACTION_NAME = "registration";
    private static Logger consoleLogger = Logger.getLogger(RegistrationAction.class);
   
    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からユーザーをKeycloakに登録します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void execute(AuthenticationFlowContext context, PlatformApiClientImpl platform) { 
        // プラットフォームへデータを送信する。
        platform.action();
        int platformStatusCode = platform.getPlatformResponse().getHttpStatusCode();
        if (! new FlowTransition().canAction(context, platformStatusCode)) {
            return;
        }

        /*
         * Keycloak内にユーザーが存在する場合は認証画面を表示する。
         */
        platform.ensureHasUniqueId();
        String uniqueId = platform.getPlatformResponse().getUniqueId();
        UserModel user = findUser(context, uniqueId);
        if (Objects.nonNull(user)) {
            actionLoginChallenge(context);
            return;
        }

        // ユーザーを作成する。
        user = context.getSession().users().addUser(context.getRealm(), uniqueId);
        user = platform.addUserModelAttributes(user);
        user.setEnabled(true);
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
            if (status == Response.Status.UNAUTHORIZED.getStatusCode()) {
                actionUnauthorized(context);
                return false;
            }
            if (status == Response.Status.CONFLICT.getStatusCode()) {
                actionReChallenge(context, "login", status);
                return false;
            }
            actionUndefinedFlow(ACTION_NAME);
            return false;
        }
    }
}
