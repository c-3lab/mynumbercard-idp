<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm'); section>
    <#if section = "header">
        ${msg("registerTitle")}
    <#elseif section = "form">
        <script>
            window.addEventListener('DOMContentLoaded', (event) => {
                <#if (screenLayout??)>
                var consentScreen = {
                    'agreeTos'   : document.querySelector('input[type="checkbox"]#${paramAgreeTos!'agree-tos'}'),
                    'agreePp'    : document.querySelector('input[type="checkbox"]#${paramAgreePp!'agree-pp'}'),
                    'nextButton' : document.querySelector('#consent-form-buttons > input[type="button"]')
                }

                function switchEnableNextButton() {
                  if(!consentScreen['agreeTos'].checked ||
                     !consentScreen['agreePp'].checked) {
                      consentScreen['nextButton'].setAttribute('disabled', true);
                      return;
                  }
                  consentScreen['nextButton'].removeAttribute('disabled');
                }

                consentScreen['agreeTos'].addEventListener('input', (event) => {
                    switchEnableNextButton();
                });
                consentScreen['agreePp'].addEventListener('input', (event) => {
                    switchEnableNextButton();
                });
                consentScreen['nextButton'].addEventListener('click', (event) => {
                    if(!consentScreen['agreeTos'].checked ||
                       !consentScreen['agreePp'].checked) {
                        return false;
                    }

                    document.querySelector('#consent-contents').style.display = 'none';
                    document.querySelector('#user-registration').style.display = 'block';

                    event.preventDefault();
                });
                </#if>

                <#if (debug!'false') == 'true'>
                let form = document.querySelector('form#kc-register-form');
                form.addEventListener('formdata', (event) => {
                    let formData = event.formData;
                    <#if (screenLayout!'link') == 'embedded'>
                    formData.delete('_tosTextarea');
                    formData.delete('_ppTextarea');
                    </#if>
                    formData.delete('x509PrivFileName');
                    formData.delete('x509FileName');
                });
                </#if>

                <#if (! screenLayout??)>
                document.querySelector('#user-registration').style.display = 'block';
                </#if>

                let usingAppButton = document.querySelector('form#kc-register-form input[name="registration-using-app"][type="button"]');
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
                            </#if>
                            console.debug(
                                'Query string: ' +
                                'action_url=' + action_url +
                                '&nonce=${(nonce!'')}'
                            );
                            <#if screenLayout??>
                            console.debug(
                                'Additional query string: ' +
                                '&${paramAgreeTos!'agree-tos'}=' + consentScreen['agreeTos'].checked +
                                '&${paramAgreePp!'agree-pp'}=' + consentScreen['agreePp'].checked
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
                    <#if screenLayout??>
                    app_uri += '&${paramAgreeTos!'agree-tos'}=' + consentScreen['agreeTos'].checked +
                               '&${paramAgreePp!'agree-pp'}=' + consentScreen['agreePp'].checked;
                    </#if>
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
        <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.registrationAction}" method="post">
            <#if (screenLayout??)>
            <div id="consent-contents">
                <#if (screenLayout!'link') == 'link'>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcLabelWrapperClass!}">
                        <p id="consent-text">${msg("consentAgreeMessage")}</p>
                    </div>
                    <div class="${properties.kcLabelWrapperClass!}">
                        <input type="checkbox" id="${paramAgreeTos!'agree-tos'}" name="${paramAgreeTos!'agree-tos'}" />
                        <label for="${paramAgreeTos!'agree-tos'}" class="${properties.kcLabelClass!}">${msg("agreePhraseBefore")}<a href="${termsOfUseUrl!''}" target="_blank">${msg("displayTextTos")}</a>${msg("agreePhraseAfter")}</label>
                    </div>
                    <div class="${properties.kcLabelWrapperClass!}">
                        <input type="checkbox" id="${paramAgreePp!'agree-pp'}" name="${paramAgreePp!'agree-pp'}" />
                        <label for="${paramAgreePp!'agree-pp'}" class="${properties.kcLabelClass!}">${msg("agreePhraseBefore")}<a href="${privacyPolicyUrl!''}" target="_blank">${msg("displayTextPp")}</a>${msg("agreePhraseAfter")}</label>
                    </div>
                </div>
                </#if>
                <#if (screenLayout!'link') == 'embedded'>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcLabelWrapperClass!}">
                        <p id="consent-text">${msg("consentAgreeMessage")}</p>
                    </div>
                    <div class="${properties.kcLabelWrapperClass!}">
                        <textarea name="_tosTextarea" readonly="true" rows="20" style="width: 100%; padding: 1ex; margin-bottom: 2em; display: block;">${termsOfUseText}</textarea>
                        <textarea name="_ppTextarea" readonly="true" rows="20" style="width: 100%; padding: 1ex; margin-bottom: 1em; display: block;">${privacyPolicyText}</textarea>
                        <p>
                            <input type="checkbox" id="${paramAgreeTos!'agree-tos'}" name="${paramAgreeTos!'agree-tos'}" />
                            <label for="${paramAgreeTos!'agree-tos'}" class="${properties.kcLabelClass!}">${msg("agreePhraseBefore")}${msg("displayTextTos")}${msg("agreePhraseAfter")}</label>
                        </p>
                        <p>
                            <input type="checkbox" id="${paramAgreePp!'agree-pp'}" name="${paramAgreePp!'agree-pp'}" />
                            <label for="${paramAgreePp!'agree-pp'}" class="${properties.kcLabelClass!}">${msg("agreePhraseBefore")}${msg("displayTextPp")}${msg("agreePhraseAfter")}</label>
                        </p>
                    </div>
                </div>
                </#if>
                <div class="${properties.kcFormGroupClass!}">
                    <div id="consent-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="button" value="${msg("doNext")}" disabled="true" />
                    </div>
                </div>
            </div>
            </#if>
            <div id="user-registration" style="display: none;">
                <#if (debug!'false') == 'true'>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcLabelWrapperClass!}">
                        <label for="privkey" class="${properties.kcLabelClass!}">${msg("[DEBUG] X509 privkey file (.pem, .key)")}</label>
                    </div>
                    <div class="${properties.kcInputWrapperClass!}">
                        <input tabindex="2" name="x509PrivFileName" id="x509PrivFileName" type="file" accept=".pem, .key" />
                        <input type="hidden" name="signature" />
                    </div>
                </div>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcLabelWrapperClass!}">
                        <label for="x509upload" class="${properties.kcLabelClass!}">${msg("X509 certificate file (.der, .cer, .crt, .pem)")}</label>
                    </div>
                    <div class="${properties.kcInputWrapperClass!}">
                        <input tabindex="2" name="x509FileName" id="x509_upload" type="file" accept=".der, .cer, .crt, .pem" />
                        <input type="hidden" id="x509File" name="x509File" />
                    </div>
                </div>
                </#if>
                <div class="${properties.kcFormGroupClass!}">
                    <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                        <div class="${properties.kcFormOptionsWrapperClass!}">
                            <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                        </div>
                    </div>

                    <#if (debug!'false') == 'true'>
                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doRegister")}"/>
                    </div>
                    </#if>

                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="registration-using-app" type="button" value="${msg("doRegisterUsingApp")}" />
                    </div>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
