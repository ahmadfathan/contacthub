from bson import ObjectId
from flask import Response, request, jsonify, current_app
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField
import wtforms_json

# project resources
from models import Contact,Customer, Token,Settings,Credit
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram,RabbitMQ, Firebase
import json 
from helpers import lang 
from datetime import datetime
import traceback
wtforms_json.init()

class CreateContactForm(Form):
    UserId = StringField('UserId', [validators.required()])
    CustomerId = StringField('CustomerId', [validators.required()])
    
class DeleteContactForm(Form):
    ContactId = StringField('ContactId', [validators.required()])
    
class ContactApi(Resource):
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

            form = CreateContactForm().from_json(request.json)
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
            body['CreatedAt'] = datetime.now
            result = Contact.create(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
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
            
            form = DeleteContactForm().from_json(request.json)
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
            

            result = Contact.delete(self,ContactId=body['ContactId'])
            
            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
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

            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            if ('page' in body and (body['page'] is None or not type(body['page']) is int)) or (not 'page' in body):
                body['page'] = 1
            
            if ('numberPage' in body and (body['numberPage'] is None or not type(body['numberPage']) is int)) or (not 'numberPage' in body):
                body['numberPage'] = 10
            
            result = Contact.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class ContactSummaryApi(Resource):
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

            CustomerId = str(Customer.objects(UserId=UserId).first()['id'])
            result = Contact.get_summary(self,UserId=UserId,CustomerId=CustomerId)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class ContactListSaveApi(Resource):
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

            body['filter'] = {
                'UserId' : UserId
            }
            result = Contact.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class ContactListShareApi(Resource):
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

            CustomerId = str(Customer.objects(UserId=UserId).first()['id'])
            body['filter'] = {
                'CustomerId' : CustomerId
            }
            result = Contact.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class BroadcastContactSaveApi(Resource):
    def post(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()

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
            
            list_contact_save = Contact.objects(UserId=UserId).all()
            if not list_contact_save is None:
                rabbit = RabbitMQ()
                for contact in list_contact_save:
                    friend_profile = Customer.get_data(self,**{'CustomerId' : contact['CustomerId']})[0]
                    friend_profile['ContactId'] = str(contact.id)
                    rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="save_contact_%s" % UserId,data=friend_profile)
                rabbit.close()
            
            my_profile = Customer.get_data(self,**{'UserId' : UserId})
            if len(my_profile) > 0:
                my_profile = my_profile[0]
                list_contact_share = Contact.objects(CustomerId=my_profile['CustomerId']).all()
                if not list_contact_share is None:
                    rabbit = RabbitMQ()
                    for contact in list_contact_share:
                        IsSaved = "0"
                        friend_profile = Customer.get_data(self,**{'UserId' : contact['UserId']})[0]
                        friend_profile['ContactId'] = str(contact.id)

                        customer_di_kontak_saya = Contact.objects(UserId=UserId,CustomerId=friend_profile['CustomerId']).first()
                        if not customer_di_kontak_saya is None:
                            IsSaved = "1"
                        
                        friend_profile['IsSaved'] = IsSaved
                        rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="share_contact_%s" % UserId,data=friend_profile)
                                        
                    rabbit.close()
            return jsonify({'result': {
                'contact_share' : len(list_contact_share),
                'contact_save' : len(list_contact_save)
            },'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class ContactDetailApi(Resource):
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

            result = Customer.get_data(self,**{'CustomerId' : body['CustomerId']})
            if len(result) > 0:
                result = result[0]

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

# save manual
class SaveContactManualApi(Resource):
    def post(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()

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

            # get Customer yang memiliki credit dan kuota masih tersedia
            Price = 100
            
            settings = Settings.objects(_id='CREDIT_CONTACT').first()
            if settings is None:
                Price = int(0)
            else:
                Price = int(settings['Value'])

            body = request.get_json()
            CustomerId = body['CustomerId']

            balance = Credit.balance(self,**{'UserId' : UserId,'Status' : 'success'})
            if len(balance) > 0:
                balance = balance[0]
                if balance['Balance'] <= 0 or balance['Balance'] < Price:
                    return jsonify({'result': [],'status':'FAILED','message': 'Saldo kredit tidak mencukupi, silahkan topup terlebih dahulu'})    
            
            list_contact_save = Contact.objects(UserId=UserId,CustomerId=CustomerId).first()
            if list_contact_save is None:
                # simpan kontak
                my_profile = Customer.get_data(self,**{'UserId' : UserId})[0]
                friend_profile = Customer.get_data(self,**{'CustomerId' : CustomerId})[0]

                save_contact = Contact.create(self,UserId=UserId,CustomerId=CustomerId,\
                    Price=Price,UserIdCustomer=friend_profile['UserId'],FriendName=friend_profile['Name'],MyName=my_profile['Name'])

                if save_contact != False:
                    IsSaved = "0"
                    # push ke rabbitmq
                    # 1. push contact save
                    my_profile['ContactId'] = str(save_contact)
                    friend_profile['ContactId'] = str(save_contact)

                    customer_di_kontak_saya = Contact.objects(UserId=UserId,CustomerId=CustomerId).first()
                    if not customer_di_kontak_saya is None:
                        IsSaved = "1"

                    my_profile['IsSaved'] = IsSaved

                    rabbit = RabbitMQ()
                    rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="save_contact_%s" % UserId,data=friend_profile)
                    # 2. push contact share

                    
                    rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="share_contact_%s" % friend_profile['UserId'],data=my_profile)
                    rabbit.close()

                    # push ke firebase
                    get_firebase_token_mycontact = Token.objects(UserId=UserId,Status=1,Platform='android').first()
                    get_firebase_token_friends = Token.objects(UserId=friend_profile['UserId'],Status=1,Platform='android').first()
                    if not get_firebase_token_mycontact is None and 'FirebaseToken' in get_firebase_token_mycontact and \
                        not get_firebase_token_mycontact['FirebaseToken'] is None:
                        # print(get_firebase_token_mycontact['FirebaseToken'])
                        raw_firebase = {
                            'registration_ids' : [get_firebase_token_mycontact['FirebaseToken']],
                            'data_message' : {
                                'action' : 'required_sync'
                            },
                            'message_body' : 'Anda mendapatkan kontak baru %s' % friend_profile['Name'],
                            'message_title' : 'Kontak Baru'
                        }
                        firebase = Firebase()
                        firebase.send_notif_multiple(**raw_firebase)

                    if not get_firebase_token_friends is None and 'FirebaseToken' in get_firebase_token_friends and \
                        not get_firebase_token_friends['FirebaseToken'] is None:
                        raw_firebase = {
                            'registration_ids' : [get_firebase_token_friends['FirebaseToken']],
                            'data_message' : {
                                'action' : 'required_sync'
                            },
                            'message_body' : 'Kontak anda telah disimpan oleh User Lain (%s)' % my_profile['Name'],
                            'message_title' : 'Menyimpan Kontak'
                        }
                        firebase = Firebase()
                        firebase.send_notif_multiple(**raw_firebase)


            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

# Cronjob Private
class ContactSaveApi(Resource):
    def get(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()

            language = default_config['DEFAULT_LANGUAGE']
            
            # get Customer yang memiliki credit dan kuota masih tersedia
            Price = 100
            
            settings = Settings.objects(_id='CREDIT_CONTACT').first()
            if settings is None:
                Price = int(0)
            else:
                Price = int(settings['Value'])

            list_customer = Customer.AvailableCreditSave(self,balanceMinimum=Price)
            count_save = 0

            telegram.send(text='Running Auto Save Contact')
            for customer in list_customer:
                # cari kontak yang sesuai dengan type
                for ind in range(10):
                    is_saved = False
                    if 'Type' in customer['SaveContactFriendBy']:
                        contact = False
                        if customer['SaveContactFriendBy']['Type'] == 'interest':
                            interest_id = customer['SaveContactFriendBy']['InterestId']
                            if not interest_id is None:
                                if len(interest_id) > 0:
                                    # cari kontak yang interestnya sesuai, belum ada dikontak sendiri 
                                    # dan saldo kontak yang mau disimpan balance.nya tersedia
                                    contact = Customer.get_new_contact(self,UserId=customer['UserId'],BalanceMinimum=Price,Type="interest",Value=interest_id)
                        elif customer['SaveContactFriendBy']['Type'] == 'random':
                            contact = Customer.get_new_contact(self,UserId=customer['UserId'],BalanceMinimum=Price,Type="random",Value=None)
                        elif customer['SaveContactFriendBy']['Type'] == 'other':
                            contact = Customer.get_new_contact(self,UserId=customer['UserId'],BalanceMinimum=Price,Type="other",\
                                Key=customer['SaveContactFriendBy']['OtherKey'],Value=customer['SaveContactFriendBy']['OtherValue'])

                        if contact == False or len(contact) == 0:
                            pass
                        else:
                            user_id_customer = contact['UserId']

                            
                            my_profile = Customer.get_data(self,**{'CustomerId' : customer['CustomerId']})[0]
                            friend_profile = Customer.get_data(self,**{'CustomerId' : str(contact['id'])})[0]
                            save_contact = Contact.create(self,UserId=customer['UserId'],CustomerId=str(contact['id']),\
                                Price=Price,UserIdCustomer=user_id_customer,FriendName=friend_profile['Name'],MyName=my_profile['Name'])
                            print(save_contact)
                            if save_contact != False:
                                IsSaved = "0"
                                is_saved = True
                                count_save = count_save + 1
                                # push ke rabbitmq
                                # 1. push contact save
                                my_profile['ContactId'] = str(save_contact)
                                friend_profile['ContactId'] = str(save_contact)

                                customer_di_kontak_saya = Contact.objects(UserId=user_id_customer,CustomerId=my_profile['CustomerId']).first()
                                if not customer_di_kontak_saya is None:
                                    IsSaved = "1"

                                my_profile['IsSaved'] = IsSaved

                                rabbit = RabbitMQ()
                                rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="save_contact_%s" % customer['UserId'],data=friend_profile)
                                # 2. push contact share

                                
                                rabbit.send_topic(exhange="guestapk",exchange_type="topic",routing_key="share_contact_%s" % user_id_customer,data=my_profile)
                                rabbit.close()

                                # push ke firebase
                                get_firebase_token_mycontact = Token.objects(UserId=customer['UserId'],Status=1,Platform='android').first()
                                get_firebase_token_friends = Token.objects(UserId=contact['UserId'],Status=1,Platform='android').first()
                                if not get_firebase_token_mycontact is None and 'FirebaseToken' in get_firebase_token_mycontact and \
                                    not get_firebase_token_mycontact['FirebaseToken'] is None:
                                    # print(get_firebase_token_mycontact['FirebaseToken'])
                                    raw_firebase = {
                                        'registration_ids' : [get_firebase_token_mycontact['FirebaseToken']],
                                        'data_message' : {
                                            'action' : 'required_sync'
                                        },
                                        'message_body' : 'Anda mendapatkan kontak baru %s' % friend_profile['Name'],
                                        'message_title' : 'Kontak Baru'
                                    }
                                    firebase = Firebase()
                                    firebase.send_notif_multiple(**raw_firebase)

                                if not get_firebase_token_friends is None and 'FirebaseToken' in get_firebase_token_friends and \
                                    not get_firebase_token_friends['FirebaseToken'] is None:
                                    raw_firebase = {
                                        'registration_ids' : [get_firebase_token_friends['FirebaseToken']],
                                        'data_message' : {
                                            'action' : 'required_sync'
                                        },
                                        'message_body' : 'Kontak anda telah disimpan oleh User Lain (%s)' % my_profile['Name'],
                                        'message_title' : 'Menyimpan Kontak'
                                    }
                                    firebase = Firebase()
                                    firebase.send_notif_multiple(**raw_firebase)

                    if is_saved == False:
                        break
            res = {
                'result': {
                    'CustomerReady' : len(list_customer),
                    'CustomerSave' : count_save,
                },
                'status':'OK',
                'message': lang(key='notif_success',filename=language)
            }

            telegram.send(text='Finish Auto Save Contact.\n\nTotal Customer : %s \nTotal Save : %s' % (str(len(list_customer)),count_save))
            return jsonify(res)
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Contact.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
