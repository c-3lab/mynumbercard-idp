# MyNumberCard Authentication App For Android

Androidでマイナンバーカードを読み取り、公的個人認証を行うアプリです。

## 構築環境
- Windows 11 Pro (バージョン22H2 ビルド22621.1992)
- [Android Studio](https://developer.android.com/studio) (Flamingo | 2022.2.1 Patch 2)
- Pixel 6a
- Android バージョン 13

## アプリ起動時のホスト名の変更
[app/src/main/AndroidManifest.xml](Android/MyNumberCardAuth/app/src/main/AndroidManifest.xml) のファイルを編集し、インテントフィルタの設定値を変更します。

`native-app.example.com` の部分をアプリの起動に使用したいホスト名に変更します。

```xml
        <activity
            android:name=".IntentFilterActivity"
            android:exported="true">
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:scheme="https"
                    android:host="native-app.example.com" />
            </intent-filter>
        </activity>

```

## 利用規約/プライバシーポリシー/個人情報保護方針の表示URLを設定
[Constants.kt](Android/MyNumberCardAuth/app/src/main/java/com/example/mynumbercardidp/data/Constants.kt)ファイルの以下を表示したいURLに変更してください。

・プライバシーポリシー

  PrivacyPolicy {override fun toString(): String { return "https://example.com/open-id/privacy-policy.html" }
  
・個人情報保護方針

  ProtectionPolicy {override fun toString(): String { return "https://example.com/open-id/personal-data-protection-policy.html" }
  
・利用規約

　TermsOfUse {override fun toString(): String { return "https://example.com/open-id/terms-of-use.html" }

## 起動手順

Android 端末と Android Studio を起動しているマシンを USB ケーブルで接続します。

接続後、Android Studio 画面上部のデバイス選択ボックスに使用している Android 端末が表示されていることを確認します。

`Run 'app'` を選択します。

## デフォルトアプリの設定

Android 端末で [設定] > [アプリ] > [My Number Auth] > [デフォルトで開く] > [+ リンクを追加] を選択します。

使用するリンクのチェックボックスにチェックを入れ [追加] を選択します。

## デジタルアセットリンクファイルの作成

Android アプリリンクを用いてアプリを起動できるようにするため、デジタルアセットリンクファイルを作成します。

Android Studio を起動し、画面上部の [Tools] > [App Links Assistant] を選択します。

③ Associate website の項目にある [Open Digital Asset Links File Generator] を選択します。

[Generate Digital Asset Links file] を選択します。

[Save file] を選択し、ファイルを任意の場所に保存します。

以上でデジタルアセットリンクファイルの作成は完了です。

保存したファイル、 `assetlinks.json` を対応ドメインサーバーに配置します。手順についてはKeycloak の README.mdを参照ください。

## ngrokの設定(ローカルで動作確認する場合)

ローカルで動作確認する場合、ngrokを使用することを想定しています。


## 動作確認

Webサービスからログイン処理を行い、認証成功画面を開くまでの動作確認手順です。

> note 
以下手順は、認証基盤としている「Keycloak 認証SPIとForm SPI」（Keycloak）の環境構築が事前に完了していることを前提としています。  
認証基盤については、 を参照ください。  
本Androidアプリやブラウザのキャッシュにより、ログイン画面に遷移しない場合があります。  
動作確認前に本Androidアプリやブラウザのキャッシュを削除してください。  

1. ブラウザからWebサービスへ接続します。
1. 画面右上部にある `ログイン` リンクをタップします。
1. 画面中央にある `ログイン` ボタンor`利用者登録` ボタンor`登録情報の変更` ボタンをタップします。
1. 本Androidアプリが起動し、トップ画面が表示されます。
1.  `ログイン` ボタンをタップした場合、`パスワード` ボックスへ、4桁の利用者証明用パスワードを入力します。
`利用者登録` ボタンor`登録情報の変更` ボタンをタップした場合、`パスワード` ボックスへ、6～16桁の署名用暗証番号を入力します。
1. `読み取り開始` ボタンをタップします。
1. Androidデバイスにマイナンバーカードをかざします。
1. `OK` ボタンをタップします。
1. 認証が成功すると、自動的にブラウザで認証成功画面を開きます。