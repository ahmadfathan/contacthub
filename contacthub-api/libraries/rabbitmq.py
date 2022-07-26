import pika
import sys
import json
from datetime import datetime
from flask import current_app

class RabbitMQ():
    def __init__(self):
        self.config = current_app.config['RabbitMQ']
        self.default_exchange = self.config['default_exchange']
        self.default_exchange_type = self.config['default_exchange_type']
        self.config_telegram = current_app.config['TELEGRAM']

        if self.config['status'] == False:
            return None
        
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=self.config['host']))
        
    def close(self):
        return self.connection.close()

    def declare_queue(self,queue_name,*args):
        if self.config['status'] == False:
            return None
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=queue_name, durable=True)
        for k in args:
            self.channel.queue_bind(queue=queue_name,exchange=k['exchange'],routing_key=k['routing_key'])
        return True
    def send_queue(self,routing_key,data):
        if self.config['status'] == False:
            return None
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=routing_key, durable=True)
        self.channel.basic_publish(exchange='',routing_key=routing_key, body=str(data))
        return True
    
    def send_topic(self,exhange,exchange_type,routing_key,data):
        if self.config['status'] == False:
            return None
        self.channel = self.connection.channel()
        self.channel.exchange_declare(exchange=exhange, exchange_type=exchange_type)
        self.channel.basic_publish(exchange=exhange, routing_key=routing_key, body=str(data))
        return True
