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
6. ファイル `keycloak/x509-relay-authenticator/src/main/resources/theme/login.ftl` 内の330、334、372~374行目のファイル参照先を以下のように変更します。  
   修正前： `https://nginx.example.com/open-id/ファイル名`  
   修正後： `https://[DockerホストのIPアドレス または DockerホストのDNS名]/open-id/ファイル名`  
7. ディレクトリ `backend` へ移動します。  
   ```
   cd ..
   ```
8. コンテナイメージをビルドします。  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml build
   ```
9. Dockerコンテナを起動し、コンテナのログを確認します。  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml up -d
   docker compose -f docker-compose.yml -f docker-compose-examples.yml logs -f
   ```
10. 以下のログが表示されるまで待機します。  
   `WARN [org.keycloak.quarkus.runtime.KeycloakMain] (main) Running the server in development mode. DO NOT use this configuration in production.`

   Dockerホストの外側から接続する場合、  
   ブラウザで `http://[DockerホストのIPアドレス または DockerホストのDNS名]:8080` へ接続します。  

   Dockerホスト自身やSSHポート転送で接続する場合、ブラウザで `http://127.0.0.1:8080` へ接続します。
11. `Administration Console` をクリックします。
12. `username` 、 `password` それぞれに `admin` と入力し、 `Sign In` をクリックします。
13. ナビゲーションエリア（画面左部の領域）にあるセレクトボックス `master` 、 `Create Realm` ボタンの順でクリックします。
14. `Realm name` に `OIdp` と入力し、 `Create` ボタンをクリックします。  
    画面が変わらない場合、 `Cancel` リンクをクリックし、ページを更新（再読み込み）します。  
    この事象は、アクセス元がlocalhost (127.0.0.1) 以外で暗号通信ではないhttp環境特有です。操作は実行されています。（以降の操作で、同じような事象が発生する場合があります。)
15. ナビゲーションエリアにある `Configure` セクションの `Authentication` をクリックします。
16. `Create flow` ボタンをクリックします。
17. `Name` に `my number card` と入力し、 `Create` ボタンをクリックします。
18. `Add execution` ボタンをクリックします。
19. `Browser Redirect for Cookie free authentication` 、 `Add` ボタンの順でクリックします。
20. `Add step` 、 `X509 Relay Authenticator` 、 `Add` ボタンの順でクリックします。
21. `X509 Relay Authenticator` の右にある `Settings` （歯車のアイコン）をクリックします。
22. 以下のように設定し、 `Save` ボタンをクリックします。
    - Alias: (任意の文字列。 例えば `x509-auth` など。)
    - Enable debug mode: On
    - Certificate Vaildator URI: http://dummy-platform
    - Run URI of Android application: (Android アプリ リンクのURL または 入力しない)
    - Run URI of iOS application: (iOS ユニバーサルリンクのURL または 入力しない)
    - Installation URI of Android/iOS application: (アプリインストール案内ページのURL または 入力しない)
23. ナビゲーションエリアにある `Configure` セクションの `Realm settings` をクリックします。
24. `Login` タブをクリックし、 `User registration` を `On` へ変更します。
25. `Themes` タブをクリックし、`Login theme` を `call-native-app` へ変更、 `Save` ボタンをクリックします。
26. ナビゲーションエリアにある `Manager` セクションの `Clients` をクリックします。
27. `Create client` をクリック、以下のように設定し、 `Next` ボタンをクリックします。
    - Client type: OpenID Connect
    - Client ID: sample-client
28. `Next` ボタンをクリックします。
29. 以下のように設定し、`Save` ボタンをクリックします。
    - Root URL: (入力しない)
    - Home URL: (入力しない)
    - Valid redirect URIs: *
    - Valid post logout redirect URIs: *
    - Web origins: *
30. `Login settings` セクションの `Consent required` を `On` に変更し、 `Save` ボタンをクリックします。
31. `Client scopes` タブをクリックします。
32. `sample-client-dedicated` リンク、 `Configure a new mapper` の順でクリックします。
33. `Audience` をクリックし、 以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: Audience
    - Included Client Audience: (入力しない)
    - Included Custom Audience: (入力しない)
    - Add to ID token: On
    - Add to access token: On
34. 画面上部にある `Dedicated scopes` リンクをクリックし、 `Add mapper` 、 `By configuration` の順でクリックします。
35. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: Unique ID
    - User Attribute: uniqueId
    - Token Claim Name: unique_id
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: Off
    - Multivalued: Off
    - Aggregate attribute values: Off
