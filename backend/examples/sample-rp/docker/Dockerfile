FROM node:18.14.2
WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install

COPY . .
ARG KEYCLOAK_REALM
ARG KEYCLOAK_AUTH_URL
ARG SERVICE_ID
ARG NOTE

RUN eval "echo \"$(cat assign_setting.json.tmpl)\"" > assign_setting.json
RUN eval "echo \"$(cat keycloak.json.tmpl)\"" > keycloak.json
RUN cat keycloak.json 
RUN cat assign_setting.json

EXPOSE 3000
CMD ["node", "server.js"]
