# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField,SequenceField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string

class RoleAccess(Document):
    _id = StringField(required=True)
    ClassMethod = ListField(required=True)
    def getAll(self,**args) -> Document:
        result = RoleAccess.objects(**args).all()
        return result

    def create(self,**args)-> Document:
        result = RoleAccess(**args).save()
        return result

    def by_id(self,access_id:str) -> Document:
        result = RoleAccess.objects(_id=access_id)
        return result

    def update_data(self,access_id:str,**args)-> Document:
        result = RoleAccess.objects(_id=access_id).update(**args)
        if result:
            result = RoleAccess.by_id(self,access_id=access_id)

        return result
        
    def get(self,**args)-> Document:
        result = RoleAccess.objects
        if 'order_by' in args and type(args['order_by']) is dict:
            order = []
            for order_by in args['order_by']:
                if args['order_by'][order_by] == 'desc':
                    order.append("-" + order_by)
                else:
                    order.append(order_by)

            result = result.order_by(*order)

        paginate = {}
        if 'page' in args:
            paginate['page'] = args['page']
        if 'per_page' in args:
            paginate['per_page'] = args['per_page']
    
        if 'keyword' in args:
            result = result.filter(Q(_id__icontains=args['keyword']) | Q(ClassMethod__icontains=args['keyword']))
        
        if paginate != {}:
            result = result.paginate(**paginate) 
        
        return result
        
class Role(Document):
    _id = SequenceField(required=True)
    Name = StringField(required=True)
    CreatedAt = DateTimeField(null=True)
    CreatedBy = StringField(null=True)
    UpdatedAt = DateTimeField(null=True)
    UpdatedBy = StringField(null=True)
    IsAdministrator = BooleanField(null=True)
    AccountId = StringField(null=True)
    RoleAccess = ListField(null=True)
    
    def create(self,Name:str,CreatedAt=None,CreatedBy=None,UpdatedAt=None,UpdatedBy=None,IsAdministrator=None,AccountId=None,RoleAccess=None):
        post_data = Role(**{
            'Name' : Name,
            'CreatedAt':CreatedAt,
            'CreatedBy' : CreatedBy,
            'UpdatedAt':UpdatedAt,
            'UpdatedBy':UpdatedBy,
            'IsAdministrator':IsAdministrator,
            'AccountId':AccountId,
            'RoleAccess':RoleAccess
        }).save()
        if post_data:
            return Role.get_data(self,**{'_id' : post_data._id})[0]
        else:
            return False
    
    def update(self, RoleId:int, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Role.objects(_id=RoleId).update(**param)
        if update_data:
            return Role.get_data(self,**{'_id':RoleId})[0]
        else:
            return False
    
    def delete(self,**data):
        return Role.objects(**data).delete()
        
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"RoleId" : "$_id"}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})

        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
        
        pipeline.append({"$project" : {
            "_id" : 0
        }})
        result = Role.objects.aggregate(pipeline)
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
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
        
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"Name" : regex}
                ] 
            }})
        pipeline.append({"$addFields" : {"RoleId" : "$_id"}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})

        pipeline.append({"$project" : {"_id" : 0,"Project._id" : 0}})
        

        
        pipeline.append({"$project" : {
            "_id" : 0
        }})
        result = Role.objects.aggregate(pipeline)
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
        
        
        result = Role.objects.aggregate(*pipeline,allowDiskUse=True)
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
        