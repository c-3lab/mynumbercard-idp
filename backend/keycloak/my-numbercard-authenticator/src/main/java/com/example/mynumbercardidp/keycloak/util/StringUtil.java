package com.example.mynumbercardidp.keycloak.util;

import java.util.Objects;

/**
 *  String型変数に関連するユーティリティクラスです。
 */
public class StringUtil {
    /**
     * String型がNullまたは文字列の長さがゼロであるかを判定します。
     *
     * @param str Nullまたは長さがゼロであるか判定したい文字列
     * @return Nullまたは長さがゼロの場合はtrue、そうでない場合はfalse
     */
    public static boolean isStringEmpty(String str) {
        return Objects.isNull(str) || str.length() == 0;
    }
}
