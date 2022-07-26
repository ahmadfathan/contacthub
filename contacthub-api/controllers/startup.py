from bson import ObjectId
from flask import Response, request, jsonify, current_app
from flask_restful import Resource
from wtforms import Form, StringField,DateTimeField, validators,SelectField
import wtforms_json

# project resources
from models import Settings
from . import response
from werkzeug.exceptions import HTTPException, NotFound
from libraries import Telegram
import json 
from helpers import lang 
from datetime import datetime
import traceback
wtforms_json.init()

class StartupApi(Resource):
    
    def get(self) -> Response:
        try:
            default_config = current_app.config
            headers = request.headers
            telegram = Telegram()
            if 'Accept-Language' in headers and type(headers['Accept-Language']) is str and not headers['Accept-Language'].strip() == "":
                language = headers['Accept-Language']
            else:
                language = default_config['DEFAULT_LANGUAGE']
            
            body = default_config['FILTER_SETTING_STARTUP']
            
            result = Settings.get_data(self,**body)
            res = {}
            for r in result:
                try:
                    res[r['SettingsId']] = json.loads(r['Value'])
                except Exception as e:
                    res[r['SettingsId']] = r['Value']
            
            res['VersionCode'] = res['VERSION_APP']
            if (res['UPDATED_REQUIRED'] == "1"):
                res['UpdateRequired'] = True
            else:
                res['UpdateRequired'] = False

            res['MessageUpdate'] = lang(key='notif_updated',filename=language)

            return jsonify({'result': res,'status':'OK','message': lang(key='notif_success',filename=language)})
        except NotFound as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Settings.DoesNotExist as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(e))
            return response.not_found_200(message=str(e))
        except Exception as e:
            traceback.print_tb(e.__traceback__)
            telegram.send_log(request=request,message=str(traceback.format_exc()))
            telegram.send_log(request=request,message=str(e))
            return response.unknown(message=str(e)) 
