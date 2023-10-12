FROM ubuntu:20.04
WORKDIR /usr/src/app

# 1回目のCOPYコマンドはパッケージダウンロード後の状態をビルドキャッシュにヒットできるようにするために実施している。
# 1回にしてしまうと1ファイル書き換えただけでパッケージダウンロードが発生するようになるため、2回実施とする。
COPY package*.json ./
RUN apt update -y
RUN apt install python3 -y -qq --no-install-recommends
RUN apt install python3-pip -y -qq --no-install-recommends
RUN pip3 install --upgrade pip

COPY . .

EXPOSE 3000
CMD ["python3", "app.py"]
