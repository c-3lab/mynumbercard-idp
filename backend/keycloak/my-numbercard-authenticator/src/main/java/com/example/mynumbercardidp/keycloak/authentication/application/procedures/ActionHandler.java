package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClientImpl;
import org.keycloak.authentication.AuthenticationFlowContext;

import javax.ws.rs.core.Response;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出すクラスです。
 *
 * ActionHandlerとしての機能の追加、変更はこのクラスを修正するか、
 * ActionHandlerのサブクラスとして新規でクラスを作成してください。
 */
public class ActionHandler extends AbstractActionHandler {

    /*
     * ユーザーが希望する動作が定義されているクラスのパッケージ名が異なる場合は
     * 以下の定数を定義するか、サブクラスを作成してください。
     *
     *  private static final USER_ACTION_PACKAGE_PREFIX_NAME = ".user";
     *  private static final USER_ACTION_PACKAGE_NAME = MY_PACKAGE_NAME + USER_ACTION_PACKAGE_PREFIX_NAME;
     */

    public ActionHandler() {

    }

    /**
     * 処理分岐前の事前処理をします。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     * @exception Exception 何らかの例外が発生した場合（あらゆる例外は認証SPIで補足できない場合、認証フローを正常に終了することができません。）
     */
    @Override
    public void preExecute(AuthenticationFlowContext context, PlatformApiClientImpl platform) {
        action.preExecute(context, platform);
    }
}
