# MyNumberCard Authentication App For iOS

iOSでマイナンバーカードを読み取り、公的個人認証を行うサンプルアプリです。


## 構築環境
- macOS Ventura(バージョン13.3.1（22E261）)
- Xcode Version 14.2 (14C18)
- iOS バージョン 16.3.1 (20D67)
- Swift 5
- Apple Developer Program メンバーシップに加入済み（NFC機能・ユニバーサルリンクを使用するために必要）
### 動作確認済みiOSバージョンについて
iOS 16.4ではユニバーサルリンクが動作しないことを確認しました。  
本アプリで動作が確認できるバージョンは以下の通り。
- 16.2
- 16.3
- 16.5

## 構築手順
以下クローン後の手順になります。

### Xcodeプロジェクトを開く
クローン後は、Xcodeで「mynumbercard-idp/iOS/MyNumberCardAuth」を開く

### Package Dependencies
プロジェクトをクローン後、Package DependenciesからNFCリーダーライブラリを追加。  
    ライブラリを検索し「Add Package」   
    「mynumbercard-idp/iOS/TRETJapanNFCReader」を追加

プロジェクトTARGETS > General > Frameworks, Libraries, and Embedded Content  
「TRETJapanNFCReader-MIFARE-IndividualNumber」を追加


### Info.plist
Info.plistに以下を追加。

- NFCリーダーライブラリを使うために
    - ISO7816 application identifiers for NFC Tag Reader Session
        * Item 0: D392F000260100000001
        * Item 1: D3921000310001010408
        * Item 2: D3921000310001010100
        * Item 3: D3921000310001010401
    - Privacy - NFC Scan Usage Description  
        valueにNFC読み取りを行う旨の内容を入力してください。  
        例：マイナンバーカードを読み取ります。

Info.plistの以下を修正。    
    - ProtectionPolicyURL   
　　　　　個人情報保護方針の表示先URL   
    - TermsOfServiceURL   
　　　　　利用規約の表示先URL   
    - PrivacyPolicyURL   
　　　　　プライバシーポリシーの表示先URL   

### Signing & Capabilities
プロジェクトTARGETS > Signing & Capabilities  
に以下を追加。

- NFCリーダーライブラリを使うために
    - Near Field Communication Tag Reading

- Universal Linksを使うために  
    - Associated Domeins  
    Domains  
    applinks:example.com?mode=developer
    ※apple-app-site-associationの配置先ドメインと一致させること  
    ※ビルドするアプリのTeamID.Bundle Identifierをapple-app-site-associationのappleIdsに記載すること

## ngrokの設定(ローカルで動作確認をする場合)
ローカルで動作確認をする場合、iOS端末からlocalhost環境にアクセスする方法としてngrokを使用することを想定しています。　　
※無償版だとユニバーサルリンクが正常に動作しないため、有償版を使用してください。

