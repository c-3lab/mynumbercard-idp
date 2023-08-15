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
FROM maven:3.8.6-openjdk-11-slim as replace-spi-build-lib-download-stage

WORKDIR /opt
COPY ./my-numbercard-authenticator/pom.xml /opt
RUN mvn dependency:go-offline -Dmaven.repo.local=~/repository clean package install test site compile --fail-never

# -----
FROM replace-spi-build-lib-download-stage as replace-spi-build-stage

WORKDIR /opt
COPY ./my-numbercard-authenticator /opt
RUN mvn package -Dmaven.repo.local=~/repository -Dmaven.legacyLocalRepo=true

# -----
FROM quay.io/keycloak/keycloak:21.0.1 as development-stage

COPY --from=spi-build-stage /opt/target/x509-relay-authenticator-jar-with-dependencies.jar /opt/keycloak/providers
COPY --from=replace-spi-build-stage /opt/target/my-numbercard-authenticator.jar /opt/keycloak/providers

ONBUILD /opt/keycloak/bin/kc.sh build
