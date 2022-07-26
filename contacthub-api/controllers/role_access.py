# flask packages
from flask import Response, request, jsonify
from flask_restful import Resource

# project resources
from . import response
from models.role import (RoleAccess)
import bson
from datetime import datetime
from slugify import slugify
from mongoengine.queryset.visitor import Q

class RoleAccessAllApi(Resource):
    def get(self) -> Response:
        try:
            body = request.get_json()

            if type(body) is not dict:
                body = {}
            
            result = RoleAccess.getAll(self,**body)
                
            return jsonify({'status':'OK','message':'SUCCESS' ,'result': result})
        except RoleAccess.DoesNotExist as e:
            return response.not_found(msg=str(e))
        except Exception as e:
	        return response.unknown(str(e))

class RolesAccessApi(Resource):
    def post(self) -> Response:
        try:
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}

            if '_id' in body:
                slug = body['_id'].replace('/','-l-')
                body['_id'] = slugify(slug)
            
            result = RoleAccess.create(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except RoleAccess.DoesNotExist as e:
            return response.not_found(msg=str(e))
        except Exception as e:
	        return response.unknown(str(e))
            
    def get(self) -> Response:
        try:
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            result = RoleAccess.get(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': {
                'items' : result.items,
                'current_page' : result.page,
                'total_pages_for_query' : result.pages,
                'item_per_page' : result.per_page,
                'total_number_of_items_that_match_query' : result.total,
            }})
        except RoleAccess.DoesNotExist as e:
            return response.not_found(msg=str(e))
        except Exception as e:
	        return response.unknown(str(e))

class RoleAccessApi(Resource):
    
    def delete(self,access_id:str) -> Response:
        try:
            result = RoleAccess.objects(_id=access_id).delete()

            return jsonify({'status':'OK','message':'SUCCESS','result': []})
        except RoleAccess.DoesNotExist as e:
            return response.not_found(msg=str(e))
        except Exception as e:
	        return response.unknown(str(e))

    def put(self,access_id:str) -> Response:
        try:
            current_date = datetime.now
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            
            if '_id' in body:
                slug = body['_id'].replace('/','-l-')
                body['_id'] = slugify(slug)
            
            get_role = RoleAccess.objects(_id=access_id)
            if get_role == None:
                return response.not_found(msg='(_id) tidak diketahui')
            
            result = RoleAccess.update_data(self,access_id,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except RoleAccess.DoesNotExist as e:
            return response.not_found(msg=str(e))
        except Exception as e:
	        return response.unknown(str(e))

    def get(self,access_id:str) -> Response:
        try:
            result = RoleAccess.by_id(self,access_id=access_id)
            return jsonify({'status':'OK','message':'SUCCESS','result': result })
        except RoleAccess.DoesNotExist as e:
            return response.not_found(msg=str(e))
        except Exception as e:
	        return response.unknown(str(e))