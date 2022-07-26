# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField,SequenceField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string

class City(Document):
    _id = SequenceField(required=True)
    ProvinceId = IntField(required=True)
    City = StringField(null=True)

    def create(self,_id:int,ProvinceId:int,CityName:str):
        if City.objects(_id=_id).first() is None:
            post_data = City(**{'_id' : _id,'ProvinceId' : ProvinceId,'City':CityName}).save()
        else:
            post_data = City.objects(_id=_id).update(set__City=CityName)

        if post_data:
            return City.get_data(self,**{'_id' : _id})[0]
        else:
            return False

    def delete(self,_id:str):
        return City.objects(_id=_id).delete()
    
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({
            "$lookup" :  {
                "from": "province",
                "localField": "ProvinceId",
                "foreignField": "_id",
                "as": "Province"
            }
        })
        pipeline.append({"$unwind" : "$Province"})
        pipeline.append({"$addFields" : {"ProvinceId" : {"$toInt" : "$ProvinceId"}}})
        pipeline.append({"$addFields" : {"Province.ProvinceId" : {"$toInt" : "$Province._id"}}})
        pipeline.append({"$addFields" : {"CityId" : {"$toInt" : "$_id"}}})
        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
        pipeline.append({"$project" : {
            "_id" : 0,
            "Province._id" : 0
        }})
        pipeline.append({"$sort" : {
            "ProvinceId" : 1,
            "CityId" : 1,
        }})
        result = City.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_paginate(self,**args_filter):
        pipeline = []

        if 'search' in args_filter and not args_filter['search'] is None:
            if str(args_filter['search']).strip() != "":
                pipeline.append({ "$match" : { "$text": { "$search": args_filter["search"] } }})

        pipeline.append({"$addFields" : {"ProvinceId" : {"$toInt" : "$ProvinceId"}}})
        pipeline.append({"$addFields" : {"CityId" : {"$toInt" : "$_id"}}})
        pipeline.append({"$addFields" : {"CityId" : {"$toString" : "$CityId"}}})

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
        
        result = City.objects.aggregate(*pipeline,allowDiskUse=True)
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