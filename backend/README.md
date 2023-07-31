# Keycloak 認証SPIとForm SPI
このSPIは、実証実験を目的としてマイナンバーカードと認証基盤を連携させる拡張機能です。  
このリポジトリには、Keycloak SPI、サンプル Web サービス、およびダミー プラットフォームのソース コードを含んでいます。

## はじめに
このリポジトリの実行には、Docker エンジンとDocker Composeが必要です。

### 動作確認済みの環境
- サーバー 
  - OS: Ubuntu 20.04.6 LTS
  - Docker エンジン:  23.0.4 (Community)
  - Docker Compose: 2.17.2
- クライアント
  - OS: Windows 10 build 19044.2846
  - ブラウザ: Google Chrome (version 112.0.5615.138)

## インストール
1. このリポジトリをダウンロードします。すでに実行している場合は次へ進んでください。  
   ```
   git clone https://github.com/c-3lab/mynumbercard-idp
   ```
2. ディレクトリ `keycloak` へ移動します。  
   ```
   cd backend/keycloak
   ```
3. Keycloakのデータディレクトリを作成します。  
   ```
   mkdir data
   ```
4. Keycloakのデータディレクトリの権限を777 (rwxrwxrwx) へ変更します。  
   ```
   chmod 777 data
   ```
5. ファイル `examples/sample-web-service/docker/keycloak.json` 内の `auth-server-url` を以下のように変更します。   
   Dockerホストの外側から接続する場合、  
   `http://[DockerホストのIPアドレス または DockerホストのDNS名]:8080/` へ変更します。  
   接続元の端末から見た、DockerホストのIPアドレス または DNS名を期待しています。  
   例：  
     - `http://192.0.2.100:8080/`  
     - `http://docker-server:8080/`  

   Dockerホスト自身やSSHポート転送で接続する場合、 `http://127.0.0.1:8080/` へ変更します。
6. ディレクトリ `backend` へ移動します。  
   ```
   cd ..
   ```
7. コンテナイメージをビルドします。  
   ```
   docker compose build
   ```
8. Dockerコンテナを起動し、コンテナのログを確認します。  
   ```
   docker compose up -d
   docker compose logs -f
   ```
9. 以下のログが表示されるまで待機します。  
   `WARN [org.keycloak.quarkus.runtime.KeycloakMain] (main) Running the server in development mode. DO NOT use this configuration in production.`

   Dockerホストの外側から接続する場合、  
   ブラウザで `http://[DockerホストのIPアドレス または DockerホストのDNS名]:8080` へ接続します。  

   Dockerホスト自身やSSHポート転送で接続する場合、ブラウザで `http://127.0.0.1:8080` へ接続します。
10. `Administration Console` をクリックします。
11. `username` 、 `password` それぞれに `admin` と入力し、 `Sign In` をクリックします。
12. ナビゲーションエリア（画面左部の領域）にあるセレクトボックス `master` 、 `Create Realm` ボタンの順でクリックします。
13. `Realm name` に `OIdp` と入力し、 `Create` ボタンをクリックします。  
    画面が変わらない場合、 `Cancel` リンクをクリックし、ページを更新（再読み込み）します。  
    この事象は、アクセス元がlocalhost (127.0.0.1) 以外で暗号通信ではないhttp環境特有です。操作は実行されています。（以降の操作で、同じような事象が発生する場合があります。)
14. ナビゲーションエリアにある `Configure` セクションの `Authentication` をクリックします。
15. `Create flow` ボタンをクリックします。
16. `Name` に `my number card` と入力し、 `Create` ボタンをクリックします。
17. `Add execution` ボタンをクリックします。
18. `Browser Redirect for Cookie free authentication` 、 `Add` ボタンの順でクリックします。
19. `Add step` 、 `X509 Relay Authenticator` 、 `Add` ボタンの順でクリックします。
20. `X509 Relay Authenticator` の右にある `Settings` （歯車のアイコン）をクリックします。
21. 以下のように設定し、 `Save` ボタンをクリックします。
    - Alias: (任意の文字列。 例えば `x509-auth` など。)
    - Enable debug mode: On
    - Certificate Vaildator URI: http://dummy-platform/verify
    - Run URI of Android application: (Android アプリ リンクのURL または 入力しない)
    - Run URI of iOS application: (iOS ユニバーサルリンクのURL または 入力しない)
    - Installation URI of Android/iOS application: (アプリインストール案内ページのURL または 入力しない)
22. 画面上部またはナビゲーションエリアにある `Authentication` リンクをクリックします。
23. `Create flow` ボタンをクリックします。
24. `Name` に `registration based on my number card` と入力し、 `Create` ボタンをクリックします。
25. `Add sub-flow` をクリックします。
26. `Name` に `User registration` と入力、`Flow type` は `Form` を選択し、 `Add` ボタンをクリックします。
27. `User registration` の右にある `Requirement` 列の値を `Disabled` から `Required` へ変更します。
28. `User registration` の右にある `Add` （十字アイコン）、 `Add step` の順にクリックします。
29. `User creation by server response` 、 `Add` ボタンの順でクリックします。
30. `User creation by server response` の右にある `Requirement` 列の値を `Disabled` から `Required` へ変更します。
31. 以下のように設定し、 `Save` ボタンをクリックします。
    - Alias: (任意の文字列。 例えば `x509-auth` など。)
    - Enable debug mode: On
    - Certificate Vaildator URI: http://dummy-platform/verify
    - Run URI of Android application: (Android アプリ リンクのURL または 入力しない)
    - Run URI of iOS application: (iOS ユニバーサルリンクのURL または 入力しない)
    - Installation URI of Android/iOS application: (アプリインストール案内ページのURL または 入力しない)
