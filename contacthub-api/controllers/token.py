# flask packages
from bson import ObjectId
from flask import Response, request, jsonify,current_app
from flask_restful import Resource

# project resources
from models import Token
from helpers.common import hash_string
from . import response
import bson
from datetime import datetime,timedelta
from werkzeug.exceptions import HTTPException, NotFound
from random import randint
import json 
from helpers import lang,validate_key
from libraries import Telegram
import traceback

class FirebaseTokenApi(Resource):
    def post(self) -> Response:
        try:
            default_config = current_app.config
            telegram = Telegram()
            headers = request.headers
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']

            result = {
                'errors' : [],
                'fields' : []
            }

            if 'X-API-KEY' in headers and type(headers['X-API-KEY']) is str and not headers['X-API-KEY'].strip() == "":
                ApiKey = headers['X-API-KEY']
            else:
                result['errors'].append("This field is required.")
                result['fields'].append("X-API-KEY")

            if 'fields' in result and len(result['fields']) > 0:
                messages = lang(key='notif_missing_field_header',filename=language)
                return response.bad_request(message=messages,result={
                    'errors' : result['errors'],
                    'fields' : result['fields']
                })
            
            body = request.get_json()
            if not type(body) is dict:
                body = {}

            update_token = Token.update(self,ApiKey,**{'FirebaseToken' : body['FirebaseToken']})
            status = "OK"
            messages = lang(key='notif_success',filename=language)
            result = jsonify({'status':status,'message':messages, 'result':body})
            return result
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found(message=str(e))
        except Token.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.unauthorized(message=lang(key='notif_invalid_access_token',filename=language))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e))
class LogoutApi(Resource):
    def post(self,role_id:str) -> Response:
        try:
            default_config = current_app.config
            telegram = Telegram()
            headers = request.headers
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']

            result = {
                'errors' : [],
                'fields' : []
            }

            if 'X-API-KEY' in headers and type(headers['X-API-KEY']) is str and not headers['X-API-KEY'].strip() == "":
                ApiKey = headers['X-API-KEY']
            else:
                result['errors'].append("This field is required.")
                result['fields'].append("X-API-KEY")

            if 'fields' in result and len(result['fields']) > 0:
                messages = lang(key='notif_missing_field_header',filename=language)
                return response.bad_request(message=messages,result={
                    'errors' : result['errors'],
                    'fields' : result['fields']
                })
            now = datetime.now()
            result = Token.access_token_verify(self,ApiKey,role_id)
            if len(result) == 0:
                return response.unauthorized(message=lang(key='notif_invalid_access_token',filename=language))
            clear_token = Token.objects(id=ObjectId(result['_id'])).update(set__Status=2)
            
            status = "OK"
            messages = lang(key='notif_success',filename=language)
            result = jsonify({'status':status,'message':messages, 'result':[]})
            result.headers['X-Auth-UserId'] = ""
            return result
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found(message=str(e))
        except Token.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.unauthorized(message=lang(key='notif_invalid_access_token',filename=language))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e))
class AccessTokenVerify(Resource):
    def post(self,role_id:str) -> Response:
        try:
            default_config = current_app.config
            telegram = Telegram()
            headers = request.headers
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']

            result = {
                'errors' : [],
                'fields' : []
            }

            if 'X-API-KEY' in headers and type(headers['X-API-KEY']) is str and not headers['X-API-KEY'].strip() == "":
                ApiKey = headers['X-API-KEY']
            else:
                result['errors'].append("This field is required.")
                result['fields'].append("X-API-KEY")

            if 'fields' in result and len(result['fields']) > 0:
                messages = lang(key='notif_missing_field_header',filename=language)
                return response.bad_request(message=messages,result={
                    'errors' : result['errors'],
                    'fields' : result['fields']
                })
            now = datetime.now()
            result = Token.access_token_verify(self,ApiKey,role_id)
            if len(result) == 0:
                return response.unauthorized(message=lang(key='notif_invalid_access_token',filename=language))

            get_token = Token.objects(id=ObjectId(result['_id'])).get()
            if 'UseExpiredToken' in get_token:
                if get_token['UseExpiredToken']:
                    expired_token  = now + timedelta(hours=48)
                    get_token = get_token.update(set__ExpiredToken=expired_token,set__UpdatedAt=now)

            status = "OK"
            messages = lang(key='notif_success',filename=language)
            result = jsonify({'status':status,'message':messages, 'result':result})
            result.headers['X-Auth-UserId'] = get_token['UserId']
            return result
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found(message=str(e))
        except Token.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.unauthorized(message=lang(key='notif_invalid_access_token',filename=language))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e))
        
