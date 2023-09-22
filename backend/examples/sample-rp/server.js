const express = require("express");
const axios = require('axios');
const { auth, requiresAuth } = require('express-openid-connect')
const useragent = require('express-useragent');

const app = express();
app.use(useragent.express());
app.set('trust proxy', 'uniquelocal');
app.set("view engine", "ejs");
app.use(express.static('public'));

const config = {
  authorizationParams: {
    response_type: 'code',
    scope: 'openid',
  },
  authRequired: false,
  baseURL: process.env.BASE_URL,
  clientID: process.env.KEYCLOAK_CLIENT_ID,
  clientSecret: process.env.KEYCLOAK_CLIENT_SECRET,
  issuerBaseURL: process.env.KEYCLOAK_URL + '/realms/' + process.env.KEYCLOAK_REALM,
  secret: process.env.KEYCLOAK_CLIENT_ID + "(A long, random string used to encrypt the session cookie)"
}

app.use(auth(config))

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

  if (req.oidc.isAuthenticated()) {
    idTokenContent = req.oidc.idTokenClaims;

    username = req.oidc.user.preferred_username;
    name = req.oidc.user.name;
    address = req.oidc.user.user_address;
    gender = req.oidc.user.gender_code;
    dateOfBirth = req.oidc.user.birth_date;
    sub = req.oidc.user.sub;
    uniqueId =  req.oidc.user.unique_id;

    accessToken = req.oidc.accessToken.access_token;
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

app.get("/reset", requiresAuth(), async (req, res, next) => {
  try {
    await req.oidc.accessToken.refresh()
    res.redirect(303, "/")
  } catch (error) {
    next(error)
  }
});

app.post("/assign", requiresAuth(), async (req, res, next) => {
  try {
    const serviceIdValue = process.env.SERVICE_ID
    const noteValue = process.env.NOTE
    const assignAPIURL =  process.env.KEYCLOAK_URL + "/realms/" + process.env.KEYCLOAK_REALM + "/custom-attribute/assign"

    // RP側のユーザーに関連した情報をIdP側のユーザーに紐づけるAPIを呼び出す
    await axios({
      method: 'post',
      url: assignAPIURL,
      headers: {
        "Content-type": "application/json",
        "Authorization": "Bearer " + req.oidc.accessToken.access_token
      },
      data: {
        "user_attributes": {
          "service_id": serviceIdValue,
          "notes": noteValue
        }
      }
    });

    await req.oidc.accessToken.refresh()
    res.redirect(303, "/")
  } catch (error) {
    next(error)
  }
});

app.post("/replace", requiresAuth(), async (req, res, next) => {
  try {
    const replaceAPIURL =  process.env.KEYCLOAK_URL + "/realms/" + process.env.KEYCLOAK_REALM + "/userinfo-replacement/login" + "?redirect_uri=" + encodeURIComponent(process.env.BASE_URL + "/reset") + "&scope=openid&response_type=code"

    const { headers } = await axios({
      method: 'post',
      url: replaceAPIURL,
      headers: {
        "Content-type": "application/x-www-form-urlencoded",
        "Authorization": "Bearer " + req.oidc.accessToken.access_token,
        "User-Agent": req.useragent.source
      },
      data: {
      },
      maxRedirects: 0, 
      validateStatus: function (status) {
        return status >= 200 && status <= 302
      }
    });
    
    res.redirect(headers['location'])
  } catch (error) {
    next(error)
  }
});

app.listen(3000, () => console.log(`[${new Date()}] server, startup`))
