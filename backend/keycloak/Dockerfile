FROM node:16.20.0-bullseye as js-build-stage

WORKDIR /usr/local/src
RUN git clone --single-branch -b v1.3.1 https://github.com/digitalbazaar/forge.git

WORKDIR /usr/local/src/forge
RUN npm install && \
    npm run build

# -----
FROM maven:3.8.6-openjdk-11-slim as spi-build-stage

WORKDIR /opt
COPY ./my-numbercard-authenticator/pom.xml /opt
RUN mvn dependency:go-offline --fail-never
COPY ./my-numbercard-authenticator /opt
COPY --from=js-build-stage /usr/local/src/forge/dist/forge.min.js /opt/src/main/resources/theme/mynumbercard-auth/login/resources/js
COPY --from=js-build-stage /usr/local/src/forge/dist/forge.min.js.map /opt/src/main/resources/theme/mynumbercard-auth/login/resources/js
COPY ./x509-relay-authenticator/src/main/java/com/example/mynumbercardidp/keycloak/rest /opt/src/main/java/com/example/mynumbercardidp/keycloak/rest
COPY ./x509-relay-authenticator/src/main/resources/META-INF/services/org.keycloak.services.resource.RealmResourceProviderFactory /opt/src/main/resources/META-INF/services
RUN mvn clean package

# -----
FROM quay.io/keycloak/keycloak:21.0.1 as development-stage

COPY --from=spi-build-stage /opt/target/my-numbercard-authenticator.jar /opt/keycloak/providers

ONBUILD RUN /opt/keycloak/bin/kc.sh build
