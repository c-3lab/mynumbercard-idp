package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClientImpl;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;

import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.Response;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出す抽象クラスです。
 */
public abstract class AbstractActionHandler implements ApplicationProcedure {

    private static final String MY_PACKAGE_NAME;
    private static final String USER_ACTION_PACKAGE_PREFIX_NAME = ".user";
    private static final String USER_ACTION_PACKAGE_NAME;
    private static final String USER_ACTION_CLASS_NAME_SUFFIX = "Action";
    private static Logger consoleLogger;
    protected ApplicationProcedure action;

    static {
        // パッケージ名の自動取得
        Class<?> my = new Object(){}.getClass();
        MY_PACKAGE_NAME = my.getPackageName();
        USER_ACTION_PACKAGE_NAME = MY_PACKAGE_NAME + USER_ACTION_PACKAGE_PREFIX_NAME;
        consoleLogger = Logger.getLogger(my);
    }

    protected AbstractActionHandler() {}

    /**
     * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    @Override
    public void execute(AuthenticationFlowContext context, PlatformApiClientImpl platform) {
        String actionMode = platform.getUserActionMode();
        actionMode = actionMode.substring(0, 1).toUpperCase() + actionMode.substring(1);
        String actionClass = USER_ACTION_PACKAGE_NAME + "." + actionMode + USER_ACTION_CLASS_NAME_SUFFIX;
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

    }

    /**
     * 内部エラーのレスポンスを設定します。
     *
     * 指定されたアクションに対応するクラスまたはメソッドが見つからなかった時に呼び出されます。
     * @param context 認証フローのコンテキスト
     * @param actionMode 要求された処理の種類
     * @param type 見つからなかったクラスまたはメソッド
     */
    protected void executeNotFoundAction(AuthenticationFlowContext context, String actionMode, NotFoundType type) {
        consoleLogger.warn("Not found " + actionMode + " action " + type.name().toLowerCase() + ".");
        ResponseCreater.createInternalServerErrorPage(context);
    }

    /**
     * 内部エラーのレスポンスを設定します。
     *
     * アクションが指定されなかった時に呼び出されます。
     * @param context 認証フローのコンテキスト
     */
    protected void executeNotFoundActionFiled(AuthenticationFlowContext context) {
        consoleLogger.warn("Not found filed action mode in user request.");
        ResponseCreater.createInternalServerErrorPage(context);
    }

    /**
     * 内部エラーのレスポンスを設定します。
     *
     * アクションに対応するクラスのインスタンスを生成できなかった時に呼び出されます。
     * @param context 認証フローのコンテキスト
     */
    protected void executeFailedNewInstance(AuthenticationFlowContext context, String actionClass) {
        consoleLogger.error("Failed create instance for " + actionClass + ".");
        ResponseCreater.createInternalServerErrorPage(context);
    }

    /**
     * 内部エラーのレスポンスを設定します。
     *
     * 配列以外のインスタンス作成、フィールドの設定または取得、メソッドの呼出しを試みた場合に呼び出されます。
     * @param context 認証フローのコンテキスト
     */
    protected void executeIllegalAccess(AuthenticationFlowContext context, IllegalAccessException e) {
        consoleLogger.error("Tried illegal cccess. See the stack trace below.");
        e.printStackTrace();
        ResponseCreater.createInternalServerErrorPage(context);
    }

    /**
     * 内部エラーのレスポンスを設定します。
     *
     * 呼び出し先のアクションクラスで補足できなかった例外を補足した場合に呼び出されます。
     * @param context 認証フローのコンテキスト
     */
    protected void executeUnreportedException(AuthenticationFlowContext context, InvocationTargetException e) {
        consoleLogger.error("Unreported exception. See the stack trace below.");
        e.getCause().printStackTrace();
        ResponseCreater.createInternalServerErrorPage(context);
    }

    protected enum NotFoundType {
        CLASS,
        METHOD;
    }
}
