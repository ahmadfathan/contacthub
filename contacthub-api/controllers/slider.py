# flask packages
from bson import ObjectId
from flask import Response, request, jsonify,current_app
from flask_restful import Resource

# project resources
from werkzeug.exceptions import HTTPException, NotFound
from . import response
from models.slider import (Slider)
import bson
from libraries import Telegram
from datetime import datetime
from slugify import slugify
from helpers import lang
from mongoengine.queryset.visitor import Q
from werkzeug.utils import secure_filename
import os
import ntpath
import shutil
import html
import html2text
import traceback

class SliderCustomerApi(Resource):
    def get(self) -> Response:
        try:
            body = request.get_json()

            telegram = Telegram()
            result_json = Slider.get_paginate(self,**{'filter' : {'Status' : 'active'}})
                
            return jsonify({'status':'OK','message':'SUCCESS' ,'result': result_json})
        except Slider.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))
            
class SlidersApi(Resource):
    def post(self) -> Response:
        def allowed_file(filename):
            return '.' in filename and \
                filename.rsplit('.', 1)[1].lower() in default_config['ALLOWED_EXTENSIONS']
        try:
            telegram = Telegram()
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            result = {
                'errors' : [],
                'fields' : []
            }

            if 'X-Auth-UserId' in headers and type(headers['X-Auth-UserId']) is str and not headers['X-Auth-UserId'].strip() == "":
                UserId = headers['X-Auth-UserId']
            else:
                result['errors'].append("This field is required.")
                result['fields'].append("X-Auth-UserId")

            if 'fields' in result and len(result['fields']) > 0:
                messages = lang(key='notif_missing_field_header',filename=language)
                return response.bad_request(message=messages,result={
                    'errors' : result['errors'],
                    'fields' : result['fields']
                })

            
            dir_name = ""
            body = request.form
            UploadFolder = default_config['UPLOAD_FOLDER']
            if 'dir' in body:
                UploadFolder = os.path.join(UploadFolder,body['dir'])
                if not os.path.exists(UploadFolder):
                    os.makedirs(UploadFolder)
                dir_name = body['dir']
                

            if 'file' not in request.files:
                pass
                # return response.bad_request(message=lang(key='notif_no_file_part',filename=language))
            else:
                file = request.files['file']
                # if user does not select file, browser also
                # submit an empty part without filename
                if file.filename == '':
                    return response.bad_request(message=lang(key='notif_select_file',filename=language))
                if file and allowed_file(file.filename):
                    filename = secure_filename(file.filename)
                    file.save(os.path.join(UploadFolder, filename))

            raw_body = body.to_dict()
            if 'dir' in raw_body:
                del raw_body['dir']
            
            if 'file' in request.files:
                raw_body['Image'] = os.path.join(dir_name,filename)
            raw_body['CreatedAt'] = datetime.now
            raw_body['CreatedBy'] = UserId
            
            result = Slider.create(self,**raw_body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Slider.DoesNotExist as e:
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
            telegram = Telegram()
            body = request.get_json()
            if type(body) is not dict:
                body = {}
            result = Slider.get_paginate(self,**body)

            return jsonify({'status':'OK','message':'SUCCESS','result': result})
        except Slider.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

class SliderApi(Resource):
    

    def delete(self,slider_id:str) -> Response:
        try:
            telegram = Telegram()
            result = Slider.objects(id=ObjectId(slider_id)).delete()

            return jsonify({'status':'OK','message':'SUCCESS','result': []})
        except Slider.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))

    def put(self,slider_id:str) -> Response:
        def allowed_file(filename):
            return '.' in filename and \
                filename.rsplit('.', 1)[1].lower() in default_config['ALLOWED_EXTENSIONS']
        try:
            telegram = Telegram()
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            result = {
                'errors' : [],
                'fields' : []
            }

            if 'X-Auth-UserId' in headers and type(headers['X-Auth-UserId']) is str and not headers['X-Auth-UserId'].strip() == "":
                UserId = headers['X-Auth-UserId']
            else:
                result['errors'].append("This field is required.")
                result['fields'].append("X-Auth-UserId")

            if 'fields' in result and len(result['fields']) > 0:
                messages = lang(key='notif_missing_field_header',filename=language)
                return response.bad_request(message=messages,result={
                    'errors' : result['errors'],
                    'fields' : result['fields']
                })

            
            dir_name = ""
            body = request.form
            UploadFolder = default_config['UPLOAD_FOLDER']
            if 'dir' in body:
                UploadFolder = os.path.join(UploadFolder,body['dir'])
                if not os.path.exists(UploadFolder):
                    os.makedirs(UploadFolder)
                dir_name = body['dir']
                

            if 'file' not in request.files:
                pass
                # return response.bad_request(message=lang(key='notif_no_file_part',filename=language))
            else:
                file = request.files['file']
                # if user does not select file, browser also
                # submit an empty part without filename
                if file.filename == '':
                    return response.bad_request(message=lang(key='notif_select_file',filename=language))
                if file and allowed_file(file.filename):
                    filename = secure_filename(file.filename)
                    file.save(os.path.join(UploadFolder, filename))

            raw_body = body.to_dict()
            if 'dir' in raw_body:
                del raw_body['dir']
            
            if 'file' in request.files:
                raw_body['Image'] = os.path.join(dir_name,filename)
            raw_body['CreatedAt'] = datetime.now
            raw_body['CreatedBy'] = UserId
            
            result = Slider.update(self,slider_id,**raw_body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Slider.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

    def get(self,slider_id:str) -> Response:
        try:
            telegram = Telegram()
            item = Slider.get_data(self,**{'SliderId' : ObjectId(slider_id)})
            if len(item) > 0:
                item = item[0]
            
            return jsonify({'status':'OK','message':'SUCCESS','result': item })
        except Slider.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(msg=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            return response.unknown(str(e))