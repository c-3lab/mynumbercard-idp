function getMobileOS() {
    var userAgent = navigator.userAgent;
    if (userAgent.match(/Android/))
        return 'Android';

    if (userAgent.match(/iPad|iPhone|iPod/))
        return 'iOS';

    // AndroidまたはiOSではない
    return 'other';
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
}

function addEventListeners() {
    let openRegistrationButton = document.querySelector('input[name="openRegistration"][type="button"]');
    openRegistrationButton.addEventListener('click', (event) => { onOpenRegistrationButton(); });

    let goBackLink = document.querySelector('a[name="go-back"]');
    goBackLink.addEventListener('click', (event) => {
        document.querySelector('#userLogin').style.display = 'block';
        document.querySelector('#userRegistration').style.display = 'none';
    });

    let loginButton = document.querySelector('input[name="login"][type="button"]');
    loginButton.addEventListener('click', () => { onClickActionButton('login'); });

    let agreeTosButton = document.querySelector('input[name="agree-tos"]');
    agreeTosButton.addEventListener('input', (event) => { switchEnableActionRegistrationButtons(); });
    let agreePpButton = document.querySelector('input[name="agree-pp"]');
    agreePpButton.addEventListener('input', (event) => { switchEnableActionRegistrationButtons(); });

    let registrationButton = document.querySelector('input[name="registration"][type="button"]');
    registrationButton.addEventListener('click', () => { onClickActionButton('registration'); });
}
