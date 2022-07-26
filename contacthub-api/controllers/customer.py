from bson import ObjectId
from flask import Response, request, jsonify, current_app,send_from_directory
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField,BooleanField
import wtforms_json

# project resources
from models import Customer,Settings,Affiliate,Credit,Token
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram,RabbitMQ,Firebase
import json 
from helpers import lang,sha1 
from datetime import datetime
import base64
from werkzeug.utils import secure_filename
import os
import ntpath
import traceback
wtforms_json.init()

class LoginForm(Form):
    Email = StringField('Email', [validators.required()])
    Password = StringField('Password', [validators.required()])
    Platform = StringField('Platform', [validators.required()])
    DeviceId = StringField('DeviceId', [validators.required()])

class RegisterForm(Form):
    Name = StringField('Name', [validators.required()])
    # Greeting = StringField('Greeting', [validators.required()])
    # Address = StringField('Address', [validators.required()])
    # CityId = StringField('CityId', [validators.required()])
    # WhatsApp = StringField('WhatsApp', [validators.required()])
    # Facebook = StringField('Facebook', [validators.required()])
    # Instagram = StringField('Instagram', [validators.required()])
    # Website = StringField('Website', [validators.required()])
    # Gender = StringField('Gender', [validators.required()])
    # DateOfBirth = StringField('DateOfBirth', [validators.required()])
    # ProfessionId = StringField('ProfessionId', [validators.required()])
    # Hoby = StringField('Hoby', [validators.required()])
    # Religion = StringField('Religion', [validators.required()])
    # InterestId = StringField('InterestId', [validators.required()])
    # RelationshipStatus = StringField('RelationshipStatus', [validators.required()])
    # BusinessName = StringField('BusinessName', [validators.required()])
    # BusinessTypeId = StringField('BusinessTypeId', [validators.required()])
    # Product = StringField('Product', [validators.required()])
    Nickname = StringField('Nickname', [validators.required()])
    Email = StringField('Email', [validators.required()])
    Password = StringField('Password', [validators.required()])
    Platform = StringField('Platform', [validators.required()])

class CreateCustomerForm(Form):
    IsOwner = BooleanField('IsOwner', [validators.required()])
    Name = StringField('Name', [validators.required()])
    Greeting = StringField('Greeting', [validators.required()])
    Address = StringField('Address', [validators.required()])
    CityId = StringField('CityId', [validators.required()])
    WhatsApp = StringField('WhatsApp', [validators.required()])
    Facebook = StringField('Facebook', [validators.required()])
    Instagram = StringField('Instagram', [validators.required()])
    Website = StringField('Website', [validators.required()])
    Gender = StringField('Gender', [validators.required()])
    DateOfBirth = StringField('DateOfBirth', [validators.required()])
    ProfessionId = StringField('ProfessionId', [validators.required()])
    Hoby = StringField('Hoby', [validators.required()])
    Religion = StringField('Religion', [validators.required()])
    # InterestId = StringField('InterestId', [validators.required()])
    RelationshipStatus = StringField('RelationshipStatus', [validators.required()])
    BusinessName = StringField('BusinessName', [validators.required()])
    BusinessTypeId = StringField('BusinessTypeId', [validators.required()])
    Product = StringField('Product', [validators.required()])
    Nickname = StringField('Nickname', [validators.required()])
    Email = StringField('Email', [validators.required()])
    Password = StringField('Password', [validators.required()])
    Platform = StringField('Platform', [validators.required()])

class DeleteCustomerForm(Form):
    CustomerId = StringField('CustomerId', [validators.required()])


class GetProfileImage(Resource):
    def get(self,dir_name,filename) -> Response:
        try:
            telegram = Telegram()
            head, tail = os.path.split(filename)

            default_config = current_app.config
            return send_from_directory(os.path.join(default_config['UPLOAD_FOLDER'],dir_name,head),tail)
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            return response.not_found(str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e))