36. 画面上部にある `Dedicated scopes` リンクをクリックし、 `Add mapper` 、 `By configuration` の順でクリックします。
37. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: User Attributes
    - User Attribute: sample-client_user_attributes
    - Token Claim Name: user_attributes
    - Claim JSON Type: JSON
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
38. 画面上部にある `Client details` リンクをクリックし、 `address` の `Assigned type` 列の値を `Optional` から `Default` へ変更します。
39. 画面上部にある `Advanced` タブをクリックします。
40. `Authentication flow overrides` セクションの `Browser Flow` を `my number card` へ変更し、 `Save` ボタンをクリックします。
41. ナビゲーションエリアにある `Manage` セクションの `Client scopes` をクリックし、`Profile`リンクをクリックします。
42. 画面上部にある `Client scope details` をクリックし、`Add mapper` 、 `By configuration` の順でクリックします。
43. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: user address
    - User Attribute: userAddress
    - Token Claim Name: user_address
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
44. 画面上部にある `Client scope details` をクリックし、`Add mapper` 、 `By configuration` の順でクリックします。
45. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: name
    - User Attribute: name
    - Token Claim Name: name
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
46. 画面上部にある `Client scope details` をクリックし、`Add mapper` 、 `By configuration` の順でクリックします。
47. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: gender code
    - User Attribute: genderCode
    - Token Claim Name: gender_code
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
48. 画面上部にある `Client scope details` をクリックし、`Add mapper` 、 `By configuration` の順でクリックします。
49. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。
    - Name: birth date
    - User Attribute: birthDate
    - Token Claim Name: birth_date
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
50. ナビゲーションエリアにある `Configure` セクションの `Realm settings` をクリックします。
51. `Localization` タブをクリックし、 `Internationalization` を `Disabled` から `Enabled` へ変更します。
52. `Supported locales` の `Select locales` 文字列部分をクリックし、 `日本語` をクリックします。
53. 画面内の余白をクリックし、言語のセレクトボックスを閉じます。
54. `Default locale` を `English` から `日本語` へ変更し、 `Save` をクリックします。

## 動作確認（マイナンバーカード用）
1. ブラウザでWebサービスへ接続します。  
   Dockerホストの外側から接続する場合、  
   ブラウザで `http://[DockerホストのIPアドレス または DockerホストのDNS名]:3000` へ接続します。  

   Dockerホスト自身やSSHポート転送で接続する場合、ブラウザで `http://127.0.0.1:3000` へ接続します。
2. 画面右上部にある `ログイン` リンクをクリックします。
3. `利用者登録` ボタンをクリックします。
4. `利用規約` と `プライバシーポリシー` の同意チェックボックスを押します。
5. 下の `利用者登録へ進む` ボタンをクリックします。
6. エラーや警告が表示されないことを確認します。
7. 画面右上部にある `ログアウト` リンクをクリックします。
8. 画面右上部にある `ログイン` リンクをクリックします。
8. 下の `ログイン` ボタンをクリックします。
10. エラーや警告が表示されないことを確認します。
11. 画面右上部にある `ログアウト` リンクをクリックします。
12. ユーザー情報変更を試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し基本4情報を修正し `Save` ボタンをクリックします。
13. Webサービスの画面右上部にある `ログイン` リンクをクリックします。
14. `登録情報の変更` ボタンをクリックします。
15. エラーや警告が表示されないことを確認します。
16. ユーザー情報変更の結果を確認したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し修正した基本4情報が初期状態に戻されたことを確認してください。
17. ユーザー登録をもう一度試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を削除してください。

## 動作確認（デバッグ用）
1. ブラウザでWebサービスへ接続します。  
   Dockerホストの外側から接続する場合、  
   ブラウザで `http://[DockerホストのIPアドレス または DockerホストのDNS名]:3000` へ接続します。  

   Dockerホスト自身やSSHポート転送で接続する場合、ブラウザで `http://127.0.0.1:3000` へ接続します。
2. 画面右上部にある `ログイン` リンクをクリックします。
3. `利用者登録` ボタンをクリックします。
4. `利用規約` と `プライバシーポリシー` の同意チェックボックスを押します。
5. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。  
   サンプル用の秘密鍵は、このリポジトリの `/backend/keycloak/private.pem` ファイルです。
6. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。  
   サンプル用の公開鍵は、このリポジトリの `/backend/keycloak/public.pem` ファイルです。
7. 上の `利用者登録へ進む` ボタンをクリックします。
8. エラーや警告が表示されないことを確認します。
9. 画面右上部にある `ログアウト` リンクをクリックします。
10. 画面右上部にある `ログイン` リンクをクリックします。
11. `Mode change` セレクトボックスで `login` を選択します。
12. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。
13. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。
14. 上の `ログイン` ボタンをクリックします。
15. エラーや警告が表示されないことを確認します。
16. 画面右上部にある `ログアウト` リンクをクリックします。
17. ユーザー情報変更を試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し基本4情報を修正し `Save` ボタンをクリックします。
18. Webサービスの画面右上部にある `ログイン` リンクをクリックします。
19. `Mode change` セレクトボックスで `replacement` を選択します。
20. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。
21. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。
22. 上の `ログイン` ボタンをクリックします。
23. エラーや警告が表示されないことを確認します。
24. ユーザー情報変更の結果を確認したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し修正した基本4情報が初期状態に戻されたことを確認してください。
25. ユーザー登録をもう一度試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を削除してください。

## Docker コンテナの停止
1. Docker ホストにて、このリポジトリをダウンロードし、配置したディレクトリへ移動します。  
2. ディレクトリ `backend` へ移動します。  
   ```
   cd backend
   ```
3. Docker コンテナを停止します。  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml down
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