#### 前提条件
1. http://[DockerホストのIPアドレス]:8080/より、Keycloak管理コンソールが開ける状態であること。
1. [ngrok公式](https://ngrok.com/)より、ダウンロードを行い、ngrok.exeを任意のフォルダに配置。
1. サインアップを行っていること。(アカウント登録無しでも実行できますが、セッション時間が制限される場合があります。) 
1. Authtokenを取得済みであること。([ngrok公式](https://ngrok.com/)よりログイン後、左側のメニューに「Your Authtoken」という項目があるのでクリックすると、Authtokenが表示されるのでコピーできます。)
1. コマンドプロンプトでngrok.exeを配置したディレクトリに移動し、`ngrok authtoken xxxxxxxxxxxxxxxxxxxxxxxxx`を実行。以下のファイルが作成されていること。
1. `上記コマンド実行後に表示されたディレクトリ/ngrok.yml`

#### 設定
・sample-rpを使用するため、使用可能なlocalhostのIPアドレスを取得します。  
（※ Dockerホストの外からのアクセスとコンテナ間のアクセス、両方で使用されるため、前提条件記載の[DockerホストのIPアドレス]以外のIPアドレスが必要です。）

・前提条件で作成したngrok.ymlに以下の設定を行います
```yml
version: "2"
authtoken: XXXXXXXXX

// 以下を追加してください
tunnels:
  samplerp:
    proto: http
    addr: [sample-rpのIPアドレス]:3000
  keycloak:	
    proto: http	
    addr: [sample-rpのIPアドレス]:8080	
  nativeapp:	
    proto: http	
    addr: [sample-rpのIPアドレス]:80
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
`https://XXXXXXXXXX.XXXXX.XXX` で、localhostのポート(上記だと80、3000、8080)上にあるサイトを表示できるようになります。  
ポート80の`https://XXXXXXXXXX.XXXXX.XXX` が、AndroidがWebサービスからアプリを起動する時のホスト名となりますのでAndroidManifest.xmlに設定してください。  

・Keycloak管理コンソールを開き、以下の設定を行います。  
realm Oidp＞realm-settings＞General>Frontend URL   
ポート8080の`https://XXXXXXXXXX.XXXXX.XXX`

・keycloak.jsonを設定します。  
[docker01のkeycloak.json](../backend/examples/sample-rp/docker01/keycloak.json)  
または  
[docker02のkeycloak.json](../backend/examples/sample-rp/docker02/keycloak.json)  
を開き、auth-server-urlにポート8080の`https://XXXXXXXXXX.XXXXX.XXX` を設定します。

```json
  "auth-server-url": "https://XXXXXXXXXX.XXXXX.XXX",
```

・assign_setting.jsonを設定します。  
[docker01のassign_setting.json](../backend/examples/sample-rp/docker01/assign_setting.json)  
または  
[docker02のassign_setting.json](../backend/examples/sample-rp/docker02/assign_setting.json)  
を開き、URLにポート8080の`https://XXXXXXXXXX.XXXXX.XXX` を設定します。

```json
  "URL": "https://XXXXXXXXXX.XXXXX.XXX",
```

※各ポートの`https://XXXXXXXXXX.XXXXX.XXX`はngrok startを行うごとに切り替わりますので、都度、AndroidManifest.xmlと、上記手順の設定値(keycloak.json、assign_setting.json、Keycloak管理コンソールのFrontend URL)を書き換えてください。

### 実機にアプリを転送
lighteningケーブルでiPhoneとMacを接続。  
実機iPhoneの設定から「デベロッパ」を開き、UNIVERSAL LINKSの「Associated Domeins Development」をオンにする。


### Universal Linkとapple-app-site-asociation(AASA)
https://developer.apple.com/documentation/xcode/supporting-associated-domains  
Universal Linkを実装する際のAASAファイルを作成し、対応ドメインサーバに配置する。  

```json:apple-app-site-asociation
{
  "applinks": {
      "details": [
           {
             "appIDs": ["XXXXXXXXXX.example.MyNumberCardAuth"],
             "components": [
               {
                  "/": "/realms/OIdp/login-actions/authenticate",
                  "comment": "login on iOS App."
               }
             ]
           }
       ]
   },
   "webcredentials": {
      "apps": [ "XXXXXXXXXX.example.MyNumberCardAuth" ]
   }
}
```  
- appIDs
  [チームID].[Bundle Identifier] を記載。
　　※実行時の環境にあわせて修正を行う
　　
- components
  アプリを起動させるパスを記載

## 動作確認
Webサービスからログイン処理を行い、認証成功画面を開くまでの動作確認手順です。

> note 
以下手順は、認証基盤としている「Keycloak 認証SPIとForm SPI」（Keycloak）の環境構築が事前に完了していることを前提としています。  
認証基盤については、c-3lab/indivcard-IdP-example/Keycloak を参照ください。  


1. SafariからWebサービスへ接続します。
1. 画面右上部にある `ログイン` リンクをタップします。
1. 画面中央にある `アプリを起動してログイン` ボタンをタップします。
1. 本iOSアプリが起動し、トップ画面が表示されます。
1. `読み取り画面へ` ボタンをタップし、マイナンバーカード認証画面へ遷移します。
1. `暗証番号` ボックスへ、4桁の利用者証明用暗証番号を入力します。
1. `読み取り開始` ボタンがタップできるようになるので、ボタンをタップします。
1. NFC読み取りが起動するので、案内の通りiOSデバイスにマイナンバーカードをかざします。
1. 認証が成功すると、自動的にSafariで認証成功画面を開きます。
