# flask packages
from bson import ObjectId
from flask import Response, request, jsonify
from flask_restful import Resource

# project resources
from . import response
from models.menu import (Menu)
from datetime import datetime
from libraries import Telegram
import json
import traceback

class MenuAllApi(Resource):
    def get(self) -> Response:
        try:
            telegram = Telegram()
            body = request.get_json()

            if type(body) is not dict:
                body = {}
            
            result_obj,result_json = Menu.getAll(self,**body)
                
            return jsonify({'status':'OK','message':'SUCCESS' ,'result': result_json})
        except Menu.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e) + str(type(body)))

class MenusApi(Resource):
    def post(self) -> Response:
        try:
            telegram = Telegram()
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            body['CreatedAt'] =  current_date
            
            result = Menu.create(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except Menu.DoesNotExist as e:
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
            result = Menu.getAll(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except Menu.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

class MenuApi(Resource):

    def delete(self,menu_id:str) -> Response:
        try:
            telegram = Telegram()
            result = Menu.objects(id=ObjectId(menu_id)).delete()

            return jsonify({'status':'OK','message':'SUCCESS','result': []})
        except Menu.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

    # def put(self,menu_id:str) -> Response:
    #     try:
    #         current_date = datetime.now
    #         body = request.get_json()
    #         if type(body) is not dict:
    #             body = {}
    #         body['UpdatedAt'] =  current_date
            
    #         get_data = Menu.objects(id=ObjectId(menu_id))
    #         if get_data == None:
    #             return not_found(msg='(_id) tidak diketahui')
            
    #         result = Menu.update_data(self,menu_id,**body)

    #         return jsonify({'status':'OK','message':'SUCCESS','result': result})
    #     except Menu.DoesNotExist as e:
    #         return not_found(msg=str(e))
    #     except Exception as e:
	#         return unknown(str(e))
    def patch(self,menu_id:str) -> Response:
        try:
            telegram = Telegram()
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            body['UpdatedAt'] =  current_date
            
            data_body = {}
            for b in body:
                data_body['set__'+b] = body[b]

            body = data_body

            get_data = Menu.objects(id=ObjectId(menu_id))
            if get_data == None:
                return response.not_found(msg='(_id) tidak diketahui')
            
            result = Menu.update_data(self,menu_id,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except Menu.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

    def get(self,menu_id:str) -> Response:
        try:
            telegram = Telegram()
            result, item = Menu.by_id(self,menu_id=menu_id)
            return jsonify({'status':'OK','message':'SUCCESS','result': item })
        except Menu.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))