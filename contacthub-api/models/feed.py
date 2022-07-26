# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string

class Feed(Document):
    Title = StringField(required=True)
    Description = StringField(required=True)
    PhoneNo = StringField(required=True)
    CreatedAt = DateTimeField(required=True)
    CreatedBy = StringField(required=True)
    Image = StringField(null=True)
    UpdatedAt = DateTimeField(null=True)
    UpdatedBy = StringField(null=True)
    Category = StringField(null=True)
    IsApproved = BooleanField(null=True)
    Reason = StringField(null=True)
    Link = StringField(null=True)
    ButtonType = StringField(null=True,default='whatsapp',choices=('whatsapp','link'))
    Status = StringField(null=True,choices=('draft','pending','publish','unpublish','reject'))

    meta = {
        'indexes': [
            {
                'fields': ['$Title', "$Description","$PhoneNo"],
                'default_language': 'english',
            }
        ]
    }

    def create(self,Title:str,Description:str,PhoneNo:str,CreatedAt,CreatedBy:str,Link:str=None,ButtonType:str='whatsapp',Category=None,Status=None,Image=None,IsAproved=False):
        raw_body = {
            'Title' : Title,
            'PhoneNo' : PhoneNo,
            'Description' : Description,
            'CreatedAt' : CreatedAt,
            'CreatedBy' : CreatedBy,
            'Category' : Category,
            'Status' : Status,
            'Image' : Image,
            'Link' : Link,
            'ButtonType' : ButtonType,
            'IsApproved' : IsAproved
        }
        save = Feed(**raw_body).save()
        if save:
            return Feed.get_paginate(self,**{'numberPage' : 1,'page':1, 'filter' : {'FeedId' : str(save.id)}})['data'][0]
        else:
            return False
    
    def update(self, FeedId:str, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Feed.objects(id=ObjectId(FeedId)).update(**param)
        if update_data:
            return Feed.get_paginate(self,**{'numberPage' : 1,'page':1, 'filter' : {'FeedId':FeedId}})['data'][0]
        else:
            return False
    
    def update_by_customer(self, FeedId:str,UserId:str, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Feed.objects(id=ObjectId(FeedId),CreatedBy=UserId).update(**param)
        if update_data:
            return Feed.get_paginate(self,**{'numberPage' : 1,'page':1, 'filter' : {'FeedId':FeedId}})['data'][0]
        else:
            return False
    def delete(self,**data):
        return Feed.objects(**data).delete()

    def get_paginate(self,**args_filter):
        pipeline = []
        if 'search' in args_filter and not args_filter['search'] is None:
            if str(args_filter['search']).strip() != "":
                pipeline.append({ "$match" : { "$text": { "$search": args_filter["search"] } }})

        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"CreatedAt" : regex},
                    {"UpdatedAt" : regex},
                    {"Title" : regex},
                    {"Description" : regex},
                    {"Category" : regex},
                    {"Status" : regex}
                ] 
            }})

        pipeline.append({"$addFields" : {"FeedId" : {"$toString" : "$_id"}}})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})

        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})

        
        pipeline.append({"$addFields" : {"FeedId" : {"$toString" : "$_id"}}})
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
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})

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
        
        result = Feed.objects.aggregate(*pipeline,allowDiskUse=True)
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
