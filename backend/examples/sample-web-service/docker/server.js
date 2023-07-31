// Sample web service using OpenID Connect.
const express  = require("express");
const session  = require("express-session");
const Keycloak = require("keycloak-connect");

const sessionStore = new session.MemoryStore();
const keycloak     = new Keycloak({ store: sessionStore });

const app = express();

app.use(session({
    secret: "secret-sign",
    resave: false,
    saveUninitialized: false
}));

app.use(keycloak.middleware());
app.set("view engine", "ejs");
app.use(express.static('public'));

function get_user_info (req) {
    let keycloak_username         = "";
    let keycloak_id_token_content = "";
    let keycloak_token_content    = "";
    let keycloak_name             = "";
    let keycloak_family_name      = "";
    let keycloak_given_name       = "";
    let keycloak_address          = "";
    let keycloak_gender           = "";
    let keycloak_date_of_birth    = "";
    let keycloak_sub              = "";
    let keycloak_unique_id        = "";
    let keycloak_access_token     = "";

    if (typeof req.kauth.grant == 'object') {
        keycloak_id_token_content = req.kauth.grant.id_token.content;
        keycloak_username         = keycloak_id_token_content.preferred_username;
        keycloak_name             = keycloak_id_token_content.name;
        keycloak_family_name      = keycloak_id_token_content.family_name;
        keycloak_given_name       = keycloak_id_token_content.given_name;
        keycloak_address          = keycloak_id_token_content.address;
        keycloak_gender           = keycloak_id_token_content.gender;
        keycloak_date_of_birth    = keycloak_id_token_content.birthdate;
        keycloak_sub              = keycloak_id_token_content.sub;
        keycloak_unique_id        = keycloak_id_token_content.unique_id;
    
        keycloak_token_content = JSON.parse(req.session['keycloak-token']);
        keycloak_access_token  = keycloak_token_content.access_token;
    }
    
    let view_args = {
      username:         keycloak_username,
      token_content:    keycloak_token_content,
      id_token_content: keycloak_id_token_content,
      name:             keycloak_name,
      family_name:      keycloak_family_name,
      given_name:       keycloak_given_name,
      address:          keycloak_address,
      gender:           keycloak_gender,
      date_of_birth:    keycloak_date_of_birth,
      sub:              keycloak_sub,
      unique_id:        keycloak_unique_id,
      access_token:     keycloak_access_token,
    }

    return view_args;
}


// public url
app.get("/", (req, res) => {
    res.render("index", { vars: get_user_info(req) })
});

// protecte url
app.get("/login", keycloak.protect(), (req, res) => {
    res.redirect(303, "/")
});

app.listen(3000, () => console.log(`[${new Date()}] server, startup`));
