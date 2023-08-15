package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * このクラスは認証SPIの設定情報定義を構造体として表現したものです。
 */
class SpiConfigProperty {
    /** コンソール用ロガー */
    private static org.jboss.logging.Logger consoleLogger = org.jboss.logging.Logger.getLogger(SpiConfigProperty.class);

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    private static final String NAME_PREFIX = "my-num-cd-auth."; // Config名に文字数制限があるため短くすること

    public SpiConfigProperty() {}

    static class DebugMode extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        static final String NAME = "debug-mode";
        static final String LABEL = "Enable debug mode";
        static final String HELP_TEXT = "Print javascript debug log to browser console, and login form screen.";
        static final String TYPE = ProviderConfigProperty.BOOLEAN_TYPE;
        static final boolean DEFAULT_VALUE = false;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    static class CertificateValidatorRootUri extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
        static final String NAME = "certificate-validator-uri";
        static final String LABEL = "Certificate Validator URI";
        static final String HELP_TEXT = null;
        static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    static class RunUriOfAndroidApplication extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
        static final String NAME = "android-app-uri";
        static final String LABEL = "Run URI of Android application";
        static final String HELP_TEXT = null;
        static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    static class RunUriOfiOSApplication extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
        static final String NAME = "ios-app-uri";
        static final String LABEL = "Run URI of iOS application";
        static final String HELP_TEXT = null;
        static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    static class InstallationUriOfSmartPhoneApplication extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
        static final String NAME = "app-uri";
        static final String LABEL = "Installation URI of Android/iOS application";
        static final String HELP_TEXT = null;
        static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    static class PlatformApiClientClassFqdn extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
        static final String NAME = "platform-class";
        static final String LABEL = "Platform API Clinet Class FQDN";
        static final String HELP_TEXT = null;
        static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        static final String DEFAULT_VALUE = "com.example.mynumbercardidp.keycloak.network.platform.BasicPlatformApiClient";

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    static class PlatformApiIdpSender extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
        static final String NAME = "platform-sender";
        static final String LABEL = "Platform API IDP sender";
        static final String HELP_TEXT = null;
        static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        static final String DEFAULT_VALUE = "ID123";

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

     static {
         configProperties.add(DebugMode.CONFIG);
         configProperties.add(CertificateValidatorRootUri.CONFIG);
         configProperties.add(RunUriOfAndroidApplication.CONFIG);
         configProperties.add(RunUriOfiOSApplication.CONFIG);
         configProperties.add(InstallationUriOfSmartPhoneApplication.CONFIG);
         configProperties.add(PlatformApiClientClassFqdn.CONFIG);
         configProperties.add(PlatformApiIdpSender.CONFIG);
     }

     static ProviderConfigProperty getDebugMode() {
         return DebugMode.CONFIG;
     }

     static ProviderConfigProperty getCertificateValidatorRootUri() {
         return CertificateValidatorRootUri.CONFIG;
     }

     static ProviderConfigProperty getRunUriOfAndroidApplication() {
         return RunUriOfAndroidApplication.CONFIG;
     }

     static ProviderConfigProperty getRunUriOfiOSApplication() {
         return RunUriOfiOSApplication.CONFIG;
     }

     static ProviderConfigProperty getInstallationUriOfSmartPhoneApplication() {
         return InstallationUriOfSmartPhoneApplication.CONFIG;
     }

     /**
      *  プラットフォームへ送るIDP送信者符号の設定名を返します。
      *
      * @return プラットフォームへ送るIDP送信者符号の設定名
      */
     static String getIdpSender() {
        return PlatformApiIdpSender.CONFIG.getName();
     }

    /**
     *  ProviderConfigPropertyクラスの配列を返します。
     *
     * @return SPI設定項目の配列
     */
    static List<ProviderConfigProperty> getPropertis() {
        return configProperties;
    }
}
