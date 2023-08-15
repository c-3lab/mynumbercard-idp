package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.AbstractUserAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.network.CommonResponseModel;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient;
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

    private static Logger consoleLogger = Logger.getLogger(LoginAction.class);
   
    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からKeycloak内の認証を実施します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void execute(AuthenticationFlowContext context, PlatformApiClient platform) { 
        // プラットフォームへデータを送信する。
        CommonResponseModel response = platform.action();
        int platformStatusCode = platform.getPlatformResponse().getHttpStatusCode();
        if (! new FlowTransition().canAction(context, platformStatusCode)) {
            return;
        }

        // ユニークIDからKeycloak内のユーザーを探す。
        String uniqueId = response.getUniqueId();
        if (Objects.isNull(uniqueId) || uniqueId.length() == 0) {
            // ユニークIDが見つからなかった場合
            return;
        }
        UserModel user = findUser(context, uniqueId);

        /*
         * Keycloak内にユーザーが存在しない場合は登録画面を表示する。
         */
        if (Objects.isNull(user)) {
            Response httpResponse = ResponseCreater.createChallengePage(context, "registration", platformStatusCode);
            ResponseCreater.setFlowStepChallenge(context, httpResponse);
            return;
        }

        context.setUser(user);
        context.success();
    } 

    /**
     * プラットフォームのステータスコードによる処理の遷移を定義するクラスです。
     */
    private class FlowTransition extends CommonFlowTransition {
        /* Actionクラスはこのメソッドを呼び出す */
        protected boolean canAction(AuthenticationFlowContext context, int status) {
            if (super.canAction(context, status)) {
                return true;
            }

            // [TODO] ステータスコードで分岐する処理を記載する
            if (status == 401) {
                return false;
            }

            if (status == 404) {
                Response httpResponse = ResponseCreater.createChallengePage(context, "registration", status);
                ResponseCreater.setFlowStepChallenge(context, httpResponse);
                return false;
            }

            if (status == 410) {
                return false;
            }

            return false;
        }
    }
}
