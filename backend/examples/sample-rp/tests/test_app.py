import pytest
from authlib.integrations.flask_client import OAuth

from app import app

app.config["SECRET_KEY"] = "your_secret_key_for_testing"

oauth: OAuth = OAuth(app)


@pytest.fixture
def client():
    app.config["TESTING"] = True
    return app.test_client()


def test_index_without_user(client):
    response = client.get("/")
    assert response.status_code == 200
    assert "ゲスト".encode() in response.data
    assert "新規登録・ログイン".encode() in response.data


def test_index_with_user(client):
    with client.session_transaction() as sess:
        sess["user"] = {"name": "test_user"}

    response = client.get("/")
    assert response.status_code == 200
    assert "test_user".encode() in response.data
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
    assert (
        '<tr><td class="topic bg-primary">お名前</td><td>test_user</td></tr>'.encode()
        in response.data
    )
    assert (
        '<tr><td class="topic bg-primary">生年月日</td><td>test_birthdate</td></tr>'.encode()
        in response.data
    )
    assert (
        '<tr><td class="topic bg-primary">性別</td><td>test_gender</td></tr>'.encode()
        in response.data
    )
    assert (
        '<tr><td class="topic bg-primary">住所</td><td>test_address</td></tr>'.encode()
        in response.data
    )


def test_account_without_user(client):
    with client.session_transaction() as sess:
        sess["user"] = None
    response = client.get("/account")
    assert response.status_code == 200
    assert (
        '<tr><td class="topic bg-primary">お名前</td><td></td></tr>'.encode()
        in response.data
    )
    assert (
        '<tr><td class="topic bg-primary">生年月日</td><td></td></tr>'.encode()
        in response.data
    )
    assert (
        '<tr><td class="topic bg-primary">性別</td><td></td></tr>'.encode()
        in response.data
    )
    assert (
        '<tr><td class="topic bg-primary">住所</td><td></td></tr>'.encode()
        in response.data
    )


def test_token_with_user(client):
    with client.session_transaction() as sess:
        sess["user"] = {
            "unique_id": "test_id",
            "sub": "test_sub",
        }
        sess["token"] = {"access_token": "test_access_token"}
    response = client.get("/token")
    assert response.status_code == 200
    assert "<td>test_id</td>".encode() in response.data
    assert "<td>test_sub</td>".encode() in response.data
    assert (
        '<td><div class="word-break-all">test_access_token</div></td>'.encode() in response.data
    )
    assert "user".encode() in response.data


def test_token_without_user(client):
    with client.session_transaction() as sess:
        sess["user"] = None
        sess["token"] = None
    response = client.get("/token")
    assert response.status_code == 200
    assert "<td></td>".encode() in response.data  # assert unique_id
    assert "<td></td>".encode() in response.data  # assert sub
    assert '<td><div class="word-break-all"></div></td>'.encode() in response.data
    assert "<p>keycloak-id-token: <br>None</p>".encode() in response.data


def test_login_keycloak(client, mocker):
    url = "/Keycloak-login"
    expected_status = 200

    oauth_mock = mocker.patch("app.oauth")
    authorize_redirect_mock = oauth_mock.keycloak.authorize_redirect

    response = client.get(url)

    assert response.status_code == expected_status
    assert authorize_redirect_mock.called


def test_refresh_with_token(client, mocker):
    oauth_mock = mocker.patch("app.oauth")
    fetch_access_token_mock = oauth_mock.keycloak.fetch_access_token
    fetch_access_token_mock.return_value = {"your_token_key": "your_token_value"}

    with client.session_transaction() as sess:
        sess["token"] = {"refresh_token": "test_refresh_token"}

    response = client.get("/refresh")

    assert response.status_code == 302
    assert fetch_access_token_mock.called


def test_refresh_without_token(client, mocker):
    oauth_mock = mocker.patch("app.oauth")
    fetch_access_token_mock = oauth_mock.keycloak.fetch_access_token

    response = client.get("/refresh")

    assert response.status_code == 302
    assert not fetch_access_token_mock.called


def test_logout(client):
    with client.session_transaction() as sess:
        sess["user"] = {"name": "test_user"}

    response = client.get("/logout", follow_redirects=True)

    assert response.status_code == 200
    assert "ゲスト".encode() in response.data


def test_auth_with_user(client, mocker):
    oauth_mock = mocker.patch("app.oauth")
    authorize_access_token_mock = oauth_mock.keycloak.authorize_access_token
    mock_token = {"access_token": "test_access_token", "userinfo": {"name": "test_user"}}
    mock_userinfo = mock_token["userinfo"]
    authorize_access_token_mock.return_value = mock_token

    response = client.get("/auth")

    with client.session_transaction() as sess:
        sess["user"] = mock_userinfo
        sess["token"] = mock_token

    assert response.status_code == 302
    assert authorize_access_token_mock.called
    assert response.location == "/"

    # Check if session information is stored correctly after redirecting to "/"
    response_after_redirect = client.get(response.location)
    assert response_after_redirect.status_code == 200
    assert "test_user".encode() in response_after_redirect.data


