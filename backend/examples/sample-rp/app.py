from flask import Flask, render_template, redirect, url_for, session,request
from authlib.integrations.flask_client import OAuth
import os
import logging
from logging.config import dictConfig


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
    authorize_params=None,
    server_metadata_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/.well-known/openid-configuration',
    access_token_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/token',
    access_token_params=None,
    api_base_url=f'{os.getenv("BASE_URL")}',
    client_kwargs={
        "scope": "openid",
    },
)

dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s in %(module)s: %(message)s',
    }},
    'handlers': {'wsgi': {
        'class': 'logging.StreamHandler',
        'stream': 'ext://flask.logging.wsgi_errors_stream',
        'formatter': 'default'
    }},
    'root': {
        'level': 'INFO',
        'handlers': ['wsgi']
    }
)

#config
app: Flask = Flask(__name__)
app.secret_key = 'your_random_secret_key_here'
logging.basicConfig(level=logging.DEBUG)
app.config.update(    
    OIDC_ID_TOKEN_COOKIE_SECURE=False,
    OIDC_USER_INFO_ENABLED=True,
    SERVICE_ID=os.getenv("SERVICE_ID"),
    NOTE=os.getenv("NOTE"),
)

oauth: OAuth = OAuth(app)
oauth.register(
    name='keycloak',
    client_id=os.getenv("KEYCLOAK_CLIENT_ID"),
    client_secret=os.getenv("KEYCLOAK_CLIENT_SECRET"),
    server_metadata_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}',
    authorize_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/auth',
    client_kwargs={
        "scope": "openid",
    },
)

print(oauth)


def getUser(request):
    # RP側に個人情報を格納する変数を定義
    idTokenContent = {}
    
    username = ""
    name = ""
    address = ""
    gender = ""
    dateOfBirth = ""
    sub = ""
    nickname = ""
    uniqueId = ""
    accessToken = ""

    # 認証を確認し、個人情報を対応する変数に格納する
    if accessToken != "":
        
        idTokenContent = req.oidc.id_token_claims

        username = req.oidc.user.preferred_username
        name = req.oidc.user.name
        address = req.oidc.user.user_address
        gender = req.oidc.user.gender_code
        dateOfBirth = req.oidc.user.birth_date
        sub = req.oidc.user.sub
        uniqueId = req.oidc.user.unique_id

        accessToken = req.oidc.access_token.access_token
        
        app.logger.debug('This is a debug message')
        app.logger.info('This is an info message')
        app.logger.warning('This is a warning message')
        app.logger.error('This is an error message')
        app.logger.critical('This is a critical message')

        print("接続済み",flush=True)
    else:
        app.logger.debug('This is a debug message')
        app.logger.info('This is an info message')
        app.logger.warning('This is a warning message')
        app.logger.error('This is an error message')
        app.logger.critical('This is a critical message')

        print("未接続",flush=True)

    user={
        "id_token_content": idTokenContent,
        "username": username,
        "name": name,
        "address": address,
        "gender": gender,
        "date_of_birth": dateOfBirth,
        "sub": sub,
        "unique_id": uniqueId,
        "access_token": accessToken
    }

    return user


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
def loginKeycloak() -> str:
    redirect_uri: str = url_for("auth", _external=True)
    return oauth.keycloak.authorize_redirect(redirect_uri)


@app.route("/auth")
def auth() -> str:
    token: OAuth = oauth.keycloak.authorize_access_token()
    userinfo: OAuth = token["userinfo"]
    if userinfo:
        session["user"] = userinfo
        session["token"] = token

    return redirect(url_for("index"))

@app.route("/connect")
def connect() -> str:
    return render_template("connect.html")


@app.route("/connected")
def connected() -> str:
    return render_template("connected.html")


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)
