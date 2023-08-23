package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.ActionType;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.LoginAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.ReplacementAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.RegistrationAction;
import com.example.mynumbercardidp.keycloak.core.authentication.application.procedures.AbstractActionResolver;
import com.example.mynumbercardidp.keycloak.core.authentication.application.procedures.ApplicationProcedure;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import org.keycloak.authentication.AuthenticationFlowContext;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出すクラスです。
 */
public class ActionResolver extends AbstractActionResolver {
    private static final String USER_ACTION_PACKAGE_NAME = "com.example.mynumbercardidp.keycloak.authentication.application.procedures.user";
    private static final LoginAction LOGIN_ACTION = new LoginAction();
    private static final RegistrationAction REGISTRATION_ACTION = new RegistrationAction();
    private static final ReplacementAction REPLACEMENT_ACTION = new ReplacementAction();
    private ApplicationProcedure action;

    public ActionResolver() {
        super(ActionResolver.USER_ACTION_PACKAGE_NAME);
    }

    @Override
    protected void preAction(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        this.action = this.resolveAction(platform);
        this.action.preAction(context, platform);
    }

    @Override
    public void onAction(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        this.action.onAction(context, platform);
    }

    @Override
    public void postAction(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        this.action.postAction(context, platform);
    }

    /**
     * ActionTypeで定義されたユーザーの要求に応じた処理のインスタンスを返します。
     *
     * @param platform プラットフォーム APIクライアントのインスタンス
     * @return ユーザーの希望する処理が定義されているクラスのインスタンス
     */
    protected ApplicationProcedure resolveAction(final PlatformApiClientImpl platform) {
        ActionType userAction = this.getActionMode(platform);
        switch (userAction) {
            case LOGIN:
                return ActionResolver.LOGIN_ACTION;
            case REGISTRATION:
                return ActionResolver.REGISTRATION_ACTION;
            case REPLACEMENT:
                return ActionResolver.REPLACEMENT_ACTION;
        }
        String message = "Action name " + userAction + " is the undefined.";
        throw new IllegalArgumentException(message);
    }

    /**
     * ユーザーの要求からActionTypeで定義された値を返します。
     *
     * @param platform プラットフォーム APIクライアントのインスタンス
     * @return ユーザーが希望する処理の種類の値
     * @exception IllegalArgumentException ActionTypeで定義されていない要求があった場合
     * @exception NullPointerException ユーザーの要求に値が存在しない場合
     */
    protected ActionType getActionMode(final PlatformApiClientImpl platform) {
        String actionMode = super.extractActionMode(platform).toUpperCase();
        return Enum.valueOf(ActionType.class, actionMode);
    }
}
