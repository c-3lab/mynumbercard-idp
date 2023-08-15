package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.ArrayList;

/**
 * このクラスは認証SPIの設定情報定義を構造体として表現したものです。
 */
class SpiConfigProperty {

    private static ArrayList<ProviderConfigProperty> configProperties;

    private static final String NAME_PREFIX = "my-num-cd-auth."; // Config名に文字数制限があるため短くすること

    private SpiConfigProperty() {}

    /**
     * Config構造体の内部クラス名と認証SPIの設定項目順序を定義します。
     *
     * 1文字目とアンダースコア(_)の直後の文字を大文字とし、その文字列をクラス名とします。
     */
    enum ConfigName {
        DEBUG_MODE,
        CERTIFICATE_VALIDATOR_ROOT_URI,
        RUN_URI_OF_ANDROID_APPLICATION,
        RUN_URI_OF_IOS_APPLICATION,
        INSTALLATION_URI_OF_SMART_PHONE_APPLICATION,
        PLATFORM_API_CLIENT_CLASS_FQDN,
        PLATFORM_API_CLIENT_URI,
        PLATFORM_API_IDP_SENDER;

        @Override
        public String toString() {
            String[] classWord = name().split("_");
            StringBuilder className = new StringBuilder();
            Arrays.asList(classWord).forEach(str -> className.append(toFirstLetterUpperCase(str)));
            return className.toString();
        }

        /**
         * 先頭の1文字目を大文字にします。
         *
         * @param str 文字列
         * @return 先頭の1文字目が大文字の文字列
         */
        static final String toFirstLetterUpperCase(String str) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

    }

    static class DebugMode extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
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

    /*
     * [NOTE] 本来、クラス名にある単語 Ios は iOS の綴りが正しいです。
     *        このクラスの名前付けは例外です。
     *        機械的処理を目的とし、各単語の1文字目を大文字とし、それ以降は小文字としています。
     */
    static class RunUriOfIosApplication extends SpiConfigProperty {
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

    static class PlatformApiClientUri extends SpiConfigProperty {
        static final ProviderConfigProperty CONFIG;
        static final String NAME = "platform-api";
        static final String LABEL = "Platform API Root URI";
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
         configProperties.add(RunUriOfIosApplication.CONFIG);
         configProperties.add(InstallationUriOfSmartPhoneApplication.CONFIG);
         configProperties.add(PlatformApiClientClassFqdn.CONFIG);
         configProperties.add(PlatformApiClientUri.CONFIG);
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
         return RunUriOfIosApplication.CONFIG;
     }

     static ProviderConfigProperty getInstallationUriOfSmartPhoneApplication() {
         return InstallationUriOfSmartPhoneApplication.CONFIG;
     }

     /**
      *  プラットフォームのAPIルートURLの設定名を返します。
      *
      * @return プラットフォームのAPIルートURLの設定名
      */
     static String getPlatformApiRootUri() {
        return NAME_PREFIX + ConfigName.PLATFORM_API_CLIENT_URI.toString();
     }

     /**
      *  プラットフォームへ送るIDP送信者符号の設定名を返します。
      *
      * @return プラットフォームへ送るIDP送信者符号の設定名
      */
     static String getIdpSender() {
        return NAME_PREFIX + ConfigName.PLATFORM_API_IDP_SENDER.toString();
     }

    /**
     *  ProviderConfigPropertyクラスの配列を返します。
     *
     * @return SPI設定項目の配列
     */
    static ArrayList<ProviderConfigProperty> getPropertis() {
        return configProperties;
    }
}
