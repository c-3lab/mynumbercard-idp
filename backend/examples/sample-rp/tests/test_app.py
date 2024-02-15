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
    assert "新規登録・ログイン".encode() in response.data


def test_index_with_user(client):
    with client.session_transaction() as sess:
        sess["user"] = {"username": "test_user"}

    response = client.get('/')
    assert response.status_code == 200
    assert b"<title>SampleRP Website</title>" in response.data
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
    with client:
        with client.session_transaction() as sess:
            sess["user"] = {"name": "test_user"}
        response = client.get("/account")
        assert response.status_code == 200
        assert '<tr><td class="topic bg-primary">お名前</td><td>test_user</td></tr>'.encode() in response.data


def test_account_without_user(client):
    with client.session_transaction() as sess:
        sess["user"] = None
    response = client.get("/account")
    assert response.status_code == 200
    assert '<tr><td class="topic bg-primary">お名前</td><td></td></tr>'.encode() in response.data


def test_token(client):
    response = client.get("/token")
    assert response.status_code == 200
    assert "<title>ID/トークン情報</title>".encode() in response.data


