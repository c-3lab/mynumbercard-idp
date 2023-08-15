const { auth } = require('express-openid-connect')
const express = require('express')
const app = express()

const config = {
  authRequired: false,
  baseURL: 'http://172.24.208.100:3002',
  clientID: 'sample-client',
  issuerBaseURL: 'http://172.24.208.100:8080/realms/OIdp',
  secret: 'long text to encrypt session'
}

// auth router attaches /login, /logout, and /callback routes to the baseURL
app.use(auth(config))

// req.isAuthenticated is provided from the auth router
app.get('/', (req, res) => {
  if (req.oidc.isAuthenticated()) {
    res.send(`<a href="/logout">Logout</a><div>${JSON.stringify(req.oidc.user)}</div>`)
  } else {
    res.send('<a href="/login">Login</a>')
  }
})

app.listen(3000, () => {
  console.log('Server has been started.')
})
