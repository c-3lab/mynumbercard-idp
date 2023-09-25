package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.AbstractUserAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformAuthenticationResponse;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

import java.util.Objects;
import javax.ws.rs.core.Response;

/**
 * 個人番号カードの公的個人認証部分を利用したプラットフォームからの応答を元に登録処理をし、ユーザーへのレスポンスを設定する定義です。
 */
public class RegistrationAction extends AbstractUserAction {
    private RegistrationFlowTransition flowTransition = new RegistrationFlowTransition();

    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からユーザーをKeycloakに登録します。
     *
     * @param context  認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    public void register(AuthenticationFlowContext context, PlatformApiClientInterface platform) {
        if (!super.validateSignature(context, platform)) {
            ResponseCreater.setLoginFormAttributes(context);
            Response response = ResponseCreater.createChallengePage(context, Messages.INVALID_REQUEST, null,
                    Response.Status.BAD_REQUEST);
            context.challenge(response);
            return;
        }

        platform.sendRequest();
        PlatformAuthenticationResponse response = (PlatformAuthenticationResponse) platform.getPlatformResponse();
        if (!this.flowTransition.canExecuteRegistration(context,
                Response.Status.fromStatusCode(response.getHttpStatusCode()))) {
            return;
        }

        String uniqueId = super.tryExtractUniqueId(response);
        UserModel user = super.findUser(context, uniqueId);
        if (Objects.nonNull(user)) {
            ResponseCreater.sendChallengeResponse(context, ActionType.LOGIN.getName(), Response.Status.CONFLICT);
            return;
        }

        user = context.getSession().users().addUser(context.getRealm(), uniqueId);
        user = response.toUserModelAttributes(user);
        user.setEnabled(true);
        context.setUser(user);
        context.success();
    }
}
