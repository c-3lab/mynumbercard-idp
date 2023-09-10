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
5. ディレクトリ `backend` へ移動します。  
   ```
   cd ..
   ```
6. ファイル `.env`内の各値を以下のように変更します。  
  **KEYCLOAK_URL:**  
   Dockerホストの外側から接続する場合、  
   `http://[DockerホストのIPアドレス または DockerホストのDNS名]:8080/` へ変更します。  
   接続元の端末から見た、DockerホストのIPアドレス または DNS名を期待しています。  
   例：  
     - `http://192.0.2.100:8080/`  
     - `http://docker-server:8080/`  

   （※ Dockerホストの外からのアクセスとコンテナ間のアクセス、両方で使用されるため、127.0.0.1は使用できません）  

   **RP1_BASEURL, RP2_BASEURL:**  
   RP1_BASEURLはKEYCLOAK_URLと同じDockerホストのIPアドレス または DNS名を期待しています。こちらはPORT番号を3000とし、RP2_BASEURLはPORT番号3001を設定します。  
7. コンテナイメージをビルドします。  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml build
   ```
8. Dockerコンテナを起動し、コンテナのログを確認します。  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml up -d
   docker compose -f docker-compose.yml -f docker-compose-examples.yml logs -f
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
    この事象は、アクセス元がlocalhost (127.0.0.1) 以外で暗号通信ではないhttp環境特有です。  
    操作は実行されています。（以降の操作で、同じような事象が発生する場合があります。）  
14. ナビゲーションエリアにある `Configure` セクションの `Authentication` をクリックします。  
15. `Create flow` ボタンをクリックします。  
16. `Name` に `my number card` と入力し、 `Create` ボタンをクリックします。  
17. `Add execution` ボタンをクリックします。  
18. `Browser Redirect for Cookie free authentication` 、 `Add` ボタンの順でクリックします。  
19. `Add step` 、 `My number card Authenticator` 、 `Add` ボタンの順でクリックします。  
20. `My number card Authenticator` の右にある `Settings` （歯車のアイコン）をクリックします。  
21. 以下のように設定し、 `Save` ボタンをクリックします。  
    - Alias: (任意の文字列。 例えば `my number card auth` など。)
    - Enable debug mode: On
    - Certificate Validator URI: http://platform-gateway
    - Run URI of Android application: (Android アプリ リンクのURL または 入力しない)
    - Run URI of iOS application: (iOS ユニバーサルリンクのURL または 入力しない)
    - Installation URI of Android/iOS application: (アプリインストール案内ページのURL または 入力しない)
    - Platform API Client Class FQDN: com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient
    - Platform API IDP sender: ID123
    - Terms of use page dir URL: https://idp.example.com/keycloak-html/
    - Privacy policy page dir URL: https://idp.example.com/keycloak-html/
    - Personal data protection policy dir URL: https://idp.example.com/keycloak-html/
22. ナビゲーションエリアにある `Configure` セクションの `Realm settings` をクリックします。  
23. `Login` タブをクリックし、 `User registration` を `On` へ変更し `Login with email` を `Off` へ変更します。  
24. `Themes` タブをクリックし、`Login theme` を `mynumbercard-auth` へ変更、 `Save` ボタンをクリックします。  
25. ナビゲーションエリアにある `Manage` セクションの `Clients` をクリックします。  
26. `Create client` をクリック、以下のように設定し、 `Next` ボタンをクリックします。  
    - Client type: OpenID Connect
    - Client ID: sample-client01
27. `Next` ボタンをクリックします。  
28. 以下のように設定し、`Save` ボタンをクリックします。  
    - Root URL: (入力しない)
    - Home URL: (入力しない)
    - Valid redirect URIs: *
    - Valid post logout redirect URIs: *
    - Web origins: *
29. `Login settings` セクションの `Consent required` を `On` に変更し、 `Save` ボタンをクリックします。  
30. `Client scopes` タブをクリックします。  
31. `sample-client01-dedicated` リンク、 `Configure a new mapper` の順でクリックします。  
32. `Audience` をクリックし、 以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: Audience
    - Included Client Audience: (入力しない)
    - Included Custom Audience: (入力しない)
    - Add to ID token: On
    - Add to access token: On
33. 画面上部にある `Dedicated scopes` リンクをクリックし、 `Add mapper` 、 `By configuration` の順でクリックします。  
34. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: Unique ID
    - User Attribute: uniqueId
    - Token Claim Name: unique_id
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: Off
    - Multivalued: Off
    - Aggregate attribute values: Off
35. 画面上部にある `Dedicated scopes` リンクをクリックし、 `Add mapper` 、 `By configuration` の順でクリックします。  
36. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: User Attributes
    - User Attribute: sample-client01_user_attributes
    - Token Claim Name: user_attributes
    - Claim JSON Type: JSON
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
37. 画面上部にある `Client details` リンクをクリックし、 `address` の `Assigned type` 列の値を `Optional` から `Default` へ変更します。  
38. 画面上部にある `Advanced` タブをクリックします。  
39. `Authentication flow overrides` セクションの `Browser Flow` を `my number card` へ変更し、 `Save` ボタンをクリックします。  
40. ナビゲーションエリアにある `Manage` セクションの `Clients` をクリックします。  
41. `Clients list`タブの`sample-client01`をクリックします。  
42. `Settings`タブのCapability configにある`Client Authentication`をOnに変更し、Saveをクリックします。  
43. `Credntials`タブが表示されるのでクリックし、`Client secret`の値をメモします。  
44. ナビゲーションエリアにある `Manage` セクションの `Clients` をクリックします。  
45. `Create client` をクリック、以下のように設定し、 `Next` ボタンをクリックします。  
    - Client type: OpenID Connect
    - Client ID: sample-client02
46. `Next` ボタンをクリックします。  
47. 以下のように設定し、`Save` ボタンをクリックします。  
    - Root URL: (入力しない)
    - Home URL: (入力しない)
    - Valid redirect URIs: *
    - Valid post logout redirect URIs: *
    - Web origins: *
48. `Login settings` セクションの `Consent required` を `On` に変更し、 `Save` ボタンをクリックします。  
49. `Client scopes` タブをクリックします。  
50. `sample-client02-dedicated` リンク、 `Configure a new mapper` の順でクリックします。  
51. `Audience` をクリックし、 以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: Audience
    - Included Client Audience: (入力しない)
    - Included Custom Audience: (入力しない)
    - Add to ID token: On
    - Add to access token: On
52. 画面上部にある `Dedicated scopes` リンクをクリックし、 `Add mapper` 、 `By configuration` の順でクリックします。  
53. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: Unique ID
    - User Attribute: uniqueId
    - Token Claim Name: unique_id
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: Off
    - Multivalued: Off
    - Aggregate attribute values: Off
54. 画面上部にある `Dedicated scopes` リンクをクリックし、 `Add mapper` 、 `By configuration` の順でクリックします。  
55. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: User Attributes
    - User Attribute: sample-client02_user_attributes
    - Token Claim Name: user_attributes
    - Claim JSON Type: JSON
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
56. 画面上部にある `Client details` リンクをクリックし、 `address` の `Assigned type` 列の値を `Optional` から `Default` へ変更します。  
57. 画面上部にある `Advanced` タブをクリックします。  
58. `Authentication flow overrides` セクションの `Browser Flow` を `my number card` へ変更し、 `Save` ボタンをクリックします。  
59. ナビゲーションエリアにある `Manage` セクションの `Clients` をクリックします。  
60. `Clients list`タブの`sample-client02`をクリックします。  
61. `Settings`タブのCapability configにある`Client Authentication`をOnに変更し、Saveをクリックします。  
62. `Credntials`タブが表示されるのでクリックし、`Client secret`の値をメモします。  
63. ナビゲーションエリアにある `Manage` セクションの `Client scopes` をクリックし、`profile`リンクをクリックします。  
64. 画面上部にある `Mappers` タブをクリックし、`Add mapper` 、 `By configuration` の順でクリックします。  
65. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: user address
    - User Attribute: user_address
    - Token Claim Name: user_address
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
66. 画面上部にある `Client scope details` をクリックし、`Add mapper` 、 `By configuration` の順でクリックします。  
67. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: name
    - User Attribute: name
    - Token Claim Name: name
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
68. 画面上部にある `Client scope details` をクリックし、`Add mapper` 、 `By configuration` の順でクリックします。  
69. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: gender code
    - User Attribute: gender_code
    - Token Claim Name: gender_code
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
70. 画面上部にある `Client scope details` をクリックし、`Add mapper` 、 `By configuration` の順でクリックします。  
71. `User Attribute` をクリックし、以下のように設定します。設定後、 `Save` ボタンをクリックします。  
    - Name: birth date
    - User Attribute: birth_date
    - Token Claim Name: birth_date
    - Claim JSON Type: String
    - Add to ID token: On
    - Add to access token: On
    - Add to userinfo: On
    - Multivalued: Off
    - Aggregate attribute values: Off
72. ナビゲーションエリアにある `Configure` セクションの `Realm settings` をクリックします。  
73. `Localization` タブをクリックし、 `Internationalization` を `Disabled` から `Enabled` へ変更します。  
74. `Supported locales` の `Select locales` 文字列部分をクリックし、 `日本語` をクリックします。  
75. 画面内の余白をクリックし、言語のセレクトボックスを閉じます。  
76. `Default locale` を `English` から `日本語` へ変更し、 `Save` をクリックします。  
77. 手順6で設定したファイル `.env`内の値を以下のように変更します。  
   **RP1_CLIENT_SECRET, RP2_CLIENT_SECRET:**  
   RP1_CLIENT_SECRETに手順44でメモした値、RP2_CLIENT_SECRETに手順63でメモした値を設定します。  
78. Dockerコンテナを停止します  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml down
   ```
