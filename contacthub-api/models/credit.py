# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField,FloatField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string

class Credit(Document):
    UserId = StringField(required=True)
    CreatedAt = DateTimeField(required=True)
    CreatedBy = StringField(required=True)
    UpdatedAt = DateTimeField(null=True)
    UpdatedBy = StringField(null=True)
    Debit = FloatField(null=True)
    Kredit = FloatField(null=True)
    Tag = StringField(null=True) 
    Description = StringField(null=True)
    RefNo = StringField(null=True)
    Status = StringField(null=True,choices=('success','pending','failed'))
    WithdrawBank = StringField(null=True)
    WithdrawNoRek = StringField(null=True)
    WithdrawPemilikRek = StringField(null=True)
    Harga = FloatField(null=True) # Harga Nilai Topup Credit 

    
    def get_sum_withdraw(self,UserId:str):
        pipeline = []
        pipeline.append({"$match" : {"UserId" : UserId}}) 
        pipeline.append({"$match" : {"Status" : 'success'}}) 
        pipeline.append({"$match" : {"Tag" : 'withdraw'}}) 
        
        pipeline.append({"$group" : {
            "_id" : 0,
            "totalAmount" : {"$sum" : "$Debit"}

        }})
        result = Credit.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    def get_sum_withdraw_pending(self,UserId:str):
        pipeline = []
        pipeline.append({"$match" : {"UserId" : UserId}}) 
        pipeline.append({"$match" : {"Status" : {"$in" : ['success','pending']}}}) 
        pipeline.append({"$match" : {"Tag" : 'withdraw'}}) 
        
        pipeline.append({"$group" : {
            "_id" : 0,
            "totalAmount" : {"$sum" : "$Debit"}

        }})
        result = Credit.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result

    def create(self,UserId:str,CreatedAt,CreatedBy,Status='pending',Debit=0,Kredit=0,RefNo=None,Description=None,Tag=None,WithdrawBank=None,WithdrawNoRek=None,WithdrawPemilikRek=None,Harga=None):
        post_data = Credit(**{
            'UserId' : UserId,
            'CreatedAt':CreatedAt,
            'CreatedBy':CreatedBy,
            'Debit':Debit,
            'Kredit' : Kredit,
            'Tag':Tag,
            'Description':Description,
            'RefNo':RefNo,
            'Status':Status,
            'WithdrawBank':WithdrawBank,
            'WithdrawNoRek':WithdrawNoRek,
            'WithdrawPemilikRek':WithdrawPemilikRek,
            'Harga' : Harga
        }).save()
        if post_data:
            return Credit.get_data(self,**{'CreditId' : str(post_data.id)})[0]
        else:
            return False
    
    def update(self, CreditId:str, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Credit.objects(id=ObjectId(CreditId)).update(**param)
        if update_data:
            return Credit.get_data(self,**{'CreditId':CreditId})[0]
        else:
            return False
    
    def delete(self,**data):
        return Credit.objects(**data).delete()
        
    def get_data(self,**args_filter):
        pipeline = []
        
        pipeline.append({"$addFields" : {"CreditId" : {"$toString" : "$_id"}}})
        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
            
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
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})

        
        pipeline.append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "User.Password" : 0

        }})
        result = Credit.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_paginate(self,**args_filter):
        pipeline = []
        
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"CreatedAt" : regex},
                    {"Debit" : regex},
                    {"Kredit" : regex},
                    {"Description" : regex},
                    {"RefNo" : regex},
                    {"Tag" : regex},
                    {"Status" : regex},
                    {"WithdrawBank" : regex},
                    {"WithdrawNoRek" : regex},
                    {"WithdrawPemilikRek" : regex},
                    {"Harga" : regex},
                    {"User.Email" : regex},
                ] 
            }})
        
        pipeline.append({"$addFields" : {"CreditId" : {"$toString" : "$_id"}}})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
                
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
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
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
        
        result = Credit.objects.aggregate(*pipeline,allowDiskUse=True)
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

    def balance_paginate(self,**args_filter):
        pipeline = []
        pipeline.append({"$match" : {"Tag" : {"$ne" : "withdraw"}}})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})

        
        if 'keyword' in args_filter and not args_filter['keyword'] is None and not args_filter['keyword'].strip() == "":
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {'$or' : [{'Customer.Name' : regex},{'User.Email' : regex}]}})
            
        pipeline.append({"$project" : {
            "UserId" : 1,
            "Kredit" : 1,
            "Debit" : 1,
            "Customer.CustomerId" : 1,
            "Customer.Name" : 1,
            "User.Email" : 1,
            "User.Nickname" : 1,
        }})
        
        facet = {
            '$facet' : {
                'data' : [],
                'metadata' : []
            }
        }
        group = {
            '$group' : {
                '_id' : '$UserId',
                'Balance': { '$sum' : {'$subtract' : ['$Kredit','$Debit']} }
            }
        }
        facet['$facet']['data'].append(group)
        facet['$facet']['metadata'].append(group)
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        facet['$facet']['metadata'].append({ "$count": 'total' })
        
        
        facet['$facet']['data'].append({"$addFields" : {"UserId" : {"$toObjectId" : "$_id"}}})

        facet['$facet']['data'].append({
            "$lookup" :  {
                "from": "user",
                "localField": "UserId",
                "foreignField": "_id",
                "as": "User"
            }
        })
        facet['$facet']['data'].append({"$unwind" : "$User"})
        facet['$facet']['data'].append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        facet['$facet']['data'].append({
            "$lookup" :  {
                "from": "customer",
                "localField": "User.UserId",
                "foreignField": "UserId",
                "as": "Customer"
            }
        })

        facet['$facet']['data'].append({"$unwind" : "$Customer"})
        facet['$facet']['data'].append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        facet['$facet']['data'].append({"$addFields" : {"Customer.CustomerId" : {"$toString" : "$Customer._id"}}})
        facet['$facet']['data'].append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        facet['$facet']['data'].append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})
        
        facet['$facet']['data'].append({"$addFields" : {"Customer.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Customer.CreatedAt"}}}})
        facet['$facet']['data'].append({"$addFields" : {"Customer.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Customer.UpdatedAt"}}}})


        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$arrayElemAt": [ '$metadata.total', 0 ] }
        }})
        
        result = Credit.objects.aggregate(*pipeline,allowDiskUse=True)
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

    def balance(self,**args_filter):
        pipeline = []
        
        pipeline.append({"$match" : {"Tag" : {"$ne" : "withdraw"}}})
        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})

        
        
        pipeline.append({"$project" : {
            "UserId" : 1,
            "Kredit" : 1,
            "Debit" : 1,
            "UserId" : 1,
            "Customer.CustomerId" : 1,
            "Customer.Name" : 1,
            "User.Email" : 1,
            "User.Nickname" : 1,
        }})        
        pipeline.append({
            '$group' : {
                '_id' : '$UserId',
                'UserId' : {'$first' : '$UserId'},
                'User' : {'$first' : '$User'},
                'Customer' : {'$first' : '$Customer'},
                'Balance': { '$sum' : {'$subtract' : ['$Kredit','$Debit']} }
            }
        })
        
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
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "customer",
                "localField": "User.UserId",
                "foreignField": "UserId",
                "as": "Customer"
            }
        })
        pipeline.append({"$unwind" : "$Customer"})
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        
        
        pipeline.append({"$addFields" : {"Customer.CustomerId" : {"$toString" : "$Customer._id"}}})
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})
        
        pipeline.append({"$addFields" : {"Customer.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Customer.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"Customer.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Customer.UpdatedAt"}}}})

        pipeline.append({"$project" : {
            "Balance" : 1,
            "Customer.CustomerId" : 1,
            "Customer.Name" : 1,
            "User.Email" : 1,
            "User.Nickname" : 1,
        }})        
        
        result = Credit.objects.aggregate(*pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))
        return result
    
    def all_balance(self):
        pipeline = []
        
        pipeline.append({"$match" : {"Tag" : {"$ne" : "withdraw"}}})
        pipeline.append({"$match" : {"Status" : 'success'}})
        pipeline.append({"$project" : {
            "Tag" : 1,
            "Kredit" : 1,
            "Debit" : 1,
            "Status" : 1
        }})        
        
        pipeline.append({
            '$group' : {
                '_id' : 0,
                'Balance': { '$sum' : {'$subtract' : ['$Kredit','$Debit']} }
            }
        })
        result = Credit.objects.aggregate(*pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))
        return result
    
    
    def penghasilan_dicairkan(self):
        pipeline = []
        
        pipeline.append({"$match" : {"Tag" : "withdraw"}})
        pipeline.append({"$match" : {"Status" : 'success'}})
        pipeline.append({"$project" : {
            "Tag" : 1,
            "Kredit" : 1,
            "Debit" : 1,
            "Status" : 1
        }})        
        
        pipeline.append({
            '$group' : {
                '_id' : 0,
                'Total': { '$sum' : "$Debit" }
            }
        })
        result = Credit.objects.aggregate(*pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))
        return result
    
    def sum_biaya_topup(self):
        pipeline = []
        
        pipeline.append({"$match" : {"Tag" : "topup"}})
        pipeline.append({"$match" : {"Status" : "success"}})
        pipeline.append({"$project" : {
            "Tag" : 1,
            "Harga" : 1,
            "Status" : 1
        }})        
        
        pipeline.append({
            '$group' : {
                '_id' : 0,
                'Total': { '$sum' : "$Harga"}
            }
        })
        result = Credit.objects.aggregate(*pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))
        return result