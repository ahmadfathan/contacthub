# flask packages
from bson import ObjectId
from flask import Response, request, jsonify
from flask_restful import Resource

# project resources
from . import response
from models.role import (Role)
import bson
from datetime import datetime
from slugify import slugify
from mongoengine.queryset.visitor import Q
from libraries import Telegram

import traceback

class RoleAllApi(Resource):
    def get(self) -> Response:
        try:
            body = request.get_json()
            telegram = Telegram()

            if type(body) is not dict:
                body = {}
            if 'RoleId' in body and not body['RoleId'] is None:
                body['RoleId'] = int(body['RoleId'])

            result_json = Role.get_data(self,**body)
                
            return jsonify({'status':'OK','message':'SUCCESS' ,'result': result_json})
        except Role.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))
            
class RolesApi(Resource):
    def post(self) -> Response:
        try:
            telegram = Telegram()
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            body['CreatedAt'] =  current_date
            
            result = Role.create(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except Role.DoesNotExist as e:
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
            result = Role.get_paginate(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except Role.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

class RoleApi(Resource):
    

    def delete(self,role_id:str) -> Response:
        try:
            telegram = Telegram()
            result = Role.objects(_id=int(role_id)).delete()

            return jsonify({'status':'OK','message':'SUCCESS','result': []})
        except Role.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

    def put(self,role_id:str) -> Response:
        try:
            telegram = Telegram()
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            body['UpdatedAt'] =  current_date
            
            get_role = Role.objects(_id=int(role_id))
            if get_role == None:
                return response.not_found(msg='(_id) tidak diketahui')
            
            raw_body = {}
            for k in body:
                raw_body['set__' + k] = body[k]

            result = Role.objects(_id=int(role_id)).update(**raw_body)
            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except Role.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

    def get(self,role_id:str) -> Response:
        try:
            telegram = Telegram()
            item = Role.get_data(self,**{'RoleId' : int(role_id)})
            if len(item) > 0:
                item = item[0]
            
            return jsonify({'status':'OK','message':'SUCCESS','result': item })
        except Role.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))