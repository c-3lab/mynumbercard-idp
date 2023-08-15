package com.example.mynumbercardidp.keycloak.network.platform;

import java.lang.reflect.Field;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容とプラットフォームへ送信するHTTPリクエスト内容で共通する構造体を表すインターフェイスです。
 */
public interface CommonRequestModelImpl {
    CertificateType getCertificateType();
    CommonRequestModelImpl setCertificateType(CertificateType certificateType);
    String getCertificate();
    CommonRequestModelImpl setCertificate(String certificate);
    String getApplicantData();
    CommonRequestModelImpl setApplicantData(String applicantData);
    String getSign();
    CommonRequestModelImpl setSign(String sign);
    // static Object get(Class<? extends CommonRequestModel> instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
    //     Field field = instance.class.getDeclaredField(fieldName);
    //     return field.get(fieldName);
    // }
}
