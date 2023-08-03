// Sample web service using OpenID Connect.
const express = require("express");
const session = require("express-session");
const Keycloak = require("keycloak-connect");
const request = require("request");
const jwt = require("jsonwebtoken");

const sessionStore = new session.MemoryStore();
const keycloak = new Keycloak({ store: sessionStore });

const app = express();
app.set('trust proxy', 'uniquelocal');
app.set("view engine", "ejs");

app.use(session({
  secret: "45b7bd88-4432-4df5-9ace-abd80c36b2ad",
  resave: false,
  saveUninitialized: false
}));

app.use(keycloak.middleware());
app.use(express.static('public'));

function getUserInfo (req) {
  let keycloak_username = "";
  let keycloak_id_token_content = "";
  let keycloak_token_content = "";
  let keycloak_name = "";
  let keycloak_address = "";
  let keycloak_gender = "";
  let keycloak_date_of_birth = "";
  let keycloak_sub = "";
  let keycloak_nickname = "";
  let keycloak_unique_id = "";
  let keycloak_access_token = "";

  if (typeof req.kauth.grant === 'object') {
    keycloak_id_token_content = req.kauth.grant.id_token.content;

    keycloak_username = keycloak_id_token_content.preferred_username;
    keycloak_name = keycloak_id_token_content.name;
    keycloak_address = keycloak_id_token_content.userAddress;
    keycloak_gender = keycloak_id_token_content.gender;
    keycloak_date_of_birth = keycloak_id_token_content.birthDate;
    keycloak_sub = keycloak_id_token_content.sub;
    keycloak_nickname = keycloak_id_token_content.nickname;
    keycloak_unique_id = keycloak_id_token_content.unique_id;

    keycloak_token_content = JSON.parse(req.session['keycloak-token']);
    keycloak_access_token = keycloak_token_content.access_token;
  }

  const user = {
    username: keycloak_username,
    token_content: keycloak_token_content,
    id_token_content: keycloak_id_token_content,
    name: keycloak_name,
    address: keycloak_address,
    gender: keycloak_gender,
    date_of_birth: keycloak_date_of_birth,
    sub: keycloak_sub,
    nickname: keycloak_nickname,
    unique_id: keycloak_unique_id,
    access_token: keycloak_access_token,
  }

  return user;
}

// public url
app.get("/", (req, res) => {
  res.render("index", { user: getUserInfo(req) })
});

// protecte url
app.get("/login", keycloak.protect(), (req, res) => {
  res.redirect(303, "/")
});

const serviceIdValue = "example@example.com";
const noteValue = "RP1";
const assignAPIURL = 'https://keycloak.example.com/realms/OIdp/custom-attribute/assign';
app.get("/assign", keycloak.protect(), async (req, res, next) => {
  try {
    // RP側のユーザーに関連した情報をIdP側のユーザーに紐づけるAPIを呼び出す
    let user = getUserInfo(req)
    request.post({
      uri: assignAPIURL,
      headers: { "Content-type": "application/json" },
      headers: { "Authorization": "Bearer " + user.access_token },
      json: {
        "userAttributes": {
          //サービスの独自ID
          "serviceId": serviceIdValue,
          "notes": noteValue
        }
      }
    }, async (err, response, data) => {
      const grant = await keycloak.grantManager.createGrant(JSON.stringify(data))
      keycloak.storeGrant(grant, req, res)

      res.redirect(303, "/")
    });
  } catch (error) {
    next(error)
  }
});

app.listen(3000, () => console.log(`[${new Date()}] server, startup`))
