from bson import ObjectId
from flask import Response, request, jsonify, current_app
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField,BooleanField
import wtforms_json

# project resources
from models import User
from . import response
from mongoengine.queryset.visitor import Q
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram,RabbitMQ
import json 
from helpers import lang,hash_login,hash_string
from datetime import datetime
import traceback
wtforms_json.init()

class LoginForm(Form):
    Email = StringField('Email', [validators.required()])
    Password = StringField('Password', [validators.required()])
    Platform = StringField('Platform', [validators.required()])

class UbahPasswordApi(Resource):
    def put(self,user_id) -> Response:
        try:
            telegram = Telegram()
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            
            res = {'status':'OK','message':'SUCCESS','result': []}
            raw_body = {
                'LastPassword' : body['LastPassword'],
                'NewPassword' : body['NewPassword']
            }
            result = User.update_password(self,user_id,**raw_body)
            if result == True:
                pass
            elif result == False:
                res['status'] = 'FAILED'
                res['message'] = 'Gagal update Password'
            else:
                res['status'] = 'FAILED'
                res['message'] = 'Password lama salah'

            return jsonify(res)
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

class UserPaginateApi(Resource):
    def post(self) -> Response:
        try:
            telegram = Telegram()
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            result = User.get_paginate(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

class UsersApi(Resource):
    def post(self) -> Response:
        try:
            telegram = Telegram()
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            body['CreatedAt'] =  current_date
            
            result = User.create(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))
    def get(self) -> Response:
        try:
            telegram = Telegram()
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            result = User.get_data(self,**body)
            
            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

class UserApi(Resource):
    def delete(self,user_id:str) -> Response:
        try:
            telegram = Telegram()
            result = User.delete(self,**{'id' : ObjectId(user_id)})
            if result > 0:
                status = "OK"
                message = "SUCCESS"
            else:
                status = "FAILED"
                message = "Gagal Hapus data"
             
            return jsonify({'status':status,'message':message,'result': result})
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

    def patch(self,user_id:str) -> Response:
        try:
            telegram = Telegram()
            body = request.get_json()
            if type(body) is not dict:
                body = {}

            result = User.update(self,user_id,**body)
            
            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))
            
    def get(self,user_id:str) -> Response:
        try:
            telegram = Telegram()
            result = User.get_data(self,**{"UserId":user_id})
            if len(result) < 1:
                return response.not_found()
            result = result[0]

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))
    
class UserLoginApi(Resource):
    def post(self) -> Response:
        try:
            telegram = Telegram()
            default_config = current_app.config
            role_default = default_config['DEFAULT_ROLE']
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            form = LoginForm().from_json(request.json)
            if not form.validate():
                field = []
                field_msg = []
                for err_key,err_val in form.errors.items():
                    field.append(err_key)
                    if len(err_val) > 1:
                        field_msg.append(err_val)
                    else:
                        field_msg.append(err_val[0])
                
                result = {
                    'fields' : field,
                    'errors' : field_msg
                }
                messages = lang(key='notif_missing_field',filename=language)
                return response.bad_request(message=messages,result=result)

            body = request.get_json()
            if not type(body) is dict:
                body = {}
            result = User.login(self,Username=body['Email'],Password=body['Password'],RoleId="1")
            if result:
                create_token = User.create_token(self,UserId=result['UserId'],Platform=body['Platform'],AccountId="")
                if create_token:
                    result['Auth'] = create_token
                else:
                    result['Auth'] = None

                return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
            else:
                return response.unauthorized()
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except User.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 