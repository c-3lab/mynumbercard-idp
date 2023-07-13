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
クローン後は、Xcodeで「NativeApp/iOS/MyNumberCardAuth」を開く

### Package Dependencies
プロジェクトをクローン後、Package DependenciesからNFCリーダーライブラリを追加。  
    ライブラリを検索し「Add Package」   
    「NativeApp/iOS/TRETJapanNFCReader」を追加

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

### Signing & Capabilities
プロジェクトTARGETS > Signing & Capabilities  
に以下を追加。

- NFCリーダーライブラリを使うために
    - Near Field Communication Tag Reading

- Universal Linksを使うために  
    - Associated Domeins  
    Domains  
    applinks:native-app.abelsoft.in-private.dev?mode=developer

    ※apple-app-site-associationの配置先ドメインと一致させること  
    ※ビルドするアプリのTeamID.Bundle Identifierをapple-app-site-associationのappleIdsに記載すること


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
             "appIDs": ["8WFPCA36MV.com.abelsoft.MyNumberCardAuth","8WFPCA36MV.com.abelsoft.TestApp"],
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
      "apps": [ "8WFPCA36MV.com.abelsoft.MyNumberCardAuth" ]
   }
}
```  
- appIDs
  [チームID].[Bundle Identifier] を記載。
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
