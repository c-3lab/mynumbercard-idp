<<<<<<< HEAD
from flask import Flask, render_template, redirect, url_for, session,request
from authlib.integrations.flask_client import OAuth
=======

from flask import Flask, render_template
from flask_oidc import OpenIDConnect
>>>>>>> be443b1 (fixed conflict)
import os

#config
app: Flask = Flask(__name__)
app.secret_key = 'your_random_secret_key_here'

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
    authorize_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/auth',
    authorize_params=None,    
    server_metadata_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/.well-known/openid-configuration',
    access_token_url=f'{os.getenv("KEYCLOAK_URL")}/realms/{os.getenv("KEYCLOAK_REALM")}/protocol/openid-connect/token',
    access_token_params=None,   
    api_base_url=f'{os.getenv("BASE_URL")}', 
    client_kwargs={
        "scope": "openid",
    }
)

#config
app.config.update(
    OIDC_CLIENT_ID=os.getenv("KEYCLOAK_CLIENT_ID"),
    OIDC_CLIENT_SECRETS=os.getenv("KEYCLOAK_CLIENT_SECRET"),
    OIDC_ID_TOKEN_COOKIE_SECURE=False,
    OIDC_USER_INFO_ENABLED=True,
    OIDC_SCOPES=["openid"],
    OIDC_OPENID_REALM=os.getenv("KEYCLOAK_REALM"),
    SERVICE_ID=os.getenv("SERVICE_ID"),
    NOTE=os.getenv("NOTE"),

)

print(app)

'''
oidc = OpenIDConnect(app)

def getUser(req):
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

    if req.oidc.is_authenticated():
        id_token_content = req.oidc.id_token_claims

        username = req.oidc.user.preferred_username
        name = req.oidc.user.name
        address = req.oidc.user.user_address
        gender = req.oidc.user.gender_code
        date_of_birth = req.oidc.user.birth_date
        sub = req.oidc.user.sub
        unique_id = req.oidc.user.unique_id

        access_token = req.oidc.access_token.access_token

    user={
        "id_token_content": id_token_content,
        "username": username,
        "name": name,
        "address": address,
        "gender": gender,
        "date_of_birth": date_of_birth,
        "sub": sub,
        "unique_id": unique_id,
        "access_token": access_token
    }

    return user
'''

@app.route("/")
def index() -> str:
    user = session.get('user')
    return render_template("index.html",user=user)


@app.route("/login")
def login() -> str:
    return render_template("login.html")

@app.route("/")
def index():
    return render_template("index.html")

<<<<<<< HEAD
@app.route('/Keycloak-login')
def loginKeycloak():
    redirect_uri = url_for('auth', _external=True)
    return oauth.keycloak.authorize_redirect(redirect_uri)


@app.route('/auth')
def auth():
    token = oauth.keycloak.authorize_access_token()
    print(token)
    userinfo = token['userinfo']
    if userinfo:
        session['user'] = userinfo
        session['token'] = token

    return redirect(url_for('index'))
=======
@app.route("/connected")
def connected() -> str:
    return render_template("connected.html")
>>>>>>> 4a2bd00 (・OKボタンを追加)


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)
