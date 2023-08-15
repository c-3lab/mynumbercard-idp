function getMobileOS() {
    var userAgent = navigator.userAgent;
    if (userAgent.match(/Android/))
        return 'Android';

    if (userAgent.match(/iPad|iPhone|iPod/))
        return 'iOS';

    // Not Android and iOS.
    return 'other';
}

function convertFileToBase64(uploadFile, publicKeyElement) {
    const curFiles = uploadFile.files;
    if (curFiles.length === 0 |
        curFiles.length > 1) {
        return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
        const binary = event.currentTarget.result;
        const base64Text = btoa(binary);

        publicKeyElement.value = base64Text;
    }

    for (const file of curFiles) {
        reader.readAsBinaryString(file);
    }
}

function signatureNonce(privKeyFile, signatureElement, nonce) {
    const curFiles = privKeyFile.files;
    if (curFiles.length === 0 |
        curFiles.length > 1) {
        return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
        const rsa = forge.pki.rsa;
        const md = forge.md.sha256.create();
        md.update(nonce, 'utf8');
        let privKey = forge.pki.privateKeyFromPem(event.currentTarget.result);
        let signature = privKey.sign(md);
        let base64Text = btoa(signature);

        signatureElement.value = base64Text;
    }

    for (const file of curFiles) {
        reader.readAsBinaryString(file);
    }
}

function clearX509Files(formData) {
    formData.delete('x509PrivFileName');
    formData.delete('x509FileName');
    formData.delete('certificate');
}

function setCertificateParameter(mode) {
    let certificate = document.querySelector('#certificate');
    if (mode === "login") certificate.name = 'userAuthenticationCertificate';
    if (mode === "registration" || mode === "replacement") certificate.name = 'encryptedDigitalSignatureCertificate';
}

function hasDebugParameters() {
    let applicantData = document.querySelector('input[name="applicantData"][type="hidden"]');
    let sign = document.querySelector('input[name="sign"][type="hidden"]');
    let certificate = document.querySelector('#certificate');
    if (applicantData.value == null ||
        sign.value == null ||
        certificate.value == null) return false;
    return true;
}

function submitWhenDebugMode(event) {
    if (hasDebugParameters()) {
        let form = document.querySelector('form#kc-form-login');
        let mode = event.currentTarget.name;
        document.querySelector('input[name="mode"][type="hidden"]').value = mode;
        setCertificateParameter(mode);
        form.submit();
    }
}

function enableDebugMode(nonce) {
    let applicantData = document.querySelector('input[name="applicantData"][type="hidden"]');
    let nonceHashMd = forge.md.sha256.create();
    nonceHashMd.update(nonce, 'utf8');
    applicantData.value = nonceHashMd.digest().toHex();

    let x509PrivFile = document.querySelector('#x509PrivFileName');
    x509PrivFile.addEventListener('change', (event) => {
        let signature = document.querySelector('input[name="sign"][type="hidden"]');
        signatureNonce(event.currentTarget, signature, applicantData.value);
    });

    let x509PublicFile = document.querySelector('#x509_upload');
    x509PublicFile.addEventListener('change', (event) => {
        let certificate = document.querySelector('#certificate');
        convertFileToBase64(event.currentTarget, certificate);
    });
}

function switchEnableActionRegistrationButtons() {
  if(!document.querySelector('input[name="agree-tos"]').checked ||
    !document.querySelector('input[name="agree-pp"]').checked) {
    document.querySelector('input[name="registration"]').setAttribute('disabled', true);
    return;
  }
  document.querySelector("input[name=registration]").removeAttribute('disabled');
}

function onOpenRegistrationButton() {
    document.querySelector('input[name="agree-tos"]').checked = false;
    document.querySelector('input[name="agree-pp"]').checked = false;
    document.querySelector('input[name="registration"]').setAttribute('disabled', true);
    document.querySelector('#userLogin').style.display = 'none';
    document.querySelector('#userRegistration').style.display = 'block';
    document.querySelector('div[name="debug-form-block"]');
    let dstElement = document.querySelector('#privacy-policy');
    let srcElement = document.querySelector('div[name="debug-form-block"]');
    dstElement.appendChild(srcElement);
    srcElement.classList.add("margin-top-20px");
}

function addEventListeners() {
    let form = document.querySelector('form#kc-form-login');
    form.addEventListener('formdata', (event) => {
        let formData = event.formData;
        clearX509Files(formData);
    });

    let openRegistrationButton = document.querySelector('input[name="openRegistration"][type="button"]');
    openRegistrationButton.addEventListener('click', (event) => { onOpenRegistrationButton(); });

    let goBackLink = document.querySelector('a[name="go-back"]');
    goBackLink.addEventListener('click', (event) => {
        document.querySelector('#userLogin').style.display = 'block';
        document.querySelector('#userRegistration').style.display = 'none';
        let dstElement = document.querySelector('#userLogin');
        let srcElement = document.querySelector('div[name="debug-form-block"]');
        dstElement.insertAdjacentElement('afterbegin', srcElement);
        srcElement.classList.remove("margin-top-20px");
    });

    let loginButton = document.querySelector('input[name="login"][type="button"]');
    loginButton.addEventListener('click', (event) => { onClickActionButton(event); });

    let replacementButton = document.querySelector('input[name="replacement"][type="button"]');
    replacementButton.addEventListener('click', (event) => { onClickActionButton(event); });

    let agreeTosButton = document.querySelector('input[name="agree-tos"]');
    agreeTosButton.addEventListener('input', (event) => { switchEnableActionRegistrationButtons(); });
    let agreePpButton = document.querySelector('input[name="agree-pp"]');
    agreePpButton.addEventListener('input', (event) => { switchEnableActionRegistrationButtons(); });

    let registrationButton = document.querySelector('input[name="registration"][type="button"]');
    registrationButton.addEventListener('click', (event) => { onClickActionButton(event); });
}
