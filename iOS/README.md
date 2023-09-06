
iOSでマイナンバーカードを読み取り、公的個人認証を行うアプリです。


## 構築環境
- macOS Ventura(バージョン13.3.1（22E261）)
- Xcode Version 14.2 (14C18)
- iOS バージョン 16.3.1 (20D67)
- Swift 5
- Apple Developer Program メンバーシップに加入済み（NFC機能・ユニバーサルリンクを使用するために必要）
### 動作確認済みiOSバージョンについて
iOS 16.4ではユニバーサルリンクが動作しないことを確認しました。  
本アプリで動作が確認できるバージョンは以下の通りです。
- 16.2
- 16.3
- 16.5

## 構築手順
以下クローン後の手順になります。

### Xcodeプロジェクトを開く
クローン後は、Xcodeで「mynumbercard-idp/iOS/MyNumberCardAuth」を開きます。

### Info.plist
Info.plistの以下を修正します。    
    - ProtectionPolicyURL   
　　　　　個人情報保護方針の表示先URL(https://example.com/open-id/personal-data-protection-policy.html)  
    - TermsOfServiceURL   
　　　　　利用規約の表示先URL(https://example.com/open-id/terms-of-use.html)  
    - PrivacyPolicyURL   
　　　　　プライバシーポリシーの表示先URL(https://example.com/open-id/privacy-policy.html)   

### Package Dependencies
XcodeのメニューからFile > Add Packagesを選択します。  
表示されたダイアログの右上検索欄に`https://github.com/treastrain/TRETJapanNFCReader.git`を入力し実行します。   

TRETJapanNFCReaderが検索されるので、Dependency Ruleに`Branch`の`master`を指定しAdd Packageを実行します。  
※動作確認時のリビジョンは`03470a515c0b06d762de39e6d80f9a07ed502cae`になります。  

Xcode左側のProject navigatorにPackage DependenciesのTRETJapanNFCReaderが表示されますので、  
Sources > MIFARE > IndovidualNumberを選択し、右クリックメニューの`Show in Finder`でフォルダを表示します。  
フォルダに下記に記載するファイルを.swpを削除するリネームを行なって配置します。  

`iOS/MyNumberCardAuth/MyNumberCardAuth/IndividualNumberCardExtensions`
```
IndividualNumberCardData.swift.swp
IndividualNumberReaderExtension.swift.swp
IndividualNumberReaderReadFunctionsExtensions.swift.swp
```

配置を行なった後、Project navigatorのTRETJapanNFCReaderを選択し、右クリックメニューの
`Update Pakage`でビルドを実行します。

その後、以下の設定を行います。  
プロジェクトTARGETS > General > Frameworks, Libraries, and Embedded Contentに
「TRETJapanNFCReader-MIFARE-IndividualNumber」を追加します。

### Signing & Capabilities
プロジェクトTARGETS > Signing & Capabilitiesに以下を追加します。

Universal Links   
- 以下の`example`をapple-app-site-associationのappIDsと同じものに変更します。   
    * Signing   
     Bundle Identifier   
     `com.example.MyNumberCardAuth`
- 以下の`example.com`をapple-app-site-associationの配置先ドメインと同じものに変更します。   
    * Associated Domains  
    Domains  
    `applinks:example.com?mode=developer`  

## ngrokの設定(ローカルで動作確認をする場合)
ローカルで動作確認をする場合、iOS端末からlocalhost環境にアクセスする方法としてngrokを使用することを想定しています。　　

#### 前提条件
1. http://[DockerホストのIPアドレス]:8080/より、Keycloak管理コンソールが開ける状態で以降の手順を実施してください。
1. [ngrok公式](https://ngrok.com/)より、ダウンロードを行い、ngrok.exeを任意のフォルダに配置してください。
1. サインアップを行ってください。  
**※有料アカウントの登録が必要になります。**  
(ユニバーサルリンクを用いてアプリを起動しますが、無料アカウントの場合、Webサービスがapple-app-site-associationをダウンロードする際に確認画面が出てしまい、jsonがダウンロードできないため。)  
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
ポート80の`https://XXXXXXXXXX.XXXXX.XXX` が、iOSがWebサービスからアプリを起動する時のホスト名となりますのでAssociated DomainsのDomainsに設定してください。  

・Keycloak管理コンソールを開き、以下の設定を行います。  
realm Oidp＞Configure＞realm-settings＞General>Frontend URL   
ポート8080の`https://XXXXXXXXXX.XXXXX.XXX`

realm Oidp＞Configure＞Authentication＞my number card>X509 Relay Authenticatorの右にあるSettings（歯車のアイコン）＞Run URI of iOS application  
ポート80の`https://XXXXXXXXXX.XXXXX.XXX`

・.envを設定します。  
../backend/.env   
を開き、RP1_BASEURLにポート3000、RP2_BASEURLにポート3001、KEYCLOAK_URLにポート8080の`https://XXXXXXXXXX.XXXXX.XXX` を設定します。

```shell
  RP1_BASEURL=https://XXXXXXXXXX.XXXXX.XXX
  RP2_BASEURL=https://XXXXXXXXXX.XXXXX.XXX
  KEYCLOAK_URL=https://XXXXXXXXXX.XXXXX.XXX
```

※各ポートの`https://XXXXXXXXXX.XXXXX.XXX`はngrok startを行うごとに切り替わりますので、都度、Associated DomainsのDomainsと、上記手順の設定値(.env、Keycloak管理コンソールのFrontend URL、Run URI of iOS application)を書き換えてください。

### 実機にアプリを転送
lighteningケーブルでiPhoneとMacを接続します。  
実機iPhoneの設定から「デベロッパ」を開き、UNIVERSAL LINKSの「Associated Domains Development」をオンにします。


### Universal Linkとapple-app-site-asociation(AASA)
https://developer.apple.com/documentation/xcode/supporting-associated-domains  
Universal Linkを実装する際のAASAファイルを作成し、対応ドメインサーバに配置します。  
ローカル環境で動作確認する場合は、backendのREADME.mdを参照ください。

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
  [チームID].[Bundle Identifier] を記載します。（※実行時の環境にあわせて修正を行います）
　　
- components
  アプリを起動させるパスを記載します

## 動作確認
Webサービスからログイン処理を行い、認証成功画面を開くまでの動作確認手順です。

> note 
以下手順は、認証基盤としている「Keycloak 認証SPIとForm SPI」（Keycloak）の環境構築が事前に完了していることを前提としています。  
認証基盤については、mynumbercard-idp/backend/README.md を参照ください。  


1. SafariからWebサービスへ接続します。
1. 画面右上部にある `ログイン` リンクをタップします。
1. 画面にある `ログイン` ボタンをタップします。
1. 本iOSアプリが起動し、トップ画面が表示されます。
1. `暗証番号` ボックスへ、4桁の利用者証明用暗証番号を入力します。
1. `読み取り開始` ボタンがタップできるようになるので、ボタンをタップします。
1. NFC読み取りが起動するので、案内の通りiOSデバイスにマイナンバーカードをかざします。
1. 認証が成功すると、自動的にSafariで認証成功画面を開きます。
