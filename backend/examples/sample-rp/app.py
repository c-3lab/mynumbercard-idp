from flask import Flask, render_template, redirect, url_for, session,request
from authlib.integrations.flask_client import OAuth
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


@app.route("/")
def index() -> str:
    user = session.get('user')
    return render_template("index.html",user=user)


@app.route("/login")
def login() -> str:
    return render_template("login.html")


@app.route("/connected")
def connected() -> str:
    return render_template("connected.html")

  
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

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)
