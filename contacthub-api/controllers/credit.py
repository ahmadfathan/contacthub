from bson import ObjectId
from flask import Response, request, jsonify, current_app
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,IntegerField,SelectField
import wtforms_json

# project resources
from models import Credit,Affiliate,Settings,Customer,Token
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram, Firebase
import json 
from helpers import lang 
from datetime import datetime
import locale
import traceback
locale.setlocale(locale.LC_ALL, '')

wtforms_json.init()

class CreateTagCreditForm(Form):
    Amount = IntegerField('Amount', [validators.required()])

class CreateCreditForm(Form):
    Tag = SelectField('Tag', choices=('topup','withdraw','fee_affiliate','fee_other'))
    UserId = StringField('UserId', [validators.required()])
    Amount = IntegerField('Amount', [validators.required()])

class RequestWithdrawCreditForm(Form):
    WithdrawBank = StringField('WithdrawBank', [validators.required()])
    WithdrawNoRek = StringField('WithdrawNoRek', [validators.required()])
    WithdrawPemilikRek = StringField('WithdrawPemilikRek', [validators.required()])

class UpdateCreditForm(Form):
    Tag = SelectField('Tag', [validators.optional()],choices=('topup','withdraw','fee_affiliate','fee_other'))
    CreditId = StringField('CreditId', [validators.required()])

class DeleteCreditForm(Form):
    CreditId = StringField('CreditId', [validators.required()])

