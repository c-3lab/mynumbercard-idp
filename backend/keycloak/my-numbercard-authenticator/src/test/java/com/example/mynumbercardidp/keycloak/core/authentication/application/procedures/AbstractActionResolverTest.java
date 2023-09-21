package com.example.mynumbercardidp.keycloak.core.authentication.application.procedures;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientResolver;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;

public class AbstractActionResolverTest {

  // 抽象クラステストの為、ダミーの実装クラスを作成
  public class ConcreteImpl extends AbstractActionResolver {

    @Override
    public void executeUserAction(final AuthenticationFlowContext context) {}

  }

  @InjectMocks
  ConcreteImpl concreteImpl = new ConcreteImpl();

  @Mock
  AuthenticationFlowContext context;

  @Mock
  PlatformApiClientResolver platformResolver;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  /**
   * createPlatformメソッドテスト
   * @throws Exception クラス名、フィールド名が存在しない場合
   */
  public void testCreatePlatform() throws Exception {

    try (
      MockedStatic<CurrentConfig> currentConfigMock = mockStatic(CurrentConfig.class);
      ) {
      currentConfigMock.when(() -> CurrentConfig.getValue(any(), eq("my-num-cd-auth.platform-class"))).thenReturn("com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient");
      currentConfigMock.when(() -> CurrentConfig.getValue(any(), eq("my-num-cd-auth.certificate-validator-uri"))).thenReturn("CertificateValidatorRootUri");
      currentConfigMock.when(() -> CurrentConfig.getValue(any(), eq("my-num-cd-auth.platform-sender"))).thenReturn("PlatformApiIdpSender");

      PlatformApiClientInterface platform = (PlatformApiClientInterface) Class.forName("com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient").getDeclaredConstructor().newInstance();

      Field field = concreteImpl.getClass().getSuperclass().getDeclaredField("platformResolver");
      field.setAccessible(true);

      doReturn(platform).when(platformResolver).createPlatform(any(), any(), any(), any());

      // 期待値
      PlatformApiClientInterface expected = platform;

      // 実行結果
      PlatformApiClientInterface result = concreteImpl.createPlatform(context);

      // 検証
      currentConfigMock.verify(() -> CurrentConfig.getValue(any(), any()), times(3));
      verify(platformResolver, times(1)).createPlatform(any(), any(), any(), any());
      assertEquals(expected, result);

    }
    
  }

  @Test
  /**
   * dreatePlatformメソッドテスト(例外)
   * @throws IllegalStateException プラットフォームAPIのURLが空値の場合
   */
  public void testCreatePlatformException() throws IllegalStateException {

    try (
      MockedStatic<CurrentConfig> currentConfigMock = mockStatic(CurrentConfig.class);
      ) {
      currentConfigMock.when(() -> CurrentConfig.getValue(any(), any())).thenReturn("");

      // 検証(IllegalStateException発生)
      assertThrows(IllegalStateException.class, () -> {
        // 実行
        concreteImpl.createPlatform(context);
      });

    }
    
  }
}
