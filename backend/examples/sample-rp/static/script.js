// toastクラスがついている要素にBootStrapのトーストを適用する
const toastElList = [].slice.call(document.querySelectorAll(".toast"));
const toastList = toastElList.map(function (toastEl) {
    return new bootstrap.Toast(toastEl, {
        // オプション
        delay: 10000,
    });
});
// ボタンをクリックしたときに実行される関数
function showToast() {
    // トーストを表示する
    toastList[0].show();
}