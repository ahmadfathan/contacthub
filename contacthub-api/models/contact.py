# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string
from .credit import Credit

class Contact(Document):
    UserId = StringField(required=True)
    CustomerId = StringField(required=True)
    CreatedAt = DateTimeField(required=True)

    def create(self,UserId:str,CustomerId:str,UserIdCustomer:str,Price:float,PhoneFriend=None,FriendName=None,MyName=None):

        CreatedAt = datetime.now
        get_data = Contact.objects(UserId=UserId,CustomerId=CustomerId).first()
        if get_data is None:
            post_data = Contact(**{'UserId' : UserId,'CustomerId':CustomerId,'CreatedAt' : CreatedAt}).save()
        else:
            return False

        if post_data:
            # potong saldo

            Credit.create(self,UserId=UserId,CreatedAt=CreatedAt,CreatedBy=UserId,Status='success',Debit=Price,Kredit=0,RefNo=str(post_data.id),Description="Pengurangan credit sebesar -Rp%s untuk menyimpan kontak %s" % (Price,FriendName),Tag='save_contact')
            Credit.create(self,UserId=UserIdCustomer,CreatedAt=CreatedAt,CreatedBy=UserId,Status='success',Debit=Price,Kredit=0,RefNo=str(post_data.id),Description="Pengurangan credit sebesar -Rp%s untuk disimpan Kontakmu di User lain (%s)" % (Price, MyName),Tag='share_contact')

            return str(post_data.id)
        else:
            return False

    def delete(self,ContactId:str):
        return Contact.objects(id=ObjectId(ContactId)).delete()
    
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"ContactId" : {"$toString" : "$_id"}}})
        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})
        
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$project" : {
            "_id" : 0
        }})
        
        result = Contact.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def summary(self,**args_filter):
        pipeline = []
        
        pipeline.append({"$addFields" : {"CreatedAtDateOnly" : {"$dateToString":{"format": "%Y-%m-%d","date":"$CreatedAt"}}}})

        for fil in args_filter:
            pipeline.append({"$match" : {fil : args_filter[fil]}})

        pipeline.append({
            "$group" : {
                "_id" : "$UserId",
                "Total" : {"$sum" : 1}
            }
        })
        result = Contact.objects.aggregate(pipeline)
        total_save = json.loads(json_util.dumps(result))
        if len(total_save) > 0:
            return total_save[0]['Total']
        else:
            return 0

    def get_summary(self,UserId:str,CustomerId:str):
        total_save = Contact.summary(self,**{'UserId' : UserId})
        total_share = Contact.summary(self,**{'CustomerId' : CustomerId})
        total_save_day = Contact.summary(self,**{'UserId' : UserId,'CreatedAtDateOnly' : datetime.strftime(datetime.now(),'%Y-%m-%d')})
        total_share_day = Contact.summary(self,**{'CustomerId' : CustomerId,'CreatedAtDateOnly' : datetime.strftime(datetime.now(),'%Y-%m-%d')})
        result = {
            'TotalSave' : total_save,
            'TotalShare' : total_share,
            'TotalSaveDay' : total_save_day,
            'TotalShareDay' : total_share_day,
        }
        return result

    def get_paginate(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"ContactId" : {"$toString" : "$_id"}}})
        pipeline.append({"$addFields" : {"UserId" : {"$toObjectId" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CustomerId" : {"$toObjectId" : "$CustomerId"}}})

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
                "from": "customer",
                "localField": "CustomerId",
                "foreignField": "_id",
                "as": "CustomerProfile"
            }
        })
        pipeline.append({"$unwind" : "$CustomerProfile"})
        pipeline.append({"$addFields" : {"CustomerProfile.UserId" : {"$toObjectId" : "$CustomerProfile.UserId"}}})
        pipeline.append({
            "$lookup" :  {
                "from": "user",
                "localField": "CustomerProfile.UserId",
                "foreignField": "_id",
                "as": "UserProfile"
            }
        })
        pipeline.append({"$unwind" : "$UserProfile"})
        pipeline.append({"$addFields" : {"CustomerId" : {"$toString" : "$CustomerId"}}})
        pipeline.append({"$addFields" : {"Customer.CustomerId" : {"$toString" : "$Customer._id"}}})
        pipeline.append({"$addFields" : {"CustomerProfile.CustomerId" : {"$toString" : "$CustomerProfile._id"}}})
        pipeline.append({"$addFields" : {"CustomerProfile.UserId" : {"$toString" : "$CustomerProfile.UserId"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        
        pipeline.append({"$addFields" : {"Contact.UserId" : "$CustomerProfile.UserId"}})
        pipeline.append({"$addFields" : {"Contact.Nickname" : "$UserProfile.Nickname"}})
        pipeline.append({"$addFields" : {"Contact.CustomerId" : "$CustomerProfile.CustomerId"}})
        pipeline.append({"$addFields" : {"Contact.Name" : "$CustomerProfile.Name"}})
        pipeline.append({"$addFields" : {"Contact.Email" : "$UserProfile.Email"}})

        pipeline.append({"$addFields" : {"Profile.UserId" : "$Customer.UserId"}})
        pipeline.append({"$addFields" : {"Profile.Nickname" : "$User.Nickname"}})
        pipeline.append({"$addFields" : {"Profile.CustomerId" : "$Customer.CustomerId"}})
        pipeline.append({"$addFields" : {"Profile.Name" : "$Customer.Name"}})
        pipeline.append({"$addFields" : {"Profile.Email" : "$User.Email"}})
        
        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
        
        pipeline.append({"$project" : {
            "_id" : 0,
            "Profile" : 1,
            "Contact" : 1,
            "ContactId" : 1,
            "CreatedAt" : 1

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
        
        result = Contact.objects.aggregate(*pipeline,allowDiskUse=True)
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
    
    def total_contact_save(self,UserId:str):
        today = datetime.now().strftime("%Y-%m-%d")
        pipeline = []

        pipeline.append({"$match" : {"UserId" : UserId}})

        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d","date":"$CreatedAt"}} }})
        pipeline.append({"$match" : {"CreatedAt" : datetime.strptime(today, "%Y-%m-%d")}})
        pipeline.append({"$group" : {
            "_id" : "$_id",
            "count" : {"$sum" : 1}
        }})
        result = Contact.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))
        return result
    
    def total_contact_share(self,CustomerId:str):
        today = datetime.now().strftime("%Y-%m-%d")
        pipeline = []
        pipeline.append({"$match" : {"CustomerId" : CustomerId}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d","date":"$CreatedAt"}} }})

        pipeline.append({"$match" : {"CreatedAt" : datetime.strptime(today, "%Y-%m-%d")}})
        pipeline.append({"$group" : {
            "_id" : "$_id",
            "count" : {"$sum" : 1}
        }})
        result = Contact.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))
        return result
    