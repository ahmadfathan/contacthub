
from flask import current_app 
from threading import Event
import socketio

class SocketIO():
    def __init__(self):
        host = current_app.config['SocketIO']
        self.sio = socketio.Client()
        self.sio.connect(host)
    
    def send_data(self,emit_name:str,**data):
        ev = Event()
        result = None
        def callback(data):
            nonlocal result
            nonlocal ev
            result = data
            ev.set()

        self.sio.emit(emit_name, data, callback=callback)
        ev.wait()
        return result