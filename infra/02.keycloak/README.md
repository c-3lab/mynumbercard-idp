# ALBの構築・ドメイン作成

* 各手順のコマンドはCloudShell上で実行する\
  (ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「cloudsell」と入力しサービスの「CloudShell」をクリック)
* 当リポジトリをCloudShellのホームディレクトにクローンする\
  ( `git clone git@github.com:c-3lab/mynumbercard-idp.git` 等を実行)

## 1. マニフェストファイルの修正

### ingress.yml

#### 11行目

> alb.ingress.kubernetes.io/certificate-arn: [arn:aws:acm:ap-northeast-1:123456789000:certificate/00000000-0000-0000-0000-000000000000]

* keycloakで使うドメインの証明書のARN
    1. ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「cm」と入力し、サービスの「Certificate Manager」をクリックする
    2. [AWSリソース作成時に作った証明書](../README.md/#証明書の発行) をクリックする
    3. 証明書のステータスから「ARN」の値をコピーして該当箇所にペーストする

#### 15行目

> [{"field":"source-ip","sourceIpConfig":{"values":["Keycloak管理コンソールへのアクセスを許可するIPアドレス1", "IPアドレス2"]}}]

* keycloak管理コンソールへのアクセスを許可するIPアドレス
  * 担当者のIPアドレスを入力する(現状のコードは複数許可の形式)

### keycloak.yml

#### 7行目

> image: [dkr.mynumbercardidp.example.com/keycloak:latest]

* プッシュしたECRのURI
    1. ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「ecr」と入力し機能の「リポジトリ」をクリックする
    2. AWSリソースの作成でプッシュしたリポジトリの行から、「URI」の値をコピーして該当箇所にペーストする。

#### 10行目

> host: [keycloak.postgresql.mynumbercardidp.example.com]

* 作成したRDSのエンドポイント
    1. ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「rds」と入力しサービスの「RDS」をクリックする
    2. 左のナビゲーターから「データベース」をクリックする
    3. 作成したRDSの「DB識別子」のリンクをクリックする
    4. 「接続とセキュリティ」のタブから「エンドポイント名」の値をコピーして該当箇所にペーストする

#### 22行目

> hostname: [keycloak.mynumbercardidp.example.com]

* 作成したドメイン名
    1. ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「route53」と入力しサービスの「Route 53」をクリックする
    2. 左のナビゲーターから「ホストゾーン」をクリックする
    3. 作成したホストゾーンからドメイン名を確認し、該当箇所に入力する

## 2. ALBの構築

#### CoreDNSの設定

1. 下記リンクの手順(`CoreDNS の更新`)を実行する
   <https://docs.aws.amazon.com/ja_jp/eks/latest/userguide/fargate-getting-started.html#fargate-gs-coredns>
2. 下記コマンドを実行する
   > kubectl rollout restart -n kube-system deployment coredns
3. 約数分後、下記コマンドを実行し、「coredns」の行が以下のように表示されているかを確認する
   > kubectl get deployments -n kube-system

      ```
      NAME                           READY   UP-TO-DATE   AVAILABLE   AGE
      coredns                        2/2     2            2           20d
      ```

#### ALBの設定

* IAM OIDC プロバイダーの作成(下記リンクの `AWS Management Console` タブの手順を実行) \
  <https://docs.aws.amazon.com/ja_jp/eks/latest/userguide/enable-iam-roles-for-service-accounts.html>

* Helmのインストール(参考リンク: <https://docs.aws.amazon.com/ja_jp/eks/latest/userguide/helm.html>)
  1. 下記コマンドを実行

   > curl <https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3> > get_helm.sh \
   > chmod 700 get_helm.sh \
   > ./get_helm.sh

  2. 「アクション▲」-「AWS CloudShell の再起動」の順でクリックする
  3. 再起動後、下記コマンドを実行

   > which helm \
   // 「~/.local/bin/helm」と表示された場合以降のコマンドはスキップして手順4へ進む \
   \
   sudo chown CloudShell-user:CloudShell-user ~/.local/helm \
   mv helm ~/.local/bin

  4. 下記コマンドを実行する。

   > // v3.9.0 のように表示されることを確認 \
    helm version --short | cut -d + -f 1

* AWS Load Balancer Controller アドオンのインストール \
  (下記リンクで手順2は`AWS CLI and kubectl`、手順3,4では`Helm`タブの手順で実行)
  <https://docs.aws.amazon.com/ja_jp/eks/latest/userguide/aws-load-balancer-controller.html>

* 各パブリックサブネットにALBを設定

  1. ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「サブネット」と入力し、機能でVPCの機能の「サブネット」をクリックする
  2. 画面上部の検索欄に「(作成したVPCのリソース名)-subnet-public」で検索する \
    (本手順でVPCを作成した場合、3件が結果として表示される)
  3. 検索結果から1つ適当なサブネットをクリックする
  4. 「タグ」のタブをクリックし、「タグを管理」をクリックする
  5. キーに「kubernetes.io/cluster/[作成したクラスター名]」、値に「shared」を設定する
  6. キーに「kubernetes.io/role/elb」、値に「1」を設定し、保存ボタンをクリックする
  7. 他のパブリックサブネットにも手順5,6を実施する

#### ALBの適用

  1. CloudShell上で以下のコマンドを実行する
       > kubectl apply -f ~/infra/02.ingress.yml
       >
       > // 実行後に以下の出力がされることを確認 \
       > ingress.networking.k8s.io/web-service created
  2. 以下のコマンドを実行し、以下のような出力がされることを確認する([次手順](#3-ドメイン作成)でADDRESSの値を使うため控えておく)
       > kubectl get ingress

            NAME          CLASS    HOSTS　　　　 ADDRESS
            web-service   <none>  （……中略……）   k8s-default-webservi-0000000000-000000000.ap-northeast-1.elb.amazonaws.com

## 3. ドメイン作成

  1. ブラウザでAWS Management Consoleを開き、画面上部の検索欄に「alb」と入力し、機能の機能の「ロードバランサ―」をクリックする
  2. 検索欄にALBの適用で控えたADDRESSの値を入力し検索する
  3. 結果で表示されているロードバランサーの「DNS名」を控える
  4. Certificate Managerのページに移動し、ALBの作成で使用した証明書のドメイン名を控える
  5. Route53のホストゾーンのページに移動し、作成したホストゾーンをクリックする
  6. 「レコードを作成」ボタンをクリックする
  7. ルーティングポリシーから「シンプルルーティング」を選択する
  8. 「次へ」ボタンをクリックする
  9. 画面中央から「シンプルなレコードを定義」をクリックする
  10. シンプルなレコードを定義のダイアログが開いたら、「レコード名」に手順4で控えたドメイン名を入力する
  11. 「レコードタイプ」で「A - IPv4アドレスと～」を選択する
  12. 「トラフィックのルーティング先」で、上のセレクトボックスから「Application Load Balancer と Classic Load Balancer へのエイリアス」を選択する
  13. 真ん中のセレクトボックスから「アジアパシフィック(東京)」を選択する
  14. 下のセレクトボックスから、「手順2で使用した控えたADDRESSの値」と同じALBを選択する
  15. 「シンプルなレコードを定義」ボタンをクリックする
  16. 「レコードを作成」ボタンをクリックする
  17. レコードが作成されたことを確認する
  18. ブラウザからドメインを入力してkeycloakのページが開けるかを確認する
