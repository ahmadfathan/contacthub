# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string,sha1,hash_string,hash_login
from random import randint
from datetime import datetime,timedelta
from .token import Token 

class User(Document):
    Username = StringField(null=True)
    Nickname = StringField(required=True)
    Email = StringField(required=True)
    Password = StringField(required=True)
    Phone = StringField(null=True)
    RoleId = StringField(required=True)
    Status = StringField(required=True,choices=('active','inactive','blocked'))
    CreatedAt = DateTimeField(required=True)
    UpdatedAt = DateTimeField(null=True)
    
    def create(self,Nickname:str,Email:str,Password:str,RoleId:str,Status:str,CreatedAt,UpdatedAt=None,Phone=None,Username=None):
        Password = sha1(Password)
        post_data = User(**{
            'Username' : Username,
            'Nickname' : Nickname,
            'Email':Email,
            'Password' : Password,
            'RoleId':RoleId,
            'Status':Status,
            'CreatedAt':CreatedAt,
            'UpdatedAt':UpdatedAt,
            'Phone':Phone
        }).save()
        if post_data:
            data_user = User.get_data(self,**{'UserId' : str(post_data.id)})
            return data_user[0]
        else:
            return False
    
    def update(self, UserId:str, **data):
        param = {}
        for x in data:
            if x == 'Password':
                data[x] = hash_login(data[x])
            
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = User.objects(id=ObjectId(UserId)).update(**param)
        if update_data:
            data_user = User.get_data(self,**{'UserId':UserId})
            return data_user[0]
        else:
            return False
    def update_password(self,UserId:str,**data):
        last_password = hash_login(data['LastPassword'])
        user = User.objects(id=ObjectId(UserId),Password=last_password).first()
        if user is None:
            return "last_password_wrong"
        else:
            update_data = User.update(self,UserId,**{'Password' : data['NewPassword']})
            if update_data:
                return True
            else:
                return False

    def delete(self,**data):
        return User.objects(**data).delete()
        
    def get_data(self,**args_filter):
        pipeline = []
        
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$_id"}}})
        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
            
        pipeline.append({"$addFields" : {"RoleId" : {"$toInt" : "$RoleId"}}})

        pipeline.append({
            "$lookup" :  {
                "from": "role",
                "localField": "RoleId",
                "foreignField": "_id",
                "as": "Role"
            }
        })
        pipeline.append({"$unwind" : "$Role"})
        pipeline.append({"$addFields" : {"Role.RoleId" : {"$toString" : "$Role._id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"Role.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Role.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"Role.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Role.UpdatedAt"}}}})

        
        pipeline.append({"$project" : {
            "_id" : 0,
            "Role._id" : 0,
            "Password" : 0
        }})
        result = User.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_paginate(self,**args_filter):
        if 'page' in args_filter and type(args_filter['page']) is int:
            page = args_filter['page']
        else:
            page = 1
        
        if 'numberPage' in args_filter and type(args_filter['numberPage']) is int:
            per_page = args_filter['numberPage']
        else:
            per_page = 10


        pipeline = []
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$_id"}}})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter[fil]}})

        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"Username" : regex},
                    {"Email" : regex},
                    {"Role.Name" : regex},
                    {"Status" : regex},
                    {"Nickname" : regex}
                ] 
            }})


        pipeline.append({"$addFields" : {"RoleId" : {"$toInt" : "$RoleId"}}})

        pipeline.append({
            "$lookup" :  {
                "from": "role",
                "localField": "RoleId",
                "foreignField": "_id",
                "as": "Role"
            }
        })
        pipeline.append({"$unwind" : "$Role"})
        pipeline.append({"$addFields" : {"Role.RoleId" : {"$toString" : "$Role._id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"Role.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Role.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"Role.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Role.UpdatedAt"}}}})

        pipeline.append({"$project" : {
            "_id" : 0,
            "Role._id" : 0,
            "Password" : 0
        }})
        
        facet = {
            '$facet' : {
                'data' : [],
                'metadata' : []
            }
        }

                
        facet['$facet']['data'].append({ "$skip": per_page * (page -1) })
        facet['$facet']['data'].append({ "$limit": per_page })
        facet['$facet']['metadata'].append({ "$count": 'total' })
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$arrayElemAt": [ '$metadata.total', 0 ] }
        }})
        
        result = User.objects.aggregate(*pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))

        totalRecord = 0
        dataRecord = []
        if len(result) > 0:
            if 'totalCount' in result[0]:
                totalRecord = result[0]['totalCount']
            if 'data' in result[0]:
                dataRecord = result[0]['data']
                
        lastPage = int(totalRecord / per_page)
        
        if lastPage < 1: lastPage = 1
        
        fromPage = (page -1) * per_page + 1
        toPage = page * per_page
        nextPage = page + 1

        return {
            "data":dataRecord,
            "current_page" : page,
            "from" : fromPage,
            "last_page" : lastPage,
            "per_page" : per_page,
            "to" : toPage,
            "total" : totalRecord,
        }
        

        return result

    def create_token(self,UserId:str,Platform:str,AccountId:str,IpAddresses=None,IsExpired=False):
        data = {}
        now = datetime.now()
        new_token = randint(1000,9999)
        new_token = str(new_token) + str(now.strftime('%Y%m%d%H%M%S'))
        data['IsExpiredToken'] = IsExpired
        data['Platform'] = Platform
        data['UserId'] = UserId
        data['Key'] = hash_string(new_token)
        data['CreatedAt'] = now
        data['Status'] = 1
        data['IpAddresses'] = IpAddresses
        data['AccountId'] = AccountId

        if 'IsExpiredToken' in data:
            if data['IsExpiredToken']:
                expired_token  = now + timedelta(hours=48)
                data['ExpiredToken'] = expired_token
        
        result = Token.create(self,**data)
        disable_access_token_other = Token.objects(id__ne=ObjectId(result['_id']),UserId=data['UserId']).update(set__Status=2)
        if result:
            return result
        else:
            return False
    
    
    def login(self,Username:str,Password:str,RoleId:str):
        password = hash_login(Password)
        user = User.objects((Q(Username=Username) | Q(Email=Username)) & Q(Password=password) & Q(RoleId=RoleId)).first()
        if user is None:
            return False
        else:
            data_user =  User.get_data(self,**{'UserId':str(user.id)})
            if len(data_user) > 0:
                return data_user[0]
            else:
                return False