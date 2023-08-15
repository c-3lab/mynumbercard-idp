package com.example.mynumbercardidp.keycloak.core.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;

import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.Response;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出す抽象クラスです。
 *
 * このクラスを継承したサブクラスは、ユーザーの希望する処理が定義されているクラスのパッケージ名を定義する必要があります。
 */
public abstract class AbstractActionHandler implements ApplicationProcedure {

    /** ユーザーの希望する処理が定義されているクラスが存在するパッケージ名 */
    protected String userActionPackageName;
    /** ユーザーの希望する処理が定義されているクラス名の接尾文字列 */
    protected String userActionClassNameSuffix = "Action";
    /** ユーザーの希望する処理が定義されているクラスのインスタンス */
    protected ApplicationProcedure action;

    protected AbstractActionHandler() {}

    /**
     * ユーザーからKeycloakへ送られたHTTPリクエストを元に、実行する処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void execute(AuthenticationFlowContext context, PlatformApiClientImpl platform) {
        String actionMode = platform.getUserActionMode();
        actionMode = actionMode.substring(0, 1).toUpperCase() + actionMode.substring(1);
        String actionClass = userActionPackageName + "." + actionMode + userActionClassNameSuffix;
        try {
            action = (ApplicationProcedure) Class.forName(actionClass)
                .getDeclaredConstructor()
                .newInstance();
            action.preExecute(context, platform);
            action.execute(context, platform);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    } 

    @Override
    public void preExecute(AuthenticationFlowContext context, PlatformApiClientImpl platform) {
        action.preExecute(context, platform);
    }
}
