from bson import ObjectId
from flask import Response, request, jsonify, current_app,send_from_directory,render_template,make_response
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField
import wtforms_json

# project resources
from models import Article,Customer
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram
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

class CreateArticleForm(Form):
    Title = StringField('Title', [validators.required()])
    Description = StringField('Description', [validators.required()])
    
class UpdateArticleForm(Form):
    ArticleId = StringField('ArticleId', [validators.required()])
    Title = StringField('Title', [validators.required()])
    Description = StringField('Description', [validators.required()])

class DeleteArticleForm(Form):
    ArticleId = StringField('ArticleId', [validators.required()])


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
class GetArticle(Resource):
    def get(self,slug) -> Response:
        try:
            telegram = Telegram()
            article = Article.get_data(self,**{'Slug' : slug})
            if len(article) > 0:
                article = article[0]
                title = article['Title']
                content = article['Description']
                author = article['User']['Nickname']
                createdAt = article['CreatedAt']
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
class ArticleApi(Resource):
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

            form = CreateArticleForm(request.form)
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
            if 'dir' in raw_body:
                del raw_body['dir']
            
            if 'file' in request.files:
                raw_body['Image'] = os.path.join(dir_name,filename)
            raw_body['CreatedAt'] = datetime.now
            raw_body['CreatedBy'] = UserId
            if 'Tag' in raw_body and not raw_body['Tag'] == "":
                raw_body['Tag'] = raw_body['Tag'].split(";")

            result = Article.create(self,**raw_body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
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

            form = UpdateArticleForm(request.form)
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
            ArticleId = raw_body['ArticleId']
            del raw_body['ArticleId']
            result = Article.update(self,ArticleId,**raw_body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
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
            
            form = DeleteArticleForm().from_json(request.json)
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
            
            body['id'] = ObjectId(body['ArticleId'])
            del body['ArticleId']

            result = Article.delete(self,**body)
            
            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
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
            
            
            result = Article.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class ArticlePublicApi(Resource):
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
            if 'X-Auth-UserId' in headers and type(headers['X-Auth-UserId']) is str and not headers['X-Auth-UserId'].strip() == "":
                UserId = headers['X-Auth-UserId']

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
                    'Status' : 'publish'
                }
            if not UserId is None:
                customer = Customer.objects(UserId=UserId).first()
                if not customer is None:
                    body['filter']['Category'] = {
                        "$in" : customer['InterestId']
                    }
            result = Article.get_paginate(self,**body)
            for x in result['data']:
                x['Url'] = "%s%s" % (default_config['BASE_URL_ARTICLE'],str(x['Slug']))
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class ArticlePublicApi(Resource):
    def post(self) -> Response:
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
            
            if ('page' in body and (body['page'] is None or not type(body['page']) is int)) or (not 'page' in body):
                body['page'] = 1
            
            if ('numberPage' in body and (body['numberPage'] is None or not type(body['numberPage']) is int)) or (not 'numberPage' in body):
                body['numberPage'] = 10
            
            if 'filter' in body and type(body['filter']) is dict:
                body['filter']['Status'] = 'publish'
            else:
                body['filter'] = {
                    'Status' : 'publish'
                }
                
            result = Article.get_paginate(self,**body)
            for x in result['data']:

                content = x['Description']
                h = html2text.HTML2Text()
                x['Url'] = "%s%s" % (default_config['BASE_URL_ARTICLE'],str(x['Slug']))
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class ArticlePublicCustomerApi(Resource):
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
                    'Status' : 'publish'
                }
            if not UserId is None:
                customer = Customer.objects(UserId=UserId).first()
                if customer['InterestId'] is None:
                    customer['InterestId'] = []
                
                customer['InterestId'].append(None)
                customer['InterestId'].append("")

                if not customer is None:
                    body['filter']['Category'] = {
                        "$in" : customer['InterestId']
                    }
            
            
            result = Article.get_paginate(self,**body)
            for x in result['data']:
                x['Url'] = "%s%s" % (default_config['BASE_URL_ARTICLE'],str(x['Slug']))
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
            
class ArticleAll(Resource):
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
            
            result = Article.get_data(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Article.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 