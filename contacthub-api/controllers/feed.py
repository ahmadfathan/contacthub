from bson import ObjectId
from flask import Response, request, jsonify, current_app,send_from_directory,render_template,make_response
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField
import wtforms_json

# project resources
from models import Feed,Customer, Settings,Credit,Token
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram
from libraries import Firebase
import json 
from helpers import lang 
from datetime import datetime
from werkzeug.utils import secure_filename
import os
import ntpath
import shutil
import html
import html2text
import traceback
wtforms_json.init()

class CreateFeedForm(Form):
    Title = StringField('Title', [validators.required()])
    Description = StringField('Description', [validators.required()])
    PhoneNo = StringField('PhoneNo', [validators.required()])
    
class UpdateFeedForm(Form):
    FeedId = StringField('FeedId', [validators.required()])
    Title = StringField('Title', [validators.required()])
    Description = StringField('Description', [validators.required()])
    PhoneNo = StringField('PhoneNo', [validators.required()])

class DeleteFeedForm(Form):
    FeedId = StringField('FeedId', [validators.required()])

class UpdateStatusFeedForm(Form):
    FeedId = StringField('FeedId', [validators.required()])
    Status = SelectField('Status',choices=('draft','pending','publish','unpublish','reject'))


class GetFile(Resource):
    def get(self,filename) -> Response:
        try:
            telegram = Telegram()
            head, tail = os.path.split(filename)

            default_config = current_app.config
            return send_from_directory(os.path.join(default_config['UPLOAD_FOLDER'],head),tail)
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e))
class GetFeed(Resource):
    def get(self,FeedId:str) -> Response:
        try:
            telegram = Telegram()
            feed = Feed.get_paginate(self,**{'page' : 1,'numberPage' : 1,'filter' : {'_id' : ObjectId(FeedId),'Status' : 'publish','IsApproved' : True} })

            if 'total' in feed and feed['total'] > 0:
                feed = feed['data'][0]
                title = feed['Title']
                content = feed['Description']
                author = feed['User']['Nickname']
                createdAt = feed['CreatedAt']
                headers = {'Content-Type': 'text/html'}
                content = html.unescape(content)

                return make_response(render_template('article.html',title=title,content=content,date=createdAt,author=author),200,headers)
            else:
                headers = {'Content-Type': 'text/html'}
                return make_response(render_template('not_found.html'),200,headers)
            
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e))

