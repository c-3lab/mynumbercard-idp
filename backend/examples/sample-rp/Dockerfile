FROM node:18.14.2
WORKDIR /usr/src/app

#1回目のCOPYコマンドはパッケージダウンロード後の状態をビルドキャッシュに
#ヒットできるようにするために実施している
#１回にしてしまうと1ファイル書き換えただけでパッケージダウンロードが発生する
#ようになるため、２回実施する
COPY package*.json ./
RUN npm install

COPY . .

EXPOSE 3000
CMD ["node", "server.js"]
