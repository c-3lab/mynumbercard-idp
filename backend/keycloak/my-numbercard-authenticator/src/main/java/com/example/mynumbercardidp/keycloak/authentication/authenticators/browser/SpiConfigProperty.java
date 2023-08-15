package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * このクラスは認証SPIの設定情報定義を構造体として表現したものです。
 */
public class SpiConfigProperty {
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<ProviderConfigProperty>();
    private static final String NAME_PREFIX = "my-num-cd-auth."; // [NOTE] Config名に文字数制限があるため省略形で表記する
    private static final Map<String, String> FREE_MARKER_JAVA_TEMPLATE_VARIABLES = new LinkedHashMap<>();

    private SpiConfigProperty() {}

    static {
        SpiConfigProperty.CONFIG_PROPERTIES.add(DebugMode.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(CertificateValidatorRootUri.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(RunUriOfAndroidApplication.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(RunUriOfiOSApplication.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(InstallationUriOfSmartPhoneApplication.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(PlatformApiClientClassFqdn.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(PlatformApiIdpSender.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(TermsOfUseDirURL.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(PrivacyPolicyDirURL.CONFIG);
        SpiConfigProperty.CONFIG_PROPERTIES.add(PersonalDataProtectionPolicyDirURL.CONFIG);
    }

    /**
    * ProviderConfigPropertyクラスの配列を返します。
    *
    * @return SPI設定項目の配列
    */
    public static final List<ProviderConfigProperty> getPropertis() {
       return SpiConfigProperty.CONFIG_PROPERTIES;
    }

    /**
     * FTLファイルへ注入する変数を返します。
     *
     * @return FTL変数名とSPI設定値の組み合わせ
     */
    public static Map<String, String> getFreeMarkerJavaTemplateVariables() {
        return FREE_MARKER_JAVA_TEMPLATE_VARIABLES;
    }

    // Authenticatorから実行され、FTLファイルへ注入する定数を作成する。
    static void initFreeMarkerJavaTemplateVariablesIfNeeded(AuthenticationFlowContext context) {
        FREE_MARKER_JAVA_TEMPLATE_VARIABLES.put("androidAppUri", CurrentConfig.getValue(context, SpiConfigProperty.RunUriOfAndroidApplication.CONFIG.getName()));
        FREE_MARKER_JAVA_TEMPLATE_VARIABLES.put("iosAppUri", CurrentConfig.getValue(context, SpiConfigProperty.RunUriOfiOSApplication.CONFIG.getName()));
        FREE_MARKER_JAVA_TEMPLATE_VARIABLES.put("otherAppUri", CurrentConfig.getValue(context, SpiConfigProperty.InstallationUriOfSmartPhoneApplication.CONFIG.getName()));
        FREE_MARKER_JAVA_TEMPLATE_VARIABLES.put("termsOfUseDirUrl", CurrentConfig.getValue(context, SpiConfigProperty.TermsOfUseDirURL.CONFIG.getName()));
        FREE_MARKER_JAVA_TEMPLATE_VARIABLES.put("privacyPolicyDirUrl", CurrentConfig.getValue(context, SpiConfigProperty.PrivacyPolicyDirURL.CONFIG.getName()));
        FREE_MARKER_JAVA_TEMPLATE_VARIABLES.put("personalDataProtectionPolicyDirUrl", CurrentConfig.getValue(context, SpiConfigProperty.PersonalDataProtectionPolicyDirURL.CONFIG.getName()));

        String debugMode = SpiConfigProperty.DebugMode.CONFIG.getName();
        String debugModeValue = CurrentConfig.getValue(context, debugMode).toLowerCase();
        debugModeValue = StringUtil.isEmpty(debugModeValue) ? "false" : debugModeValue.toLowerCase();
        SpiConfigProperty.FREE_MARKER_JAVA_TEMPLATE_VARIABLES.put("debug", debugModeValue);
    }

    // [NOTE] インナークラス内のクラス変数（定数）はインナークラスを増やす場合の作業量が増えるため、呼び出すときにクラス名をつけない。
    public static class DebugMode extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "debug-mode";
        public static final String LABEL = "Enable debug mode";
        public static final String HELP_TEXT = "Print javascript debug log to browser console, and login form screen.";
        public static final String TYPE = ProviderConfigProperty.BOOLEAN_TYPE;
        public static final boolean DEFAULT_VALUE = false;

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class CertificateValidatorRootUri extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "certificate-validator-uri";
        public static final String LABEL = "Certificate Validator URI";
        public static final String HELP_TEXT = "Platform API URL for authentication using the public personal authentication part of My Number Card.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class RunUriOfAndroidApplication extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "android-app-uri";
        public static final String LABEL = "Run URI of Android application";
        public static final String HELP_TEXT = "Android deep link or app link.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class RunUriOfiOSApplication extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "ios-app-uri";
        public static final String LABEL = "Run URI of iOS application";
        public static final String HELP_TEXT = "iOS deep link or universal links.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class InstallationUriOfSmartPhoneApplication extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "app-uri";
        public static final String LABEL = "Installation URI of Android/iOS application";
        public static final String HELP_TEXT = "Access from non Android or iOS. (e.g., PC)";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class PlatformApiClientClassFqdn extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "platform-class";
        public static final String LABEL = "Platform API Clinet Class FQDN";
        public static final String HELP_TEXT = "Fully qualified class name of the platform API client for authentication using the public personal authentication part of My Number Card.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class PlatformApiIdpSender extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "platform-sender";
        public static final String LABEL = "Platform API IDP sender";
        public static final String HELP_TEXT = "Identity provider sender code that platform API identify.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "ID123";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class TermsOfUseDirURL extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "terms-of-use";
        public static final String LABEL = "Terms of use page dir URL";
        public static final String HELP_TEXT = "The terms of use page directory path URL for identity provider.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "https://idp.example.com/open-id/";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class PrivacyPolicyDirURL extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "privacy-policy";
        public static final String LABEL = "Privacy policy page dir URL";
        public static final String HELP_TEXT = "The privacy policy page directory path URL for identity provider.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "https://idp.example.com/open-id/";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }

    public static class PersonalDataProtectionPolicyDirURL extends SpiConfigProperty {
        public static final ProviderConfigProperty CONFIG;
        public static final String NAME = "peronal-dpp";
        public static final String LABEL = "Personal data protection policy dir URL";
        public static final String HELP_TEXT = "The personal data protection policy page directory path URL for identity provider.";
        public static final String TYPE = ProviderConfigProperty.STRING_TYPE;
        public static final String DEFAULT_VALUE = "https://idp.example.com/open-id/";

        static {
            CONFIG = new ProviderConfigProperty(SpiConfigProperty.NAME_PREFIX + NAME, LABEL, HELP_TEXT, TYPE, DEFAULT_VALUE);
        }
    }
}
