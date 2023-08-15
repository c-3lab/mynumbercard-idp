const { auth, requiresAuth } = require('express-openid-connect')
const express = require('express')
const app = express()

const config = {
  authorizationParams: {
    response_type: 'code',
    scope: 'openid',
  },
  authRequired: false,
  baseURL: 'http://172.24.208.100:3002',
  clientID: 'sample-client',
  //  clientSecret is not used but MUST be specified with any value due to validation in express-openid-connect
  clientSecret: 'dummy',
  issuerBaseURL: 'http://172.24.208.100:8080/realms/OIdp',
  secret: 'long text to encrypt session'
}

// auth router attaches /login, /logout, and /callback routes to the baseURL
app.use(auth(config))

// req.isAuthenticated is provided from the auth router
app.get('/', (req, res) => {
  if (req.oidc.isAuthenticated()) {
    res.send(`<a href="/logout">Logout</a><div>${JSON.stringify(req.oidc.user)}</div><a href="/me">/me (require login)</a>`)
  } else {
    res.send('<a href="/login">Login</a><br><a href="/me">/me (require login)</a>')
  }
})

app.get('/me', requiresAuth(), (req, res) => {
  res.send(`<h1>me</h1><div>${JSON.stringify(req.oidc.user)}</div>`)
})

app.listen(3000, () => {
  console.log('Server has been started.')
})