79. コンテナイメージをビルドします。  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml build
   ```
80. Dockerコンテナを起動し、コンテナのログを確認します。  
   ```
   docker compose -f docker-compose.yml -f docker-compose-examples.yml up -d
   docker compose -f docker-compose.yml -f docker-compose-examples.yml logs -f
   ```
## 動作確認（マイナンバーカード用）
> note  
ローカル環境で実施する場合、以下を実施してください。  
・デジタルアセットリンクファイル及びapple-app-site-asociationファイルを以下のディレクトリへ配置します。  
　配置先ディレクトリ： `(リポジトリの配置ディレクトリ)/backend/native-app-settings/nginx/html/.well-known`

1. ブラウザでWebサービスへ接続します。  
   Dockerホストの外側から接続する場合、  
   ブラウザで `http://[DockerホストのIPアドレス または DockerホストのDNS名]:3000` へ接続します。  

   Dockerホスト自身やSSHポート転送で接続する場合、ブラウザで `http://127.0.0.1:3000` へ接続します。  
2. 画面右上部にある `ログイン` リンクをクリックします。  
3. `利用者登録` ボタンをクリックします。  
4. `利用規約` と `プライバシーポリシー` の同意チェックボックスを押します。  
5. `利用者登録へ進む` ボタンをクリックします。  
6. アプリが起動し、トップ画面が表示されます。  
7. `パスワード` ボックスへ、6～16桁の署名用暗証番号を入力します。  
8. `読み取り開始` ボタンをタップします。  
9. デバイスにマイナンバーカードをかざします。  
10. `OK` ボタンをタップします。  
11. 表示される `これらのアクセス権限を付与しますか？` 画面で `はい` ボタンをクリックします。  
12. エラーや警告が表示されないことを確認します。  
13. 画面右上部にある `ログアウト` リンクをクリックします。  
14. 画面右上部にある `ログイン` リンクをクリックします。  
15. `ログイン` ボタンをクリックします。  
16. アプリが起動し、トップ画面が表示されます。  
17. `パスワード` ボックスへ、4桁の利用者証明用パスワードを入力します。  
18. `読み取り開始` ボタンをタップします。  
19. デバイスにマイナンバーカードをかざします。
20. `OK` ボタンをタップします。  
21. エラーや警告が表示されないことを確認します。  
22. 画面右上部にある `ログアウト` リンクをクリックします。  
23. ユーザー情報変更を試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `Manage` - `Users` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し `Attributes` タブで以下の基本4情報を修正し `Save` ボタンをクリックします。  
    - user_address
    - birth_date
    - name
    - gender_code
