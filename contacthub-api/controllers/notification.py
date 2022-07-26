from bson import ObjectId
from flask import Response, request, jsonify, current_app
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField
import wtforms_json

# project resources
from models import Notification,Token,Article
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram,Firebase
import json 
from helpers import lang 
from datetime import datetime
import traceback
wtforms_json.init()

class CreateNotificationForm(Form):
    Name = StringField('Name', [validators.required()])
    Title = StringField('Title', [validators.required()])
    Body = StringField('Body', [validators.required()])
    Type = StringField('Type', [validators.required()])

class SendNotificationForm(Form):
    NotificationId = StringField('NotificationId', [validators.required()])

class DeleteNotificationForm(Form):
    NotificationId = StringField('NotificationId', [validators.required()])

class NotificationSendApi(Resource):
    def post(self) -> Response:
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


            form = SendNotificationForm().from_json(request.json)
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

            registration_ids = []
            result = Notification.get_data(self,**{'NotificationId' : body['NotificationId']})
            if len(result) > 0:
                result = result[0]
                if 'ReceiverUser' in body and type(body['ReceiverUser']) is list and len(body['ReceiverUser']) > 0:
                    token_firebase = Token.objects(UserId__in=body['ReceiverUser'],Status=1).all()
                    for token in token_firebase:
                        registration_ids.append(token.FirebaseToken)
                    
                else:
                    receiver_user = []
                    token_firebase = Token.objects(Status=1).all()
                    for token in token_firebase:
                        if not token.FirebaseToken is None:
                            registration_ids.append(token.FirebaseToken)
                
                get_article = Article.objects(id=ObjectId(result['ArticleId'])).first()
                articleTitle = ""
                articleUrl = ""
                if not get_article is None:
                    articleTitle = get_article['Title']
                    articleUrl = "%s%s" % (default_config['BASE_URL_ARTICLE'],str(get_article['Slug']))
                    

                raw_firebase = {
                    'registration_ids' : registration_ids,
                    'data_message' : {
                        'action' : result['Type'],
                        'ArticleTitle' : articleTitle,
                        'ArticleUrl' : articleUrl,
                        'Link' : result['Link'],
                    },
                    'message_body' : result['Body'],
                    'message_title' : result['Title']
                }
                # send notif
                firebase = Firebase()
                firebase.send_notif_multiple(**raw_firebase)

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Notification.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
class NotificationApi(Resource):
    def post(self) -> Response:
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


            form = CreateNotificationForm().from_json(request.json)
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
            
            body['CreatedBy'] = UserId

            registration_ids = []
            result = Notification.create(self,**body)
            if result != False:
                if 'ReceiverUser' in body and type(body['ReceiverUser']) is list and len(body['ReceiverUser']) > 0:
                    token_firebase = Token.objects(UserId__in=body['ReceiverUser'],Status=1).all()
                    for token in token_firebase:
                        registration_ids.append(token.FirebaseToken)
                else:
                    receiver_user = []
                    token_firebase = Token.objects(Status=1).all()
                    for token in token_firebase:
                        if not token.FirebaseToken is None:
                            registration_ids.append(token.FirebaseToken)

                get_article = Article.objects(id=ObjectId(result['ArticleId'])).first()
                articleTitle = ""
                articleUrl = ""
                if not get_article is None:
                    articleTitle = get_article['Title']
                    articleUrl = "%s%s" % (default_config['BASE_URL_ARTICLE'],str(get_article['Slug']))

                raw_firebase = {
                    'registration_ids' : registration_ids,
                    'data_message' : {
                        'action' : result['Type'],
                        'ArticleTitle' : articleTitle,
                        'ArticleUrl' : articleUrl,
                        'Link' : result['Link'],
                    },
                    'message_body' : result['Body'],
                    'message_title' : result['Title']
                }

                # send notif
                firebase = Firebase()
                firebase.send_notif_multiple(**raw_firebase)

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Notification.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
    def post(self) -> Response:
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


            form = CreateNotificationForm().from_json(request.json)
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
            
            body['CreatedBy'] = UserId

            registration_ids = []
            result = Notification.create(self,**body)
            if result != False:
                if 'ReceiverUser' in body and type(body['ReceiverUser']) is list and len(body['ReceiverUser']) > 0:
                    token_firebase = Token.objects(UserId__in=body['ReceiverUser'],Status=1).all()
                    for token in token_firebase:
                        registration_ids.append(token.FirebaseToken)
                else:
                    receiver_user = []
                    token_firebase = Token.objects(Status=1).all()
                    for token in token_firebase:
                        if not token.FirebaseToken is None:
                            registration_ids.append(token.FirebaseToken)

                get_article = Article.objects(id=ObjectId(result['ArticleId'])).first()
                articleTitle = ""
                articleUrl = ""
                if not get_article is None:
                    articleTitle = get_article['Title']
                    articleUrl = "%s%s" % (default_config['BASE_URL_ARTICLE'],str(get_article['Slug']))

                raw_firebase = {
                    'registration_ids' : registration_ids,
                    'data_message' : {
                        'action' : result['Type'],
                        'ArticleTitle' : articleTitle,
                        'ArticleUrl' : articleUrl,
                        'Link' : result['Link'],
                    },
                    'message_body' : result['Body'],
                    'message_title' : result['Title']
                }

                # send notif
                firebase = Firebase()
                firebase.send_notif_multiple(**raw_firebase)

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Notification.DoesNotExist as e:
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
            
            form = DeleteNotificationForm().from_json(request.json)
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
            
            body['id'] = ObjectId(body['NotificationId'])
            del body['NotificationId']

            result = Notification.delete(self,**body)
            
            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Notification.DoesNotExist as e:
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
            
            result = Notification.get_data(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Notification.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class NotificationPaginateApi(Resource):
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
            
            result = Notification.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Notification.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 