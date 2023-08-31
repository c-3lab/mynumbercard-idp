FROM node:16.20.0-bullseye as js-build-stage

WORKDIR /usr/local/src
RUN git clone --single-branch -b v1.3.1 https://github.com/digitalbazaar/forge.git

WORKDIR /usr/local/src/forge
RUN npm install && \
    npm run build

# -----
FROM maven:3.8.6-openjdk-11-slim as spi-build-stage

WORKDIR /opt/my-numbercard-authenticator
# 依存関係のダウンロード
ARG MAVEN_CACHE_SETTINGS_PATH=/usr/share/maven/ref/settings-docker.xml
COPY ./my-numbercard-authenticator/pom.xml .
RUN mvn -B -f pom.xml -s $MAVEN_CACHE_SETTINGS_PATH dependency:resolve

# パッケージの生成
COPY ./my-numbercard-authenticator .
COPY --from=js-build-stage /usr/local/src/forge/dist/forge.min.js /opt/my-numbercard-authenticator/src/main/resources/theme/mynumbercard-auth/login/resources/js
COPY --from=js-build-stage /usr/local/src/forge/dist/forge.min.js.map /opt/my-numbercard-authenticator/src/main/resources/theme/mynumbercard-auth/login/resources/js
RUN mvn -B -s $MAVEN_CACHE_SETTINGS_PATH clean package

# -----
FROM quay.io/keycloak/keycloak:21.0.1 as development-stage

WORKDIR /opt/keycloak
COPY --from=spi-build-stage /opt/my-numbercard-authenticator/target/my-numbercard-authenticator.jar providers
