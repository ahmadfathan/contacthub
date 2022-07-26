# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string
from slugify import slugify

class Notification(Document):
    Name = StringField(required=True)
    Title = StringField(required=True)
    Body = StringField(required=True)
    Type = StringField(required=True,choices=("info","link","article","contact","referral"))
    Link = StringField(null=True)
    ArticleId = StringField(null=True)
    ReceiverUser = ListField(null=True)
    CreatedAt = DateTimeField(required=True)
    CreatedBy = StringField(required=True)

    def create(self,Name:str,Title:str,Body:str,CreatedBy:str,Type:str,Link=None,ArticleId=None,ReceiverUser=None):
        CreatedAt = datetime.now
        post_data = Notification(**{
            'Name' : Name,
            'Title' : Title,
            'Body':Body,
            'CreatedBy' : CreatedBy,
            'Type':Type,
            'Link':Link,
            'ArticleId':ArticleId,
            'ReceiverUser':ReceiverUser,
            'CreatedAt':CreatedAt
        }).save()
        if post_data:
            return Notification.get_data(self,**{'NotificationId' : str(post_data.id)})[0]
        else:
            return False
    
    def update(self, NotificationId:str, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Notification.objects(id=ObjectId(NotificationId)).update(**param)
        if update_data:
            return Notification.get_data(self,**{'NotificationId':NotificationId})[0]
        else:
            return False
    
    def delete(self,**data):
        return Notification.objects(**data).delete()
        
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"NotificationId" : {"$toString" : "$_id"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toObjectId" : "$CreatedBy"}}})

        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "CreatedBy",
                "foreignField": "_id",
                "as": "User"
            }
        })
        pipeline.append({"$unwind" : "$User"})
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toString" : "$CreatedBy"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})

        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
        
        pipeline.append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "User.Password" : 0

        }})
        result = Notification.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_paginate(self,**args_filter):
        pipeline = []
        if 'search' in args_filter and not args_filter['search'] is None:
            if str(args_filter['search']).strip() != "":
                pipeline.append({ "$match" : { "$text": { "$search": args_filter["search"] } }})

        pipeline.append({"$addFields" : {"NotificationId" : {"$toString" : "$_id"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toObjectId" : "$CreatedBy"}}})

        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "CreatedBy",
                "foreignField": "_id",
                "as": "User"
            }
        })
        pipeline.append({"$unwind" : "$User"})
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toString" : "$CreatedBy"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})
        
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"CreatedAt" : regex},
                    {"Name" : regex},
                    {"Type" : regex}
                ] 
            }})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
        
        pipeline.append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "User.Password" : 0

        }})
        facet = {
            '$facet' : {
                'data' : [],
                'metadata' : []
            }
        }

        if 'search' in args_filter and not args_filter['search'] is None:
            if str(args_filter['search']).strip() != "":
                facet['$facet']['data'].append({ "$sort": { "score": { "$meta": "textScore" } } })
                
        facet['$facet']['data'].append({"$sort" : {"CreatedAt" : -1}})
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        facet['$facet']['metadata'].append({ "$count": 'total' })
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$arrayElemAt": [ '$metadata.total', 0 ] }
        }})
        
        result = Notification.objects.aggregate(*pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))

        totalRecord = 0
        dataRecord = []
        if len(result) > 0:
            if 'totalCount' in result[0]:
                totalRecord = result[0]['totalCount']
            if 'data' in result[0]:
                dataRecord = result[0]['data']
                
        lastPage = int(totalRecord / args_filter['numberPage'])
        
        if lastPage < 1: lastPage = 1
        
        fromPage = (args_filter['page'] -1) * args_filter['numberPage'] + 1
        toPage = args_filter['page'] * args_filter['numberPage']
        nextPage = args_filter['page'] + 1

        return {
            "data":dataRecord,
            "current_page" : args_filter['page'],
            "from" : fromPage,
            "last_page" : lastPage,
            "per_page" : args_filter['numberPage'],
            "to" : toPage,
            "total" : totalRecord,
        }