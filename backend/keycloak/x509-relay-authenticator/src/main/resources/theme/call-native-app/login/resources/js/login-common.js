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
