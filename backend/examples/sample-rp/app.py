from flask import Flask, abort, render_template, redirect, url_for, session, request
from authlib.integrations.flask_client import OAuth
from urllib.parse import quote
import os
import requests
from werkzeug import Response

# config
app: Flask = Flask(__name__)
app.secret_key = os.getenv("APP_SECRET_KEY")


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
    server_metadata_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/.well-known/openid-configuration',
    access_token_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/token',
    userinfo_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/userinfo',
    authorize_params=None,
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


@app.route("/assign", methods=["POST"])
def assign() -> Response:
    token: dict[str, str] | None = session.get("token")

    if token is None:
        abort(400, "Token not available")

    service_id: str = os.getenv("SERVICE_ID", "")
    note: str = os.getenv("NOTE", "")
    assign_api_url: str = (
        os.getenv("KEYCLOAK_URL", "")
        + "/realms/"
        + os.getenv("KEYCLOAK_REALM", "")
        + "/custom-attribute/assign"
    )

    headers: dict[str, str] = {
        "Content-type": "application/json",
        "Authorization": f"Bearer {token['access_token']}" if token else "",
    }

    data: dict[str, dict[str, str]] = {
        "user_attributes": {
            "service_id": service_id,
            "notes": note,
        },
    }

    requests.post(
        assign_api_url,
        headers=headers,
        json=data,
        timeout=(3.0, 7.5),
    )

    # refresh token
    if token and token.get("refresh_token"):
        new_token: OAuth = oauth.keycloak.fetch_access_token(
            refresh_token=token["refresh_token"],
            grant_type="refresh_token",
        )
        session["token"] = new_token

    return redirect("/connected")


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
def refresh() -> str:
    token = session.get("token", {})
    if token and token.get("refresh_token"):
        new_token: OAuth = oauth.keycloak.fetch_access_token(
            refresh_token=token["refresh_token"],
            grant_type="refresh_token",
        )
        session["token"] = new_token
    return redirect(url_for("index"))


@app.route("/replace", methods=["POST"])
def replace() -> Response:
    token: dict[str, str] | None = session.get("token")

    if token is None:
        abort(400, "Token not available")

    replaceAPIURL = (
        f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/userinfo-replacement/login'
        f'?redirect_uri={quote(os.getenv("BASE_URL") + "/refresh")}&scope=openid&response_type=code'
    )

    headers = {
        "Content-type": "application/x-www-form-urlencoded",
        "Authorization": f"Bearer {token['access_token']}",
        "User-Agent": request.headers.get("User-Agent"),
    }

    response = requests.post(
        replaceAPIURL,
        headers=headers,
        data={},
        allow_redirects=False,
    )

    userinfo_response = requests.get(
        f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/userinfo',
        headers={"Authorization": f"Bearer {token['access_token']}"},
    )

    if userinfo_response.status_code == 200:
        userinfo = userinfo_response.json()
        merged_userinfo = {**session.get("user", {}), **userinfo}
        session["user"] = merged_userinfo
        session["token"] = token

    return redirect(response.headers["Location"])


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)
