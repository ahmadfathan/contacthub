from bson import ObjectId
from flask import Response, request, jsonify, current_app
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField
import wtforms_json

# project resources
from models import Province
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram
import json 
from helpers import lang 
from datetime import datetime
import traceback
wtforms_json.init()

class CreateProvinceForm(Form):
    ProvinceId = StringField('ProvinceId', [validators.required()])
    
class DeleteProvinceForm(Form):
    ProvinceId = StringField('ProvinceId', [validators.required()])
    
class ProvinceApi(Resource):
    def post(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            form = CreateProvinceForm().from_json(request.json)
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
            result = Province.create(self,_id=body['ProvinceId'],ProvinceName=body['Province'])
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Province.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
    def delete(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            form = DeleteProvinceForm().from_json(request.json)
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
            
            body['_id'] = body['ProvinceId']
            del body['ProvinceId']

            result = Province.delete(self,**body)
            
            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Province.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
    def get(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            result = Province.get_data(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Province.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class ProvincePaginateApi(Resource):
    def get(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            result = Province.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Province.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
