package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;
import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformAuthenticationResponse;
import com.example.mynumbercardidp.keycloak.util.Encryption;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginActionTest {
	private AutoCloseable closeable;
	private String certificateStr;
	private String signStr;
	private String certificateJsonStr;

	@InjectMocks
	LoginAction loginAction;

	@Mock
	LoginFlowTransition flowTransition;
	@Mock
	AuthenticationFlowContext context;
	@Mock
	AuthenticationRequest authenticationRequest;
	@Mock
	KeycloakSession keycloakSession;
	@Mock
	KeyManager keyManager;
	@Mock
	KeyWrapper keyWrapper;
	@Mock
	RealmModel realmModel;
	@Mock
	AuthenticationSessionModel authenticationSessionModel;
	@Mock
	UserModel userModel;
	@Mock
	PlatformApiClient platform;
	@Mock
	PlatformAuthenticationResponse platformAuthenticationResponse;
	@Mock
	UserIdentityToModelMapper userIdentityToModelMapper;

	@BeforeEach
	public void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);

		certificateStr = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.pSbLIkV3vVbtQ2ddCDlFfej--YiNS-v_tn3YKnidlStaXxW2CcapTudbLlNCJmXggRPXTdCo-9maHBkld4wTwrLncphMz9fsnPk7QDbu3WqBlrwRLgVqcOddKirk0FqTj2IVKqDJCfZ-JUrr5KW0EPNOtx4LrvIJSBOr7mUpwxZ2ZbeXtpTxyLBHuSo-3YSXqEVzT9twBF7FyNTi9RzJGXVsXHjbbwufYNOnaUawnLa4xTNDQNb3qwc2r7zMRCNwzuHprnu0U5pFblnZHnGiYtQ4Bf5t9J7f0MgAn08vnKkTYmDaw_IgHtQmkESj0a_GCo-LVmdd0TBQH27v7ktL7w.l92Waa59-LZIrwhZch2M5w.VHjaRmTr-8nhhHTQd71oKbrUVHp8KQbRJdUC1J5XFRqG52HD_PLwA5sI8sO1IlPuk5KETNFSIEij21c1zlU0nmggrJQJTun8lIVAnAA1VStC0U2kHFy08kHmyyUTNlmWeIyPgO2L0tSUJRyz9LbBg_oCx71ed1d8gYJmGWqCxkxE3vsn336ixVjjuvYW6jniPrdnsWBeiNTpGUqMCStlSA_bm0C9NQgMw75HYMlFmamtfkMYIlhh5qYRKYOd569GTIuW0IaGIREhpNsGLvQ1ojh6KRn4RhyVenp1R-kKbAaZfSiXzL84se3Py4lUqP_ihg5p0mpCs9JvdvTo0sRSy2AnZmEKC6gQLYIk6eS8etm9OxqXPd32t0cPm5yhxf2-g5McypfZ8SrZqlikZ8_zKIOhboWUFUY1GJawkDJRv-2RoLmb4RGQMpV_M95IgpTfrIYQkOe3OTPQ9iEDLK9ixgVyn4unAiIIbuqMe3et68Kcafe89QMI8ZzJCryFEwMfpUwK0v1TZmNojFJ1w1ifjr78uTI26FupxDoSGxcnMlK7M_AKStFlyTw_wYPfWJrubYYVH_cFYdoMEZkq2Y2N1ebluG7AAL4u5z-7Ug0P5tIlKwR1DZ6b_2r2XzaC-Y60HzKa7OwpkGVmWk6QQOWhkVdfLA_HTLadlTBJJREtrtCOWgyYSe2g5-7SIEXe1i8_XKT5waNwnyGC3fQlP9-EfvTISVQi8gxjFlFQ3tr0EI7QWSN5VGBDw5K8gbOqz3_HUwtFf6CSF0IZYV3PSEeMPUZvtQi_tUbtpntOwTeIpE-lfBVU8vnD6Txx0tb2G5PShxwb2ENZJV_OcMprnaqN-kIAlMPG8GqqTWZnqulv6XD8fMmYXqGZRzuAbBRwPsofmXj2BMl2u687bHD5wEmHTrpsYPU6BE1ykYKdD00IARlgt822Ws9Gvbc-FOKQOGpk1AKgpyrLb0zOmF2Cc23XHyaAGsvU8EcOu5IKgqvC-3GfBqRROMCmoVshIq5Q7ouBevai_ST-Kifj1csRhHQ6Zoay6vy80nYSIWDl2Gdw24mmrfrRD-qBLHXKyhiDzSBcD3QRVtxwF_ExwLR_uMlDM_VPTjsd_NDt-W7_dMXu9bvu2u-ZvVMyxSdelVWndPGqhHA8zKPvNU0H_VvJ-iRqAvDEnPP-dpiSfkq2RHhZ-qqN-tK9dJwE7qBy8jkqMZEDsnLYKRpoXAZg_P-fs6Yc9nvTBN3PY3lVbHx0qzDwTisW7eh51EHzt0LCsv31hwkXIlcvEURjX0ioyy1pCvN9-wjtRd9r6IrA8b2S3QJ2FLFfbYTmbzSToqjDam4lhav9IykO1MAzBo_ayiwa4W_T92onXrAlMb3CeJdDu8VBdS_UXwqRHXYNkCuQahgeWci3F8DlkQz0mT20ahU6oOQLuKKx9hs0KNNQRSQonMNx3PBKq8DwhyZ4rCVaqSjhEzWsK5Zb3Jda71uIjY4Sbx5VCDZunhh8kf4BHtZHhOWXwkze2aOWXH6UnY-d8pTkGW8-dWdZxv-oiM3rfuiP8FhuPM65FG3vH6AuwOhEEEhRGYwhQpQRH0Ylgjp3NdbJeozsSFuzk2fpUKZUfjAVoI3N8zwSpfyiSVmAuJazR-jadT-HeoAjWYG7-MtrflvRuwBDOP_1wqfS6farr5EPBmwFUu5XVyqr_1l0ottBbt_LHc63zE97Nxwqum39uQ83wBP-JA6oLuDEcpY5d-TUFeI3oG_6SUZW2qgCSXHLUsSB812VhQatAV7UevRUR_g7r-jIg8rtxmakVv5C3z1po6Ql3Yt0UhI8q_Io8YW8HO2z2HIkY7pcTxGx7uQrOOfiYK4A1-oF7E9jVPaU0R7BnH5j8BUsq46dBftfghEd0MZBgR1AUtvv5RbvPJKk-gv7X-Cts08lArUR9Xfnr8UfRFsoPSceg3C5TKY0WIjHpLmd4Df9YR9a_x54xO84q7jyDwnvHy1YTgqXnodaff5fAbxjkrJR52bDWfZbeQCtB0-TgByKDuFKfp7iuX_eBAggSynSvzGfzkj0AdFr24TWUBdqC0qWJ0tWLGN1xfrIEoDDTLMsFWEN1HAzl3VYzepGxi6Vml8Px196hc39pOkQIafcvJ1XCMVviAkW0skkuNdw60wc8BF62ujGunWwsSSknqh_fNufwtlF30hdA3-A0Ndjc9amhQTHHe7IkBP5_vgvDjNUeHo_x7mzTtT78H6wbt4isXkpXgEeKArjWKTDch7iBDA9IuW_lZSpDsO9SxUdnCkHsUwGCocipkOQeIlcFtfp9jqjCPA8XAcalG0y2yhygauNDkGtm4to9tPWbWkrzi4hVXHkVZmhCStiDP0wbXmoA_NODOb1VWamoviXtxX1KsPFfxspew2rUmDcszKe0eDFaEmLOBeIcbfRuN8P7u12RpZZz6SAZWo8nHKHOBra-NvAsT15qfeP9Egv6_is2VBOB6aa2di3EG8R4kKk-VR6FQHyUAhZ2NiyMIMp97ONMqDadldhX7REe31jZpvsPoZ95s9dRZyWPeCxo2aKmurWwONyUqVHVdarFK2M6bWXDL-xi_V-B82evFjqVxSDKPaOsg75nakjWwdHIawmsD6CJgBw5oGZNYrkHlFUgPF1mqJh10lpVhhLJGuHrZWoZjh1j8ZZ9vsVxasUWDrbeYLCYnCeMXjmwGJM_hdhkYzY4DWxTHZKcGuiNDHU_K9JOjBhv-thfd5nTiVWU_x4Ap-I_B3RMyFY_aYrXQVFtq_uSQSk6K3MOBSh9FAQWzhl0PX90FuBHDFNQ7Fh1OXFLoJfpnD8XcPB0I30MGPbYT5QfOaJJCyuoCVVoO9m_O3AvNHz04yWnELF83h91epn5gVWwstEZh8rBPqL6MzNB9e_Uq3jCvaiimlFZDC9WGDiwZheYOeiZDp_WVNG_RwMpMLtyrgfZnsE_CJnGZ3y5ICdBKub_2Hc0tXn7XzmXDq25UR--vfruF9b5QXy6dxURqd-jkeK3c-of1MUp_mlaynZifAQGUn9MhypHsoZggRNclQLkohqlzLvHOTQyRPDlTVbqOSjDk3KmzfTnCXZCI0Q1_Po1NeKKnpva9s-kD1aj9Tf2QxS2JJmXbBb1VL9le_-jqZjQXjaJb3FdRQ7A2vGbEEBJyKGfKypCXUVRyHIYC8wFgayrTZbZvwUYLQasRojwm-R_FBg1GGwNfzAiELAzEbV-1X2IkjtmB8VNbupAe8mlO6GF9KA_tL_6peW7W-dFUeAW7GYQ90iDVj117h_SGox8RBYd9lLmarcuVunlmZ3gtnRbdBlWSgC0foaR70qb2C-mD4mfW574XRFcNT5CsVwoSJZU-v8oCvZzKNS451-lyfXNHa6sw_kXwE0DQpehm7qrL_OQmFDlUCgg-oYM3kzGohk8bdmIzPBhKfaS3olBP0-rkJ1y1pP_PNounRlXmLQAhg7Mn2VjmtGiXpv9FpGP6Zz5gHYBKyfXJiiogTDceG2X6sF3xjND-dQKDk5QHujuMT6OboL18leimA_0JApCZ4GCcjYpn732y25ggODJL_m4d4RiOqqsXYYSFax3IyM8ZVCButcD5mlG2wMSQQKdUaOnrLQuC09mohl7KAxnC9OCSjTYJ3w7zCy0VuSYE7pihiGr8hVZRZQ7qrR8Nsd9K0BLX7e2iqc5LCDHIpKDuKqEnFceGM1kNTY_9q36v9m2D-TrBczVGQ-kyBcoJw4O0Hp0c9Y00K8yHBFVZSz-SB0yNhIB-8ElZC0m8EzfwrqhaeXe8KLUYbb4u_AjrjM5QAOda1TpXTNZ8aDyATyZvTT78XHqjgk5RGVC8KX5_LUVDP_hlO7jee0Lxt8geXOzNT1JxMbNCEHKw-vCxXExpwyAarCRBHRbYP_uG8Xtw2WGHGo31GXJiw7SO5PMZIY8dVHpc73y7UZkvu_-tLyRE0_gBUEtX_TCGrJk5zqJVNUDikNySXrE2Ej-qfFDovnN7VSt0GRjPs_V_DtmUJpyk2C_ZeRwWIGtK3hkK9JgfRbr9iRJkQOBNk2BhovhyZ1hb7DiEi7Z6DtrK1qNW6faLjvionCH-sQXourDNGz2CGrKO6T7KSgkaOm0Lw_W-wJ6HQEs7-Smjsat9Al40pCNK_jucs6OFTr_H5z7QzhxemEwJfAtfsMMUD5tVdg8h8fXPz08awl9y5On3wLACAmhoxy5LBFWulaMXrRbJDfWrlg01KJyr9OWPIuH2RC-U8spX30KM2wOj-AreJXKwqQr6MSbe-aikwnsZdfjTMoznTepOM2JTDc2KWJtS_w5QvKt0SVWCYI6ru63Vaih060Lgt-urYDvElKVeGXmCxDJns6RnlAyRK8m_gZt4N7R6hCj5OzVR08y1CFKSnPyNQvQA0wLv44LgpmvXnqILKlIN3OVhJg8t72DK-CX9_wdRjR2rIchgUmHrjfi_NdV-3_LmCKdfbD5AmnT4oZb0KOrMJrxXgdcA8Alsja_8yZslIl5yqWelO5MbUVgTs9TvlkJHVi-0uX_15kavAsHZjhlJt7bGDWVipYS5BH1V-DhT8Q_flw3xkAofgpEZpnrxAC87iVSzhGtH5QXdC5ezH_cunWS8eNcun8TvE-UDxmwQZFB0C6KSsx_2D7lct3GuLNQNT7U3gp2qPZC_iZihuledmvgV7a1eOaYNXqaqlOgC5DN_y7EqiL2krDo9X0I5sJYs0-paGuqUFkT8cm2C05gQcxw_BIVhHDFfwvFvZ6ygDHZQ8HLUYF6XUzsUWN7emG3cyfxUBv9iHYHCLwc8g1lTbv-r-lEalvLh3Ru3z4h_dJ4QJV836VX4T8cREaiYXpB6qvVM76CEpBlJFLUINhlZ-YFvowyk71Xj02ilndYAuZpS5gcJYo1VGFhYFFTHjq2JoZanwofz1mhH8Wir-h4tROjnwxbiaAGYvFAGkHdGT3Ka4WMAgRXsshEfkwv-jS21kTUIo_-3XiBL37VSJAvuwSKv0etHKfwabUqmeT5cWOHYPcckliZ5RDwg_kWKHfNsiPYGPpNUEIJzdvneyj7TT7LjMVUNuQMYTv-9Vin1qXmojgiZAWRLbp-q-xl7L4_q1wAlxzdH_A3t2y7BJ6BCn3EoXVL_6ru1yAeZWnHBvquX6HnvCWeuY54-NSjS81Ju7psJIpwMfdfZwWj9FqiABGV14MSi2_p5I0mYD_0rOWXRgT9zJjm6Ts4r0MTQ6VtfQGUa09PGJcCUit4XAntsQq7Q8T423ZJrLNzrG1Pm12Ej51MLp5rhk1MmaXcjHttlbz2QNHIHNodUNShWFm2WnQieEe7wjpiFXuDw3Zi5CyVKTL-u-OcHv8XvE4N-s5nkbrwmpsOZKZZfxpKhti9ZDWoUai5UMbhNHZi-TQ-v5S2boClbTyKkaqIQ9IUklYvpFb3X7KuV5I6uiOEDTas3B0Vh6tr7VFN9IjgG4Cd0nVPgUd5Dw0ulwd4PIq-0RqJAvRlD208Ph5TMzy8ecq_4yM0rIR0BzepeGkZH1nYEFdD5ZTV5v9WSysz9ufaj12J8L1BdZfj2t_0aIo-NK90uVql5tO1FAHQjfHZ0gkaG1M0hImdQc3JSY7JR4dt4hfDjIv8-wyocqlqUQtEnwKuJinw_YJjay7SFoXbsSnJ2J6uIgQEdri-3_BP3XEpPfemsJLxHpZZDnxdjasYG3O1FVeMa18qSHXv652y7A3Kx6OdOUesO1dRcG22OiuWd-dpOJP6VdIg85PG0llh-6xMSpLCxQp1R7tQ8vKHG9iL-S892xsRbILtQVTgtDe8euNEPItbH804ltK5FzSrOBvHObiJ8bZFgPRoyuzvVC6ANZt-nIWmo9HQUNPqkEspSUTgRfYHAHY6Bv8tNm8ZqAKF4LwegOHHbdJ8z-ZqviTj7tyKlSYATKHwvp-9y69Ybs-8WdjRmxZJM34TYeivlkfDtWL5gq4D0E4gIN9k-u5yKtIkfZ110ns7Q0Fb97w5Nkv-ciWbeV4K1LfuCrqpB2W9uJXXsZVzhh6zGsVjZZSTNtSRLYSQ4Neu9wjwaYFdTiHG0FLSyiEfnoT5YGjjaWhPyw68G20G-7Z1J_2NPZa7VSqy81UODjv0KhF1QKoTvqKKQ6HZLbnegMEeb6tcTD_Mbi9LubXKnGos_1dBIYc8djXzrW-Vs1ioOnoFe_FvQvC0aqyvPQ8YEZ4nXSj6ZHVadr3-7J3vL_s8PnGTMIDCSw0ccXaE2DVkbTS3Ug1Ql05hq5rdA-DJtvUo5YIEx87.iQlQaBO6Vb89KHSHFSfYsQ";
		signStr = "CaCoiG0yTJI51so9xCOsFl5u/xK73On14nLhf7lRLUfLlxvhiilOzKehMgX3ZH9fcvRMAjy7Me24H9WYj2Z/WKIPOz2giaJSa9VKiDniaL21QwG1fkpnED05z4BQxn8SiKYoa5R7e6liRRR4X+yl6CQbQiWeTJ7QCEg2N+amyjsQ5xXJICrXVuo77hIH/WFV0rLCqwmPF/Tg0PC9Se3D8Q27l9CMrQXcrZMQFkOztEYcNB2TDBRLsCyKMXn/+Y0L8uzEo5rPC9asBC6Ej7pIVcxf7HN3Qj2pSiuZhqeam+34F4teq5Ev5Lr1WLtoBP+OcVdrZWJ+0nvdd2yJ+9YzCg==";
		certificateJsonStr = "{\"claim\":\"Certificate:\\n" + //
			"    Data:\\n" + //
			"        Version: 3 (0x2)\\n" + //
			"        Serial Number: 3 (0x3)\\n" + //
			"        Signature Algorithm: sha256WithRSAEncryption\\n" + //
			"        Issuer: C=JP, CN=Self Sign Intermediate CA\\n" + //
			"        Validity\\n" + //
			"            Not Before: Mar 17 03:19:26 2023 GMT\\n" + //
			"            Not After : Mar 14 03:19:26 2033 GMT\\n" + //
			"        Subject: C=JP, CN=Taro Sample/emailAddress=client06@example.com\\n" + //
			"        Subject Public Key Info:\\n" + //
			"            Public Key Algorithm: rsaEncryption\\n" + //
			"                Public-Key: (2048 bit)\\n" + //
			"                Modulus:\\n" + //
			"                    00:ab:92:13:01:75:4e:6a:b9:a0:a2:79:51:ab:ca:\\n" + //
			"                    20:ac:ce:65:97:08:c2:77:31:4b:1d:42:9c:59:31:\\n" + //
			"                    af:9c:96:5d:82:ef:ec:50:70:dd:ae:2b:2d:3e:1d:\\n" + //
			"                    75:04:98:26:dc:fd:2a:f9:41:ab:7f:69:ca:4c:d8:\\n" + //
			"                    21:61:5d:fe:3a:5f:41:d1:11:c1:fb:2c:bb:66:9e:\\n" + //
			"                    e4:44:fc:b1:17:60:07:8e:66:a5:1d:00:47:16:6a:\\n" + //
			"                    58:c4:ea:0b:8d:a9:fa:c7:a1:e7:26:09:62:a1:09:\\n" + //
			"                    01:07:e6:41:b9:9e:7f:ee:b0:4e:62:ff:9e:5a:71:\\n" + //
			"                    2c:1e:82:41:81:d5:25:97:a2:f1:9c:81:71:c8:49:\\n" + //
			"                    5e:12:40:56:9a:59:23:f7:d3:1a:0a:ad:59:28:ca:\\n" + //
			"                    9f:28:6f:4e:56:6b:b6:c2:b7:72:01:43:46:c8:1c:\\n" + //
			"                    5f:0e:95:21:7d:be:53:c8:12:b5:d7:42:99:36:02:\\n" + //
			"                    3f:ef:45:9e:60:d8:2a:79:b6:d8:2b:ae:e4:0f:bf:\\n" + //
			"                    b8:e8:bb:b7:4a:85:a7:63:ea:ca:79:9e:12:87:ba:\\n" + //
			"                    92:2d:5c:79:21:56:43:bd:b0:da:9f:9f:26:c1:f0:\\n" + //
			"                    b3:3e:e7:98:97:92:e1:5c:c3:6d:87:cf:1d:64:4a:\\n" + //
			"                    e1:12:8d:ab:a8:80:af:db:1d:1a:ae:a2:f1:7c:22:\\n" + //
			"                    62:fb\\n" + //
			"                Exponent: 65537 (0x10001)\\n" + //
			"        X509v3 extensions:\\n" + //
			"            X509v3 Basic Constraints: critical\\n" + //
			"                CA:FALSE\\n" + //
			"            X509v3 Key Usage: critical\\n" + //
			"                Digital Signature, Key Agreement\\n" + //
			"            X509v3 Extended Key Usage: critical\\n" + //
			"                TLS Web Client Authentication\\n" + //
			"            X509v3 Subject Key Identifier: \\n" + //
			"                64:2B:88:61:C7:77:BC:A0:02:CC:60:2E:ED:AC:32:EE:2B:5C:36:EB\\n" + //
			"            X509v3 Authority Key Identifier: \\n" + //
			"                F0:62:8A:F1:93:FA:76:8D:0F:83:29:76:ED:65:08:1F:B2:4B:15:68\\n" + //
			"            X509v3 Subject Alternative Name: \\n" + //
			"                email:client06@example.com\\n" + //
			"    Signature Algorithm: sha256WithRSAEncryption\\n" + //
			"    Signature Value:\\n" + //
			"        47:76:ed:0c:96:be:00:03:ee:c9:a2:69:d1:fd:78:8f:e5:38:\\n" + //
			"        d6:c9:c9:2c:8b:55:a5:a3:3f:17:2b:ca:c3:46:89:5c:dd:8b:\\n" + //
			"        f6:98:cd:0e:82:bc:90:75:7a:a6:e5:03:6c:72:12:bc:b2:46:\\n" + //
			"        c0:58:65:c7:0e:1a:c9:0c:f7:0b:19:b7:38:39:2d:79:0b:e7:\\n" + //
			"        ce:8b:6b:28:f0:d0:a7:72:49:ca:7c:23:c9:c6:54:db:3a:ee:\\n" + //
			"        c8:4e:8d:aa:28:78:07:cc:85:67:0a:c5:b8:5a:45:9a:72:24:\\n" + //
			"        fa:6c:3a:87:1f:40:fe:4b:c1:61:05:50:97:bf:37:ee:be:b0:\\n" + //
			"        fa:fd:29:71:9c:09:bc:9f:1f:69:6a:f4:c5:83:1d:21:38:bb:\\n" + //
			"        8e:ea:fb:0a:52:61:38:ec:c3:83:67:b9:6b:a1:08:f5:39:44:\\n" + //
			"        85:e5:6a:5c:d6:87:cb:85:51:03:df:63:d4:22:98:a0:fe:e8:\\n" + //
			"        c4:c8:5b:3b:e3:51:7f:f3:ac:4d:21:73:46:d7:7f:27:42:cd:\\n" + //
			"        82:36:07:37:05:54:01:1e:09:e8:db:88:d1:5b:a0:71:0c:2b:\\n" + //
			"        b0:da:4d:9c:d9:35:f9:16:cc:56:62:63:bc:f2:c1:ad:02:44:\\n" + //
			"        4a:76:2d:26:bd:a8:51:26:da:d2:3c:39:82:5b:a4:c7:5a:c4:\\n" + //
			"        d4:f5:ef:74\\n" + //
			"-----BEGIN CERTIFICATE-----\\n" + //
			"MIIDjzCCAnegAwIBAgIBAzANBgkqhkiG9w0BAQsFADAxMQswCQYDVQQGEwJKUDEi\\n" + //
			"MCAGA1UEAwwZU2VsZiBTaWduIEludGVybWVkaWF0ZSBDQTAeFw0yMzAzMTcwMzE5\\n" + //
			"MjZaFw0zMzAzMTQwMzE5MjZaMEgxCzAJBgNVBAYTAkpQMRQwEgYDVQQDDAtUYXJv\\n" + //
			"IFNhbXBsZTEjMCEGCSqGSIb3DQEJARYUY2xpZW50MDZAZXhhbXBsZS5jb20wggEi\\n" + //
			"MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCrkhMBdU5quaCieVGryiCszmWX\\n" + //
			"CMJ3MUsdQpxZMa+cll2C7+xQcN2uKy0+HXUEmCbc/Sr5Qat/acpM2CFhXf46X0HR\\n" + //
			"EcH7LLtmnuRE/LEXYAeOZqUdAEcWaljE6guNqfrHoecmCWKhCQEH5kG5nn/usE5i\\n" + //
			"/55acSwegkGB1SWXovGcgXHISV4SQFaaWSP30xoKrVkoyp8ob05Wa7bCt3IBQ0bI\\n" + //
			"HF8OlSF9vlPIErXXQpk2Aj/vRZ5g2Cp5ttgrruQPv7jou7dKhadj6sp5nhKHupIt\\n" + //
			"XHkhVkO9sNqfnybB8LM+55iXkuFcw22Hzx1kSuESjauogK/bHRquovF8ImL7AgMB\\n" + //
			"AAGjgZowgZcwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCA4gwFgYDVR0lAQH/\\n" + //
			"BAwwCgYIKwYBBQUHAwIwHQYDVR0OBBYEFGQriGHHd7ygAsxgLu2sMu4rXDbrMB8G\\n" + //
			"A1UdIwQYMBaAFPBiivGT+naND4Mpdu1lCB+ySxVoMB8GA1UdEQQYMBaBFGNsaWVu\\n" + //
			"dDA2QGV4YW1wbGUuY29tMA0GCSqGSIb3DQEBCwUAA4IBAQBHdu0Mlr4AA+7JomnR\\n" + //
			"/XiP5TjWycksi1Wloz8XK8rDRolc3Yv2mM0OgryQdXqm5QNschK8skbAWGXHDhrJ\\n" + //
			"DPcLGbc4OS15C+fOi2so8NCncknKfCPJxlTbOu7ITo2qKHgHzIVnCsW4WkWaciT6\\n" + //
			"bDqHH0D+S8FhBVCXvzfuvrD6/SlxnAm8nx9pavTFgx0hOLuO6vsKUmE47MODZ7lr\\n" + //
			"oQj1OUSF5Wpc1ofLhVED32PUIpig/ujEyFs741F/86xNIXNG138nQs2CNgc3BVQB\\n" + //
			"Hgno24jRW6BxDCuw2k2c2TX5FsxWYmO88sGtAkRKdi0mvahRJtrSPDmCW6THWsTU\\n" + //
			"9e90\\n" + //
			"-----END CERTIFICATE-----\\n" + //
			"\",\"exp\":1695104412}";

		authenticationRequest = new AuthenticationRequest() {
			{
				setActionMode("login");
				setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
				setCertificate(certificateStr);
				setSign(signStr);
				setApplicantData("752bb712-055a-4091-b35e-45973c475dcc");
			}
		};

		doReturn(keycloakSession).when(context).getSession();
		doReturn(keyManager).when(keycloakSession).keys();
		doReturn(keyWrapper).when(keyManager).getActiveKey(realmModel, KeyUse.ENC, "RSA-OAEP-256");
		doReturn(authenticationSessionModel).when(context).getAuthenticationSession();
		doReturn("752bb712-055a-4091-b35e-45973c475dcc").when(authenticationSessionModel).getAuthNote("nonce");
		doReturn(realmModel).when(context).getRealm();
		doReturn(authenticationRequest).when(platform).getUserRequest();
		doReturn(platformAuthenticationResponse).when(platform).getPlatformResponse();
		doReturn("xxxxxxxxxxxxxxxxxxxxx").when(platformAuthenticationResponse).getUniqueId();
		doNothing().when(platform).sendRequest();
		doNothing().when(context).setUser(userModel);
		doNothing().when(context).success();
		doNothing().when(context).challenge(any());
	}

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

	@ParameterizedTest
	@CsvSource({
		"752bb712-055a-4091-b35e-45973c475dcc, true, true",    // ユーザー情報あり
		"752bb712-055a-4091-b35e-45973c475dcc, true, false",   // ユーザー情報なし
		"752bb712-055a-4091-b35e-45973c475dcc, false, true",   // ステータスコード200以外
		"dummy, true, true",                                   // nonce値不一致
	})
	public void testAuthenticate(String applicantData, Boolean isCheckedStatusCode, Boolean existsUser) throws Exception {

		try(
			MockedStatic<ResponseCreater> responseCreaterStatic = mockStatic(ResponseCreater.class);
			MockedStatic<UserIdentityToModelMapper> userIdentityToModelMapperStatic = mockStatic(UserIdentityToModelMapper.class);
			MockedStatic<Encryption> encryptionStatic = mockStatic(Encryption.class);
		) {

			authenticationRequest.setApplicantData(applicantData);


			Field loginActionField = loginAction.getClass().getDeclaredField("flowTransition");
			loginActionField.setAccessible(true);
			loginActionField.set(loginAction, flowTransition);

			doReturn(isCheckedStatusCode).when(flowTransition).canExecuteAuthentication(any(), any());

			if (!existsUser) {
				userModel = null;
			}

			doReturn(userModel).when(userIdentityToModelMapper).find(any(), any());

			userIdentityToModelMapperStatic.when(() -> UserIdentityToModelMapper.getUserIdentityToCustomAttributeMapper(any())).thenReturn(userIdentityToModelMapper);
			responseCreaterStatic.when(() -> ResponseCreater.setLoginFormAttributes(any())).thenAnswer((Answer<Void>) invocation -> null);
			responseCreaterStatic.when(() -> ResponseCreater.createChallengePage(any(), any(), any(), any())).thenReturn(null);
			responseCreaterStatic.when(() -> ResponseCreater.sendChallengeResponse(any(), any(), any())).thenAnswer((Answer<Void>) invocation -> null);
			encryptionStatic.when(() -> Encryption.decrypt(any(), any())).thenReturn(toJsonNode(certificateJsonStr));

			// 実行結果
			loginAction.authenticate(context, platform);

			if (applicantData.equals("dummy")) {
				verify(context, times(1)).challenge(any());
				responseCreaterStatic.verify(() -> ResponseCreater.setLoginFormAttributes(any()), times(1));
				responseCreaterStatic.verify(() -> ResponseCreater.createChallengePage(any(), any(), any(), any()), times(1));
			} else if (!isCheckedStatusCode) {
				verify(context, times(1)).getSession();
				verify(context, times(1)).getAuthenticationSession();
				verify(context, times(1)).getRealm();
				verify(platform, times(3)).getUserRequest();
				verify(platform, times(1)).getPlatformResponse();
				verify(platform, times(1)).sendRequest();
				verify(flowTransition, times(1)).canExecuteAuthentication(any(), any());
				encryptionStatic.verify(() -> Encryption.decrypt(any(), any()), times(1));
			} else if (!existsUser) {
				verify(context, times(1)).getSession();
				verify(context, times(1)).getAuthenticationSession();
				verify(context, times(1)).getRealm();
				verify(platform, times(3)).getUserRequest();
				verify(platform, times(1)).getPlatformResponse();
				verify(platform, times(1)).sendRequest();
				verify(flowTransition, times(1)).canExecuteAuthentication(any(), any());
				verify(platformAuthenticationResponse, times(1)).getUniqueId();
				verify(userIdentityToModelMapper, times(1)).find(any(), any());
				userIdentityToModelMapperStatic.verify(() -> UserIdentityToModelMapper.getUserIdentityToCustomAttributeMapper(any()), times(1));
				encryptionStatic.verify(() -> Encryption.decrypt(any(), any()), times(1));
				responseCreaterStatic.verify(() -> ResponseCreater.sendChallengeResponse(any(), any(), any()), times(1));
			} else {
				verify(context, times(1)).getSession();
				verify(context, times(1)).getAuthenticationSession();
				verify(context, times(1)).getRealm();
				verify(context, times(1)).setUser(userModel);
				verify(context, times(1)).success();
				verify(platform, times(3)).getUserRequest();
				verify(platform, times(1)).getPlatformResponse();
				verify(platform, times(1)).sendRequest();
				verify(flowTransition, times(1)).canExecuteAuthentication(any(), any());
				verify(platformAuthenticationResponse, times(1)).getUniqueId();
				verify(userIdentityToModelMapper, times(1)).find(any(), any());
				userIdentityToModelMapperStatic.verify(() -> UserIdentityToModelMapper.getUserIdentityToCustomAttributeMapper(any()), times(1));
				encryptionStatic.verify(() -> Encryption.decrypt(any(), any()), times(1));
			}
		}
	}

	private JsonNode toJsonNode(String jsonStr) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(jsonStr);
	}
}
