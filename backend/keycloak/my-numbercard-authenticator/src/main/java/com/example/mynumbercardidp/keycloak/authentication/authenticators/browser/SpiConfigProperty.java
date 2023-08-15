package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * このクラスは認証SPIの設定情報定義を構造体として表現したものです。
 */
public class SpiConfigProperty {
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    private static final String NAME_PREFIX = "my-num-cd-auth."; // Config名に文字数制限があるため短くすること

    public SpiConfigProperty() {}

    public static class DebugMode extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "debug-mode";
        public static final String LABEL = "Enable debug mode";
        public static final String HELP_TEXT = "Print javascript debug log to browser console, and login form screen.";
        public static final String TYPE = ProviderConfigProperty.BOOLEAN_TYPE;
        public static final boolean DEFAULT_VALUE = false;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class CertificateValidatorRootUri extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "certificate-validator-uri";
        public static final String LABEL = "Certificate Validator URI";
        public static final String HELP_TEXT = null;
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class RunUriOfAndroidApplication extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "android-app-uri";
        public static final String LABEL = "Run URI of Android application";
        public static final String HELP_TEXT = null;
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class RunUriOfiOSApplication extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "ios-app-uri";
        public static final String LABEL = "Run URI of iOS application";
        public static final String HELP_TEXT = null;
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class InstallationUriOfSmartPhoneApplication extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "app-uri";
        public static final String LABEL = "Installation URI of Android/iOS application";
        public static final String HELP_TEXT = null;
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = null;

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class PlatformApiClientClassFqdn extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "platform-class";
        public static final String LABEL = "Platform API Clinet Class FQDN";
        public static final String HELP_TEXT = null;
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "com.example.mynumbercardidp.keycloak.network.platform.BasicPlatformApiClient";

        static {
            CONFIG = new ProviderConfigProperty(NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class PlatformApiIdpSender extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "platform-sender";
        public static final String LABEL = "Platform API IDP sender";
        public static final String HELP_TEXT = null;
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "ID123";

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

     public static ProviderConfigProperty getDebugMode() {
         return DebugMode.CONFIG;
     }

     public static ProviderConfigProperty getCertificateValidatorRootUri() {
         return CertificateValidatorRootUri.CONFIG;
     }

     public static ProviderConfigProperty getRunUriOfAndroidApplication() {
         return RunUriOfAndroidApplication.CONFIG;
     }

     public static ProviderConfigProperty getRunUriOfiOSApplication() {
         return RunUriOfiOSApplication.CONFIG;
     }

     public static ProviderConfigProperty getInstallationUriOfSmartPhoneApplication() {
         return InstallationUriOfSmartPhoneApplication.CONFIG;
     }

     /**
      * プラットフォームへ送るIDP送信者符号の設定名を返します。
      *
      * @return プラットフォームへ送るIDP送信者符号の設定名
      */
     public static String getIdpSender() {
        return PlatformApiIdpSender.CONFIG.getName();
     }

    /**
     * ProviderConfigPropertyクラスの配列を返します。
     *
     * @return SPI設定項目の配列
     */
    public static List<ProviderConfigProperty> getPropertis() {
        return configProperties;
    }
}
