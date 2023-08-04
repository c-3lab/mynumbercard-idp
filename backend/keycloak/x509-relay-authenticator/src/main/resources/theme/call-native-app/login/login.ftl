<#import "template.ftl" as layout>
<#assign termsofServiceFileName>${msg("termsofServiceFileName")}</#assign>
<#assign privacyPolicyFileName>${msg("privacyPolicyFileName")}</#assign>
<#assign personalDataProtectionPolicyFileName>${msg("personalDataProtectionPolicyFileName")}</#assign>
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

                let loginUsingAppButton = document.querySelector('form#kc-form-login input[name="applogin"][type="button"]');
                loginUsingAppButton.addEventListener('click', (event) => {

                    let action_url = "${url.loginAction}";
                    action_url = action_url.replace(/\&amp;/gi,'&');
                    action_url = encodeURIComponent(action_url);

                    let app_url;
                    switch (getMobileOS()) {
                        case 'Android':
                            app_uri = '${(androidAppUri!'')}';
                            break;
                        case 'iOS':
                            app_uri = '${(iosAppUri!'')}';
                            break;
                        default:
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

                    app_uri += '?' + 'action_url=' + action_url + '&nonce=${(nonce!'')}' + '&mode=login';
                    <#if initialView??>
                        let refreshUrl = encodeURIComponent('${refreshUrl}&amp;initialView=registration');
                    <#else>
                        let refreshUrl = encodeURIComponent('${refreshUrl}&amp;initialView=login');
                    </#if>
                    app_uri += '&error_url=' + refreshUrl;

                    location.href = app_uri;
                });

                let doRegistrationUsingAppButton = document.querySelector('input[name="openRegistration"][type="button"]');
                doRegistrationUsingAppButton.addEventListener('click', (event) => {
                    document.querySelector("input[name=${paramAgreeTos!'agree-tos'}]").checked = false;
                    document.querySelector("input[name=${paramAgreeTos!'agree-pp'}]").checked = false;
                    <#if (debug!'false') == 'true'>
                        document.querySelector("input[name=registration]").setAttribute('disabled', true);
                    </#if>
                    document.querySelector("input[name=appregistration]").setAttribute('disabled', true);
                    document.querySelector('#userLogin').style.display = 'none';
                    document.querySelector('#userRegistration').style.display = 'block';
                });

                let replacementUsingAppButton = document.querySelector('form#kc-form-replacement input[name="replacement"][type="button"]');
                replacementUsingAppButton.addEventListener('click', (event) => {
                    let action_url = "${url.loginAction}";
                    action_url = action_url.replace(/\&amp;/gi,'&');
                    action_url = encodeURIComponent(action_url);

                    let app_url;
                    switch (getMobileOS()) {
                        case 'Android':
                            app_uri = '${(androidAppUri!'')}';
                            break;
                        case 'iOS':
                            app_uri = '${(iosAppUri!'')}';
                            break;
                        default:
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

                    app_uri += '?' + 'action_url=' + action_url + '&nonce=${(nonce!'')}' + '&mode=replacement';
                    <#if (initialView??)>
                        let refreshUrl = encodeURIComponent('${refreshUrl}&amp;initialView=registration');
                    <#else>
                        let refreshUrl = encodeURIComponent('${refreshUrl}&amp;initialView=login');
                    </#if>
                    app_uri += '&error_url=' + refreshUrl;

                    location.href = app_uri;
                });

                <#if (debug!'false') == 'true'>
                    document.querySelector("input[name=registration]").setAttribute('disabled', true);
                </#if>
                document.querySelector("input[name=appregistration]").setAttribute('disabled', true);

                function switchEnableNextButtons() {
                  if(!document.querySelector("input[name=${paramAgreeTos!'agree-tos'}]").checked ||
                    !document.querySelector("input[name=${paramAgreeTos!'agree-pp'}]").checked) {
                    <#if (debug!'false') == 'true'>
                        document.querySelector("input[name=registration]").setAttribute('disabled', true);
                    </#if>
                    document.querySelector("input[name=appregistration]").setAttribute('disabled', true);
                    return;
                  }
                  <#if (debug!'false') == 'true'>
                    document.querySelector("input[name=registration]").removeAttribute('disabled');
                  </#if>
                  document.querySelector("input[name=appregistration]").removeAttribute('disabled');
                }

                document.querySelector("input[name=${paramAgreeTos!'agree-tos'}]").addEventListener('input', (event) => {
                    switchEnableNextButtons();
                });
                document.querySelector("input[name=${paramAgreeTos!'agree-pp'}]").addEventListener('input', (event) => {
                    switchEnableNextButtons();
                });
                <#if (debug!'false') == 'true'>
                    document.querySelector("input[name=registration]").addEventListener('click', (event) => {
                        if(!document.querySelector("input[name=termsOfServiceCheck]").checked ||
                        !document.querySelector("input[name=privacyPolicyCheck]").checked) {
                            return false;
                        }

                        event.preventDefault();
                    });
                </#if>

                document.querySelector("input[name=appregistration]").addEventListener('click', (event) => {
                    if(!document.querySelector("input[name=termsOfServiceCheck]").checked ||
                       !document.querySelector("input[name=privacyPolicyCheck]").checked) {
                        return false;
                    }

                    document.querySelector('#user-registration').style.display = 'block';

                    event.preventDefault();
                });

                <#if (debug!'false') == 'true'>
                let registrationForm = document.querySelector('form#kc-register-form');
                registrationForm.addEventListener('formdata', (event) => {
                    let formData = event.formData;
                    formData.delete('x509PrivFileName');
                    formData.delete('x509FileName');
                });
                </#if>

                let registrationUsingAppButton = document.querySelector('form#kc-register-form input[name="appregistration"][type="button"]');
                registrationUsingAppButton.addEventListener('click', (event) => {
                    let action_url = "${url.loginAction}";
                    action_url = action_url.replace(/\&amp;/gi,'&');
                    action_url = encodeURIComponent(action_url);

                    let app_url;
                    switch (getMobileOS()) {
                        case 'Android':
                            app_uri = '${(androidAppUri!'')}';
                            break;
                        case 'iOS':
                            app_uri = '${(iosAppUri!'')}';
                            break;
                        default:
                            app_uri = '${(otherAppUri!'')}';
                            <#if (debug!'false') == 'true'>
                            console.debug('Android: ${(androidAppUri!'')}');
                            console.debug('iOS: ${(iosAppUri!'')}');
                            console.debug('Other: ${(otherAppUri!'')}');
                            </#if>
                            console.debug(
                                'Query string: ' +
                                'action_url=' + action_url +
                                '&nonce=${(nonce!'')}'
                            );
                    }

                    if (app_uri === '') {
                        return;
                    }

                    let refreshUrl = encodeURIComponent('${refreshUrl}&amp;initialView=login');
                    app_uri += '?' + 'action_url=' + action_url + '&nonce=${(nonce!'')}' + '&mode=registration' + '&error_url=' + refreshUrl;

                    location.href = app_uri;
                });

                let goBackLink = document.querySelector('span[name="go-back"]');
                goBackLink.addEventListener('click', (event) => {
                    document.querySelector('#userLogin').style.display = 'block';
                    document.querySelector('#userRegistration').style.display = 'none';
                });

                <#if (debug!'false') == 'true'>
                let x509PrivFile = document.querySelector('#x509PrivFileName');
                x509PrivFile.addEventListener('change', (event) => {
                    let signatureElement = document.querySelector('input[name="sign"]');
                    signatureNonce(event.currentTarget, signatureElement, '${(nonce!'')}');
                });

                let x509PublicFile = document.querySelector('#x509_upload');
                x509PublicFile.addEventListener('change', (event) => {
                    let x509FileElement  = document.querySelector('input[name="userAuthenticationCertificate"]');
                    let x509File2Element = document.querySelector('input[name="encryptedDigitalSignatureCertificate"]');
                    convertFileToBase64(event.currentTarget, x509FileElement);
                    convertFileToBase64(event.currentTarget, x509File2Element);
                });

                let x509PrivFileReg = document.querySelector('#x509PrivFileNameReg');
                x509PrivFileReg.addEventListener('change', (event) => {
                    let signatureElement = document.querySelector('#regSign > input[name="sign"]');
                    signatureNonce(event.currentTarget, signatureElement, '${(nonce!'')}');
                });

                let x509PublicFileReg = document.querySelector('#x509_uploadReg');
                x509PublicFileReg.addEventListener('change', (event) => {
                    let x509FileElementReg  = document.querySelector('#regCert > input[name="userAuthenticationCertificate"]');
                    let x509File2ElementReg = document.querySelector('#regCert > input[name="encryptedDigitalSignatureCertificate"]');
                    convertFileToBase64(event.currentTarget, x509FileElementReg);
                    convertFileToBase64(event.currentTarget, x509File2ElementReg);
                });

                let applicantDataElement = document.querySelector('input[name="applicantData"]');
                let applicantDataElementReg = document.querySelector('#regSign > input[name="applicantData"]');
                const nonceHashMd = forge.md.sha256.create();
                nonceHashMd.update('${(nonce!'')}', 'utf8');
                applicantDataElement.value = nonceHashMd.digest().toHex();
                applicantDataElementReg.value = nonceHashMd.digest().toHex();
                </#if>

                switch ('${(initialView!'')}') {
                  case 'registration':
                    doRegistrationUsingAppButton.click();
                    break;
                  default:
                }
            });
        </script>
        <div id="userLogin" style="display: block;">
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <#if (debug!'false') == 'true'>
                <div class="${properties.kcFormGroupClass!}">
                    <label for="mode" class="${properties.kcLabelClass!}">Mode Change</label>
                    <select name="mode">
                        <option value="login">login</option>
                        <option value="registration">registration</option>
                        <option value="replacement">replacement</option>
                    </select>
                </div>
                <div class="${properties.kcFormGroupClass!}">
                    <label for="privkey" class="${properties.kcLabelClass!}">${msg("[DEBUG] X509 privkey file (.pem, .key)")}</label>
                    <input tabindex="2" name="x509PrivFileName" id="x509PrivFileName" type="file" accept=".pem, .key" />
                    <input type="hidden" name="sign" />
                    <input type="hidden" name="applicantData" />
                </div>
                <div class="${properties.kcFormGroupClass!}">
                    <label for="x509upload" class="${properties.kcLabelClass!}">${msg("X509 certificate file (.der, .cer, .crt, .pem)")}</label>
                    <input tabindex="2" name="x509FileName" id="x509_upload" type="file" accept=".der, .cer, .crt, .pem" />
                    <input type="hidden" id="userAuthenticationCertificate" name="userAuthenticationCertificate" />
                    <input type="hidden" id="encryptedDigitalSignatureCertificate" name="encryptedDigitalSignatureCertificate" />
                </div>
                <label for="login" class="${properties.kcLabelClass!}">${msg("loginLabel")}</label>
                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input type="hidden" id="login-id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                    <input type="hidden" id="login-mode-hidden-input" name="mode" value="login"/>
                    <input type="hidden" id="login-nonce-hidden-input" name="nonce" value="${(nonce!'')}"/>
                    <input type="hidden" id="login-error-url-hidden-input" name="error_url" value="${refreshUrl}&initialView=registration"/>
                    <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                </div>
                </#if>
                <label for="applogin" class="${properties.kcLabelClass!}">${msg("loginLabel")}</label>
                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="applogin" id="kc-applogin" type="button" value="${msg("doLogIn")}"/>
                </div>
            </form>
            <label for="openRegistration" class="${properties.kcLabelClass!}">${msg("registrationLabel")}</label>
            <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="openRegistration" id="kc-openRegistration" type="button" value="${msg("doRegistration")}"/>
            </div>
            <form id="kc-form-replacement" onsubmit="replacement.disabled = true; return true;" action="${url.loginAction}" method="post">
                <label for="replacement" class="${properties.kcLabelClass!}">${msg("replacementLabel")}</label>
                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="replacement" id="kc-replacement" type="button" value="${msg("doReplacement")}"/>
                </div>
            </form>
        </div>
        <div id="userRegistration" style="display: none;">
            <div id="kc-form-options">
                <div class="${properties.kcFormOptionsWrapperClass!}">
                    <span id="go-back" name="go-back"><a href="#">${msg("back")}</a></span>
                </div>
            </div>
            <div class="${properties.kcFormHeaderClass!}">
                <label class="pf-c-form__label">${msg("registration")}</label>
            </div>
            <label class="${properties.kcLabelClass!}">${msg("registrationExplanation")}</label>
            
            <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
                <div id="user-registration">
                    <div id="terms-of-service" class="${properties.kcLabelWrapperClass!} terms-of-service">
                        <input type="checkbox" id="${paramAgreeTos!'agree-tos'}" name="${paramAgreeTos!'agree-tos'}" />
                        <label for="${paramAgreeTos!'agree-tos'}" class="pf-c-form__label-text terms-of-service">${msg("agreePhraseBefore")}<a href="https://nginx.example.com/open-id/${termsofServiceFileName}" target="_blank">${msg("displayTextTos")}</a>${msg("agreePhraseAfter")}</label>
                    </div>
                    <div id="privacy-policy" class="${properties.kcLabelWrapperClass!} privacy-policy kc-terms-text">
                        <input type="checkbox" id="${paramAgreePp!'agree-pp'}" name="${paramAgreePp!'agree-pp'}" />
                        <label for="${paramAgreePp!'agree-pp'}" class="pf-c-form__label-text privacy-policy">${msg("agreePhraseBefore")}<a href="https://nginx.example.com/open-id/${privacyPolicyFileName}" target="_blank">${msg("displayTextPp")}</a>${msg("agreePhraseAfter")}</label>
                    </div>
                    <#if (debug!'false') == 'true'>
                    <div class="${properties.kcFormGroupClass!}">
                        <div class="${properties.kcLabelWrapperClass!}">
                            <label for="privkey" class="${properties.kcLabelClass!}">${msg("[DEBUG] X509 privkey file (.pem, .key)")}</label>
                        </div>
                        <div class="${properties.kcInputWrapperClass!}" id="regSign">
                            <input tabindex="2" name="x509PrivFileNameReg" id="x509PrivFileNameReg" type="file" accept=".pem, .key" />
                            <input type="hidden" name="sign" />
                            <input type="hidden" name="applicantData" />
                        </div>
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <div class="${properties.kcLabelWrapperClass!}">
                            <label for="x509upload" class="${properties.kcLabelClass!}">${msg("X509 certificate file (.der, .cer, .crt, .pem)")}</label>
                        </div>
                        <div class="${properties.kcInputWrapperClass!}" id="regCert">
                            <input tabindex="2" name="x509FileNameReg" id="x509_uploadReg" type="file" accept=".der, .cer, .crt, .pem" />
                            <input type="hidden" id="encryptedDigitalSignatureCertificate" name="encryptedDigitalSignatureCertificate" />
                        </div>
                    </div>
                    <div id="kc-form-buttons">
                        <input type="hidden" id="registration-mode-hidden-input" name="mode" value="registration"/>
                        <input type="hidden" id="registration-nonce-hidden-input" name="nonce" value="${(nonce!'')}"/>
                        <input type="hidden" id="registration-error-url-hidden-input" name="error_url" value="${refreshUrl}&initialView=login"/>
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="registration" type="submit" value="${msg("doUserRegistration")}"/>
                    </div>
                    </#if>
                    <div id="kc-form-buttons">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="appregistration" type="button" value="${msg("doUserRegistration")}"/>
                    </div>
                </div>
            </form>
        </div>
      </div>
    </div>
    <#elseif section = "info" >
        <span><a tabindex="6" href="https://nginx.example.com/open-id/${termsofServiceFileName}" target="_blank">${msg("displayTextTos")}</a></span>
        <span><a tabindex="6" href="https://nginx.example.com/open-id/${privacyPolicyFileName}" target="_blank">${msg("displayTextPp")}</a></span>
        <span><a tabindex="6" href="https://nginx.example.com/open-id/${personalDataProtectionPolicyFileName}" target="_blank">${msg("displayTextPdpp")}</a></span>
    </#if>

</@layout.registrationLayout>
