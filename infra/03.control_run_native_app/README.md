# アプリ/ユニバーサルリンク制御・ALB適用

ユーザーがウェブサイト上のリンクをクリックした際にアプリを起動するか、ウェブサイトをブラウザで開くかの制御をするマニフェストファイルになります

以下のファイルに対して、作成したAWSリソースの内容に修正してください

## マニフェストファイルの修正

* 各手順のコマンドはCloudShell上で実行する\
  (ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「cloudsell」と入力しサービスの「CloudShell」をクリック)
* 当リポジトリをCloudShellのホームディレクトにクローンする\
  ( `git clone git@github.com:c-3lab/mynumbercard-idp.git` 等を実行)

### configMap.yml

アプリ/ユニバーサルリンクを作成した場合に以下の箇所を修正\
本設定はkeycloakと同じEKS上で構築しているが、当ファイルが1MB超える場合は別途リソースの作成が必要

* 34 - 43行目(assetlinks.json から始まる行)
* 44 - 62行目(apple-app-site-association から始まる行)

### ingress.yml

最初に[過去に作成した手順](../README.md/#route53)を参考にアプリ/ユニバーサルリンクで使用する証明書を作成しておく

`infra/02.keycloak/ingress.yml` の設定で構築したALBへ差分適用として更新される

* #### 11行目

    > alb.ingress.kubernetes.io/certificate-arn: [arn:aws:acm:ap-northeast-1:123456789000:certificate/00000000-0000-0000-0000-000000000000],[arn:aws:acm:ap-northeast-1:123456789000:certificate/12345678-90ab-cdef-1234-567890abcdef]
  * keycloakで使うドメインの証明書とアプリ/ユニバーサルリンクで使うドメインの証明書
    1. ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「cm」と入力し、サービスの「Certificate Manager」をクリックする
    2. [AWSリソース作成時に作った証明書](../README.md/#証明書の発行)をクリックする
    3. 証明書のステータスから「ARN」の値をコピーする
    4. `[]`内に「ARN」をペーストする
    5. 今回の修正で作成した証明書も同様の手順で値を取得しペーストする

* #### 15行目

    > [{"field":"source-ip","sourceIpConfig":{"values":["Keycloak管理コンソールへのアクセスを許可するIPアドレス1", "IPアドレス2"]}}]
  * keycloak管理コンソールへのアクセスを許可するIPアドレス
    * 担当者のIPアドレスを入力する(現状のコードは複数許可の形式)

* #### 20行目

    > \- host: [native-app.mynumbercardidp.example.com]
  * アプリ/ユニバーサルリンクのドメイン名

* #### 30行目

    > \- host: [keycloak.mynumbercardidp.example.com]
  * keycloakのドメイン名

## ALBへ適用

  1. 以下コマンドを上から順に実行する
     > kubectl apply -f infra/03.control_run_native_app/configMap.yml \
     > kubectl apply -f infra/03.control_run_native_app/deployment.yml \
     > kubectl apply -f infra/03.control_run_native_app/service.yml \
     > kubectl apply -f infra/03.control_run_native_app/ingress.yml

## ドメイン作成

  1. ブラウザでAWS Management Consoleを開き、Certificate Managerのページに移動し、ALBの作成で使用した証明書のドメイン名を控える
  2. 「レコードを作成」ボタンをクリックする
  3. ルーティングポリシーから「シンプルルーティング」を選択する
  4. 「次へ」ボタンをクリックする
  5. 画面中央から「シンプルなレコードを定義」をクリックする
  6. シンプルなレコードを定義のダイアログが開いたら、「レコード名」に手順4で控えたドメイン名を入力する
  7. 「レコードタイプ」で「A - IPv4アドレスと～」を選択する
  8. 「トラフィックのルーティング先」で、上のセレクトボックスから「Application Load Balancer と Classic Load Balancer へのエイリアス」を選択する
  9. 真ん中のセレクトボックスから「アジアパシフィック(東京)」を選択する
  10. 下のセレクトボックスから、[3. ドメイン作成](../02.keycloak/README.md/#3-ドメイン作成)と同じALBを選択する
  11. 「シンプルなレコードを定義」ボタンをクリックする
  12. 「レコードを作成」ボタンをクリックする
  13. レコードが作成されたことを確認する
  14. ブラウザから以下リンク先のページが表示されるか確認する
   > https://[ドメイン名] \
   > https://[ドメイン名]/realms/OIdp/login-actions/authenticate

    // 表示例
    Android アプリのインストールが必要です

   > https://[ドメイン名]/.well-known/assetlinks.json \
   > https://[ドメイン名]/.well-known/apple-app-site-association

    // 表示例
    {"realm":"master","public_key":（……中略……）,"tokens-not-before":0}