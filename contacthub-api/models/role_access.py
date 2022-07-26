# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string

class RoleAccess(Document):
    _id = StringField(required=True,unique=True)
    Description = StringField(null=True)
    IsAdministrator = BooleanField(null=True)
    ClassMethod = ListField(null=True)
    
    def create(self,Description:str,IsAdministrator=None,ClassMethod=None):
        post_data = RoleAccess(**{
            'Description' : Description,
            'IsAdministrator':IsAdministrator,
            'ClassMethod':ClassMethod
        }).save()
        if post_data:
            return RoleAccess.get_data(self,**{'_id' : post_data._id})[0]
        else:
            return False
    
    def update(self, RoleAccessId:int, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = RoleAccess.objects(_id=RoleAccessId).update(**param)
        if update_data:
            return RoleAccess.get_data(self,**{'_id':RoleAccessId})[0]
        else:
            return False
    
    def delete(self,**data):
        return RoleAccess.objects(**data).delete()
        
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"RoleAccessId" : {"$toString" : "$_id"}}})

        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
        
        pipeline.append({"$project" : {
            "_id" : 0
        }})
        result = RoleAccess.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result