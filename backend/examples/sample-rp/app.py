import logging
import os

import requests
from authlib.integrations.flask_client import OAuth
from flask import Flask, redirect, render_template, session, url_for
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


@app.route("/assign", methods=["GET", "POST"])
def assign() -> Response:
    token = session.get("token")

    service_id = os.getenv("SERVICE_ID")
    note = os.getenv("NOTE")
    assign_api_url = (
        os.getenv("KEYCLOAK_URL")
        + "/realms/"
        + os.getenv("KEYCLOAK_REALM")
        + "/custom-attribute/assign"
    )

    headers = {
        "Content-type": "application/json",
        "Authorization": f"Bearer {token['access_token']}",
    }
    data = {
        "user_attributes": {
            "service_id": service_id,
            "notes": note,
        },
    }

    response = requests.post(assign_api_url, headers=headers, json=data)
    response.raise_for_status()

    # refresh token
    if token and token.get("refresh_token"):
        new_token = oauth.keycloak.fetch_access_token(
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


@app.route("/token")
def token() -> str:
    return render_template("token.html")


@app.route("/logout")
def logout() -> Response:
    session.pop("user", None)
    return redirect("/")


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)  # noqa: S104
