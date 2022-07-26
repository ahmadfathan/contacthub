# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string

class Settings(Document):
    _id = StringField(required=True)
    Value = StringField(null=True)

    def create(self,_id:str,Value=None):
        if type(Value) is list:
            Value = json_util.dumps(Value)
        elif type(Value) is dict:
            Value = json_util.dumps(Value)

        if Settings.objects(_id=_id).first() is None:
            post_data = Settings(**{'_id' : _id,'Value':Value}).save()
        else:
            post_data = Settings.objects(_id=_id).update(set__Value=Value)

        if post_data:
            return Settings.get_data(self,**{'_id' : _id})[0]
        else:
            return False

    def delete(self,_id:str):
        return Settings.objects(_id=_id).delete()
    
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"SettingsId" : "$_id"}})
        for fil in args_filter:
            if type(args_filter[fil]) is list:
                pipeline.append({"$match" : {fil : {"$in" : args_filter[fil]}}})
            else:
                pipeline.append({"$match" : {fil : args_filter[fil]}})
        pipeline.append({"$project" : {
            "_id" : 0
        }})
        
        result = Settings.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_paginate(self,**args_filter):
        pipeline = []

        if 'search' in args_filter and not args_filter['search'] is None:
            if str(args_filter['search']).strip() != "":
                pipeline.append({ "$match" : { "$text": { "$search": args_filter["search"] } }})

        pipeline.append({"$addFields" : {"SettingId" : {"$toString" : "$_id"}}})

        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
        
        pipeline.append({"$project" : {
            "_id" : 0,

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
                
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        facet['$facet']['metadata'].append({ "$count": 'total' })
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$arrayElemAt": [ '$metadata.total', 0 ] }
        }})
        
        result = Settings.objects.aggregate(*pipeline,allowDiskUse=True)
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