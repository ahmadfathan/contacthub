
from flask import Flask, app,jsonify,make_response

def create_error_handler(app):
    @app.errorhandler(404)
    def not_found(e):
        result = jsonify({'status':'NOT_FOUND','message':str(e),'result':[]})
        result.headers['X-MESSAGE'] = str(e)
        return make_response(result,404)

    @app.errorhandler(500)
    def internal_server(e):
        result = jsonify({'status':'INTERNAL_SERVER','message':str(e),'result':[]})
        result.headers['X-MESSAGE'] = str(e)
        return make_response(result,500)

    @app.errorhandler(502)
    def bad_gateway(e):
        result = jsonify({'status':'BAD_GATEWAY','message':str(e),'result':[]})
        result.headers['X-MESSAGE'] = str(e)
        return make_response(result,502)
    
    @app.errorhandler(503)
    def unavailable(e):
        result = jsonify({'status':'UNAVAILABLE','message':str(e),'result':[]})
        result.headers['X-MESSAGE'] = str(e)
        return make_response(result,503)

    @app.errorhandler(504)
    def timeout(e):
        result = jsonify({'status':'TIMEOUT','message':str(e),'result':[]})
        result.headers['X-MESSAGE'] = str(e)
        return make_response(result,504)

    @app.errorhandler(405)
    def method_not_allowed(e):
        result = jsonify({'status':'METHOD_NOT_ALLOWED','message':str(e),'result':[]})
        result.headers['X-MESSAGE'] = str(e)
        return make_response(result,504)

