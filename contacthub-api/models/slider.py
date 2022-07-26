# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField,SequenceField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string

class Slider(Document):
    Name = StringField(required=True)
    Image = StringField(required=True)
    Link = StringField(null=True)
    CreatedAt = DateTimeField(null=True)
    CreatedBy = StringField(null=True)
    UpdatedAt = DateTimeField(null=True)
    UpdatedBy = StringField(null=True)
    Status = StringField(null=True,choices=('active','inactive'))

    def create(self,Name:str,Image:str,Status='active',Link=None,CreatedAt=None,CreatedBy=None,UpdatedAt=None,UpdatedBy=None):
        post_data = Slider(**{
            'Name' : Name,
            'Image' : Image,
            'Link' : Link,
            'CreatedAt':CreatedAt,
            'CreatedBy' : CreatedBy,
            'UpdatedAt':UpdatedAt,
            'UpdatedBy':UpdatedBy,
            'Status':Status,
        }).save()
        if post_data:
            return Slider.get_data(self,**{'SliderId' : str(post_data.id)})[0]
        else:
            return False
    
    def update(self, SliderId:str, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Slider.objects(id=ObjectId(SliderId)).update(**param)
        if update_data:
            return Slider.get_data(self,**{'SliderId':SliderId})[0]
        else:
            return False
    
    def delete(self,**data):
        return Slider.objects(**data).delete()
        
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"SliderId" : {"$toString" : "$_id"}}})
        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})

        
        pipeline.append({"$project" : {
            "_id" : 0
        }})
        result = Slider.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    
    def get_paginate(self,**args_filter):
        if 'page' in args_filter and type(args_filter['page']) is int:
            page = args_filter['page']
        else:
            page = 1
        
        if 'per_page' in args_filter and type(args_filter['per_page']) is int:
            per_page = args_filter['per_page']
        else:
            per_page = 10


        pipeline = []

        if 'filter' in args_filter and type(args_filter['filter']) is dict:
            for fil in args_filter['filter']:
                if fil == 'SliderId':
                    pipeline.append({"$match" : {"_id" : ObjectId(args_filter['filter'][fil])}})
                else:
                    pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
        
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"Name" : regex}
                ] 
            }})
        pipeline.append({"$addFields" : {"SliderId" : {"$toString" : "$_id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})

        pipeline.append({"$project" : {"_id" : 0}})
        

        
        pipeline.append({"$project" : {
            "_id" : 0
        }})
        result = Slider.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

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
        
        
        result = Slider.objects.aggregate(*pipeline,allowDiskUse=True)
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

        return {
            "data":dataRecord,
            "current_page" : page,
            "last_page" : lastPage,
            "per_page" : per_page,
            "total" : totalRecord,
        }
        