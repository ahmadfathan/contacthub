# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string,mongodatetime_to_stringformat

class Token(Document):
    UserId = StringField(required=True)
    Tag = StringField(null=True)
    Key = StringField(null=True)
    IpAddresses = StringField(null=True)
    ExpiredToken = DateTimeField(null=True)
    IsExpiredToken = BooleanField(null=True)
    CreatedAt = DateTimeField(null=True)
    UpdatedAt = DateTimeField(null=True)
    Platform = StringField(null=True,choices=('web','android','ios','desktop'))
    Status = IntField(null=True,choices=(1,2))
    AccountId = StringField(required=True)
    FirebaseToken = StringField(null=True)
    
    def access_token_verify(self,access_token:str,RoleId:str):
        pipeline = []
        pipeline.append({"$addFields" : {"UserId" : {"$toObjectId" : "$UserId"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "UserId",
                "foreignField": "_id",
                "as": "User"
            }
        })
        pipeline.append({"$unwind" : "$User"})
        
        pipeline.append({"$match" : {"Key" : access_token}})
        pipeline.append({"$match" : {"Status" : 1}})
        pipeline.append({"$match" : {"User.RoleId" : RoleId}})

        result = Token.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        if len(result) > 0:
            result = Token.objects(Key=access_token,Status=1)
            result = result.get()
            if 'UseExpiredToken' in result:
                if result['UseExpiredToken'] == True:
                    now = datetime.now()
                    result = Token.objects(Key=access_token,Status=1,ExpiredToken__gt=now).get()

            item = Token.get_data(self,**{'_id' : str(result.id)})
            return item[0]
        else:
            return []

    def update(self,Key:str, **args):
        raw_data = {}
        for x in args:
            raw_data['set__%s' % x] = args[x]
        
        return Token.objects(Key=Key).update(**raw_data)

    def create(self,UserId:str,Key:str,IpAddresses:str,IsExpiredToken:bool,Platform:str,Status:int, \
            AccountId:str,CreatedAt,ExpiredToken=None,UpdatedAt=None,FirebaseToken=None):
        post_data = Token(**{'UserId' : UserId,'Key':Key,'IpAddresses':IpAddresses,'IsExpiredToken':IsExpiredToken, \
            'AccountId':AccountId,'Platform':Platform,'Status':Status,'CreatedAt':CreatedAt,'ExpiredToken':ExpiredToken, \
            'UpdatedAt':UpdatedAt,'FirebaseToken' : FirebaseToken}).save()
        if post_data:
            return Token.get_data(self,**{'Key':Key})[0]
        else:
            return False
    
    def get_data(self,**args_filter):
        pipeline = []
        
        pipeline.append({"$addFields" : {"_id" : {"$toString" : "$_id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})

        for key in args_filter:
            pipeline.append({"$match" : {key : args_filter[key]}})
        
        result = Token.objects.aggregate(pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))

        return result