class TotalAmountWithdrawApi(Resource):
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


            result_topup_paid_pending = Credit.get_sum_withdraw_pending(self,UserId=UserId)
            result_topup_all = Affiliate.get_summary(self,**{'UserId' : UserId,'Tag':'topup'})
            if len(result_topup_paid_pending) > 0:
                result_topup_paid_pending = result_topup_paid_pending[0]['totalAmount']
            else:
                result_topup_paid_pending = 0
            
            if len(result_topup_all) > 0:
                result_topup_all = result_topup_all[0]['totalAmount']
            else:
                result_topup_all = 0
            
            return jsonify({'result': {
                'totalAmount' : result_topup_all - result_topup_paid_pending
            },'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class RequestWithdrawApi(Resource):
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


            form = RequestWithdrawCreditForm().from_json(request.json)
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

            result_topup_paid_pending = Credit.get_sum_withdraw_pending(self,UserId=UserId)
            result_topup_all = Affiliate.get_summary(self,**{'UserId' : UserId,'Tag':'topup'})
            if len(result_topup_all) > 0:
                result_topup_all = result_topup_all[0]['totalAmount']
            else:
                result_topup_all = 0
            
            if len(result_topup_paid_pending) > 0:
                result_topup_paid = result_topup_paid_pending[0]['totalAmount']
            else:
                result_topup_paid = 0
            
            body = request.get_json()
            if not type(body) is dict:
                body = {}
            
            body['Amount'] = result_topup_all - result_topup_paid
            if body['Amount'] <= 0:
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_withdraw_error',filename=language)})

            result_balance = Credit.balance(self,**{'UserId' : UserId,'Status' : 'success'})
            limit_withdraw = Settings.get_data(self,**{"SettingsId" : "LIMIT_WITHDRAW"})[0]['Value']
            balance = None
            if body['Amount'] > 0:
                balance = body['Amount']
            
            if balance is None:
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_withdraw_error',filename=language)})
            elif limit_withdraw != "null" and int(limit_withdraw) > balance:
                return jsonify({'result': [],'status':'FAILED','message': lang(key='notif_withdraw_limit',filename=language)})

            body['CreatedAt'] = datetime.now
            body['CreatedBy'] = UserId
            body['UserId'] = UserId
            body['Tag'] = "withdraw"
            body['Debit'] = body['Amount']
            body['Kredit'] = 0
            body['Status'] = "pending"
            body['Description'] = 'Penarikan dana sebesar -Rp. %s' % body["Amount"]
            del body['Amount']

            result = Credit.create(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class CreditTagApi(Resource):
    def post(self,Tag:str) -> Response:
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

            form = CreateTagCreditForm().from_json(request.json)
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
            body['CreatedBy'] = UserId
            body['UserId'] = UserId
            Tag = str(Tag).lower().strip()
            if Tag == 'topup':
                body['Kredit'] = body['Amount']
                body['Debit'] = 0
                body['Tag'] = Tag
                body['Description'] = 'Credit Topup'
            
            if Tag == 'withdraw':
                body['Tag'] = Tag
                body['Debit'] = body['Amount']
                body['Kredit'] = 0
                body['Description'] = 'Credit Withdraw'
            
            del body['Amount']
            
            result = Credit.create(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class CreditApi(Resource):
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
            body['CreatedAt'] = datetime.now
            body['CreatedBy'] = UserId
            body['Tag'] = str(body['Tag']).lower().strip()
            if body['Tag'] == 'topup' or body['Tag'] == 'fee_affiliate' or body['Tag'] == 'fee_other':
                body['Kredit'] = body['Amount']
                body['Debit'] = 0

            if body['Tag'] == 'withdraw':
                body['Debit'] = body['Amount']
                body['Kredit'] = 0
            
            del body['Amount']
            result = Credit.create(self,**body)
            if result:
                get_data_credit = Credit.objects(id=result['CreditId']).first()
                if not get_data_credit is None:
                    get_firebase_token = Token.objects(UserId=get_data_credit['UserId'],Status=1,Platform='android').first()
                    if not get_firebase_token is None and 'FirebaseToken' in get_firebase_token:
                        raw_firebase = None
                        if body['Tag'] == 'topup' and body['Status'] == 'success':
                            raw_firebase = {
                                'registration_ids' : [get_firebase_token['FirebaseToken']],
                                'data_message' : {
                                    'action' : 'topup'
                                },
                                'message_body' : 'Topup kredit sebesar Rp%s telah berhasil ditambahkan' % body['Kredit'],
                                'message_title' : 'Topup Kredit Berhasil'
                            }
                        elif body['Tag'] == 'withdraw' and body['Status'] == 'success':
                            # kirim notif ke pengguna
                            raw_firebase = {
                                'registration_ids' : [get_firebase_token['FirebaseToken']],
                                'data_message' : {
                                    'action' : 'withdraw'
                                },
                                'message_body' : 'Penarikan dana sebesar Rp%s telah berhasil dikirimkan' % body['Debit'],
                                'message_title' : 'Penarikan dana Berhasil'
                            }
                        if not raw_firebase is None:
                            firebase = Firebase()
                            firebase.send_notif_multiple(**raw_firebase)

                if body['Tag'] == 'topup' and 'Status' in body and body['Status'] == 'success':
                    upline_id = Affiliate.objects(CreatedBy=body['UserId'],Tag='register').first()
                    if not upline_id is None:
                        upline = Customer.objects(MarketingCode=upline_id['ReferralCode']).first()
                        me = Customer.objects(UserId=UserId).first()
                        if not upline is None:
                            fee_credit_percent = Settings.get_data(self,**{"SettingsId" : "FEE_AFFILIATE_CREDIT"})[0]['Value']
                            if fee_credit_percent != "null":
                                fee_credit_percent = float(fee_credit_percent)
                                fee_credit = (fee_credit_percent * int(body['Harga'])) / 100

                                fee_credit = round(fee_credit)
                                # create commission affiliate
                                create_affiliate = Affiliate.create(self,
                                    UserId=upline['UserId'],
                                    ReferralCode=upline_id['ReferralCode'],
                                    Commission=fee_credit,
                                    Tag='topup',
                                    CreatedAt=datetime.now,
                                    CreatedBy=result['UserId'],
                                    RefNo=result['CreditId'],
                                    Paid=False
                                )
                                if not create_affiliate:
                                    telegram.send_log(request=request,message='Failed Create Affiliate')
                                else:
                                    # kirim notif komisi ke upline
                                    get_firebase_token = Token.objects(UserId=upline['UserId'],Status=1,Platform='android').first()
                                    if not get_firebase_token is None and 'FirebaseToken' in get_firebase_token:
                                        raw_firebase = {
                                            'registration_ids' : [get_firebase_token['FirebaseToken']],
                                            'data_message' : {
                                                'action' : 'fee_topup'
                                            },
                                            'message_body' : 'Kamu baru saja mendapatkan Komisi sebesar Rp%s dari topup kredit User %s' % (fee_credit,me['Name']),
                                            'message_title' : 'Komisi Topup Kredit'
                                        }
                                        
                                        if not raw_firebase is None:
                                            firebase = Firebase()
                                            firebase.send_notif_multiple(**raw_firebase)

            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
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

            form = UpdateCreditForm().from_json(request.json)
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
            body['UpdatedAt'] = datetime.now
            body['UpdatedBy'] = UserId
            CreditId = body['CreditId']
            if 'Tag' in body and 'Amount' in body:
                body['Tag'] = str(body['Tag']).lower().strip()
                if body['Tag'] == 'topup' or body['Tag'] == 'fee_affiliate' or body['Tag'] == 'fee_other':
                    body['Kredit'] = body['Amount']
                    body['Debit'] = 0

                if body['Tag'] == 'withdraw':
                    body['Debit'] = body['Amount']
                    body['Kredit'] = 0
                del body['Amount']

            del body['CreditId']
            result = Credit.update(self,CreditId,**body)
            if result:
                get_data_credit = Credit.objects(id=CreditId).first()
                if not get_data_credit is None:
                    get_firebase_token = Token.objects(UserId=get_data_credit['UserId'],Status=1,Platform='android').first()
                    if not get_firebase_token is None and 'FirebaseToken' in get_firebase_token:
                        raw_firebase = None
                        if body['Tag'] == 'topup' and body['Status'] == 'success':
                            raw_firebase = {
                                'registration_ids' : [get_firebase_token['FirebaseToken']],
                                'data_message' : {
                                    'action' : 'topup'
                                },
                                'message_body' : 'Topup kredit sebesar Rp%s telah berhasil ditambahkan' % body['Kredit'],
                                'message_title' : 'Topup Kredit Berhasil'
                            }
                        elif body['Tag'] == 'withdraw' and body['Status'] == 'success':
                            # kirim notif ke pengguna
                            raw_firebase = {
                                'registration_ids' : [get_firebase_token['FirebaseToken']],
                                'data_message' : {
                                    'action' : 'withdraw'
                                },
                                'message_body' : 'Penarikan dana sebesar Rp%s telah berhasil dikirimkan' % body['Debit'],
                                'message_title' : 'Penarikan dana Berhasil'
                            }
                        if not raw_firebase is None:
                            firebase = Firebase()
                            firebase.send_notif_multiple(**raw_firebase)


            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
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
            
            form = DeleteCreditForm().from_json(request.json)
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
            
            body['id'] = ObjectId(body['CreditId'])
            del body['CreditId']

            result = Credit.delete(self,**body)
            
            return jsonify({'result': [],'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
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
            
            if 'page' in body:
                if body['page'] is None:
                    body['page'] = 1
                else:
                    body['page'] = int(body['page'])
            else:
                body['page'] = 1

            if 'numberPage' in body:
                if body['numberPage'] is None:
                    body['numberPage'] = 10
                else:
                    body['numberPage'] = int(body['numberPage'])
            else:
                body['numberPage'] = 10

            body['filter'] = {
                "Tag" : {
                    "$ne" : "withdraw"
                }
            }
            result = Credit.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class CreditCustomerApi(Resource):
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
            
            if 'page' in body:
                if body['page'] is None:
                    body['page'] = 1
                else:
                    body['page'] = int(body['page'])
            else:
                body['page'] = 1

            if 'numberPage' in body:
                if body['numberPage'] is None:
                    body['numberPage'] = 10
                else:
                    body['numberPage'] = int(body['numberPage'])
            else:
                body['numberPage'] = 10
            
            if 'filter' in body and type(body['filter']) is dict:
                body['filter']['UserId'] = UserId
            else:
                body['filter'] = {
                    'UserId' : UserId
                }
            result = Credit.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class CreditBalanceApi(Resource):
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
                
            body['filter'] = {
                'Status' : 'success'
            }
            result = Credit.balance_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 


class CreditBalanceCustomerApi(Resource):
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
            result = Credit.balance(self,**{'UserId' : UserId,'Status' : 'success'})
            
            if len(result) > 0:
                balance = result[0]
                if balance['Balance'] <= 0:
                    Customer.objects(UserId=UserId).update(set__AllowedShareProfile=False)


            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class CreditDatatablesApi(Resource):
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
            
            if 'page' in body:
                if body['page'] is None:
                    body['page'] = 1
                else:
                    body['page'] = int(body['page'])
            else:
                body['page'] = 1

            if 'numberPage' in body:
                if body['numberPage'] is None:
                    body['numberPage'] = 10
                else:
                    body['numberPage'] = int(body['numberPage'])
            else:
                body['numberPage'] = 10

            result = Credit.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Credit.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 