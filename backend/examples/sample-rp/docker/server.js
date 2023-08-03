// Sample web service using OpenID Connect.
const express  = require("express");
const session  = require("express-session");
const Keycloak = require("keycloak-connect");
const request  = require("request");

const sessionStore = new session.MemoryStore();
const keycloak     = new Keycloak({ store: sessionStore });

const app = express();
app.set('trust proxy', 'uniquelocal');

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
    let keycloak_address          = "";
    let keycloak_gender           = "";
    let keycloak_date_of_birth    = "";
    let keycloak_sub              = "";
    let keycloak_nickname         = "";
    let keycloak_unique_id        = "";
    let keycloak_access_token     = "";

    if (typeof req.kauth.grant == 'object') {
        keycloak_id_token_content = req.kauth.grant.id_token.content;

        keycloak_username         = keycloak_id_token_content.preferred_username;
        keycloak_name             = keycloak_id_token_content.name;
        keycloak_address          = keycloak_id_token_content.user_address;
        keycloak_gender           = keycloak_id_token_content.gender;
        keycloak_date_of_birth    = keycloak_id_token_content.birthDate;
        keycloak_sub              = keycloak_id_token_content.sub;
        keycloak_nickname         = keycloak_id_token_content.nickname;
        keycloak_unique_id        = keycloak_id_token_content.unique_id;
    
        keycloak_token_content = JSON.parse(req.session['keycloak-token']);
        keycloak_access_token  = keycloak_token_content.access_token;
    }
    
    let view_args = {
      username:         keycloak_username,
      token_content:    keycloak_token_content,
      id_token_content: keycloak_id_token_content,
      name:             keycloak_name,
      address:          keycloak_address,
      gender:           keycloak_gender,
      date_of_birth:    keycloak_date_of_birth,
      sub:              keycloak_sub,
      nickname:         keycloak_nickname,
      unique_id:        keycloak_unique_id,
      access_token:     keycloak_access_token,
    }

    return view_args;
}

function get_assign_api (data) {
	let assign_args = {
		access_token:		data.access_token,
		expires_in:		data.expires_in,
		refresh_expires_in:	data.refresh_expires_in,
		refresh_token:		data.refresh_token,
		token_type:		data.token_type,
		id_token:		data.id_token,
		notBeforePolicy:	data['not-before-policy'],
		session_state:		data.session_state,
		scope:			data.scope,
		refresh_id_token:	parseJwt(data.id_token),
	}


	return assign_args;
}

function get_assign_api_empty(){
          let assign_args = {
                access_token:           '',
                expires_in:             '',
                refresh_expires_in:     '',
                refresh_token:          '',
                token_type:             '',
                id_token:               '',
                notBeforePolicy:        '',
                session_state:          '',
                scope:                  '',
		refresh_id_token:	'',
        }


        return assign_args;
}

function parseJwt (token) 
{    
	return JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString());
}

// public url
app.get("/", (req, res) => {
    let args = get_user_info(req)
   
    if(!(args.username == ''))
    {
	 console.log ('AccessToken');
    }

    res.render("index", { vars: get_user_info(req)  ,vars_assign:get_assign_api_empty()})
	
});

// protecte url
app.get("/login", keycloak.protect(), (req, res) => {
    res.redirect(303, "/")
});


const serviceIdValue = "example@example.com";
const noteValue = "RP1";
app.get("/assign", keycloak.protect(), (req, res) => {
    console.log('assign');

	// RP側のユーザーに関連した情報をIdP側のユーザーに紐づけるAPIを呼び出す
        console.log ('API call');
	let args = get_user_info(req)
	console.log (args.access_token);
        var URL = 'https://keycloak.example.com/realms/OIdp/custom-attribute/assign';
        let assign_args;
        request.post({
		uri: URL,
                headers: { "Content-type": "application/json" },
                headers: { "Authorization": "Bearer " + args.access_token },
                json: {"userAttributes":
			{
				//サービスの独自ID
                                "serviceId": serviceIdValue ,
                                "notes": noteValue
                        }
                }
	}, (err, response, data) => {
		console.log('response');
                console.log(err);
                console.log(data);

                res.render("index", { vars: get_user_info(req) ,vars_assign:get_assign_api(data)})
        });
});

app.listen(3000, () => console.log(`[${new Date()}] server, startup`));