class RemoveCoverApi(Resource):
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

            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            result = Customer.objects(UserId=UserId).update(set__CoverContact=None)
            
            return jsonify({'result': {"CoverContact" : None},'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
class RemoveFotoApi(Resource):
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

            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            result = Customer.objects(UserId=UserId).update(set__Foto=None)
            my_profile = Customer.get_data(self, **{'UserId' : UserId})[0]
            rabbit = RabbitMQ()
            rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="update_contact",data=my_profile)
            rabbit.close()
            return jsonify({'result': {"Foto" : None},'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class UpdateFotoApi(Resource):
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

            body = request.get_json()
            if not type(body) is dict:
                body = {}

            dir_name = UserId
            UploadFolder = default_config['UPLOAD_FOLDER']
            UploadFolder = os.path.join(UploadFolder,dir_name)
            if not os.path.exists(UploadFolder):
                os.makedirs(UploadFolder)
            

            filename = 'foto%s.png' % sha1(datetime.now().strftime('%s'))
            full_path = os.path.join(UploadFolder,filename)
            with open(full_path, 'wb') as fh:
                fh.write(base64.b64decode(body['Foto']))

            # remove last file
            data_customer = Customer.objects(UserId=UserId).first()
            if not data_customer is None:
                last_file = data_customer['Foto']
                if not last_file is None and last_file != "":
                    try:
                        filename_remove = os.path.join(default_config['UPLOAD_FOLDER'],last_file)
                        os.remove(filename_remove)
                    except OSError:
                        pass

            path_save = os.path.join(UserId,filename)
            result = Customer.objects(UserId=UserId).update(set__Foto=path_save)

            my_profile = Customer.get_data(self, **{'UserId' : UserId})[0]
            rabbit = RabbitMQ()
            rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="update_contact",data=my_profile)
            rabbit.close()

            return jsonify({'result': {"Foto" : path_save},'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class UpdateCoverApi(Resource):
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

            body = request.get_json()
            if not type(body) is dict:
                body = {}

            dir_name = UserId
            UploadFolder = default_config['UPLOAD_FOLDER']
            UploadFolder = os.path.join(UploadFolder,dir_name)
            if not os.path.exists(UploadFolder):
                os.makedirs(UploadFolder)
            

            filename = 'cover%s.png' % sha1(datetime.now().strftime('%s'))
            full_path = os.path.join(UploadFolder,filename)
            with open(full_path, 'wb') as fh:
                fh.write(base64.b64decode(body['CoverContact']))

            # remove last file
            data_customer = Customer.objects(UserId=UserId).first()
            if not data_customer is None:
                last_file = data_customer['CoverContact']
                if not last_file is None and last_file != "":
                    try:
                        filename_remove = os.path.join(default_config['UPLOAD_FOLDER'],last_file)
                        os.remove(filename_remove)
                    except OSError:
                        pass

            path_save = os.path.join(UserId,filename)
            result = Customer.objects(UserId=UserId).update(set__CoverContact=path_save)
            
            return jsonify({'result': {"CoverContact" : path_save},'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
class CustomerAllApi(Resource):
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
            
            result = Customer.get_data(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class CustomerApi(Resource):
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
            
            if ('page' in body and (body['page'] is None or not type(body['page']) is int)) or (not 'page' in body):
                body['page'] = 1
            
            if ('numberPage' in body and (body['numberPage'] is None or not type(body['numberPage']) is int)) or (not 'numberPage' in body):
                body['numberPage'] = 10
            
            
            result = Customer.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
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
            role_default = default_config['DEFAULT_ROLE']
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            form = CreateCustomerForm().from_json(request.json)
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

            settings = Settings.objects(_id='FEE_AFFILIATE').first()
            if settings is None:
                fee_affiliate = int(0)
            else:
                fee_affiliate = int(settings['Value'])

            body['RoleId'] = role_default['CUSTOMER']
            body['AccountId'] = None
            body['Email'] = str(body['Email']).strip()
            
            IsReferrer = False
            if 'ReferralCode' in body and not body['ReferralCode'] is None and not str(body['ReferralCode']).strip() == "":
                cari_marketingcode = Customer.objects(MarketingCode=body['ReferralCode']).first()
                if cari_marketingcode is None:
                    return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_referral_code_not_found',filename=language)})    
                IsReferrer = True

            
            result = Customer.register(self,**body)
            if result == 'username_already':
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_username_already',filename=language)})
            elif result == 'email_already':
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_email_already',filename=language)})

            if result != False: 

                if IsReferrer:
                    # create commission affiliate
                    create_affiliate = Affiliate.create(self,
                        UserId=result['UserId'],
                        ReferralCode=body['ReferralCode'],
                        Commission=fee_affiliate,
                        Tag='register',
                        CreatedAt=datetime.now,
                        CreatedBy=result['UserId'],
                        Paid=False
                    )
                    if not create_affiliate:
                        telegram.send_log(request=request,message='Failed Create Affiliate')
            else:
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_register_failed',filename=language)})

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
    def put(self) -> Response:
        try:
            default_config = current_app.config
            role_default = default_config['DEFAULT_ROLE']
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            body = request.get_json()
            if not type(body) is dict:
                body = {}
            data_customer = Customer.objects(id=ObjectId(body['CustomerId'])).get()

            result = Customer.update(self,data_customer['UserId'],**body)

            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
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
            role_default = default_config['DEFAULT_ROLE']
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            form = DeleteCustomerForm().from_json(request.json)
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
            CustomerId = body['CustomerId']
            result = Customer.delete(self,CustomerId=CustomerId)

            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
    
class CustomerRegisterApi(Resource):
    def post(self) -> Response:
        try:
            default_config = current_app.config
            role_default = default_config['DEFAULT_ROLE']
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            form = RegisterForm().from_json(request.json)
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

            settings = Settings.objects(_id='FEE_AFFILIATE').first()
            if settings is None:
                fee_affiliate = int(0)
            else:
                fee_affiliate = int(settings['Value'])


            settings = Settings.objects(_id='ADD_CREDIT_AFTER_REGISTER').first()
            if settings is None:
                free_kredit = int(0)
            else:
                free_kredit = int(settings['Value'])

            body['RoleId'] = role_default['CUSTOMER']
            body['IsOwner'] = True
            body['AccountId'] = None
            body['Email'] = str(body['Email']).strip()
            
            IsReferrer = False
            ReferralCode = ""
            DeviceId = body['DeviceId']
            del body['DeviceId']

            if 'ReferralCode' in body and not body['ReferralCode'] is None and not str(body['ReferralCode']).strip() == "":
                cari_marketingcode = Customer.objects(MarketingCode=body['ReferralCode']).first()
                if cari_marketingcode is None:
                    return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_referral_code_not_found',filename=language)})    
                IsReferrer = True
                ReferralCode = body['ReferralCode']

            if 'ReferralCode' in body:
                del body['ReferralCode']
            
            
            result = Customer.register(self,**body)
            if result == 'username_already':
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_username_already',filename=language)})
            elif result == 'email_already':
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_email_already',filename=language)})

            if result != False:
                if free_kredit > 0:
                    credit = {}
                    credit['UserId'] = result['UserId']
                    credit['CreatedAt'] = datetime.now
                    credit['CreatedBy'] = result['UserId']
                    credit['Tag'] = 'topup'
                    credit['Kredit'] = free_kredit
                    credit['Debit'] = 0
                    credit['Description'] = "Free Saldo Credit For New Register User"
                    credit['RefNo'] = ''
                    credit['Status'] = 'success'

                    result_credit = Credit.create(self,**credit) 

                if IsReferrer:
                    # create commission affiliate
                    create_affiliate = Affiliate.create(self,
                        UserId=cari_marketingcode['UserId'],
                        ReferralCode=ReferralCode,
                        Commission=fee_affiliate,
                        Tag='register',
                        CreatedAt=datetime.now,
                        CreatedBy=result['UserId'],
                        Paid=True
                    )
                    if not create_affiliate:
                        telegram.send_log(request=request,message='Failed Create Affiliate : ' + str(body))
                    else:
                        # paid register fee referal
                        if fee_affiliate > 0:
                            credit = {}
                            credit['UserId'] = cari_marketingcode['UserId']
                            credit['CreatedAt'] = datetime.now
                            credit['CreatedBy'] = result['UserId']
                            credit['Tag'] = 'fee_affiliate'
                            credit['Kredit'] = fee_affiliate
                            credit['Debit'] = 0
                            credit['Description'] = "Penambahan credit sebesar + Rp %s dari komisi affiliate user %s" % (fee_affiliate,result['Name'])
                            credit['RefNo'] = create_affiliate['AffiliateId']
                            credit['Status'] = 'success'

                            result_credit = Credit.create(self,**credit)
                            if not result_credit:
                                telegram.send_log(request=request,message='Failed Create Credit : ' + str(credit))
                            else:
                                # kirim notif 
                                get_firebase_token = Token.objects(UserId=cari_marketingcode['UserId'],Status=1,Platform='android').first()
                                if not get_firebase_token is None and 'FirebaseToken' in get_firebase_token:
                                    raw_firebase = {
                                        'registration_ids' : [get_firebase_token['FirebaseToken']],
                                        'data_message' : {
                                            'action' : 'fee_affiliate'
                                        },
                                        'message_body' : 'Kamu baru saja mendapatkan Komisi Credit sebesar Rp%s dari registrasi user baru %s' % (fee_affiliate,result['Name']),
                                        'message_title' : 'Komisi Referral'
                                    }
                                    
                                    if not raw_firebase is None:
                                        firebase = Firebase()
                                        firebase.send_notif_multiple(**raw_firebase)
                
                
                bind_keys1 = "save_contact_%s" % str(result['UserId'])
                bind_keys2 = "share_contact_%s" % str(result['UserId'])
                bind_keys3 = "update_contact"
                create_token = Customer.create_token(self,UserId=result['UserId'],Platform=body['Platform'],AccountId=result['AccountId'])
                if create_token:
                    result['Auth'] = create_token
                else:
                    result['Auth'] = None
                
                # declare queue
                bind_keys = []
                bind_keys.append({"exchange" : "guestapk","routing_key" : bind_keys1})
                bind_keys.append({"exchange" : "guestapk","routing_key" : bind_keys2})
                bind_keys.append({"exchange" : "guestapk","routing_key" : bind_keys3})
                rabbit = RabbitMQ()
                rabbit.declare_queue(DeviceId,*bind_keys)
                rabbit.close()
            else:
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_register_failed',filename=language)})

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class CustomerLoginApi(Resource):
    def post(self) -> Response:
        try:
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

            result = Customer.login(self,Username=body['Email'],Password=body['Password'],RoleId=role_default['CUSTOMER'])
            if result:
                bind_keys1 = "save_contact_%s" % str(result['UserId'])
                bind_keys2 = "share_contact_%s" % str(result['UserId'])
                bind_keys3 = "update_contact"
                create_token = Customer.create_token(self,UserId=result['UserId'],Platform=body['Platform'],AccountId=result['AccountId'])
                if create_token:
                    result['Auth'] = create_token
                else:
                    result['Auth'] = None

                # declare queue
                bind_keys = []
                bind_keys.append({"exchange" : "guestapk","routing_key" : bind_keys1})
                bind_keys.append({"exchange" : "guestapk","routing_key" : bind_keys2})
                bind_keys.append({"exchange" : "guestapk","routing_key" : bind_keys3})
                rabbit = RabbitMQ()
                rabbit.declare_queue(body['DeviceId'],*bind_keys)
                rabbit.close()
                return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
            else:
                return response.unauthorized()

        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class CustomerProfileApi(Resource):
    def put(self) -> Response:
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
            
            if 'AllowedShareProfile' in body and body['AllowedShareProfile'] == True:
                # check saldo, jika nol maka tidak bisa disimpan
                balance = Credit.balance(self,**{'UserId' : UserId,'Status' : 'success'})
                if len(balance) > 0:
                    balance = balance[0]
                    if balance['Balance'] <= 0:
                        return jsonify({'result': [],'status':'FAILED','message': 'Saldo kredit tidak mencukupi, silahkan topup terlebih dahulu'})    


            result = Customer.update(self,UserId,**body)

            if result == "email_already":
                return jsonify({'result': [],'status':'FAILED','message': 'Email sudah terdaftar'})    
            elif result == False:
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_update_failed',filename=language)})
            
            my_profile = result
            # broadcast ke rabbitmq
            if len(body) == 1 and 'AllowedShareProfile' in body:
                pass
            else:
                rabbit = RabbitMQ()
                rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="update_contact",data=my_profile)
                rabbit.close()
                
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class UplineProfileApi(Resource):
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
            
            
            upline_id = Affiliate.objects(CreatedBy=body['UserId'],Tag='register').first()
            result = []
            if not upline_id is None:
                result = Customer.get_data(self,**{'MarketingCode' : upline_id['ReferralCode']})

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class DownlineApi(Resource):
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
            
            result = Affiliate.get_downline_paginate(self,**body)

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Customer.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 