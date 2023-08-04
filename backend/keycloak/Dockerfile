FROM node:16.20.0-bullseye as js-build-stage

WORKDIR /usr/local/src
RUN git clone --single-branch -b v1.3.1 https://github.com/digitalbazaar/forge.git

WORKDIR /usr/local/src/forge
RUN npm install && \
    npm run build

# -----
FROM maven:3.8.6-openjdk-11-slim as spi-build-stage

WORKDIR /opt
COPY ./x509-relay-authenticator /opt
COPY --from=js-build-stage /usr/local/src/forge/dist/forge.min.js /opt/src/main/resources/theme/call-native-app/login/resources/js
COPY --from=js-build-stage /usr/local/src/forge/dist/forge.min.js.map /opt/src/main/resources/theme/call-native-app/login/resources/js

RUN mvn clean package

# -----
FROM quay.io/keycloak/keycloak:21.0.1 as production-stage

COPY --from=spi-build-stage /opt/target/x509-relay-authenticator-jar-with-dependencies.jar /opt/keycloak/providers