def test_auth_without_user(client, mocker):
    oauth_mock = mocker.patch("app.oauth")
    authorize_access_token_mock = oauth_mock.keycloak.authorize_access_token
    mock_token = {"access_token": "test_access_token", "userinfo": None}
    mock_userinfo = mock_token["userinfo"]
    authorize_access_token_mock.return_value = mock_token

    response = client.get("/auth")

    with client.session_transaction() as sess:
        sess["user"] = mock_userinfo
        sess["token"] = mock_token

    assert response.status_code == 302
    assert authorize_access_token_mock.called
    assert response.location == "/"

    # Check if session information is NOT stored correctly after redirecting to "/"
    response_after_redirect = client.get(response.location)
    assert response_after_redirect.status_code == 200
    assert "ゲスト".encode() in response_after_redirect.data


def test_assign_with_token(client, mocker):
    # mock environment variables
    mocker.patch(
        "os.getenv",
        side_effect=lambda key, default=None: {
            "SERVICE_ID": "mock_service_id",
            "NOTE": "mock_note",
            "KEYCLOAK_URL": "mock_keycloak_url",
            "KEYCLOAK_REALM": "mock_keycloak_realm",
        }.get(key, default),
    )

    # mock methods
    oauth_mock = mocker.patch("app.oauth")
    fetch_access_token_mock = oauth_mock.keycloak.fetch_access_token
    fetch_access_token_mock.return_value = {
        "access_token": "mock_new_access_token",
        "refresh_token": "mock_new_refresh_token",
    }
    requests_post_mock = mocker.patch("requests.post")

    with client.session_transaction() as sess:
        init_token = fetch_access_token_mock.return_value
        sess["token"] = init_token

    response = client.post("/assign")

    assert response.status_code == 302
    assert fetch_access_token_mock.called
    assert requests_post_mock.called

    # Check the contents of the POST
    requests_post_mock.assert_called_with(
        "mock_keycloak_url/realms/mock_keycloak_realm/custom-attribute/assign",
        headers={
            "Content-type": "application/json",
            "Authorization": f"Bearer {init_token['access_token']}",
        },
        json={
            "user_attributes": {
                "service_id": "mock_service_id",
                "notes": "mock_note",
            },
        },
        timeout=(3.0, 7.5),
    )

    # Check that the new token is set in the session after refresh
    with client.session_transaction() as sess:
        new_token = fetch_access_token_mock.return_value
        assert sess["token"] == new_token


def test_assign_without_token(client, mocker):
    mocker.patch("flask.session", {"token": None})
    mocker.patch("requests.post")
    with client.session_transaction():
        response = client.post("/assign")
    assert response.status_code == 400


def test_assign_with_token_without_refresh_token(client, mocker):
    # mock environment variables
    mocker.patch(
        "os.getenv",
        side_effect=lambda key, default=None: {
            "SERVICE_ID": "mock_service_id",
            "NOTE": "mock_note",
            "KEYCLOAK_URL": "mock_keycloak_url",
            "KEYCLOAK_REALM": "mock_keycloak_realm",
        }.get(key, default),
    )

    # mock methods
    oauth_mock = mocker.patch("app.oauth")
    fetch_access_token_mock = oauth_mock.keycloak.fetch_access_token
    fetch_access_token_mock.return_value = {
        "access_token": "mock_new_access_token",
        "refresh_token": None,
    }
    requests_post_mock = mocker.patch("requests.post")

    with client.session_transaction() as sess:
        init_token = fetch_access_token_mock.return_value
        sess["token"] = init_token

    response = client.post("/assign")

    assert response.status_code == 302
    assert not fetch_access_token_mock.called
    assert requests_post_mock.called


def test_replace_with_token(client, mocker):
    mocker.patch(
        "os.getenv",
        side_effect=lambda key, default=None: {
            "KEYCLOAK_URL": "mock_keycloak_url",
            "KEYCLOAK_REALM": "mock_keycloak_realm",
            "BASE_URL": "mock_base_url",
        }.get(key, default),
    )

    with app.test_client() as client:
        with client.session_transaction() as sess:
            init_token = {
                "access_token": "mock_old_access_token",
                "refresh_token": "mock_old_refresh_token",
            }
            sess["token"] = init_token

        requests_post_mock = mocker.patch("requests.post")
        requests_post_mock.return_value.headers = {"Location": "mock_redirect_location"}

        userinfo_response_mock = mocker.Mock()
        userinfo_response_mock.status_code = 200
        userinfo_response_mock.json.return_value = {"user_info_key": "user_info_value"}

        requests_get_mock = mocker.patch("requests.get")
        requests_get_mock.return_value = userinfo_response_mock

        response = client.post("/replace")

        assert response.status_code == 302
        assert requests_post_mock.called
        assert requests_get_mock.called


def test_replace_without_token(client, mocker):
    mocker.patch("flask.session", {"token": None})
    mocker.patch("requests.post")
    mocker.patch("requests.get")

    mocker.patch.dict(
        "os.environ",
        {
            "KEYCLOAK_URL": "mock_keycloak_url",
            "KEYCLOAK_REALM": "mock_keycloak_realm",
            "BASE_URL": "http://localhost",
        },
    )

    with client.session_transaction():
        response = client.post("/replace")

    assert response.status_code == 400
