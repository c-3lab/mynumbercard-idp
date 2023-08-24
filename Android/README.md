# MyNumberCard Authentication App For Android

Androidでマイナンバーカードを読み取り、公的個人認証を行うアプリです。

## 構築環境
- Windows 11 Pro (バージョン22H2 ビルド22621.1992)
- [Android Studio](https://developer.android.com/studio) (Flamingo | 2022.2.1 Patch 2)
- Pixel 6a
- Android バージョン 13

## リポジトリクローン後の操作
以下、本リポジトリをクローン後の手順になります。  
1. AndroidStudioを開き、「Open」を選択します。  
(Welcome to Android Studio画面の場合は、画面上部の「Open」、すでにプロジェクトを開いている場合はFileタブ＞Open)
1. リポジトリ＞Android＞MynumberCardAuthを選択して「OK」を選択します。
1. プロジェクトウインドウ上部のプルダウンに「Android」が表示されていることを確認します。
1. 画面上部の`Run 'app'` が選択可能になっていれば完了です。

## アプリ起動時のホスト名の変更
[Android/MyNumberCardAuth/app/src/main/AndroidManifest.xml](./MyNumberCardAuth/app/src/main/AndroidManifest.xml)を編集し、インテントフィルタの設定値を変更します。

`native-app.example.com` の部分をアプリの起動に使用したいホスト名に変更します。  
(ローカル環境で動作確認する場合は、本READMEの「ngrokの設定(ローカルで動作確認をする場合)」で取得したホスト名を記載ください。)

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
以下ファイルの`=`以降を、表示したいURLに変更してください。  
[Android/MyNumberCardAuth/app/src/main/assets/external_urls.properties](./MyNumberCardAuth/app/src/main/assets/external_urls.properties)

```properties
　//プライバシーポリシー
  privacyPolicyUrl = https://example.com/open-id/privacy-policy.html

　//個人情報保護方針
  protectionPolicyUrl = https://example.com/open-id/personal-data-protection-policy.html

　//利用規約
  termsOfUseUrl = https://example.com/open-id/terms-of-use.html
```

## 起動手順
1. Android 端末と Android Studio を起動しているマシンを USB ケーブルで接続します。
1. 接続後、Android Studio 画面上部のデバイス選択ボックスに使用している Android 端末が表示されていることを確認します。
1. `Run 'app'` を選択します。

## Android端末のNFC設定の有効化
1. Android端末の設定アプリをタップします。
1. 検索欄に「NFC」と入力します。
1. 「NFCを使用」をONにします。

## デフォルトアプリの設定
1. Android 端末で [設定] > [アプリ] > [My Number Auth] > [デフォルトで開く] > [+ リンクを追加] を選択します。
1. 使用するリンクのチェックボックスにチェックを入れ [追加] を選択します。

## デジタルアセットリンクファイルの作成
Android アプリリンクを用いてアプリを起動できるようにするため、デジタルアセットリンクファイルを作成します。

1. Android Studio を起動し、画面上部の [Tools] > [App Links Assistant] を選択します。
1. ③ Associate website の項目にある [Open Digital Asset Links File Generator] を選択します。
1. [Generate Digital Asset Links file] を選択します。
1. [Save file] を選択し、ファイルを任意の場所に保存します。

以上でデジタルアセットリンクファイルの作成は完了です。

保存したファイル、 `assetlinks.json` を対応ドメインサーバーに配置します。  
ローカル環境で動作確認する場合は、[backendのREADME.md](../backend/README.md)を参照ください。

## ngrokの設定(ローカルで動作確認をする場合)
ローカルで動作確認をする場合、Android端末からlocalhost環境にアクセスする方法としてngrokを使用することを想定しています。

#### 前提条件
1. http://[DockerホストのIPアドレス]:8080/より、Keycloak管理コンソールが開ける状態で以降の手順を実施してください。
1. [ngrok公式](https://ngrok.com/)より、ダウンロードを行い、ngrok.exeを任意のフォルダに配置してください。
1. サインアップを行ってください。  
**※有料アカウントの登録が必要になります。**  
(Android アプリリンクを用いてアプリを起動しますが、  
無料アカウントの場合、Webサービスがassetlinks.jsonをダウンロードする際に確認画面が出てしまい、jsonがダウンロードできないため。)
1. Authtokenを取得してください。([ngrok公式](https://ngrok.com/)よりログイン後、左側のメニューに「Your Authtoken」という項目があるのでクリックすると、Authtokenが表示されるのでコピーできます。)
1. コマンドプロンプトでngrok.exeを配置したディレクトリに移動し、`ngrok authtoken xxxxxxxxxxxxxxxxxxxxxxxxx`を実行後、以下のファイルが作成されていることを確認してください。  
`上記コマンド実行後に表示されたディレクトリ/ngrok.yml`

#### 設定
・sample-rpを使用するため、使用可能なIPアドレスを取得します。  
（※ [DockerホストのIPアドレス]を指定してください。ただし、127.0.0.1は使用できません。）

・前提条件で作成したngrok.ymlに以下の設定を行います
```yml
version: "2"
authtoken: XXXXXXXXX

// 以下を追加してください
tunnels:
  samplerp:
    proto: http
    addr: [DockerホストのIPアドレス]:3000
  keycloak:
    proto: http
    addr: [DockerホストのIPアドレス]:8080
  nativeapp:
    proto: http
    addr: [DockerホストのIPアドレス]:80
```

・コマンドプロンプトを開き、ngrok.exeを配置したディレクトリに移動し、以下を実行します。  
`ngrok start --all`  または  
`ngrok start samplerp keycloak nativeapp`  (samplerp/keycloak/nativeapp以外にもポートを記載している場合は明示的に指定する必要があります。)  

・以下のような実行結果が表示されます。  
```shell
Forwarding        https://XXXXXXXXXX.XXXXX.XXX -> http://XXX.XX.XX.XXX:80
Forwarding        https://XXXXXXXXXX.XXXXX.XXX -> http://XXX.XX.XX.XXX:3000
Forwarding        https://XXXXXXXXXX.XXXXX.XXX -> http://XXX.XX.XX.XXX:8080
```
ポート80の`https://XXXXXXXXXX.XXXXX.XXX` が、AndroidがWebサービスからアプリを起動する時のホスト名となりますので[Android/MyNumberCardAuth/app/src/main/AndroidManifest.xml](./MyNumberCardAuth/app/src/main/AndroidManifest.xml)に設定してください。  

・Keycloak管理コンソールを開き、以下の設定を行います。  
realm Oidp＞Configure＞Realm settings＞General>Frontend URL   
ポート8080の`https://XXXXXXXXXX.XXXXX.XXX`

realm Oidp＞Configure＞Authentication＞my number card>X509 Relay Authenticatorの右にあるSettings（歯車のアイコン）＞Run URI of Android application  
ポート80の`https://XXXXXXXXXX.XXXXX.XXX`

・keycloak.jsonを設定します。  
[backend/examples/sample-rp/docker01/keycloak.json](../backend/examples/sample-rp/docker01/keycloak.json)  
または  
[backend/examples/sample-rp/docker02/keycloak.json](../backend/examples/sample-rp/docker02/keycloak.json)  
を開き、auth-server-urlにポート8080の`https://XXXXXXXXXX.XXXXX.XXX` を設定します。

```json
  "auth-server-url": "https://XXXXXXXXXX.XXXXX.XXX",
```

・assign_setting.jsonを設定します。  
[backend/examples/sample-rp/docker01/assign_setting.json](../backend/examples/sample-rp/docker01/assign_setting.json)  
または  
[backend/examples/sample-rp/docker02/assign_setting.json](../backend/examples/sample-rp/docker02/assign_setting.json)  
を開き、URLにポート8080の`https://XXXXXXXXXX.XXXXX.XXX` を設定します。

```json
  "URL": "https://XXXXXXXXXX.XXXXX.XXX/realms/OIdp/custom-attribute/assign",
```

※各ポートの`https://XXXXXXXXXX.XXXXX.XXX`はngrok startを行うごとに切り替わりますので、都度、[Android/MyNumberCardAuth/app/src/main/AndroidManifest.xml](./MyNumberCardAuth/app/src/main/AndroidManifest.xml)と、  
上記手順の設定値(keycloak.json、assign_setting.json、Keycloak管理コンソールのFrontend URL、Run URI of Android application)を書き換えてください。

## 動作確認
Webサービスからログイン処理を行い、認証成功画面を開くまでの動作確認手順です。

> note  
以下手順は、認証基盤としている[Keycloak 認証SPIとForm SPI](../backend/README.md)の環境構築が事前に完了していることを前提としています。  
認証基盤については、[backend] を参照ください。  
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
1. 認証が成功すると、自動的にブラウザで認証成功画面を開きます。

### ローカル環境で動作確認時の注意点
以下のエラーが発生する場合があります。  
`java.net.UnknownServiceException: CLEARTEXT communication to`  
※この事象は、http環境特有です。発生した場合は以下を実施してください。  

[Android/MyNumberCardAuth/app/src/main/AndroidManifest.xml](./MyNumberCardAuth/app/src/main/AndroidManifest.xml)を編集し、以下のように属性を追加します。  
```diff
    <application
+       android:usesCleartextTraffic="true"
        android:name=".KeycloakConnectionApplication"
```