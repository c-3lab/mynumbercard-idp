# mynumbercard-idp

マイナンバーカードを利用したIdP認証を実現するOSSです。
本OSSはスマートフォンからマイナンバーカードの読み取りを行いKeycloakの拡張機能によってマイナンバー認証を実現します。

# 各種ディレクトリの詳細

* [Android](./Android)
  * マイナンバーの読み取りを行うAndroidアプリケーション
* [iOS](./iOS)
  * マイナンバーの読み取りを行うiOSアプリケーション
* [backend](./backend)
  * 本OSSを実現するためのバックエンドモジュール
* [infra](./infra)
  * AWSにデプロイを行うためのコード
  * ※ ローカルで実行(docker compose)を想定した手順は[backendのREADME.md](./backend)に記載


# License
[Apache License 2.0](LICENSE)
