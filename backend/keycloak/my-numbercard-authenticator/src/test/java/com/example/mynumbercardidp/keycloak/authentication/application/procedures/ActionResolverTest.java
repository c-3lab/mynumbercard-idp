package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticatorConfigModel;
import org.mockito.*;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.LoginAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.RegistrationAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.ReplacementAction;
import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;
import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient;

public class ActionResolverTest {

  private String certificateStr;
  private String signStr;

  @InjectMocks
  ActionResolver actionResolver;

  @Mock
  AuthenticationRequest authenticationRequest;
  @Mock
  AuthenticationFlowContext context;
  @Mock
  HttpRequest httpRequest;
  @Mock
  AuthenticatorConfigModel authenticatorConfigModel;
  @Mock
  PlatformApiClient platformApiClient;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    certificateStr = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.pSbLIkV3vVbtQ2ddCDlFfej--YiNS-v_tn3YKnidlStaXxW2CcapTudbLlNCJmXggRPXTdCo-9maHBkld4wTwrLncphMz9fsnPk7QDbu3WqBlrwRLgVqcOddKirk0FqTj2IVKqDJCfZ-JUrr5KW0EPNOtx4LrvIJSBOr7mUpwxZ2ZbeXtpTxyLBHuSo-3YSXqEVzT9twBF7FyNTi9RzJGXVsXHjbbwufYNOnaUawnLa4xTNDQNb3qwc2r7zMRCNwzuHprnu0U5pFblnZHnGiYtQ4Bf5t9J7f0MgAn08vnKkTYmDaw_IgHtQmkESj0a_GCo-LVmdd0TBQH27v7ktL7w.l92Waa59-LZIrwhZch2M5w.VHjaRmTr-8nhhHTQd71oKbrUVHp8KQbRJdUC1J5XFRqG52HD_PLwA5sI8sO1IlPuk5KETNFSIEij21c1zlU0nmggrJQJTun8lIVAnAA1VStC0U2kHFy08kHmyyUTNlmWeIyPgO2L0tSUJRyz9LbBg_oCx71ed1d8gYJmGWqCxkxE3vsn336ixVjjuvYW6jniPrdnsWBeiNTpGUqMCStlSA_bm0C9NQgMw75HYMlFmamtfkMYIlhh5qYRKYOd569GTIuW0IaGIREhpNsGLvQ1ojh6KRn4RhyVenp1R-kKbAaZfSiXzL84se3Py4lUqP_ihg5p0mpCs9JvdvTo0sRSy2AnZmEKC6gQLYIk6eS8etm9OxqXPd32t0cPm5yhxf2-g5McypfZ8SrZqlikZ8_zKIOhboWUFUY1GJawkDJRv-2RoLmb4RGQMpV_M95IgpTfrIYQkOe3OTPQ9iEDLK9ixgVyn4unAiIIbuqMe3et68Kcafe89QMI8ZzJCryFEwMfpUwK0v1TZmNojFJ1w1ifjr78uTI26FupxDoSGxcnMlK7M_AKStFlyTw_wYPfWJrubYYVH_cFYdoMEZkq2Y2N1ebluG7AAL4u5z-7Ug0P5tIlKwR1DZ6b_2r2XzaC-Y60HzKa7OwpkGVmWk6QQOWhkVdfLA_HTLadlTBJJREtrtCOWgyYSe2g5-7SIEXe1i8_XKT5waNwnyGC3fQlP9-EfvTISVQi8gxjFlFQ3tr0EI7QWSN5VGBDw5K8gbOqz3_HUwtFf6CSF0IZYV3PSEeMPUZvtQi_tUbtpntOwTeIpE-lfBVU8vnD6Txx0tb2G5PShxwb2ENZJV_OcMprnaqN-kIAlMPG8GqqTWZnqulv6XD8fMmYXqGZRzuAbBRwPsofmXj2BMl2u687bHD5wEmHTrpsYPU6BE1ykYKdD00IARlgt822Ws9Gvbc-FOKQOGpk1AKgpyrLb0zOmF2Cc23XHyaAGsvU8EcOu5IKgqvC-3GfBqRROMCmoVshIq5Q7ouBevai_ST-Kifj1csRhHQ6Zoay6vy80nYSIWDl2Gdw24mmrfrRD-qBLHXKyhiDzSBcD3QRVtxwF_ExwLR_uMlDM_VPTjsd_NDt-W7_dMXu9bvu2u-ZvVMyxSdelVWndPGqhHA8zKPvNU0H_VvJ-iRqAvDEnPP-dpiSfkq2RHhZ-qqN-tK9dJwE7qBy8jkqMZEDsnLYKRpoXAZg_P-fs6Yc9nvTBN3PY3lVbHx0qzDwTisW7eh51EHzt0LCsv31hwkXIlcvEURjX0ioyy1pCvN9-wjtRd9r6IrA8b2S3QJ2FLFfbYTmbzSToqjDam4lhav9IykO1MAzBo_ayiwa4W_T92onXrAlMb3CeJdDu8VBdS_UXwqRHXYNkCuQahgeWci3F8DlkQz0mT20ahU6oOQLuKKx9hs0KNNQRSQonMNx3PBKq8DwhyZ4rCVaqSjhEzWsK5Zb3Jda71uIjY4Sbx5VCDZunhh8kf4BHtZHhOWXwkze2aOWXH6UnY-d8pTkGW8-dWdZxv-oiM3rfuiP8FhuPM65FG3vH6AuwOhEEEhRGYwhQpQRH0Ylgjp3NdbJeozsSFuzk2fpUKZUfjAVoI3N8zwSpfyiSVmAuJazR-jadT-HeoAjWYG7-MtrflvRuwBDOP_1wqfS6farr5EPBmwFUu5XVyqr_1l0ottBbt_LHc63zE97Nxwqum39uQ83wBP-JA6oLuDEcpY5d-TUFeI3oG_6SUZW2qgCSXHLUsSB812VhQatAV7UevRUR_g7r-jIg8rtxmakVv5C3z1po6Ql3Yt0UhI8q_Io8YW8HO2z2HIkY7pcTxGx7uQrOOfiYK4A1-oF7E9jVPaU0R7BnH5j8BUsq46dBftfghEd0MZBgR1AUtvv5RbvPJKk-gv7X-Cts08lArUR9Xfnr8UfRFsoPSceg3C5TKY0WIjHpLmd4Df9YR9a_x54xO84q7jyDwnvHy1YTgqXnodaff5fAbxjkrJR52bDWfZbeQCtB0-TgByKDuFKfp7iuX_eBAggSynSvzGfzkj0AdFr24TWUBdqC0qWJ0tWLGN1xfrIEoDDTLMsFWEN1HAzl3VYzepGxi6Vml8Px196hc39pOkQIafcvJ1XCMVviAkW0skkuNdw60wc8BF62ujGunWwsSSknqh_fNufwtlF30hdA3-A0Ndjc9amhQTHHe7IkBP5_vgvDjNUeHo_x7mzTtT78H6wbt4isXkpXgEeKArjWKTDch7iBDA9IuW_lZSpDsO9SxUdnCkHsUwGCocipkOQeIlcFtfp9jqjCPA8XAcalG0y2yhygauNDkGtm4to9tPWbWkrzi4hVXHkVZmhCStiDP0wbXmoA_NODOb1VWamoviXtxX1KsPFfxspew2rUmDcszKe0eDFaEmLOBeIcbfRuN8P7u12RpZZz6SAZWo8nHKHOBra-NvAsT15qfeP9Egv6_is2VBOB6aa2di3EG8R4kKk-VR6FQHyUAhZ2NiyMIMp97ONMqDadldhX7REe31jZpvsPoZ95s9dRZyWPeCxo2aKmurWwONyUqVHVdarFK2M6bWXDL-xi_V-B82evFjqVxSDKPaOsg75nakjWwdHIawmsD6CJgBw5oGZNYrkHlFUgPF1mqJh10lpVhhLJGuHrZWoZjh1j8ZZ9vsVxasUWDrbeYLCYnCeMXjmwGJM_hdhkYzY4DWxTHZKcGuiNDHU_K9JOjBhv-thfd5nTiVWU_x4Ap-I_B3RMyFY_aYrXQVFtq_uSQSk6K3MOBSh9FAQWzhl0PX90FuBHDFNQ7Fh1OXFLoJfpnD8XcPB0I30MGPbYT5QfOaJJCyuoCVVoO9m_O3AvNHz04yWnELF83h91epn5gVWwstEZh8rBPqL6MzNB9e_Uq3jCvaiimlFZDC9WGDiwZheYOeiZDp_WVNG_RwMpMLtyrgfZnsE_CJnGZ3y5ICdBKub_2Hc0tXn7XzmXDq25UR--vfruF9b5QXy6dxURqd-jkeK3c-of1MUp_mlaynZifAQGUn9MhypHsoZggRNclQLkohqlzLvHOTQyRPDlTVbqOSjDk3KmzfTnCXZCI0Q1_Po1NeKKnpva9s-kD1aj9Tf2QxS2JJmXbBb1VL9le_-jqZjQXjaJb3FdRQ7A2vGbEEBJyKGfKypCXUVRyHIYC8wFgayrTZbZvwUYLQasRojwm-R_FBg1GGwNfzAiELAzEbV-1X2IkjtmB8VNbupAe8mlO6GF9KA_tL_6peW7W-dFUeAW7GYQ90iDVj117h_SGox8RBYd9lLmarcuVunlmZ3gtnRbdBlWSgC0foaR70qb2C-mD4mfW574XRFcNT5CsVwoSJZU-v8oCvZzKNS451-lyfXNHa6sw_kXwE0DQpehm7qrL_OQmFDlUCgg-oYM3kzGohk8bdmIzPBhKfaS3olBP0-rkJ1y1pP_PNounRlXmLQAhg7Mn2VjmtGiXpv9FpGP6Zz5gHYBKyfXJiiogTDceG2X6sF3xjND-dQKDk5QHujuMT6OboL18leimA_0JApCZ4GCcjYpn732y25ggODJL_m4d4RiOqqsXYYSFax3IyM8ZVCButcD5mlG2wMSQQKdUaOnrLQuC09mohl7KAxnC9OCSjTYJ3w7zCy0VuSYE7pihiGr8hVZRZQ7qrR8Nsd9K0BLX7e2iqc5LCDHIpKDuKqEnFceGM1kNTY_9q36v9m2D-TrBczVGQ-kyBcoJw4O0Hp0c9Y00K8yHBFVZSz-SB0yNhIB-8ElZC0m8EzfwrqhaeXe8KLUYbb4u_AjrjM5QAOda1TpXTNZ8aDyATyZvTT78XHqjgk5RGVC8KX5_LUVDP_hlO7jee0Lxt8geXOzNT1JxMbNCEHKw-vCxXExpwyAarCRBHRbYP_uG8Xtw2WGHGo31GXJiw7SO5PMZIY8dVHpc73y7UZkvu_-tLyRE0_gBUEtX_TCGrJk5zqJVNUDikNySXrE2Ej-qfFDovnN7VSt0GRjPs_V_DtmUJpyk2C_ZeRwWIGtK3hkK9JgfRbr9iRJkQOBNk2BhovhyZ1hb7DiEi7Z6DtrK1qNW6faLjvionCH-sQXourDNGz2CGrKO6T7KSgkaOm0Lw_W-wJ6HQEs7-Smjsat9Al40pCNK_jucs6OFTr_H5z7QzhxemEwJfAtfsMMUD5tVdg8h8fXPz08awl9y5On3wLACAmhoxy5LBFWulaMXrRbJDfWrlg01KJyr9OWPIuH2RC-U8spX30KM2wOj-AreJXKwqQr6MSbe-aikwnsZdfjTMoznTepOM2JTDc2KWJtS_w5QvKt0SVWCYI6ru63Vaih060Lgt-urYDvElKVeGXmCxDJns6RnlAyRK8m_gZt4N7R6hCj5OzVR08y1CFKSnPyNQvQA0wLv44LgpmvXnqILKlIN3OVhJg8t72DK-CX9_wdRjR2rIchgUmHrjfi_NdV-3_LmCKdfbD5AmnT4oZb0KOrMJrxXgdcA8Alsja_8yZslIl5yqWelO5MbUVgTs9TvlkJHVi-0uX_15kavAsHZjhlJt7bGDWVipYS5BH1V-DhT8Q_flw3xkAofgpEZpnrxAC87iVSzhGtH5QXdC5ezH_cunWS8eNcun8TvE-UDxmwQZFB0C6KSsx_2D7lct3GuLNQNT7U3gp2qPZC_iZihuledmvgV7a1eOaYNXqaqlOgC5DN_y7EqiL2krDo9X0I5sJYs0-paGuqUFkT8cm2C05gQcxw_BIVhHDFfwvFvZ6ygDHZQ8HLUYF6XUzsUWN7emG3cyfxUBv9iHYHCLwc8g1lTbv-r-lEalvLh3Ru3z4h_dJ4QJV836VX4T8cREaiYXpB6qvVM76CEpBlJFLUINhlZ-YFvowyk71Xj02ilndYAuZpS5gcJYo1VGFhYFFTHjq2JoZanwofz1mhH8Wir-h4tROjnwxbiaAGYvFAGkHdGT3Ka4WMAgRXsshEfkwv-jS21kTUIo_-3XiBL37VSJAvuwSKv0etHKfwabUqmeT5cWOHYPcckliZ5RDwg_kWKHfNsiPYGPpNUEIJzdvneyj7TT7LjMVUNuQMYTv-9Vin1qXmojgiZAWRLbp-q-xl7L4_q1wAlxzdH_A3t2y7BJ6BCn3EoXVL_6ru1yAeZWnHBvquX6HnvCWeuY54-NSjS81Ju7psJIpwMfdfZwWj9FqiABGV14MSi2_p5I0mYD_0rOWXRgT9zJjm6Ts4r0MTQ6VtfQGUa09PGJcCUit4XAntsQq7Q8T423ZJrLNzrG1Pm12Ej51MLp5rhk1MmaXcjHttlbz2QNHIHNodUNShWFm2WnQieEe7wjpiFXuDw3Zi5CyVKTL-u-OcHv8XvE4N-s5nkbrwmpsOZKZZfxpKhti9ZDWoUai5UMbhNHZi-TQ-v5S2boClbTyKkaqIQ9IUklYvpFb3X7KuV5I6uiOEDTas3B0Vh6tr7VFN9IjgG4Cd0nVPgUd5Dw0ulwd4PIq-0RqJAvRlD208Ph5TMzy8ecq_4yM0rIR0BzepeGkZH1nYEFdD5ZTV5v9WSysz9ufaj12J8L1BdZfj2t_0aIo-NK90uVql5tO1FAHQjfHZ0gkaG1M0hImdQc3JSY7JR4dt4hfDjIv8-wyocqlqUQtEnwKuJinw_YJjay7SFoXbsSnJ2J6uIgQEdri-3_BP3XEpPfemsJLxHpZZDnxdjasYG3O1FVeMa18qSHXv652y7A3Kx6OdOUesO1dRcG22OiuWd-dpOJP6VdIg85PG0llh-6xMSpLCxQp1R7tQ8vKHG9iL-S892xsRbILtQVTgtDe8euNEPItbH804ltK5FzSrOBvHObiJ8bZFgPRoyuzvVC6ANZt-nIWmo9HQUNPqkEspSUTgRfYHAHY6Bv8tNm8ZqAKF4LwegOHHbdJ8z-ZqviTj7tyKlSYATKHwvp-9y69Ybs-8WdjRmxZJM34TYeivlkfDtWL5gq4D0E4gIN9k-u5yKtIkfZ110ns7Q0Fb97w5Nkv-ciWbeV4K1LfuCrqpB2W9uJXXsZVzhh6zGsVjZZSTNtSRLYSQ4Neu9wjwaYFdTiHG0FLSyiEfnoT5YGjjaWhPyw68G20G-7Z1J_2NPZa7VSqy81UODjv0KhF1QKoTvqKKQ6HZLbnegMEeb6tcTD_Mbi9LubXKnGos_1dBIYc8djXzrW-Vs1ioOnoFe_FvQvC0aqyvPQ8YEZ4nXSj6ZHVadr3-7J3vL_s8PnGTMIDCSw0ccXaE2DVkbTS3Ug1Ql05hq5rdA-DJtvUo5YIEx87.iQlQaBO6Vb89KHSHFSfYsQ";
    signStr = "CaCoiG0yTJI51so9xCOsFl5u/xK73On14nLhf7lRLUfLlxvhiilOzKehMgX3ZH9fcvRMAjy7Me24H9WYj2Z/WKIPOz2giaJSa9VKiDniaL21QwG1fkpnED05z4BQxn8SiKYoa5R7e6liRRR4X+yl6CQbQiWeTJ7QCEg2N+amyjsQ5xXJICrXVuo77hIH/WFV0rLCqwmPF/Tg0PC9Se3D8Q27l9CMrQXcrZMQFkOztEYcNB2TDBRLsCyKMXn/+Y0L8uzEo5rPC9asBC6Ej7pIVcxf7HN3Qj2pSiuZhqeam+34F4teq5Ev5Lr1WLtoBP+OcVdrZWJ+0nvdd2yJ+9YzCg==";

