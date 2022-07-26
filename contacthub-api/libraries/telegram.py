from flask import current_app
import requests
from datetime import datetime

class Telegram():
    def __init__(self):
        self.config = current_app.config['TELEGRAM']
        
    def send(self,text:str):
        url = 'https://api.telegram.org/bot' + self.config['TOKEN'] +  '/sendmessage?'
        for chat_id in self.config['CHAT_ID']:
            params = {
                'chat_id' : chat_id,
                'text' : text,
            }
            result = requests.post(url,params=params)
        return result
    
    def send_log(self,message:str,request):
        try:
            template = request.method + ' ' + request.url + '\n\n'
            template = template + 'datetime : \n' +datetime.now().strftime("%Y-%m-%d %H:%M:%S") +  '\n\n'
            template = template + 'message : \n' + message + '\n\n'
            template = template + 'headers : \n' + str(request.headers) + '\n\n'
            template = template + 'data : \n' + str(request.data) + '\n\n'
            template = template + 'body : \n' + str(request.get_json()) + '\n\n'
            template = template + 'params : \n' + str(request.args)
            return self.send(template)
        except Exception as e:
            print(str(e))