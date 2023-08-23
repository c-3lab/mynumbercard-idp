package com.example.mynumbercardidp.keycloak.util;

import java.util.Objects;

/**
 *  String型変数に関連するユーティリティクラスです。
 */
public class StringUtil {

    private StringUtil() {}

    /**
     * String型がNullまたは文字列の長さがゼロであるかを判定します。
     *
     * @param str Nullまたは長さがゼロであるか判定したい文字列
     * @return Nullまたは長さがゼロの場合はtrue、そうでない場合はfalse
     */
    public static boolean isEmpty(final String str) {
        return Objects.isNull(str) || str.length() == 0;
    }

    /**
     * String型がNullまたは文字列の長さがゼロでは無いことを判定します。
     *
     * @param str Nullまたは長さがゼロでは無いことを判定したい文字列
     * @return Nullまたは長さがゼロでは無い場合はtrue、そうでない場合はfalse
     */
    public static boolean isNonEmpty(final String str) {
        return Objects.nonNull(str) &&  0 < str.length();
    }

    public static String toFirstUpperCase(final String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
