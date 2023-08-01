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
## デフォルトアプリの設定

Android 端末で [設定] > [アプリ] > [My Number Auth] > [デフォルトで開く] > [+ リンクを追加] を選択します。

使用するリンクのチェックボックスにチェックを入れ [追加] を選択します。
