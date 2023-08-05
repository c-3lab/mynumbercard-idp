// Sample web service using OpenID Connect.
const express = require("express");
const session = require("express-session");
const Keycloak = require("keycloak-connect");
const request = require("request");
const fs = require('fs');

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

function getUser(req) {
  let idTokenContent = {};

  let username = "";
  let name = "";
  let address = "";
  let gender = "";
  let dateOfBirth = "";
  let sub = "";
  let nickname = "";
  let uniqueId = "";
  let accessToken = "";

  if (typeof req.kauth.grant === 'object') {
    idTokenContent = req.kauth.grant.id_token.content;

    username = idTokenContent.preferred_username;
    name = idTokenContent.name;
    address = idTokenContent.userAddress;
    gender = idTokenContent.gender;
    dateOfBirth = idTokenContent.birthDate;
    sub = idTokenContent.sub;
    nickname = idTokenContent.nickname;
    uniqueId = idTokenContent.unique_id;

    accessToken = req.kauth.grant.access_token.token;
  }

  const user = {
    idTokenContent: idTokenContent,
    username: username,
    name: name,
    address: address,
    gender: gender,
    dateOfBirth: dateOfBirth,
    sub: sub,
    nickname: nickname,
    uniqueId: uniqueId,
    accessToken: accessToken,
  }

  return user;
}

// public url
app.get("/", (req, res) => {
  res.render("index", { user: getUser(req) })
});

// protecte url
app.get("/login", keycloak.protect(), (req, res) => {
  res.redirect(303, "/")
});

app.post("/assign", keycloak.protect(), async (req, res, next) => {
  try {
    const setting = JSON.parse(fs.readFileSync('./assign_setting.json', 'utf8'));
    const serviceIdValue = setting.serviceId
    const noteValue = setting.note
    const assignAPIURL = setting.URL

    // RP側のユーザーに関連した情報をIdP側のユーザーに紐づけるAPIを呼び出す
    const user = getUser(req)
    request.post({
      uri: assignAPIURL,
      headers: { "Content-type": "application/json" },
      headers: { "Authorization": "Bearer " + user.accessToken },
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
