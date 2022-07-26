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

class DashboardApi(Resource):
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

            all_member = Customer.count_all_member(self)
            balance = Credit.all_balance(self)
            biaya_topup = Credit.sum_biaya_topup(self)
            penghasilan_dicairkan = Credit.penghasilan_dicairkan(self)
            total_penghasilan = Affiliate.total_penghasilan(self)

            if len(total_penghasilan) > 0: 
                total_penghasilan = total_penghasilan[0]['Total']
            else:
                total_penghasilan = 0

            if len(penghasilan_dicairkan) > 0: 
                penghasilan_dicairkan = penghasilan_dicairkan[0]['Total']
            else:
                penghasilan_dicairkan = 0
            if len(biaya_topup) > 0: 
                biaya_topup = biaya_topup[0]['Total']
            else:
                biaya_topup = 0
            if len(balance) > 0: 
                balance = balance[0]['Balance']
            else:
                balance = 0
            if len(all_member) > 0: 
                all_member = all_member[0]['Count']
            else:
                all_member = 0

            return jsonify({'result': {
                'Balance' : balance,
                'BiayaTopup' : biaya_topup,
                'TotalPenghasilan' : total_penghasilan,
                'PenghasilanDicairkan' : penghasilan_dicairkan,
                'Member' : all_member
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