24. Webサービスの画面右上部にある `ログイン` リンクをクリックします。  
25. `登録情報の変更` ボタンをクリックします。  
26. アプリが起動し、トップ画面が表示されます。  
27. `パスワード` ボックスへ、6～16桁の署名用暗証番号を入力します。  
28. `読み取り開始` ボタンをタップします。  
29. デバイスにマイナンバーカードをかざします。  
30. `OK` ボタンをタップします。  
31. エラーや警告が表示されないことを確認します。  
32. ユーザー情報変更の結果を確認したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `Manage` - `Users` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し修正した基本4情報が初期状態に戻されたことを確認してください。  
33. ユーザー登録をもう一度試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `Manage` - `Users` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を削除してください。  

## 動作確認（デバッグ用）
1. ブラウザでWebサービスへ接続します。  
   Dockerホストの外側から接続する場合、  
   ブラウザで `http://[DockerホストのIPアドレス または DockerホストのDNS名]:3000` へ接続します。  

   Dockerホスト自身やSSHポート転送で接続する場合、ブラウザで `http://127.0.0.1:3000` へ接続します。  
2. 画面右上部にある `ログイン` リンクをクリックします。  
3. `利用者登録` ボタンをクリックします。  
4. `利用規約` と `プライバシーポリシー` の同意チェックボックスを押します。  
5. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。  
   サンプル用の秘密鍵は、  
   このリポジトリの `/backend/keycloak/private.pem` ファイルです。  
6. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。  
   サンプル用の公開鍵は、このリポジトリの `/backend/keycloak/public.pem` ファイルです。  
7. `利用者登録へ進む` ボタンをクリックします。  
8. 表示される `これらのアクセス権限を付与しますか？` 画面で `はい` ボタンをクリックします。  
9. エラーや警告が表示されないことを確認します。  
10. 画面右上部にある `ログアウト` リンクをクリックします。  
11. 画面右上部にある `ログイン` リンクをクリックします。  
12. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。  
13. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。  
14. `ログイン` ボタンをクリックします。  
15. エラーや警告が表示されないことを確認します。  
16. 画面右上部にある `ログアウト` リンクをクリックします。  
17. ユーザー情報変更を試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `Manage` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し `Attributes` タブで以下の基本4情報を修正し `Save` ボタンをクリックします。  
    - user_address
    - birth_date
    - name
    - gender_code
18. Webサービスの画面右上部にある `ログイン` リンクをクリックします。  
19. `X509 privkey file` の `ファイル選択` をクリックし、次のステップで選択する公開鍵（証明書）に対応する秘密鍵を選択します。  
20. `X509 Certificate File` の `ファイル選択` をクリックし、前のステップで選択した秘密鍵に対応する公開鍵（証明書）を選択します。  
21. `ログイン` ボタンをクリックします。  
22. エラーや警告が表示されないことを確認します。  
23. ユーザー情報変更の結果を確認したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `Manage` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を選択し修正した基本4情報が初期状態に戻されたことを確認してください。  
24. ユーザー登録をもう一度試したい場合は、Keycloakの `Administration Console` 、 `Realm OIdp` - `Manage` - `User` からユーザー `7910ae5f-a6c1-4117-b890-fc2df2db63f1` を削除してください。  

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
