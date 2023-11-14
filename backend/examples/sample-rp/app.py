from flask import Flask, render_template, redirect, url_for, session, request, jsonify
from authlib.integrations.flask_client import OAuth
import os
import logging
from logging.config import dictConfig

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
})

#config
app: Flask = Flask(__name__)
logging.basicConfig(level=logging.DEBUG)
app.config.update(    
    OIDC_CLIENT_ID=os.getenv("KEYCLOAK_CLIENT_ID"),
    OIDC_CLIENT_SECRETS=os.getenv("KEYCLOAK_CLIENT_SECRET"),
    OIDC_ID_TOKEN_COOKIE_SECURE=False,
    OIDC_USER_INFO_ENABLED=True,
    SERVICE_ID=os.getenv("SERVICE_ID"),
    NOTE=os.getenv("NOTE"),
)

oauth: OAuth = OAuth(app)
oauth.register(
    name='rp',
    server_metadata_url=f'https://{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("REALM")}',
    client_kwargs={
        "scope": "openid",
    },
    api_base_url=os.getenv("KEYCLOAK_URL")
)

print(oauth)


def getUser(req):
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
        token = oauth.keycloak.authorize_access_token()        
        userinfo = token['userinfo']

        # userinfo の内容確認
        print("userinfo:", userinfo)

        # userinfo から必要な情報を取り出す
        idTokenContent = userinfo.get('id_token_content', '')
        username = userinfo.get('username', '')
        name = userinfo.get('name', '')
        address = userinfo.get('address', '')
        gender = userinfo.get('gender', '')
        dateOfBirth = userinfo.get('date_of_birth', '')
        sub = userinfo.get('sub', '')
        uniqueId = userinfo.get('unique_id', '')
        
        # log取得
        app.logger.debug('This is a debug message')
        app.logger.info('This is an info message')
        app.logger.warning('This is a warning message')
        app.logger.error('This is an error message')
        app.logger.critical('This is a critical message')

        print("接続済み",flush=True)
    else:
        # log取得
        app.logger.debug('This is a debug message')
        app.logger.info('This is an info message')
        app.logger.warning('This is a warning message')
        app.logger.error('This is an error message')
        app.logger.critical('This is a critical message')

        print("未接続",flush=True)

    # userinfo に情報を格納
    userinfo={
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

    print(userinfo)

    return userinfo


@app.route("/")
def index() -> str:
    userinfo = getUser(None)
    return render_template("index.html")


@app.route("/login")
def login() -> str:
    return render_template("login.html")


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)
