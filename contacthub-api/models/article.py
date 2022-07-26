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

class Article(Document):
    Tag = ListField(null=True)
    Title = StringField(required=True)
    Description = StringField(required=True)
    Image = StringField(null=True)
    Slug = StringField(required=True,unique=True)
    CreatedAt = DateTimeField(required=True)
    CreatedBy = StringField(required=True)
    UpdatedAt = DateTimeField(null=True)
    UpdatedBy = StringField(null=True)
    Category = StringField(null=True)
    Status = StringField(null=True,choices=('draft','publish','unpublish'))

    meta = {
        'indexes': [
            {
                'fields': ['$Title', "$Description"],
                'default_language': 'english',
            }
        ]
    }

    def generate_slug(self,Title:str,ExcludeId=None) -> str:
        is_exist = True
        slug_ori = Title.replace('/','-l-')
        slug = slug_ori
        index_val = 0
        while(is_exist):
            slug = slugify(slug)
            if ExcludeId is None:
                check_slug = Article.objects(Slug=slug).first()
            else:
                check_slug = Article.objects(Slug=slug,id__ne=ObjectId(ExcludeId)).first()

            if check_slug is None:
                is_exist = False
            else:
                rand_value = "%s%s" % ("-",str(index_val))
                slug = "%s%s" % (slug_ori,rand_value)
                is_exist = True
                index_val = index_val + 1
        return slug

    def create(self,Title:str,Description:str,CreatedAt,CreatedBy,Status='draft',Slug=None,Tag=None,Image=None,Category=None):
        if Slug is None:
            Slug = Article.generate_slug(self,Title=Title)
            
        post_data = Article(**{
            'Tag' : Tag,
            'Image' : Image,
            'Title':Title,
            'Description' : Description,
            'Slug':Slug,
            'CreatedAt':CreatedAt,
            'CreatedBy':CreatedBy,
            'Category':Category,
            'Status':Status
        }).save()
        if post_data:
            return Article.get_data(self,**{'ArticleId' : str(post_data.id)})[0]
        else:
            return False
    
    def update(self, ArticleId:str, **data):
        param = {}
        for x in data:
            param['%s%s' % ('set__',x)] = data[x]
        
        update_data = Article.objects(id=ObjectId(ArticleId)).update(**param)
        if update_data:
            return Article.get_data(self,**{'ArticleId':ArticleId})[0]
        else:
            return False
    
    def delete(self,**data):
        return Article.objects(**data).delete()
        
    def get_data(self,**args_filter):
        pipeline = []
        pipeline.append({"$addFields" : {"ArticleId" : {"$toString" : "$_id"}}})
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
        result = Article.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result
    
    def get_paginate(self,**args_filter):
        pipeline = []
        if 'search' in args_filter and not args_filter['search'] is None:
            if str(args_filter['search']).strip() != "":
                pipeline.append({ "$match" : { "$text": { "$search": args_filter["search"] } }})


        pipeline.append({"$addFields" : {"ArticleId" : {"$toString" : "$_id"}}})
        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"CreatedAt" : regex},
                    {"UpdatedAt" : regex},
                    {"Title" : regex},
                    {"Description" : regex},
                    {"Slug" : regex},
                    {"Category" : regex},
                    {"Status" : regex}
                ] 
            }})

        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})
                
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
        
        result = Article.objects.aggregate(*pipeline,allowDiskUse=True)
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