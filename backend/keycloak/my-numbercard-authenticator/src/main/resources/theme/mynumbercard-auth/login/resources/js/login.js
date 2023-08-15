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
