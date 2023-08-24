FROM node:18.14.2
WORKDIR /usr/src/app

# 1回目のCOPYコマンドはパッケージダウンロード後の状態をビルドキャッシュにヒットできるようにするために実施している。
# 1回にしてしまうと1ファイル書き換えただけでパッケージダウンロードが発生するようになるため、2回実施とする。
COPY package*.json ./
RUN npm install

COPY . .

EXPOSE 3000
CMD ["node", "server.js"]
