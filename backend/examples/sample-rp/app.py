from flask import Flask, render_template

app: Flask = Flask(__name__)


@app.route("/")
def index() -> str:
    return render_template("index.html")


@app.route("/login")
def login() -> str:
    return render_template("login.html")


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=3000)
