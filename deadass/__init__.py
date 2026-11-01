####################################
# File name: __init__.py           #
# Author: Joe Abbate               #
####################################
from subprocess import check_output
import os
import pytz
from flask_pyoidc.flask_pyoidc import OIDCAuthentication
from flask_pyoidc.provider_configuration import ProviderConfiguration, ClientMetadata
from flask import Flask, render_template, send_from_directory, redirect
from flask_migrate import Migrate, migrate
from flask_wtf.csrf import CSRFProtect
from flask_pymongo import MongoClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
import radosgw
import socket

# from flask_login import login_user, logout_user, login_required, LoginManager, current_user


# Setting up Flask and csrf token for forms.
app = Flask(__name__)
csrf = CSRFProtect(app)
csrf.init_app(app)
# Get app config from absolute file path
if os.path.exists(os.path.join(os.getcwd(), "config.py")):
    app.config.from_pyfile(os.path.join(os.getcwd(), "config.py"))
else:
    app.config.from_pyfile(os.path.join(os.getcwd(), "config.env.py"))

# Establish SQL Database
deadass_db = create_engine(app.config["DEADASS_URI"])
postgres_db = create_engine(app.config["POSTGRES_URI"])
mysql_db = create_engine(app.config["MYSQL_URI"])
mongo_db = MongoClient(app.config["MONGO_URI"])
rgwadmin = radosgw.connection.RadosGWAdminConnection(
    host=socket.gethostbyname(app.config["S3_HOST"]) if app.config["S3_DISABLE_SSL"] else app.config["S3_HOST"],
    port=app.config["S3_PORT"],
    is_secure=app.config["S3_DISABLE_SSL"] == False,
    access_key=app.config["S3_ACCESS_KEY"],
    secret_key=app.config["S3_SECRET_KEY"],
)

# OIDC Authentication
CSH_AUTH = ProviderConfiguration(
    issuer=app.config["OIDC_ISSUER"],
    client_metadata=ClientMetadata(
        app.config["OIDC_CLIENT_ID"], app.config["OIDC_CLIENT_SECRET"]
    ),
)
auth = OIDCAuthentication({"default": CSH_AUTH}, app)

auth.init_app(app)

# Commit
commit = check_output(["git", "rev-parse", "--short", "HEAD"]).decode("utf-8").rstrip()

# pylint: disable=wrong-import-position
from .utils import get_user
from .models import Database, S3, Model

# Migrate
migrate = Migrate(app, Model)

# time setup for the server side time
eastern = pytz.timezone("America/New_York")
fmt = "%Y-%m-%d %H:%M"

# Favicon
@app.route("/favicon.ico")
def favicon():
    return send_from_directory(
        os.path.join(app.root_path, "static/assets"),
        "favicon.ico",
        mimetype="image/vnd.microsoft.icon",
    )


@app.route("/logout")
@auth.oidc_logout
def _logout():
    return redirect("/", 302)


@app.route("/")
@auth.oidc_auth("default")
@get_user
def home(user_dict=None):
    with Session(deadass_db) as session:
        dbs = (
            session.query(Database)
            .filter(Database.owner == user_dict["username"])
            .all()
        )
        s3 = (
            session.query(S3)
            .filter(S3.owner == user_dict["username"])
            .all()
        )
    return render_template(
        "index.html", dbs=dbs, s3s=s3, commit=commit, username=user_dict["username"]
    )


from .blueprints.db import db_bp
from .blueprints.s3 import s3_bp

app.register_blueprint(db_bp)
app.register_blueprint(s3_bp)