class FeedAdminStatus(Resource):
    def put(self) -> Response:
        def allowed_file(filename):
            return '.' in filename and \
                filename.rsplit('.', 1)[1].lower() in default_config['ALLOWED_EXTENSIONS']
        try:
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

            form = UpdateStatusFeedForm(request.form)
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

            body = request.form
            
            raw_body = body.to_dict()

            raw_body['UpdatedAt'] = datetime.now
            raw_body['UpdatedBy'] = UserId
            if 'Status' in raw_body and raw_body['Status'] == 'publish':
                raw_body['IsApproved'] = True
            elif 'Status' in raw_body and raw_body['Status'] == 'reject':
                raw_body['IsApproved'] = False
            
            FeedId = raw_body['FeedId']
            del raw_body['FeedId']
            result = Feed.update(self,FeedId,**raw_body)
            
            if raw_body['IsApproved'] == True:
                msg_feed = 'telah disetujui. Sekaras Ads Feed sudah diterbitkan.'
            else:
                msg_feed = 'ditolak untuk diterbitkan, lihat selengkapnya di Menu Ads Feed'
                
            get_firebase_token = Token.objects(UserId=result['CreatedBy'],Status=1,Platform='android').first()
            if not get_firebase_token is None and 'FirebaseToken' in get_firebase_token and \
                not get_firebase_token['FirebaseToken'] is None:
                # print(get_firebase_token_mycontact['FirebaseToken'])
                raw_firebase = {
                    'registration_ids' : [get_firebase_token['FirebaseToken']],
                    'data_message' : {
                        'action' : 'required_sync'
                    },
                    'message_body' : 'Ads Feed anda %s' % msg_feed,
                    'message_title' : 'Ads Feed'
                }
                firebase = Firebase()
                firebase.send_notif_multiple(**raw_firebase)

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
class FeedAdmin(Resource):
    def post(self) -> Response:
        def allowed_file(filename):
            return '.' in filename and \
                filename.rsplit('.', 1)[1].lower() in default_config['ALLOWED_EXTENSIONS']
        try:
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

            form = CreateFeedForm(request.form)
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
            if 'Status' in raw_body:
                raw_body['Status'] = 'pending'

            if 'dir' in raw_body:
                del raw_body['dir']
            
            if 'file' in request.files:
                raw_body['Image'] = os.path.join(dir_name,filename)
            raw_body['CreatedAt'] = datetime.now
            raw_body['CreatedBy'] = UserId
            if 'Tag' in raw_body and not raw_body['Tag'] == "":
                raw_body['Tag'] = raw_body['Tag'].split(";")

            result = Feed.create(self,**raw_body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
    
    def put(self) -> Response:
        def allowed_file(filename):
            return '.' in filename and \
                filename.rsplit('.', 1)[1].lower() in default_config['ALLOWED_EXTENSIONS']
        try:
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

            form = UpdateFeedForm(request.form)
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

            body = request.form
            dir_name = ""
            UploadFolder = default_config['UPLOAD_FOLDER']
            if 'dir' in body:
                UploadFolder = os.path.join(UploadFolder,body['dir'])
                if not os.path.exists(UploadFolder):
                    os.makedirs(UploadFolder)
                dir_name = body['dir']
            
            raw_body = body.to_dict()

            if 'dir' in raw_body:
                del raw_body['dir']

            if 'file' not in request.files:
                pass
            else:
                file = request.files['file']
                # if user does not select file, browser also
                # submit an empty part without filename
                if file.filename == '':
                    return response.bad_request(message=lang(key='notif_select_file',filename=language))
                if file and allowed_file(file.filename):
                    filename = secure_filename(file.filename)
                    file.save(os.path.join(UploadFolder, filename))
                raw_body['Image'] = os.path.join(dir_name,filename)
            raw_body['UpdatedAt'] = datetime.now
            FeedId = raw_body['FeedId']
            del raw_body['FeedId']
            result = Feed.update(self,FeedId,**raw_body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
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
            
            form = DeleteFeedForm().from_json(request.json)
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
            
            body['id'] = ObjectId(body['FeedId'])
            del body['FeedId']

            result = Feed.delete(self,**body)
            
            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
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

            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            if ('page' in body and (body['page'] is None or not type(body['page']) is int)) or (not 'page' in body):
                body['page'] = 1
            
            if ('numberPage' in body and (body['numberPage'] is None or not type(body['numberPage']) is int)) or (not 'numberPage' in body):
                body['numberPage'] = 10
            
            
            result = Feed.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class FeedCustomer(Resource):
    def post(self) -> Response:
        def allowed_file(filename):
            return '.' in filename and \
                filename.rsplit('.', 1)[1].lower() in default_config['ALLOWED_EXTENSIONS']
        try:
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

            form = CreateFeedForm(request.form)
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
            raw_body['Status'] = 'pending'

            if 'dir' in raw_body:
                del raw_body['dir']
            
            if 'file' in request.files:
                raw_body['Image'] = os.path.join(dir_name,filename)
            raw_body['CreatedAt'] = datetime.now
            raw_body['CreatedBy'] = UserId
            if 'Tag' in raw_body and not raw_body['Tag'] == "":
                raw_body['Tag'] = raw_body['Tag'].split(";")

            result = Feed.create(self,**raw_body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
    
    def put(self) -> Response:
        def allowed_file(filename):
            return '.' in filename and \
                filename.rsplit('.', 1)[1].lower() in default_config['ALLOWED_EXTENSIONS']
        try:
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

            form = UpdateFeedForm(request.form)
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

            body = request.form
            dir_name = ""
            UploadFolder = default_config['UPLOAD_FOLDER']
            if 'dir' in body:
                UploadFolder = os.path.join(UploadFolder,body['dir'])
                if not os.path.exists(UploadFolder):
                    os.makedirs(UploadFolder)
                dir_name = body['dir']
            
            raw_body = body.to_dict()

            if 'dir' in raw_body:
                del raw_body['dir']

            if 'file' not in request.files:
                pass
            else:
                file = request.files['file']
                # if user does not select file, browser also
                # submit an empty part without filename
                if file.filename == '':
                    return response.bad_request(message=lang(key='notif_select_file',filename=language))
                if file and allowed_file(file.filename):
                    filename = secure_filename(file.filename)
                    file.save(os.path.join(UploadFolder, filename))
                raw_body['Image'] = os.path.join(dir_name,filename)
            raw_body['UpdatedAt'] = datetime.now
            raw_body['UpdatedBy'] = UserId
            FeedId = raw_body['FeedId']
            del raw_body['FeedId']
            if 'Status' in raw_body and (raw_body['Status'] == 'pending' or raw_body['Status'] == 'reject'):
                del raw_body['Status']
            
            cari_feed = Feed.get_paginate(self,**{ 'page' : 1,'numberPage' : 1, 'filter' : {
                '_id' : ObjectId(FeedId),
                'CreatedBy' : UserId
            }})
            if 'total' in cari_feed and cari_feed['total'] > 0:
                if cari_feed['data'][0]['IsApproved'] == False:
                    if 'Status' in raw_body:
                        del raw_body['Status']

                result = Feed.update_by_customer(self,FeedId,UserId,**raw_body)
                
                return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
            
            return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_update_failed',filename=language)})
            
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
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

            body = request.args
            if not 'FeedId' in body:
                messages = lang(key='notif_missing_field_header',filename=language)
                return response.bad_request(message=messages)
                
            result = Feed.delete(self,**{'id' : ObjectId(body['FeedId'])})
            
            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
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

            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            if ('page' in body and (body['page'] is None or not type(body['page']) is int)) or (not 'page' in body):
                body['page'] = 1
            
            if ('numberPage' in body and (body['numberPage'] is None or not type(body['numberPage']) is int)) or (not 'numberPage' in body):
                body['numberPage'] = 10
            
            if not 'filter' in body:
                body['filter'] = {}
            
            body['filter']['CreatedBy'] = UserId
            result = Feed.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class FeedPublicCustomerApi(Resource):
    def post(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            UserId = None
            
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
            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            if ('page' in body and (body['page'] is None or not type(body['page']) is int)) or (not 'page' in body):
                body['page'] = 1
            
            if ('numberPage' in body and (body['numberPage'] is None or not type(body['numberPage']) is int)) or (not 'numberPage' in body):
                body['numberPage'] = 10
            
            if 'filter' in body and type(body['filter']) is dict:
                body['filter']['Status'] = 'publish'
            else:
                body['filter'] = {
                    'Status' : 'publish',
                    'IsApproved' : True
                }
            if not UserId is None:
                customer = Customer.objects(UserId=UserId).first()
                if customer is None:
                    return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
                
                if customer['InterestId'] is None:
                    customer['InterestId'] = []
                    
                if not customer is None:
                    body['filter']['Category'] = {
                        "$in" : customer['InterestId']
                    }
            
            
            result = Feed.get_paginate(self,**body)
            for x in result['data']:
                x['Url'] = "%s%s" % (default_config['BASE_URL_FEED'],str(x['FeedId']))
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
        

class ClickAdsFeed(Resource):
    def get(self,FeedId:str) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()

            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            

            telegram = Telegram()
            feed = Feed.get_paginate(self,**{'page' : 1,'numberPage' : 1,'filter' : {'_id' : ObjectId(FeedId),'Status' : 'publish','IsApproved' : True} })


            settings = Settings.objects(_id='FEE_CLICK_ADS_FEED').first()
            if settings is None:
                Price = int(0)
            else:
                Price = int(settings['Value'])

            if 'total' in feed and feed['total'] > 0:
                feed = feed['data'][0]
                phoneNo = feed['PhoneNo']
                
                if Price > 0:

                    # cek kredit
                    # jika kredit tidak memenuhi maka kirim pesan feed tidak ditemukan
                    balance = Credit.balance(self,**{'UserId' : feed['CreatedBy'],'Status' : 'success'})
                    if len(balance) > 0:
                        balance = balance[0]
                        if balance['Balance'] <= 0 or balance['Balance'] < Price:
                            return jsonify({'result': [],'status':'FAILED','message': 'Untuk sementara Pemilik Feed tidak bisa dihubungi'})    
                    
                    credit = {}
                    credit['UserId'] = feed['CreatedBy']
                    credit['CreatedAt'] = datetime.now
                    credit['CreatedBy'] = feed['CreatedBy']
                    credit['Tag'] = 'click_ads_feed'
                    credit['Kredit'] = 0
                    credit['Debit'] = Price
                    credit['Description'] = "Click Ads Feed '%s'" % feed['Title']
                    credit['RefNo'] = ''
                    credit['Status'] = 'success'

                    result_credit = Credit.create(self,**credit) 
                    if result_credit == False:
                        return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_update_failed',filename=language)})

                return jsonify({'result': {
                    'PhoneNo' : phoneNo
                },'status':'OK','message': lang(key='notif_success',filename=language)})
            else:
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_feed_not_found',filename=language)})
            
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e))


class ListFeedPublicCustomerApi(Resource):
    def get(self,CustomerId:str) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            
            customer = Customer.objects(id=ObjectId(CustomerId)).first()
            if customer is None:
                customer = Customer.objects(UserId=CustomerId).first()
                
            body = {}
            
            if ('page' in body and (body['page'] is None or not type(body['page']) is int)) or (not 'page' in body):
                body['page'] = 1
            
            if ('numberPage' in body and (body['numberPage'] is None or not type(body['numberPage']) is int)) or (not 'numberPage' in body):
                body['numberPage'] = 1000
            
            if 'filter' in body and type(body['filter']) is dict:
                body['filter']['Status'] = 'publish'
            else:
                body['filter'] = {
                    'CreatedBy' : customer['UserId'],
                    'Status' : 'publish',
                    'IsApproved' : True
                }
            
            result = Feed.get_paginate(self,**body)
            for x in result['data']:
                x['Url'] = "%s%s" % (default_config['BASE_URL_FEED'],str(x['FeedId']))
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Feed.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
        