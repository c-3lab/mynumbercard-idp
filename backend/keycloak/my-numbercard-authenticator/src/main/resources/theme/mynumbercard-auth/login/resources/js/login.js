function getMobileOS() {
    var userAgent = navigator.userAgent;
    if (userAgent.match(/Android/))
        return 'Android';

    if (userAgent.match(/iPad|iPhone|iPod/))
        return 'iOS';

    // AndroidまたはiOSではない
    return 'other';
}

async function encryptDataAsJWE(data, jwksUrl) {
    // JWKS形式の公開鍵をダウンロードして公開鍵を読み込み
    const JWKS = jose.createRemoteJWKSet(jwksUrl);
    const publicKey = await JWKS({alg: 'RSA-OAEP-256'});

    const jwe = await new jose.EncryptJWT({ 'claim': data })
        .setProtectedHeader({ alg: 'RSA-OAEP-256', enc: 'A128CBC-HS256' })
        .setExpirationTime('5m')
        .encrypt(publicKey);

    return jwe;
}

function readFile(fileElement) {
    const reader = new FileReader();
    return new Promise(resolve => {
        reader.onload = (event) => {
            resolve(event.currentTarget.result);
        }
    
        reader.readAsBinaryString(fileElement.files[0]);
    })
}

function signatureNonce(nonce, privateKey) {
    const rsa = forge.pki.rsa;
    const md = forge.md.sha256.create();
    md.update(nonce, 'utf8');
    const key = forge.pki.privateKeyFromPem(privateKey);
    return btoa(key.sign(md));
}

function hasDebugParameters() {
    const privateKey = document.querySelector('input[name="privateKey"]');
    const publicCertificate = document.querySelector('input[name="publicCertificate"]');
    if (privateKey.files.length === 0) return false; 
    if (publicCertificate.files.length === 0) return false; 
    return true;
}

async function submitWhenDebugMode(mode) {
    if (!hasDebugParameters()) return;

    const form = document.querySelector('form#kc-form-login');
    form.mode.value = mode;

    //  公開鍵をJWEで暗号化してフォームに設定
    const publicCertificateElement = document.querySelector('#publicCertificate');
    const publicCertificate = await readFile(publicCertificateElement);
    const jwksPath = document.querySelector('#jwksPath').value;
    const jwksUrl = new URL(jwksPath, location.href);
    const jwe = await encryptDataAsJWE(publicCertificate, jwksUrl);
    if (mode === 'login') {
        form.encryptedUserAuthenticationCertificate.value = jwe;
    } else {
        form.encryptedDigitalSignatureCertificate.value = jwe;
    }

    //  nonceをフォームに設定
    const nonce = document.querySelector('#nonce').value;
    form.applicantData.value = nonce;

    //  nonceのハッシュを秘密鍵で署名してフォームに設定
    const privateKeyElement = document.querySelector('#privateKey');
    const privateKey = await readFile(privateKeyElement);
    const signature = signatureNonce(nonce, privateKey);
    form.sign.value = signature;

    form.submit();
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
    moveToRegistrationBlockWhenDebugModeEnable();
}

function moveToRegistrationBlockWhenDebugModeEnable() {
    let srcElement = document.querySelector('div[name="debug-form-block"]');
    if (srcElement == null) {
        return;
    }
    let dstElement = document.querySelector('#kc-form-options + div');
    dstElement.appendChild(srcElement);
    srcElement.classList.add("margin-top-20px");
}

function moveToLoginBlockWhenDebugModeEnable() {
    let srcElement = document.querySelector('div[name="debug-form-block"]');
    if (srcElement == null) {
        return;
    }
    let dstElement = document.querySelector('#userLogin');
    dstElement.insertAdjacentElement('afterbegin', srcElement);
    srcElement.classList.remove("margin-top-20px");
}

function addEventListeners() {
    let openRegistrationButton = document.querySelector('input[name="openRegistration"][type="button"]');
    openRegistrationButton.addEventListener('click', (event) => { onOpenRegistrationButton(); });

    let goBackLink = document.querySelector('a[name="go-back"]');
    goBackLink.addEventListener('click', (event) => {
        document.querySelector('#userLogin').style.display = 'block';
        document.querySelector('#userRegistration').style.display = 'none';
        moveToLoginBlockWhenDebugModeEnable();
    });

    let loginButton = document.querySelector('input[name="login"][type="button"]');
    loginButton.addEventListener('click', () => { onClickActionButton('login'); });

    let replacementButton = document.querySelector('input[name="replacement"][type="button"]');
    replacementButton.addEventListener('click', () => { onClickActionButton('replacement'); });

    let agreeTosButton = document.querySelector('input[name="agree-tos"]');
    agreeTosButton.addEventListener('input', (event) => { switchEnableActionRegistrationButtons(); });
    let agreePpButton = document.querySelector('input[name="agree-pp"]');
    agreePpButton.addEventListener('input', (event) => { switchEnableActionRegistrationButtons(); });

    let registrationButton = document.querySelector('input[name="registration"][type="button"]');
    registrationButton.addEventListener('click', () => { onClickActionButton('registration'); });
}