    Map<String, String> config = Map.of(
        "my-num-cd-auth.android-app-uri", "RunUriOfAndroidApplication",
        "my-num-cd-auth.ios-app-uri", "RunUriOfiOSApplication",
        "my-num-cd-auth.app-uri", "InstallationUriOfSmartPhoneApplication",
        "my-num-cd-auth.terms-of-use", "TermsOfUseDirURL",
        "my-num-cd-auth.privacy-policy", "PrivacyPolicyDirURL",
        "my-num-cd-auth.peronal-dpp", "PersonalDataProtectionPolicyDirURL",
        "my-num-cd-auth.platform-class", "com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient",
        "my-num-cd-auth.certificate-validator-uri", "CertificateValidatorRootUri",
        "my-num-cd-auth.platform-sender", "PlatformApiIdpSender"
    );
    
    MultivaluedMap<String, String> decodedParams = new MultivaluedHashMap<>() {
      {
        putSingle("mode", "login");
        putSingle("encryptedUserAuthenticationCertificate", certificateStr);
        putSingle("sign", signStr);
        putSingle("encryptedDigitalSignatureCertificate", "");
        putSingle("applicantData", "752bb712-055a-4091-b35e-45973c475dcc");
      }
    };

    when(context.getHttpRequest()).thenReturn(httpRequest);
    when(httpRequest.getDecodedFormParameters()).thenReturn(decodedParams);
    when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfigModel);
    when(authenticatorConfigModel.getConfig()).thenReturn(config);
  }

  @ParameterizedTest
  @ValueSource(strings = {"login", "registration", "replacement", "exception"})
  public void testExecuteUserAction(String userActionMode) throws Exception {

    AuthenticationRequest authenticationRequest = new AuthenticationRequest() {
      {
          setCertificate(certificateStr);
          setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
          setApplicantData("752bb712-055a-4091-b35e-45973c475dcc");
          setSign(signStr);
          setActionMode(userActionMode);
      }
    };

    try(
      MockedConstruction<LoginAction> loginAction = mockConstruction(LoginAction.class,
                                                     (mock, ctx) -> {
                                                      doNothing().when(mock).authenticate(any(), any());
                                                     });
      MockedConstruction<RegistrationAction> registrationAction = mockConstruction(RegistrationAction.class,
                                                     (mock, ctx) -> {
                                                      doNothing().when(mock).register(any(), any());
                                                     });
      MockedConstruction<ReplacementAction> replacementAction = mockConstruction(ReplacementAction.class,
                                                     (mock, ctx) -> {
                                                      doNothing().when(mock).replace(any(), any());
                                                     });
      MockedConstruction<PlatformApiClient> platformApiClient = mockConstruction(PlatformApiClient.class,
                                                     (mock, ctx) -> {
                                                      doNothing().when(mock).setContextForDataManager(any());
                                                      when(mock.getUserRequest()).thenReturn(authenticationRequest);
                                                     });
    ) {

      if (userActionMode.equals("exception")) {
          assertThrows(IllegalArgumentException.class, () -> {
            actionResolver.executeUserAction(context);
        });
      } else {
        actionResolver.executeUserAction(context);

        PlatformApiClient platformApiClientMock = platformApiClient.constructed().get(0);
        verify(platformApiClientMock, times(1)).setContextForDataManager(context);
        verify(platformApiClientMock, times(1)).getUserRequest();
        verify(context, times(1)).getHttpRequest();
        verify(context, times(3)).getAuthenticatorConfig();
        verify(authenticatorConfigModel, times(3)).getConfig();
        verify(httpRequest, times(1)).getDecodedFormParameters();

        if (userActionMode.equals("login")) {
          LoginAction loginActionMock = loginAction.constructed().get(0);
          verify(loginActionMock, times(1)).authenticate(any(), any());
        } else if (userActionMode.equals("registration")) {
          RegistrationAction registrationActionMock = registrationAction.constructed().get(0);
          verify(registrationActionMock, times(1)).register(any(), any());
        } else if (userActionMode.equals("replacement")) {
          ReplacementAction replacementActionMock = replacementAction.constructed().get(0);
          verify(replacementActionMock, times(1)).replace(any(), any());
        }

      }
    }
  }
}
