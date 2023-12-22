from flask import Flask, render_template, redirect, url_for, session, request
from authlib.integrations.flask_client import OAuth
from urllib.parse import quote
import os
import requests
from werkzeug import Response

# config
app: Flask = Flask(__name__)
app.secret_key = "your_random_secret_key_here"


app.config.update(
    OIDC_ID_TOKEN_COOKIE_SECURE=False,
    OIDC_USER_INFO_ENABLED=True,
    SERVICE_ID=os.getenv("SERVICE_ID"),
    NOTE=os.getenv("NOTE"),
)


oauth: OAuth = OAuth(app)
oauth.register(
    name="keycloak",
    client_id=os.getenv("KEYCLOAK_CLIENT_ID"),
    client_secret=os.getenv("KEYCLOAK_CLIENT_SECRET"),
    authorize_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/auth',
    authorize_params=None,
    server_metadata_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/.well-known/openid-configuration',
    access_token_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/token',
    access_token_params=None,
    api_base_url=f'{os.getenv("BASE_URL")}',
    client_kwargs={
        "scope": "openid",
    },
)


@app.route("/")
def index() -> str:
    user: object = session.get("user")
    return render_template("index.html", user=user)


@app.route("/login")
def login() -> str:
    return render_template("login.html")


@app.route("/connect")
def connect() -> str:
    return render_template("connect.html")


@app.route("/connected")
def connected() -> str:
    return render_template("connected.html")


@app.route("/Keycloak-login")
def login_keycloak() -> str:
    redirect_uri: str = url_for("auth", _external=True)
    return oauth.keycloak.authorize_redirect(redirect_uri)


@app.route("/auth")
def auth() -> Response:
    token: OAuth = oauth.keycloak.authorize_access_token()
    userinfo: OAuth = token["userinfo"]
    if userinfo:
        session["user"] = userinfo
        session["token"] = token

    return redirect(url_for("index"))


@app.route("/account")
def account() -> str:
    user = session.get("user")
    return render_template("account.html", user=user)


@app.route("/token")
def token() -> str:
    token = session.get("token")
    user = session.get("user")
    return render_template("token.html", token=token, user=user)


@app.route("/logout")
def logout() -> Response:
    session.pop("user", None)
    return redirect("/")


@app.route("/refresh")
def refresh():
    token = session.get("token")
    if token and "refresh_token" in token:
        # 新しいトークンを取得
        new_token = oauth.keycloak.fetch_access_token(
            refresh_token=token["refresh_token"]
        )
        session["token"] = new_token
        return redirect(url_for("index"))
    else:  # noqa: RET505
        return redirect(url_for("login"))


@app.route("/replace", methods=["GET", "POST"])
def replace() -> str:
    token = session.get("token")

    replaceAPIURL = (
        f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/userinfo-replacement/login'
        f'?redirect_uri={quote(os.getenv("BASE_URL") + "/refresh")}&scope=openid&response_type=code'
    )

    headers = {
        "Content-type": "application/x-www-form-urlencoded",
        "Authorization": f"Bearer {token['access_token']}",
        "User-Agent": request.headers.get("User-Agent"),
    }

    # ユーザー情報の更新を行うためにPOSTリクエストを送信
    response = requests.post(
        replaceAPIURL, headers=headers, data={}, allow_redirects=False
    )

    if "Location" in response.headers:
        return redirect(response.headers["Location"])
    else:  # noqa: RET505
        return "Userinfo replacement completed."


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)  # noqa: S104