32. 画面右上部にある `Action` 、`Bind flow` の順でクリックします。  
    `Choose binding type` を `Browser flow` から `Registration flow` へ変更し、 `Save` ボタンをクリックします。
33. ナビゲーションエリアにある `Configure` セクションの `Realm settings` をクリックします。
34. `Login` タブをクリックし、 `User registration` を `On` へ変更します。
35. `Themes` タブをクリックし、`Login theme` を `call-native-app` へ変更、 `Save` ボタンをクリックします。
36. ナビゲーションエリアにある `Manager` セクションの `Clients` をクリックします。
37. `Create client` をクリック、以下のように設定し、 `Next` ボタンをクリックします。
    - Client type: OpenID Connect
    - Client ID: sample-client
38. `Next` ボタンをクリックします。
39. 以下のように設定し、`Save` ボタンをクリックします。
    - Root URL: (入力しない)
    - Home URL: (入力しない)
    - Valid redirect URIs: *
    - Valid post logout redirect URIs: *
    - Web origins: *
40. `Login settings` セクションの `Consent required` を `On` に変更し、 `Save` ボタンをクリックします。
41. `Client scopes` タブをクリックします。
42. `sample-client-dedicated` リンク、 `Configure a new mapper` の順でクリックします。
43. `Audience` をクリックし、 以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: Audience
    - Included Client Audience: (入力しない)
    - Included Custom Audience: (入力しない)
    - Add to ID token: On
    - Add to access token: On
44. 画面上部にある `Dedicated scopes` リンクをクリックし、 `Add mapper` 、 `By configuration` の順でクリックします。
45. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: Unique ID
    - User Attribute: uniqueId
    - Token Claim Name: unique_id
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: Off
    - Multivalued: Off
    - Aggregate attribute values: Off
46. 画面上部にある `Client details` リンクをクリックし、 `address` の `Assigned type` 列の値を `Optional` から `Default` へ変更します。
47. 画面上部にある `Advanced` タブをクリックします。
48. `Authentication flow overrides` セクションの `Browser Flow` を `my number card` へ変更し、 `Save` ボタンをクリックします。
49. ナビゲーションエリアにある `Configure` セクションの `Realm settings` をクリックします。
50. `Localization` タブをクリックし、 `Internationalization` を `Disabled` から `Enabled` へ変更します。
51. `Supported locales` の `Select locales` 文字列部分をクリックし、 `日本語` をクリックします。
52. 画面内の余白をクリックし、言語のセレクトボックスを閉じます。
53. `Default locale` を `English` から `日本語` へ変更し、 `Save` をクリックします。

## 動作確認
1. ブラウザでWebサービスへ接続します。  
   Dockerホストの外側から接続する場合、  
   ブラウザで `http://[DockerホストのIPアドレス または DockerホストのDNS名]:3000` へ接続します。  

   Dockerホスト自身やSSHポート転送で接続する場合、ブラウザで `http://127.0.0.1:3000` へ接続します。
2. 画面右上部にある `ログイン` リンクをクリックします。
3. `登録` リンクをクリックします。
4. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。  
   サンプル用の秘密鍵は、このリポジトリの `/backend/keycloak/private.pem` ファイルです。
5. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。  
   サンプル用の公開鍵は、このリポジトリの `/backend/keycloak/public.pem` ファイルです。
6. `登録` ボタン、`はい` の順でクリックします。
7. エラーや警告が表示されないことを確認します。
8. 画面右上部にある `ログアウト` リンクをクリックします。
9. 画面右上部にある `ログイン` リンクをクリックします。
10. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。
11. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。
12. `ログイン` ボタンをクリックします。
13. エラーや警告が表示されないことを確認します。
14. 画面右上部にある `ログアウト` リンクをクリックします。
15. ユーザー登録をもう一度試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `User` からユーザー `1182b536-d882-4046-baaa-7c35d9f44d59` を削除してください。

## Docker コンテナの停止
1. Docker ホストにて、このリポジトリをダウンロードし、配置したディレクトリへ移動します。  
2. ディレクトリ `backend` へ移動します。  
   ```
   cd backend
   ```
3. Docker コンテナを停止します。  
   ```
   docker compose down
   ```

## Keycloak データの初期化（root権限が必要）
1. Docker ホストにて、このリポジトリをダウンロードし、配置したディレクトリへ移動します。  
2. ディレクトリ `backend/keycloak` へ移動します。  
   ```
   cd backend/keycloak
   ```
3. root権限にて、Keycloak のデータディレクトリを削除します。  
   OSユーザーとコンテナ内のユーザーでユーザーIDが異なる場合があるため、root権限での操作が必要です。  
   ```
   sudo rm -rf data
   ```
4. Keycloakのデータディレクトリを作成します。  
   ```
   mkdir data
   ```
5. Keycloakのデータディレクトリの権限を777 (rwxrwxrwx) へ変更します。  
   ```
   chmod 777 data
   ```
