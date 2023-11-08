
from flask import Flask, render_template
from flask_oidc import OpenIDConnect
import os

app: Flask = Flask(__name__)

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
    return render_template("index.html")


@app.route("/login")
def login() -> str:
    return render_template("login.html")

@app.route("/")
def index():
    return render_template("index.html")

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)
