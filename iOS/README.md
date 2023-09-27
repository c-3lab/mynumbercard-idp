
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
### iOSアプリのテストカバレッジについて
以下のファイルはユニットテスト(XCTest)ではなく結合テストで確認を行なっています。そのため、ユニットテストにおけるカバレッジは100%を下回ります。  
- `iOS/MyNumberCardAuth/MyNumberCardAuth/Views/`以下のファイル  
・UIに対する確認が必要なため  
- `iOS/MyNumberCardAuth/MyNumberCardAuth/Controllers/AuthenticationController.swift`
- `iOS/MyNumberCardAuth/MyNumberCardAuth/Models/HTTPSession.swift`  
- `iOS/MyNumberCardAuth/MyNumberCardAuth/Models/AuthenticationManager.swift`
- `iOS/MyNumberCardAuth/MyNumberCardAuth/MyNumberCardAuthApp.swift`
- `iOS/MyNumberCardAuth/MyNumberCardAuth/Protocols/URLSessionProtocol.swift`  
・テストコードでは動作しない処理が含まれているため

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
ローカルで動作確認をする場合、80番ポートについてはiOS端末からlocalhost環境にアクセスする方法としてngrokを使用することを想定しています。  
PCのファイアウォールによってはiPhoneとの通信をブロックする場合があるので、必要に応じてファイアウォールの設定を変更してください。  
また、WindowsのWSL内でDockerを立ち上げている場合は、ポートフォワーディングの設定が必要なため以下のコマンドを実行してください。

1. Windows、WSL内のLinux両方のIPアドレスを調べてメモしておきます。
1. Windowsのコマンドプロンプト、もしくはPowerShellを管理者として実行します。
1. 以下のコマンドを実行しサンプルRP（3000番ポート）への通信を転送するように設定します。  
`netsh.exe interface portproxy add v4tov4 listenaddress=WindowsのIPアドレス listenport=Windows側で受け付けるポート※ connectaddress=LinuxのIPアドレス connectport=3000`
1. 以下のコマンドを実行しKeycloak（8080番ポート）への通信を転送するように設定します。  
`netsh.exe interface portproxy add v4tov4 listenaddress=WindowsのIPアドレス listenport=Windows側で受け付けるポート※ connectaddress=LinuxのIPアドレス connectport=8080`
1. 上記の設定ができているか以下のコマンドで確認します。  
`netsh.exe interface portproxy show v4tov4`  

※ 「Windows側で受け付けるポート」は、他の通信で使われていないポートを指定してください。  
※ Windows、もしくはLinuxのIPアドレスが変わった場合は、以下コマンドで設定を削除し、上記の手順をやり直す必要があります。  
`netsh.exe interface portproxy delete v4tov4 listenport=Windows側で受け付けるポート listenaddress=WindowsのIPアドレス`


#### 前提条件
1. http://[DockerホストのIPアドレス]:8080/より、Keycloak管理コンソールが開ける状態で以降の手順を実施してください。
1. [ngrok公式](https://ngrok.com/)より、ダウンロードを行い、ngrok.exeを任意のフォルダに配置してください。
1. サインアップを行ってください。  
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
  nativeapp:	
    proto: http	
    addr: [DockerホストのIPアドレス]:80
```

・コマンドプロンプトを開き、ngrok.exeを配置したディレクトリに移動し、以下を実行します。  
`ngrok start --all`  または  
`ngrok start nativeapp` 

・以下のような実行結果が表示されます。  
```shell
Forwarding        https://XXXXXXXXXX.XXXXX.XXX -> http://XXX.XX.XX.XXX:80
```
ポート80の`https://XXXXXXXXXX.XXXXX.XXX` が、iOSがWebサービスからアプリを起動する時のホスト名となりますのでAssociated DomainsのDomainsに設定してください。  

・Keycloak管理コンソールを開き、以下の設定を行います。  
realm Oidp＞Configure＞realm-settings＞General>Frontend URL   
ポート8080の「netshコマンド」でポートフォワーディングするよう設定した「WindowsのIPアドレス：Windows側で受け付けるポート」を設定します。

realm Oidp＞Configure＞Authentication＞my number card>X509 Relay Authenticatorの右にあるSettings（歯車のアイコン）＞Run URI of iOS application  
ポート80の`https://XXXXXXXXXX.XXXXX.XXX`

・.envを設定します。  
../backend/.env   
を開き、RP1_BASEURLにポート3000、KEYCLOAK_URLにポート8080の「netshコマンド」でポートフォワーディングするよう設定した「WindowsのIPアドレス：Windows側で受け付けるポート」を設定します。

```shell
  RP1_BASEURL=https://XXXXXXXXXX.XXXXX.XXX
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
   ※Windows側で3000ポートのコンテナにポートフォワーディングするよう設定したIPアドレス：ポート番号で接続します。
1. 画面右上部にある `ログイン` リンクをタップします。
1. 画面にある `ログイン` ボタンをタップします。
1. 本iOSアプリが起動し、トップ画面が表示されます。
1. `暗証番号` ボックスへ、4桁の利用者証明用暗証番号を入力します。
1. `読み取り開始` ボタンがタップできるようになるので、ボタンをタップします。
1. NFC読み取りが起動するので、案内の通りiOSデバイスにマイナンバーカードをかざします。
1. 認証が成功すると、自動的にSafariで認証成功画面を開きます。
