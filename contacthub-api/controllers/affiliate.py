from bson import ObjectId
from flask import Response, request, jsonify, current_app
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,IntegerField,SelectField
import wtforms_json

# project resources
from models import Affiliate,Credit
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram
import json 
from helpers import lang 
from datetime import datetime
import traceback
wtforms_json.init()


class AffiliateSummaryApi(Resource):
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

            raw_data_all = {}
            for k in body:
                if k == 'fromDate' or k == 'toDate': continue
                raw_data_all[k] = body[k]


            body['UserId'] = UserId
            body['Tag'] = 'register'
            raw_data_all['UserId'] = UserId
            raw_data_all['Tag'] = 'register'
            result_register_bulan_ini = Affiliate.get_summary(self,**body)
            result_register = Affiliate.get_summary(self,**raw_data_all)
            body['Tag'] = 'topup'
            raw_data_all['Tag'] = 'topup'
            result_topup_bulan_ini = Affiliate.get_summary(self,**body)
            result_topup = Affiliate.get_summary(self,**raw_data_all)

            body['Paid'] = False
            result_topup_paid = Credit.get_sum_withdraw(self,UserId=body['UserId'])

            result_topup_all = Affiliate.get_summary(self,**{'UserId' : body['UserId'],'Tag':'topup'})

            if len(result_topup_paid) > 0:
                result_topup_paid = result_topup_paid[0]['totalAmount']
            else:
                result_topup_paid = 0

            if len(result_topup_all) > 0:
                result_topup_all = result_topup_all[0]['totalAmount']
            else:
                result_topup_all = 0

            totalDownline = Affiliate.get_downline(self,**{'UserId' : body['UserId'],'Tag':'register'})
            if len(totalDownline) > 0:
                totalDownline = totalDownline[0]['totalDownline']
            else:
                totalDownline = 0

            if len(result_register) > 0:
                result_register = result_register[0]['totalAmount']
            else:
                result_register = 0
            
            if len(result_register_bulan_ini) > 0:
                result_register_bulan_ini = result_register_bulan_ini[0]['totalAmount']
            else:
                result_register_bulan_ini = 0
            
            if len(result_topup) > 0:
                result_topup = result_topup[0]['totalAmount']
            else:
                result_topup = 0
            
            if len(result_topup_bulan_ini) > 0:
                result_topup_bulan_ini = result_topup_bulan_ini[0]['totalAmount']
            else:
                result_topup_bulan_ini = 0

            result = {
                'fromDate' : body['fromDate'],
                'toDate' : body['toDate'],
                'totalKomisi' : result_register,
                'totalKomisiBulanIni' : result_register_bulan_ini,
                'totalPenghasilan' : result_topup,
                'totalPenghasilanBulanIni' : result_topup_bulan_ini,
                'totalPenghasilanBelumCair' : result_topup_all - result_topup_paid,
                'totalDownline' : totalDownline,
                'totalCommission' : result_topup_all,
                'commissionRegister' : 0,
                'commissionTopup' : 0,
                'commissionTopupUnpaid' : 0
            }
            result['commissionRegister'] = result_register
            result['commissionTopup'] = result_topup
            
            result['commissionTopupUnpaid'] = result_topup_all - result_topup_paid

            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Affiliate.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

# khusus untuk customer
class AffiliateApi(Resource):
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

            body['filter'] = {
                'UserId' : UserId
            }
            
            result = Affiliate.get_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Affiliate.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class AffiliatePenghasilanApi(Resource):
    def post(self):
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

            result = Affiliate.get_affiliate_penghasilan(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Affiliate.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
class AffiliateKomisiApi(Resource):
    def post(self):
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

            result = Affiliate.get_affiliate_paginate(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Affiliate.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 

class AffiliatePenghasilanDetailApi(Resource):
    def post(self):
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

            result = Affiliate.get_affiliate_penghasilan_detail(self,**body)
            
            return jsonify({'result': result,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Affiliate.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 