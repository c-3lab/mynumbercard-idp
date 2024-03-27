FROM python:3.11.5
WORKDIR /usr/src/app

# 1回目のCOPYコマンドはパッケージダウンロード後の状態をビルドキャッシュにヒットできるようにするために実施している。
# 1回にしてしまうと1ファイル書き換えただけでパッケージダウンロードが発生するようになるため、2回実施とする。
COPY pyproject.toml poetry.lock ./
RUN pip3 install --upgrade pip
RUN pip3 install poetry
RUN poetry config virtualenvs.create false
RUN poetry install

COPY . .

EXPOSE 3000
CMD ["python3", "app.py"]
