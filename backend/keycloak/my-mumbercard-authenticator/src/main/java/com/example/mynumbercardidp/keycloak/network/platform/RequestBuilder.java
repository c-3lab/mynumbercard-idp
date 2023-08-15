package com.example.mynumbercardidp.keycloak.network.platform;

import org.keycloak.authentication.AuthenticationFlowContext;

import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容を元にデータ構造体を作成するクラスです。
 */
public class RequestBuilder implements RequestBuilderImpl {

    /** Keycloakがユーザーから受け取ったHTMLフォームデータ */
    private MultivaluedMap<String, String> formData;

    /** Keycloakがユーザーから受け取ったHTMLフォームデータの構造体 */
    private CommonRequestModelImpl userRequest;

    /** プラットフォームへ送るデータの構造体 */
    private CommonRequestModelImpl platformRequest;

    /**
     *  HTTPリクエスト内容を元に作成するデータ構造体のインスタンスを初期化します。
     *
     * @param context 認証フローのコンテキスト
     */
    public RequestBuilder(AuthenticationFlowContext context) {
        formData = RequestBuilderImpl.extractFormData(context);
    }

    /**
     *  ユーザーリクエストのデータ構造体でインスタンスを初期化します。
     *
     * @param context 認証フローのコンテキスト
     */
    public RequestBuilder(CommonRequestModelImpl request) {
        userRequest = request;
    }

    /**
     * ユーザーリクエスト構造のインスタンスを返します。
     *
     * @return ユーザーリクエスト構造のインスタンス
     */
    public CommonRequestModelImpl getUserRequest() {
        return userRequest;
    }

    /**
     * プラットフォームリクエスト構造のインスタンスを返します。
     *
     * @return プラットフォーム構造のインスタンス
     */
    public CommonRequestModelImpl getPlatformRequest() {
        return platformRequest;
    }

    /**
     * ユーザーリクエスト構造のインスタンスを設定します。
     *
     * @param request ユーザーリクエスト構造のインスタンス
     */
    public void setUserRequest(CommonRequestModelImpl request) {
        userRequest = request;
    }

    /**
     * プラットフォームリクエスト構造のインスタンスを設定します。
     *
     * @param request プラットフォーム構造のインスタンス
     */
    public void  setPlatformRequest(CommonRequestModelImpl request) {
        platformRequest = request;
    }

    /**
     * ユーザーのリクエスト構造体からプラットフォームへのリクエスト構造体に変換します。
     * 
     * @param platformRequestModel プラットフォームへ送るリクエスト構造体のクラス
     * @param sender プラットフォームが識別するIDプロバイダーの送信者符号
     * @return ユーザーリクエスト構造のインスタンス
     */
    public CommonRequestModelImpl toPlatformRequest(Class<? extends CommonRequestModelImpl> platformRequestModel, String sender) {
        try {
            CommonRequestModelImpl model = platformRequestModel.getDeclaredConstructor()
                .newInstance();
            // インスタンスの初期化
            platformRequest = model.setCertificateType(userRequest.getCertificateType())
               .setCertificate(userRequest.getCertificate())
               .setApplicantData(userRequest.getApplicantData())
               .setSign(userRequest.getSign());
            return platformRequest;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // /**
    //  * ユーザーのリクエスト構造体からプラットフォームへのリクエスト構造体に変換します。
    //  * 
    //  * @param userRequest ユーザーリクエスト構造体のインスタンス
    //  * @param platformRequestModel プラットフォームへ送るリクエスト構造体のクラス
    //  * @param sender プラットフォームが識別するIDプロバイダーの送信者符号
    //  * @return ユーザーリクエスト構造のインスタンス
    //  */
    // protected CommonRequestModel toPlatformRequest(CommonRequestModel userRequest, Class<? extends CommonRequestModel> platformRequestModel, String sender) {
    //     // インスタンスの初期化
    //     platformRequest = platformRequestModel.getDeclaredConstructor(sender).setCertificateType(userRequest.getCertificateType())
    //        .setCertificate(userRequest.getCertificate())
    //        .setApplicantData(userRequest.getApplicantData())
    //        .setSign(userRequest.getSign());
    //     return platformRequest;
    // }

    // /**
    //  * 認証フローのコンテキストからHTMLフォームデータを抽出します。
    //  *
    //  * @param context 認証フローのコンテキスト
    //  * @return ユーザーが送ったHTMLフォームデータのマップ
    //  */
    // public static final MultivaluedMap<String, String> extractFormData(AuthenticationFlowContext context) {
    //     return context.getHttpRequest().getDecodedFormParameters();
    // }

    // /**
    //  * HTMLフォームデータをユーザーのリクエスト構造体へ変換します。
    //  * 
    //  * @param data ユーザーが送ったHTMLフォームデータのマップ
    //  * @param requestModelClass ユーザーリクエスト構造体のクラス
    //  * @return ユーザーリクエスト構造のインスタンス
    //  */
    // protected CommonRequestModel toUserRequest(MultivaluedMap<String, String> data, Class<? extends CommonRequestModel> userRequestModel) {
    //     // インスタンスの初期化
    //     userRequest = userRequestModel.getClass().getDeclaredConstructor().newInstance(); 

    //     // インスタンスへHTMLフォームデータを代入
    //     userRequest.setActionMode(data.getFirst(UserRequestModel.Filed.ACTION_MODE.name()))
    //         .setNonce(data.getFirst(UserRequestModel.Filed.NONCE.name()))
    //         .setApplicantData(data.getFirst(UserRequestModel.Filed.APPLICANT_DATA.name()))
    //         .setSign(data.getFirst(UserRequestModel.Filed.SIGN.name()));

    //     // modeパラメータに対応する証明書情報を代入
    //     switch (Enum.valueOf(userRequestModel.ActionMode, userRequest.getActionMode())) {
    //         case userRequestModel.ActionMode.LOGIN:
    //             userRequest.setCertificateType(data.getFirst(CommonRequestModel.CertificateType.USER_AUTHENTICATION));
    //             break;
    //         case userRequestModel.ActionMode.REGISTRATION:
    //         case userRequestModel.ActionMode.REPLACEMENT:
    //             userRequest.setCertificateType(data.getFirst(CommonRequestModel.CertificateType.ENCRYPTED_DIGITAL_SIGNATURE));
    //             break;
    //     }
    //     String certificateTypeName = Enum.valueOf(CommonRequestModel.CertificateType, userRequest.getCertificateType()).name();
    //     userRequest.setCertificate(data.getFirst(certificateTypeName));
    //     return userRequest;
    // }


}

