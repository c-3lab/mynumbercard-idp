# MyNumberCard Authentication App For Android

Andoidでマイナンバーカードを読み取り、公的個人認証を行うアプリです。

## アプリ起動時のホスト名の変更

[app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml) のファイルを編集し、インテントフィルタの設定値を変更します。

`native-app.example.com` の部分をアプリの起動に使用したいホスト名に変更します。

```xml
        <activity
            android:name=".IntentFilterActivity"
            android:exported="true">
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:scheme="https"
                    android:host="native-app.example.com" />
            </intent-filter>
        </activity>

```

## 利用規約/プライバシーポリシー/個人情報保護方針の表示URLを設定
[MyNumberCardAuth/app/src/main/java/com/example/mynumbercardidp/data/Constants.kt]ファイルの以下を表示したいURLに変更する
・プライバシーポリシー
  PrivacyPolicy {override fun toString(): String { return "https://example.com/open-id/privacy-policy.html" }
・個人情報保護方針
  ProtectionPolicy {override fun toString(): String { return "https://example.com/open-id/personal-data-protection-policy.html" }
・利用規約
　TermsOfUse {override fun toString(): String { return "https://example.com/open-id/terms-of-use.html" }


## デフォルトアプリの設定

Android 端末で [設定] > [アプリ] > [My Number Auth] > [デフォルトで開く] > [+ リンクを追加] を選択します。

使用するリンクのチェックボックスにチェックを入れ [追加] を選択します。
