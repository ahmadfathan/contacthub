# flask packages
from flask import Response, jsonify

def not_found_200(message = "Data tidak ditemukan") -> Response:
    output = {
        "result" : [],
        "message" : message,
        "status" : "NOT_FOUND"
    }
    resp = jsonify(output)
    resp.status_code = 200
    resp.headers['X-MESSAGE'] = message
    return resp

def custom(message:str,status:str,result=[],code=500) -> Response:
    output = {
        "result" : result,
        "message" : message,
        "status" : status
    }
    resp = jsonify(output)
    resp.status_code = code
    resp.headers['X-MESSAGE'] = message
    return resp

def unknown(message:str,status="ERROR") -> Response:
    output = {
        "result" : [],
        "message" : message,
        "status" : status
    }
    resp = jsonify(output)
    resp.status_code = 500
    resp.headers['X-MESSAGE'] = message
    return resp

def not_found(message = "Data tidak ditemukan") -> Response:
    output = {
        "result" : [],
        "message" : message,
        "status" : "NOT_FOUND"
    }
    resp = jsonify(output)
    resp.status_code = 404
    resp.headers['X-MESSAGE'] = message
    return resp

def accepted(message = "Data tidak ditemukan",status="NOT_FOUND",result=[]) -> Response:
    output = {
        "result" : result,
        "message" : message,
        "status" : status
    }
    resp = jsonify(output)
    resp.status_code = 202
    resp.headers['X-MESSAGE'] = message
    return resp
    
def bad_request(message="Bad Request",result=[]) -> Response:
    output = {
        "result" : result,
        "message" : message,
        "status" : "BAD_REQUEST"
    }
    resp = jsonify(output)
    resp.status_code = 400
    resp.headers['X-MESSAGE'] = message
    return resp

def unauthorized(message="401 error: The email or password provided is invalid.") -> Response:
    output = {
        "result" : [],
        "message" : message,
        "status" : "UNAUTHORIZED"
    }
    resp = jsonify(output)
    resp.status_code = 401
    resp.headers['X-MESSAGE'] = message
    return resp


def forbidden(message= "403 error: The current user is not authorized to take this action.") -> Response:
    output = {
        "result" : [],
        "message" : message,
        "status" : "FORBIDDEN"
    }
    resp = jsonify(output)
    resp.status_code = 403
    resp.headers['X-MESSAGE'] = message
    return resp


def invalid_route(message="404 error: This route is currently not supported. See API documentation.") -> Response:
    output = {
        "result" : [],
        "message" : message,
        "status" : "INVALID_ROUTE"
    }
    resp = jsonify(output)
    resp.status_code = 404
    resp.headers['X-MESSAGE'] = message
    return resp