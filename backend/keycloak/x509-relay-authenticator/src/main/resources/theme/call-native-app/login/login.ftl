<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
    <div id="kc-form">
      <div id="kc-form-wrapper">
        <script>
            window.addEventListener('DOMContentLoaded', (event) => {
                <#if (debug!'false') == 'true'>
                let form = document.querySelector('form#kc-form-login');
                form.addEventListener('formdata', (event) => {
                    let formData = event.formData;
                    formData.delete('x509PrivFileName');
                    formData.delete('x509FileName');
                });
                </#if>

                let usingAppButton = document.querySelector('form#kc-form-login input[name="login-using-app"][type="button"]');
                usingAppButton.addEventListener('click', (event) => {
                    let action_url = "${url.loginAction}";
                    action_url = action_url.replace(/\&amp;/gi,'&');
                    action_url = encodeURIComponent(action_url);

                    let app_url;
                    let removeQueryStrings = false;
                    switch (getMobileOS()) {
                        case 'Android':
                            app_uri = '${(androidAppUri!'')}';
                            break;
                        case 'iOS':
                            app_uri = '${(iosAppUri!'')}';
                            break;
                        default:
                            removeQueryStrings = true;
                            app_uri = '${(otherAppUri!'')}';
                            <#if (debug!'false') == 'true'>
                            console.debug('Android: ${(androidAppUri!'')}');
                            console.debug('iOS: ${(iosAppUri!'')}');
                            console.debug('Other: ${(otherAppUri!'')}');
                            console.debug(
                                'Query string: ' +
                                'action_url=' + action_url +
                                '&nonce=${(nonce!'')}'
                            );
                            </#if>
                    }

                    if (app_uri === '') {
                        return;
                    }

                    if (removeQueryStrings) {
                        location.href = app_uri;
                        return;
                    }

                    app_uri += '?' + 'action_url=' + action_url + '&nonce=${(nonce!'')}';
                    location.href = app_uri;
                });

                <#if (debug!'false') == 'true'>
                let x509PrivFile = document.querySelector('#x509PrivFileName');
                x509PrivFile.addEventListener('change', (event) => {
                    let signatureElement = document.querySelector('input[name="signature"]');
                    signatureNonce(event.currentTarget, signatureElement, '${(nonce!'')}');
                });

                let x509PublicFile = document.querySelector('#x509_upload');
                x509PublicFile.addEventListener('change', (event) => {
                    let x509FileElement = document.querySelector('input[name="x509File"]');
                    convertFileToBase64(event.currentTarget, x509FileElement);
                });
                </#if>
            });
        </script>
        <#if realm.password>
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <#if (debug!'false') == 'true'>
                <div class="${properties.kcFormGroupClass!}">
                    <label for="privkey" class="${properties.kcLabelClass!}">${msg("[DEBUG] X509 privkey file (.pem, .key)")}</label>
                    <input tabindex="2" name="x509PrivFileName" id="x509PrivFileName" type="file" accept=".pem, .key" />
                    <input type="hidden" name="signature" />
                </div>
                <div class="${properties.kcFormGroupClass!}">
                    <label for="x509upload" class="${properties.kcLabelClass!}">${msg("X509 certificate file (.der, .cer, .crt, .pem)")}</label>
                    <input tabindex="2" name="x509FileName" id="x509_upload" type="file" accept=".der, .cer, .crt, .pem" />
                    <input type="hidden" id="x509File" name="x509File" />
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                    <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                </div>
                </#if>

                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login-using-app" id="kc-login-using-app" type="button" value="${msg("doLogInUsingApp")}" />
                </div>
            </form>
        </#if>
        </div>

    </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a tabindex="6"
                                                 href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>
    <#elseif section = "socialProviders" >
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}">
                <hr/>
                <h4>${msg("identity-provider-login-label")}</h4>

                <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
                    <#list social.providers as p>
                        <a id="social-${p.alias}" class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                                type="button" href="${p.loginUrl}">
                            <#if p.iconClasses?has_content>
                                <i class="${properties.kcCommonLogoIdP!} ${p.iconClasses!}" aria-hidden="true"></i>
                                <span class="${properties.kcFormSocialAccountNameClass!} kc-social-icon-text">${p.displayName!}</span>
                            <#else>
                                <span class="${properties.kcFormSocialAccountNameClass!}">${p.displayName!}</span>
                            </#if>
                        </a>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
