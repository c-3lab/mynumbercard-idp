package com.example.mynumbercardidp.keycloak.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.JWKSHttpUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EncryptionTest {

  @InjectMocks
  Encryption encryption;

  @Mock
  KeycloakSession keycloakSessionMock;
  @Mock
  JWEKeyStorage jweKeyStorageMock;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  /**
   * encryptメソッドテスト
   * @throws Exception 入出力エラー
   */
  public void testEncrypt() throws Exception {

    String certificate = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.pSbLIkV3vVbtQ2ddCDlFfej--YiNS-v_tn3YKnidlStaXxW2CcapTudbLlNCJmXggRPXTdCo-9maHBkld4wTwrLncphMz9fsnPk7QDbu3WqBlrwRLgVqcOddKirk0FqTj2IVKqDJCfZ-JUrr5KW0EPNOtx4LrvIJSBOr7mUpwxZ2ZbeXtpTxyLBHuSo-3YSXqEVzT9twBF7FyNTi9RzJGXVsXHjbbwufYNOnaUawnLa4xTNDQNb3qwc2r7zMRCNwzuHprnu0U5pFblnZHnGiYtQ4Bf5t9J7f0MgAn08vnKkTYmDaw_IgHtQmkESj0a_GCo-LVmdd0TBQH27v7ktL7w.l92Waa59-LZIrwhZch2M5w.VHjaRmTr-8nhhHTQd71oKbrUVHp8KQbRJdUC1J5XFRqG52HD_PLwA5sI8sO1IlPuk5KETNFSIEij21c1zlU0nmggrJQJTun8lIVAnAA1VStC0U2kHFy08kHmyyUTNlmWeIyPgO2L0tSUJRyz9LbBg_oCx71ed1d8gYJmGWqCxkxE3vsn336ixVjjuvYW6jniPrdnsWBeiNTpGUqMCStlSA_bm0C9NQgMw75HYMlFmamtfkMYIlhh5qYRKYOd569GTIuW0IaGIREhpNsGLvQ1ojh6KRn4RhyVenp1R-kKbAaZfSiXzL84se3Py4lUqP_ihg5p0mpCs9JvdvTo0sRSy2AnZmEKC6gQLYIk6eS8etm9OxqXPd32t0cPm5yhxf2-g5McypfZ8SrZqlikZ8_zKIOhboWUFUY1GJawkDJRv-2RoLmb4RGQMpV_M95IgpTfrIYQkOe3OTPQ9iEDLK9ixgVyn4unAiIIbuqMe3et68Kcafe89QMI8ZzJCryFEwMfpUwK0v1TZmNojFJ1w1ifjr78uTI26FupxDoSGxcnMlK7M_AKStFlyTw_wYPfWJrubYYVH_cFYdoMEZkq2Y2N1ebluG7AAL4u5z-7Ug0P5tIlKwR1DZ6b_2r2XzaC-Y60HzKa7OwpkGVmWk6QQOWhkVdfLA_HTLadlTBJJREtrtCOWgyYSe2g5-7SIEXe1i8_XKT5waNwnyGC3fQlP9-EfvTISVQi8gxjFlFQ3tr0EI7QWSN5VGBDw5K8gbOqz3_HUwtFf6CSF0IZYV3PSEeMPUZvtQi_tUbtpntOwTeIpE-lfBVU8vnD6Txx0tb2G5PShxwb2ENZJV_OcMprnaqN-kIAlMPG8GqqTWZnqulv6XD8fMmYXqGZRzuAbBRwPsofmXj2BMl2u687bHD5wEmHTrpsYPU6BE1ykYKdD00IARlgt822Ws9Gvbc-FOKQOGpk1AKgpyrLb0zOmF2Cc23XHyaAGsvU8EcOu5IKgqvC-3GfBqRROMCmoVshIq5Q7ouBevai_ST-Kifj1csRhHQ6Zoay6vy80nYSIWDl2Gdw24mmrfrRD-qBLHXKyhiDzSBcD3QRVtxwF_ExwLR_uMlDM_VPTjsd_NDt-W7_dMXu9bvu2u-ZvVMyxSdelVWndPGqhHA8zKPvNU0H_VvJ-iRqAvDEnPP-dpiSfkq2RHhZ-qqN-tK9dJwE7qBy8jkqMZEDsnLYKRpoXAZg_P-fs6Yc9nvTBN3PY3lVbHx0qzDwTisW7eh51EHzt0LCsv31hwkXIlcvEURjX0ioyy1pCvN9-wjtRd9r6IrA8b2S3QJ2FLFfbYTmbzSToqjDam4lhav9IykO1MAzBo_ayiwa4W_T92onXrAlMb3CeJdDu8VBdS_UXwqRHXYNkCuQahgeWci3F8DlkQz0mT20ahU6oOQLuKKx9hs0KNNQRSQonMNx3PBKq8DwhyZ4rCVaqSjhEzWsK5Zb3Jda71uIjY4Sbx5VCDZunhh8kf4BHtZHhOWXwkze2aOWXH6UnY-d8pTkGW8-dWdZxv-oiM3rfuiP8FhuPM65FG3vH6AuwOhEEEhRGYwhQpQRH0Ylgjp3NdbJeozsSFuzk2fpUKZUfjAVoI3N8zwSpfyiSVmAuJazR-jadT-HeoAjWYG7-MtrflvRuwBDOP_1wqfS6farr5EPBmwFUu5XVyqr_1l0ottBbt_LHc63zE97Nxwqum39uQ83wBP-JA6oLuDEcpY5d-TUFeI3oG_6SUZW2qgCSXHLUsSB812VhQatAV7UevRUR_g7r-jIg8rtxmakVv5C3z1po6Ql3Yt0UhI8q_Io8YW8HO2z2HIkY7pcTxGx7uQrOOfiYK4A1-oF7E9jVPaU0R7BnH5j8BUsq46dBftfghEd0MZBgR1AUtvv5RbvPJKk-gv7X-Cts08lArUR9Xfnr8UfRFsoPSceg3C5TKY0WIjHpLmd4Df9YR9a_x54xO84q7jyDwnvHy1YTgqXnodaff5fAbxjkrJR52bDWfZbeQCtB0-TgByKDuFKfp7iuX_eBAggSynSvzGfzkj0AdFr24TWUBdqC0qWJ0tWLGN1xfrIEoDDTLMsFWEN1HAzl3VYzepGxi6Vml8Px196hc39pOkQIafcvJ1XCMVviAkW0skkuNdw60wc8BF62ujGunWwsSSknqh_fNufwtlF30hdA3-A0Ndjc9amhQTHHe7IkBP5_vgvDjNUeHo_x7mzTtT78H6wbt4isXkpXgEeKArjWKTDch7iBDA9IuW_lZSpDsO9SxUdnCkHsUwGCocipkOQeIlcFtfp9jqjCPA8XAcalG0y2yhygauNDkGtm4to9tPWbWkrzi4hVXHkVZmhCStiDP0wbXmoA_NODOb1VWamoviXtxX1KsPFfxspew2rUmDcszKe0eDFaEmLOBeIcbfRuN8P7u12RpZZz6SAZWo8nHKHOBra-NvAsT15qfeP9Egv6_is2VBOB6aa2di3EG8R4kKk-VR6FQHyUAhZ2NiyMIMp97ONMqDadldhX7REe31jZpvsPoZ95s9dRZyWPeCxo2aKmurWwONyUqVHVdarFK2M6bWXDL-xi_V-B82evFjqVxSDKPaOsg75nakjWwdHIawmsD6CJgBw5oGZNYrkHlFUgPF1mqJh10lpVhhLJGuHrZWoZjh1j8ZZ9vsVxasUWDrbeYLCYnCeMXjmwGJM_hdhkYzY4DWxTHZKcGuiNDHU_K9JOjBhv-thfd5nTiVWU_x4Ap-I_B3RMyFY_aYrXQVFtq_uSQSk6K3MOBSh9FAQWzhl0PX90FuBHDFNQ7Fh1OXFLoJfpnD8XcPB0I30MGPbYT5QfOaJJCyuoCVVoO9m_O3AvNHz04yWnELF83h91epn5gVWwstEZh8rBPqL6MzNB9e_Uq3jCvaiimlFZDC9WGDiwZheYOeiZDp_WVNG_RwMpMLtyrgfZnsE_CJnGZ3y5ICdBKub_2Hc0tXn7XzmXDq25UR--vfruF9b5QXy6dxURqd-jkeK3c-of1MUp_mlaynZifAQGUn9MhypHsoZggRNclQLkohqlzLvHOTQyRPDlTVbqOSjDk3KmzfTnCXZCI0Q1_Po1NeKKnpva9s-kD1aj9Tf2QxS2JJmXbBb1VL9le_-jqZjQXjaJb3FdRQ7A2vGbEEBJyKGfKypCXUVRyHIYC8wFgayrTZbZvwUYLQasRojwm-R_FBg1GGwNfzAiELAzEbV-1X2IkjtmB8VNbupAe8mlO6GF9KA_tL_6peW7W-dFUeAW7GYQ90iDVj117h_SGox8RBYd9lLmarcuVunlmZ3gtnRbdBlWSgC0foaR70qb2C-mD4mfW574XRFcNT5CsVwoSJZU-v8oCvZzKNS451-lyfXNHa6sw_kXwE0DQpehm7qrL_OQmFDlUCgg-oYM3kzGohk8bdmIzPBhKfaS3olBP0-rkJ1y1pP_PNounRlXmLQAhg7Mn2VjmtGiXpv9FpGP6Zz5gHYBKyfXJiiogTDceG2X6sF3xjND-dQKDk5QHujuMT6OboL18leimA_0JApCZ4GCcjYpn732y25ggODJL_m4d4RiOqqsXYYSFax3IyM8ZVCButcD5mlG2wMSQQKdUaOnrLQuC09mohl7KAxnC9OCSjTYJ3w7zCy0VuSYE7pihiGr8hVZRZQ7qrR8Nsd9K0BLX7e2iqc5LCDHIpKDuKqEnFceGM1kNTY_9q36v9m2D-TrBczVGQ-kyBcoJw4O0Hp0c9Y00K8yHBFVZSz-SB0yNhIB-8ElZC0m8EzfwrqhaeXe8KLUYbb4u_AjrjM5QAOda1TpXTNZ8aDyATyZvTT78XHqjgk5RGVC8KX5_LUVDP_hlO7jee0Lxt8geXOzNT1JxMbNCEHKw-vCxXExpwyAarCRBHRbYP_uG8Xtw2WGHGo31GXJiw7SO5PMZIY8dVHpc73y7UZkvu_-tLyRE0_gBUEtX_TCGrJk5zqJVNUDikNySXrE2Ej-qfFDovnN7VSt0GRjPs_V_DtmUJpyk2C_ZeRwWIGtK3hkK9JgfRbr9iRJkQOBNk2BhovhyZ1hb7DiEi7Z6DtrK1qNW6faLjvionCH-sQXourDNGz2CGrKO6T7KSgkaOm0Lw_W-wJ6HQEs7-Smjsat9Al40pCNK_jucs6OFTr_H5z7QzhxemEwJfAtfsMMUD5tVdg8h8fXPz08awl9y5On3wLACAmhoxy5LBFWulaMXrRbJDfWrlg01KJyr9OWPIuH2RC-U8spX30KM2wOj-AreJXKwqQr6MSbe-aikwnsZdfjTMoznTepOM2JTDc2KWJtS_w5QvKt0SVWCYI6ru63Vaih060Lgt-urYDvElKVeGXmCxDJns6RnlAyRK8m_gZt4N7R6hCj5OzVR08y1CFKSnPyNQvQA0wLv44LgpmvXnqILKlIN3OVhJg8t72DK-CX9_wdRjR2rIchgUmHrjfi_NdV-3_LmCKdfbD5AmnT4oZb0KOrMJrxXgdcA8Alsja_8yZslIl5yqWelO5MbUVgTs9TvlkJHVi-0uX_15kavAsHZjhlJt7bGDWVipYS5BH1V-DhT8Q_flw3xkAofgpEZpnrxAC87iVSzhGtH5QXdC5ezH_cunWS8eNcun8TvE-UDxmwQZFB0C6KSsx_2D7lct3GuLNQNT7U3gp2qPZC_iZihuledmvgV7a1eOaYNXqaqlOgC5DN_y7EqiL2krDo9X0I5sJYs0-paGuqUFkT8cm2C05gQcxw_BIVhHDFfwvFvZ6ygDHZQ8HLUYF6XUzsUWN7emG3cyfxUBv9iHYHCLwc8g1lTbv-r-lEalvLh3Ru3z4h_dJ4QJV836VX4T8cREaiYXpB6qvVM76CEpBlJFLUINhlZ-YFvowyk71Xj02ilndYAuZpS5gcJYo1VGFhYFFTHjq2JoZanwofz1mhH8Wir-h4tROjnwxbiaAGYvFAGkHdGT3Ka4WMAgRXsshEfkwv-jS21kTUIo_-3XiBL37VSJAvuwSKv0etHKfwabUqmeT5cWOHYPcckliZ5RDwg_kWKHfNsiPYGPpNUEIJzdvneyj7TT7LjMVUNuQMYTv-9Vin1qXmojgiZAWRLbp-q-xl7L4_q1wAlxzdH_A3t2y7BJ6BCn3EoXVL_6ru1yAeZWnHBvquX6HnvCWeuY54-NSjS81Ju7psJIpwMfdfZwWj9FqiABGV14MSi2_p5I0mYD_0rOWXRgT9zJjm6Ts4r0MTQ6VtfQGUa09PGJcCUit4XAntsQq7Q8T423ZJrLNzrG1Pm12Ej51MLp5rhk1MmaXcjHttlbz2QNHIHNodUNShWFm2WnQieEe7wjpiFXuDw3Zi5CyVKTL-u-OcHv8XvE4N-s5nkbrwmpsOZKZZfxpKhti9ZDWoUai5UMbhNHZi-TQ-v5S2boClbTyKkaqIQ9IUklYvpFb3X7KuV5I6uiOEDTas3B0Vh6tr7VFN9IjgG4Cd0nVPgUd5Dw0ulwd4PIq-0RqJAvRlD208Ph5TMzy8ecq_4yM0rIR0BzepeGkZH1nYEFdD5ZTV5v9WSysz9ufaj12J8L1BdZfj2t_0aIo-NK90uVql5tO1FAHQjfHZ0gkaG1M0hImdQc3JSY7JR4dt4hfDjIv8-wyocqlqUQtEnwKuJinw_YJjay7SFoXbsSnJ2J6uIgQEdri-3_BP3XEpPfemsJLxHpZZDnxdjasYG3O1FVeMa18qSHXv652y7A3Kx6OdOUesO1dRcG22OiuWd-dpOJP6VdIg85PG0llh-6xMSpLCxQp1R7tQ8vKHG9iL-S892xsRbILtQVTgtDe8euNEPItbH804ltK5FzSrOBvHObiJ8bZFgPRoyuzvVC6ANZt-nIWmo9HQUNPqkEspSUTgRfYHAHY6Bv8tNm8ZqAKF4LwegOHHbdJ8z-ZqviTj7tyKlSYATKHwvp-9y69Ybs-8WdjRmxZJM34TYeivlkfDtWL5gq4D0E4gIN9k-u5yKtIkfZ110ns7Q0Fb97w5Nkv-ciWbeV4K1LfuCrqpB2W9uJXXsZVzhh6zGsVjZZSTNtSRLYSQ4Neu9wjwaYFdTiHG0FLSyiEfnoT5YGjjaWhPyw68G20G-7Z1J_2NPZa7VSqy81UODjv0KhF1QKoTvqKKQ6HZLbnegMEeb6tcTD_Mbi9LubXKnGos_1dBIYc8djXzrW-Vs1ioOnoFe_FvQvC0aqyvPQ8YEZ4nXSj6ZHVadr3-7J3vL_s8PnGTMIDCSw0ccXaE2DVkbTS3Ug1Ql05hq5rdA-DJtvUo5YIEx87.iQlQaBO6Vb89KHSHFSfYsQ";

    try(
      MockedConstruction<JWE> jweMock = mockConstruction(JWE.class,
                                                     (mock, ctx) -> {
                                                      when(mock.header(any())).thenReturn(new JWE());
                                                      when(mock.content(any())).thenReturn(new JWE());
                                                      when(mock.getKeyStorage()).thenReturn(new JWEKeyStorage());
                                                      when(mock.encodeJwe()).thenReturn(certificate);
                                                     });
      MockedConstruction<JWEHeader> jweHeaderMock = mockConstruction(JWEHeader.class, (mock, ctx) -> {});
      MockedConstruction<JWKParser> jwkParserMock = mockConstruction(JWKParser.class, (mock, ctx) -> {});
      MockedStatic<JWKSHttpUtils> jwksHttpUtilsMock = mockStatic(JWKSHttpUtils.class);
    ) {

      JSONWebKeySet jwks = createJsonWebKeySet();
      
      jwksHttpUtilsMock.when(() -> JWKSHttpUtils.sendJwksRequest(any(), any())).thenReturn(jwks);
      doReturn(new JWEKeyStorage()).when(jweKeyStorageMock).setEncryptionKey(any());

      String expected = certificate;
      String result = Encryption.encrypt(keycloakSessionMock, "testContext", "testJwksUrl");

      assertEquals(expected, result);

      var constructedJweMock = jweMock.constructed().get(0);
      verify(constructedJweMock, times(1)).header(any());
      verify(constructedJweMock, times(1)).content(any());
      verify(constructedJweMock, times(1)).getKeyStorage();
      verify(constructedJweMock, times(1)).encodeJwe();

    }

  }

  @Test
  /**
   * decryptメソッドテスト
   * @throws Exception 例外
   */
  public void testDecrypt() throws Exception {

    String jsonStr = "{\"sampleKey\": \"sampleValue\"}";
    byte[] strByte = jsonStr.getBytes();

    try(
      MockedConstruction<JWE> jweMock = mockConstruction(JWE.class,
                                                     (mock, ctx) -> {
                                                      when(mock.getKeyStorage()).thenReturn(new JWEKeyStorage());
                                                      when(mock.verifyAndDecodeJwe()).thenReturn(new JWE() {
                                                        {
                                                          content(strByte);
                                                        }
                                                      });
                                                      when(mock.getContent()).thenReturn(strByte);
                                                     });
    ) {


      var mapper = new ObjectMapper();
      JsonNode expected = mapper.readTree(jsonStr);

      doReturn(new JWEKeyStorage()).when(jweKeyStorageMock).setDecryptionKey(any());
      JsonNode result = Encryption.decrypt("xxxxxxxxx", createPrivateKey());

      var constructedJweMock = jweMock.constructed().get(0);
      verify(constructedJweMock, times(1)).getKeyStorage();
      verify(constructedJweMock, times(1)).verifyAndDecodeJwe();

      assertEquals(expected, result);

    }
  }

  /**
   * テスト用のPrivateKeyを生成
   * @return PrivateKey
   */
  private Key createPrivateKey() {
      String keyInfo = "szEiQkY2iJtIVghIcu6x3a3KwcIsKFRJmUuHZ0-YpoFWi_Mj53ghWIbNpGODBlWVgNtAUxTHAFxubCiR_AN4DmimCyWeHpMJ2mO7TfVcpJzh7pL4cdeIv1VaBr0CnonEhdN36MUmXCwRLNfPq-Xm9gmXj8gdRNPV2WrK2maTef5OegMaW2T5onkDz0yikwGsLP1GxcTvdqokXSHYzBNIfdXuNi-ZLip_9iwmLMlaxfQvy_daWtEWKpuFrNBcRJeTtV6ebQakXBad6FY4x2eK5ZhEq3sZamlYkJK2HiVqXV1FxD8WPO4FxIhW1Fb1g3ThNBZ5NCog2_xrfzIO3pygtw";
      return new SecretKeySpec(keyInfo.getBytes(), "RSA256");
  }

  /**
   * テスト用のJSONWebKeySetオブジェクトの作成
   * @return JSONWebKeySetオブジェクト
   */
  private JSONWebKeySet createJsonWebKeySet() {

    JWK[] jwkKeys = new JWK[1];
    jwkKeys[0] = new JWK() {
      {
        setKeyId("keyId");
        setKeyType("RSA");
        setAlgorithm("algorithm");
        setPublicKeyUse("publicKeyUse");
      }
    };

    JSONWebKeySet jwks = new JSONWebKeySet() {
      {
        setKeys(jwkKeys);
      }
    };

    return jwks;
  }
}
