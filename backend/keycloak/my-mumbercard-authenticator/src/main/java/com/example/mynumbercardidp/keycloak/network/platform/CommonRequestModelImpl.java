package com.example.mynumbercardidp.keycloak.network.platform;

import java.lang.reflect.Field;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容とプラットフォームへ送信するHTTPリクエスト内容で共通する構造体を表すインターフェイスです。
 */
public interface CommonRequestModelImpl {
    public CertificateType getCertificateType();
    public CommonRequestModelImpl setCertificateType(CertificateType certificateType);
    public String getCertificate();
    public CommonRequestModelImpl setCertificate(String certificate);
    public String getApplicantData();
    public CommonRequestModelImpl setApplicantData(String applicantData);
    public String getSign();
    public CommonRequestModelImpl setSign(String sign);
    public default Object get(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Class<?> myClass = this.getClass();
        Field field = myClass.getDeclaredField(fieldName);
        return field.get(fieldName);
    }
}
