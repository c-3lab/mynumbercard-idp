import pytest
from authlib.integrations.flask_client import OAuth

from app import app

app.config["SECRET_KEY"] = "your_secret_key_for_testing"

oauth: OAuth = OAuth(app)

@pytest.fixture
def client():
    app.config['TESTING'] = True
    return app.test_client()


def test_index_without_user(client):
    response = client.get("/")
    assert response.status_code == 200
    assert "ゲスト".encode() in response.data
    assert "新規登録・ログイン".encode() in response.data


def test_index_with_user(client):
    with client.session_transaction() as sess:
        sess["user"] = {"name": "test_user"}

    response = client.get('/')
    assert response.status_code == 200
    assert b"test_user" in response.data
    assert "アカウント情報照会".encode() in response.data


def test_login(client):
    response = client.get("/login")
    assert response.status_code == 200
    assert "<title>新規会員登録/ログイン</title>".encode() in response.data


def test_connect(client):
    response = client.get("/connect")
    assert response.status_code == 200
    assert "<title>連携前画面</title>".encode() in response.data


def test_connected(client):
    response = client.get("/connected")
    assert response.status_code == 200
    assert "<title>連携後</title>".encode() in response.data


def test_account_with_user(client):
    with client.session_transaction() as sess:
        sess["user"] = {
            "name": "test_user",
            "birth_date": "test_birthdate",
            "gender_code": "test_gender",
            "user_address": "test_address",
        }
    response = client.get("/account")
    assert response.status_code == 200
    assert '<tr><td class="topic bg-primary">お名前</td><td>test_user</td></tr>'.encode() in response.data
    assert '<tr><td class="topic bg-primary">生年月日</td><td>test_birthdate</td></tr>'.encode() in response.data
    assert '<tr><td class="topic bg-primary">性別</td><td>test_gender</td></tr>'.encode() in response.data
    assert '<tr><td class="topic bg-primary">住所</td><td>test_address</td></tr>'.encode() in response.data


def test_account_without_user(client):
    with client.session_transaction() as sess:
        sess["user"] = None
    response = client.get("/account")
    assert response.status_code == 200
    assert '<tr><td class="topic bg-primary">お名前</td><td></td></tr>'.encode() in response.data
    assert '<tr><td class="topic bg-primary">生年月日</td><td></td></tr>'.encode() in response.data
    assert '<tr><td class="topic bg-primary">性別</td><td></td></tr>'.encode() in response.data
    assert '<tr><td class="topic bg-primary">住所</td><td></td></tr>'.encode() in response.data


def test_token_with_user(client):
    with client.session_transaction() as sess:
        sess["user"] = {
            "unique_id": "test_id",
            "sub": "test_sub",
            }
        sess["token"] = {"access_token": "test_access_token"}
    response = client.get("/token")
    assert response.status_code == 200
    assert b"<td>test_id</td>" in response.data
    assert b"<td>test_sub</td>" in response.data
    assert b'<td><div class="word-break-all">test_access_token</div></td>' in response.data
    assert b"user" in response.data


def test_token_without_user(client):
    with client.session_transaction() as sess:
        sess["user"] = None
        sess["token"] = None
    response = client.get("/token")
    assert response.status_code == 200
    assert b"<td></td>" in response.data # assert unique_id
    assert b"<td></td>" in response.data # assert sub
    assert b'<td><div class="word-break-all"></div></td>' in response.data
    assert b" <p>keycloak-id-token: <br>None</p>" in response.data
