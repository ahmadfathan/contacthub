# flask packages
from flask import Flask, app
from flask_restful import Api
from flask_mongoengine import MongoEngine

# local packages
from routes import create_routes
from errorhandler import create_error_handler

# external packages
import os
import json

with open('./config/config-local.json') as f:
    default_config = json.load(f)


def get_flask_app(config: dict = None) -> app.Flask:
    """
    Initializes Flask app with given configuration.
    Main entry point for wsgi (gunicorn) server.
    :param config: Configuration dictionary
    :return: app
    """
    # init flask
    flask_app = Flask(__name__,template_folder='./views')

    # configure app
    config = default_config if config is None else config
    flask_app.config.update(config)

    # init api and routes
    api = Api(app=flask_app)
    create_routes(api=api)

    # init mongoengine
    db = MongoEngine(app=flask_app)

    return flask_app


if __name__ == '__main__':
    # Main entry point when run in stand-alone mode.
    app = get_flask_app()
    create_error_handler(app)
    app.run(host=default_config['APP_HOST'],port=default_config['APP_PORT'],debug=default_config['APP_DEBUG'])
