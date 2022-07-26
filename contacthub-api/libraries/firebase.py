from pyfcm import FCMNotification
from flask import current_app 

class Firebase():
    def __init__(self):
        api_key = current_app.config['FCM_KEY']
        self.push_service = FCMNotification(api_key=api_key)

    def send_notif_single(self,**data):
        return self.push_service.notify_single_device(**data)
        
    def send_notif_multiple(self,**data):
        return self.push_service.notify_multiple_devices(**data)
    
    def send_data_single(self,**data):
        return self.push_service.single_device_data_message(**data)
        
    def send_data_multiple(self,**data):
        return self.push_service.multiple_devices_data_message(**data)
        