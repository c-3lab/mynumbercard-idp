package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.jboss.logging.Logger;

/**
 * プラットフォームのステータスコードによる処理の遷移を定義するクラスです。
 *
 * このクラスは継承されることを前提としています。
 *
 * 警告：デバッグログは開発者向けの重要な情報です。
 *       不用意に削除すると開発や保守のコストが増大します。
 */
public class CommonFlowTransition {

    /** コンソール用ロガー */
    protected static final Logger CONSOLE_LOGGER;

    static {
         CONSOLE_LOGGER = Logger.getLogger(new Object(){}.getClass());
    }

    protected boolean canAction(AuthenticationFlowContext context, int status) {
        CONSOLE_LOGGER.debug("Platform response status code: " + status);

        if (status == 200) {
            return true;
        }

        if (status == 400) {
            ResponseCreater.setFlowStepChallenge(context, ResponseCreater.createChallengePage(context, status));
            return false;
        }

        if (status == 500) {
            // [TODO] 処理を記載する
            return false;
        }

        if (status == 503) {
            // [TODO] 処理を記載する
            return false;
        }

        return false;
    }
}
