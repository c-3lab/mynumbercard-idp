window.addEventListener('DOMContentLoaded', (event) => {
    let pageCloseButton = document.querySelector('input[name="page-close"][type="button"]');
    pageCloseButton.addEventListener('click', (event) => {
        window.close();
        event.preventDefault();
    });
});
