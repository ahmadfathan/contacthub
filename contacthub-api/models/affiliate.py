# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,FloatField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string
from .credit import Credit
class Affiliate(Document):
    UserId = StringField(required=True)
    ReferralCode = StringField(required=True)
    Commission = FloatField(null=True)
    Tag = StringField(null=True) # register, shop, dll.
    RefNo = StringField(null=True) # bisa digunakan untuk InvoiceNo jika Tagnya shop
    CreatedAt = DateTimeField(required=True)
    CreatedBy = StringField(required=True)
    UpdatedAt = DateTimeField(null=True)
    UpdatedBy = StringField(null=True)
    Paid = BooleanField(null=True)

    def total_penghasilan(self):
        pipeline = []
        
        pipeline.append({"$project" : {
            "Tag" : 1,
            "Commission" : 1
        }})        
        pipeline.append({"$match" : {"Tag" : "topup"}})
        
        pipeline.append({
            '$group' : {
                '_id' : 0,
                'Total': { '$sum' : "$Commission" }
            }
        })
        result = Affiliate.objects.aggregate(*pipeline,allowDiskUse=True)
        result = json.loads(json_util.dumps(result))
        return result

    def get_affiliate_penghasilan(self,**args_filter):
        pipeline = []
        pipeline.append({"$match" : {'Tag' : 'topup'}})

        pipeline.append({"$addFields" : {"AffiliateId" : {"$toString" : "$_id"}}})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"Customer.Name" : regex},
                    {"User.Email" : regex}
                ] 
            }})

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
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "customer",
                "localField": "UserId",
                "foreignField": "UserId",
                "as": "Customer"
            }
        })
        
        pipeline.append({"$unwind" : "$Customer"})
        pipeline.append({
            "$lookup" :  {
                "from": "credit",
                "let": { "user_id": "$UserId" },
                "pipeline": [
                    { "$match":
                        { "$expr":
                            { "$and":
                                [
                                    { "$eq": [ "$UserId",  "$$user_id" ] },
                                    { "$eq": [ "$Tag",  "withdraw" ] },
                                ]
                            }
                        }
                    },
                    { "$project": { "Debit": 1,"_id" : 0 } },
                    { "$group": { "_id": None,"Debit" : {"$sum" : "$Debit"} } },
                ],
                "as" : "Credit"
            }
        })

        pipeline.append({"$addFields" : {"Dicairkan" : {"$arrayElemAt" : ["$Credit",0]}}})
        pipeline.append({"$addFields" : {"Dicairkan" : "$Dicairkan.Debit"}})

        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})
        
        
        pipeline.append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "User.Password" : 0,
            "Credit" : 0,
        }})
        facet = {
            '$facet' : {
                'data' : [],
                'metadata' : []
            }
        }

        facet['$facet']['data'].append({
            "$group" : {
                "_id" : '$UserId',
                'Penghasilan' : {"$sum" : "$Commission"},
                'Dicairkan' : {"$first" : "$Dicairkan"},
                'Name' : {"$first" : "$Customer.Name"},
                'Email' : {"$first" : "$User.Email"}
            }
        })
        facet['$facet']['data'].append({"$addFields" : {"BelumDicairkan" : {"$subtract" : ["$Penghasilan","$Dicairkan"]}}})
        facet['$facet']['data'].append({"$sort" : {"Downline" : -1}})
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        facet['$facet']['metadata'].append({ 
            "$group": {
                "_id": "$UserId",
            }
        })
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$size": "$metadata" }
        }})
        
        result = Affiliate.objects.aggregate(*pipeline,allowDiskUse=True)
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

    def get_affiliate_paginate(self,**args_filter):
        pipeline = []
        pipeline.append({"$match" : {'Tag' : 'register'}})
        pipeline.append({"$addFields" : {"AffiliateId" : {"$toString" : "$_id"}}})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})

        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"Customer.Name" : regex},
                    {"User.Email" : regex}
                ] 
            }})
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
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "customer",
                "localField": "UserId",
                "foreignField": "UserId",
                "as": "Customer"
            }
        })
        pipeline.append({"$unwind" : "$Customer"})
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
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

        facet['$facet']['data'].append({
            "$group" : {
                "_id" : '$UserId',
                'Downline' : {
                    "$sum" :1
                },
                'Komisi' : {"$sum" : "$Commission"},
                'Name' : {"$first" : "$Customer.Name"},
                'Email' : {"$first" : "$User.Email"}
            }
        })
        facet['$facet']['data'].append({"$sort" : {"Downline" : -1}})
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        facet['$facet']['metadata'].append({ 
            "$group": {
                "_id": "$UserId",
            }
        })
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$size": "$metadata" }
        }})
        
        result = Affiliate.objects.aggregate(*pipeline,allowDiskUse=True)
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
    def get_downline_paginate(self,**args_filter):
        pipeline = []
        pipeline.append({"$match" : {'Tag' : 'register'}})
        pipeline.append({"$addFields" : {"AffiliateId" : {"$toString" : "$_id"}}})
        
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"Downline.Name" : regex}
                ] 
            }})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
        
        pipeline.append({"$addFields" : {"UserId" : {"$toObjectId" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toObjectId" : "$CreatedBy"}}})

        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "CreatedBy",
                "foreignField": "_id",
                "as": "UserDownline"
            }
        })
        pipeline.append({"$unwind" : "$UserDownline"})
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toString" : "$CreatedBy"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "customer",
                "localField": "CreatedBy",
                "foreignField": "UserId",
                "as": "Downline"
            }
        })
        pipeline.append({"$unwind" : "$Downline"})
        pipeline.append({"$addFields" : {"UserDownline.UserId" : {"$toString" : "$UserDownline._id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"UserDownline.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UserDownline.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UserDownline.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UserDownline.UpdatedAt"}}}})
        
        
        pipeline.append({"$project" : {
            "_id" : 0,
            "UserDownline._id" : 0,
            "UserDownline.Password" : 0

        }})
        facet = {
            '$facet' : {
                'data' : [],
                'metadata' : []
            }
        }
        facet['$facet']['data'].append({"$sort" : {"Downline" : -1}})
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$size": "$metadata" }
        }})
        
        result = Affiliate.objects.aggregate(*pipeline,allowDiskUse=True)
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
    def create(self,UserId:str,ReferralCode:str,CreatedAt,CreatedBy,Tag=None,RefNo=None,Paid=False,Commission=None):
        post_data = Affiliate(**{
            'UserId' : UserId,
            'ReferralCode':ReferralCode,
            'CreatedAt' : CreatedAt,
            'CreatedBy' : CreatedBy,
            'Tag' : Tag,
            'RefNo':RefNo,
            'Paid':Paid,
            'Commission':Commission
        }).save()
        if post_data:
            return Affiliate.get_data(self,**{'AffiliateId' : str(post_data.id)})[0]
        else:
            return False
    
    def update(self, AffiliateId:str, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Affiliate.objects(id=ObjectId(AffiliateId)).update(**param)
        if update_data:
            return Affiliate.get_data(self,**{'AffiliateId':AffiliateId})[0]
        else:
            return False
    
    def delete(self,**data):
        return Affiliate.objects(**data).delete()
    
    def get_downline(self,**data):
        pipeline = []
        
        pipeline.append({"$match" : {"UserId" : data['UserId']}}) 

        if 'Tag' in data:
            pipeline.append({"$match" : {"Tag" : data['Tag']}}) 

        pipeline.append({"$group" : {
            "_id" : 0,
            "totalDownline" : {"$sum" : 1}

        }})
        result = Affiliate.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result

    def get_summary(self,**data):
        pipeline = []
        pipeline.append({"$match" : {"UserId" : data['UserId']}}) 
        
        if 'Tag' in data:
            pipeline.append({"$match" : {"Tag" : data['Tag']}}) 
        if 'Paid' in data:
            pipeline.append({"$match" : {"Paid" : data['Paid']}}) 

        if 'fromDate' in data:
            pipeline.append({"$match" : {"CreatedAt" : {"$gte" : datetime.strptime(data['fromDate'] + " 00:00:00","%Y-%m-%d %H:%M:%S") }}}) 
            pipeline.append({"$match" : {"CreatedAt" : {"$lte" : datetime.strptime(data['toDate'] + " 23:59:59","%Y-%m-%d %H:%M:%S") }}}) 
            

        pipeline.append({"$group" : {
            "_id" : 0,
            "totalAmount" : {"$sum" : "$Commission"}

        }})
        result = Affiliate.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"AffiliateId" : {"$toString" : "$_id"}}})
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
        result = Affiliate.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_paginate(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"AffiliateId" : {"$toString" : "$_id"}}})
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"CreatedAt" : regex},
                    {"UpdatedAt" : regex},
                    {"ReferralCode" : regex},
                    {"Commission" : regex},
                    {"Tag" : regex},
                    {"Category" : regex},
                    {"Downline.Email" : regex},
                    {"User.Email" : regex}
                ] 
            }})
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})

        pipeline.append({"$addFields" : {"UserId" : {"$toObjectId" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toObjectId" : "$CreatedBy"}}})

        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "UserId",
                "foreignField": "_id",
                "as": "User"
            }
        })
        
        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "CreatedBy",
                "foreignField": "_id",
                "as": "Downline"
            }
        })
        pipeline.append({"$unwind" : "$Downline"})
        pipeline.append({"$unwind" : "$User"})
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CreatedBy" : {"$toString" : "$CreatedBy"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"Downline.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Downline.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"Downline.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Downline.UpdatedAt"}}}})
        
        
        
        pipeline.append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "Downline._id" : 0,
            "User.Password" : 0

        }})
        facet = {
            '$facet' : {
                'data' : [],
                'metadata' : []
            }
        }

        facet['$facet']['data'].append({"$sort" : {"CreatedAt" : -1}})
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        facet['$facet']['metadata'].append({ "$count": 'total' })
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$arrayElemAt": [ '$metadata.total', 0 ] }
        }})
        
        result = Affiliate.objects.aggregate(*pipeline,allowDiskUse=True)
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
    
    def get_affiliate_penghasilan_detail(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"AffiliateId" : {"$toString" : "$_id"}}})
        pipeline.append({"$match" : {'Tag' : 'topup'}})

        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
                
        pipeline.append({"$addFields" : {"RefNo" : {"$toObjectId" : "$RefNo"}}})

        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "credit",
                "let": { "user_id": "$UserId" },
                "pipeline": [
                    { "$match":
                        { "$expr":
                            { "$and":
                                [
                                    { "$eq": [ "$UserId",  "$$user_id" ] },
                                    { "$eq": [ "$Tag",  "withdraw" ] },
                                ]
                            }
                        }
                    },
                    { "$project": { "Debit": 1,"_id" : 0 } },
                    { "$group": { "_id": None,"Debit" : {"$sum" : "$Debit"} } },
                ],
                "as" : "Credit"
            }
        })
        pipeline.append({
            "$lookup" :  {
                "from": "credit",
                "localField": "RefNo",
                "foreignField": "_id",
                "as": "CreditRef"
            }
        })
        pipeline.append({"$unwind" : "$CreditRef"})
        
        pipeline.append({
            "$lookup" :  {
                "from": "customer",
                "localField": "CreditRef.UserId",
                "foreignField": "UserId",
                "as": "Downline"
            }
        })
        pipeline.append({"$unwind" : "$Downline"})
        
        pipeline.append({"$addFields" : {"Downline.UserId" : {"$toObjectId" : "$Downline.UserId"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "Downline.UserId",
                "foreignField": "_id",
                "as": "UserDownline"
            }
        })
        pipeline.append({"$unwind" : "$UserDownline"})
        pipeline.append({"$addFields" : {"Dicairkan" : {"$arrayElemAt" : ["$Credit",0]}}})
        pipeline.append({"$addFields" : {"Dicairkan" : "$Dicairkan.Debit"}})

        pipeline.append({"$addFields" : {"RefNo" : {"$toString" : "$RefNo"}}})
        pipeline.append({"$addFields" : {"Downline.UserId" : {"$toString" : "$Downline.UserId"}}})
        pipeline.append({"$addFields" : {"Downline.CustomerId" : {"$toString" : "$Downline._id"}}})
        pipeline.append({"$addFields" : {"UserDownline.UserId" : {"$toString" : "$UserDownline._id"}}})
        pipeline.append({"$addFields" : {"CreditRef.CreditId" : {"$toString" : "$CreditRef._id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"CreditRef.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreditRef.UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"CreditRef.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreditRef.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"Downline.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Downline.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"Downline.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$Downline.UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"UserDownline.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UserDownline.UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"UserDownline.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UserDownline.CreatedAt"}}}})
        
        
        pipeline.append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "CreditRef._id" : 0,
            "Downline._id" : 0,
            "UserDownline._id" : 0,
            "UserDownline.Password" : 0,
            "User.Password" : 0,
            "Credit" : 0,
        }})
        facet = {
            '$facet' : {
                'data' : [],
                'metadata' : []
            }
        }

        # facet['$facet']['data'].append({
        #     "$group" : {
        #         "_id" : '$UserId',
        #         'Penghasilan' : {"$sum" : "$Commission"},
        #         'Dicairkan' : {"$first" : "$Dicairkan"},
        #         'Name' : {"$first" : "$Customer.Name"},
        #         'Email' : {"$first" : "$User.Email"}
        #     }
        # })
        # facet['$facet']['data'].append({"$addFields" : {"BelumDicairkan" : {"$subtract" : ["$Penghasilan","$Dicairkan"]}}})
        # facet['$facet']['data'].append({"$sort" : {"Downline" : -1}})
        facet['$facet']['data'].append({ "$skip": args_filter['numberPage'] * (args_filter['page'] -1) })
        facet['$facet']['data'].append({ "$limit": args_filter['numberPage'] })
        # facet['$facet']['metadata'].append({ 
        #     "$group": {
        #         "_id": "$UserId",
        #     }
        # })
        
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$size": "$metadata" }
        }})
        
        result = Affiliate.objects.aggregate(*pipeline,allowDiskUse=True)
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