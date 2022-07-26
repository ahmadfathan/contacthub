# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField)
from datetime import datetime
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string,mongodatetime_to_stringformat

class Menu(Document):
    Title = StringField(required=True)
    LinkType = StringField(required=True,choices=('page','link'))
    Uri = StringField(null=True,required=True)
    Position = IntField(null=True,required=True)
    Icon = StringField(null=True)
    ModuleName = StringField(null=True)
    IsParent = BooleanField(required=True)
    IsVisible = BooleanField(required=True)
    CreatedAt = DateTimeField(null=True)
    ParentId = StringField(null=True)
    UpdatedAt = DateTimeField(null=True)
    IsAdministrator = BooleanField(null=True)


    def create(self,**args)-> Document:
        result = Menu(**args).save()
        if result:
            item = json.loads(result.to_json())
            item['_id'] = str(result['id'])
            if 'CreatedAt' in item and not item['CreatedAt'] is None:
                item['CreatedAt'] = mongodatetime_to_stringformat(item['CreatedAt'],'%Y-%m-%d %H:%M:%S')
            if 'UpdatedAt' in item and not item['UpdatedAt'] is None:
                item['UpdatedAt'] = mongodatetime_to_stringformat(item['UpdatedAt'],'%Y-%m-%d %H:%M:%S')
                
            return item
        return result

    def by_id(self,menu_id:str) -> Document:
        result = Menu.objects(id=ObjectId(menu_id))
        item = []
        if result:
            result = result.get()
            item = json.loads(result.to_json())
            item['_id'] = str(result['id'])
            if 'ParentId' in result:
                item['ParentId'] = str(result['ParentId'])
            if 'CreatedAt' in item and not item['CreatedAt'] is None:
                item['CreatedAt'] = mongodatetime_to_stringformat(item['CreatedAt'],'%Y-%m-%d %H:%M:%S')
            if 'UpdatedAt' in item and not item['UpdatedAt'] is None:
                item['UpdatedAt'] = mongodatetime_to_stringformat(item['UpdatedAt'],'%Y-%m-%d %H:%M:%S')
        return result,item

    def getAll(self,**args) -> Document:
        args_filter = {}

        for f in args:
            if f == 'id':
                args_filter[f] = ObjectId(args[f])
            else:    
                args_filter[f] = args[f]
            
        result = Menu.objects(**args_filter).order_by('Position').all()
        item_data = []
        if result:
            item_data = []
            for r in result:    
                item = json.loads(r.to_json())
                item['_id'] = str(r.id)
                
                # if 'ParentId' in r:
                #     item['ParentId'] = str(r.ParentId)
                if 'CreatedAt' in item and not item['CreatedAt'] is None:
                    item['CreatedAt'] = mongodatetime_to_stringformat(item['CreatedAt'],'%Y-%m-%d %H:%M:%S')
                if 'UpdatedAt' in item and not item['UpdatedAt'] is None:
                    item['UpdatedAt'] = mongodatetime_to_stringformat(item['UpdatedAt'],'%Y-%m-%d %H:%M:%S')
                item_data.append(item)
        return result,item_data

    def update_data(self,menu_id:str,**args)-> Document:
        result = Menu.objects(id=ObjectId(menu_id)).update(**args)
        if result:
            result,i = Menu.by_id(self,menu_id=menu_id)
            return i
        return result
        
    def get(self,**args)-> Document:
        result = Menu.objects.order_by('Name')
        paginate = {}
        if 'page' in args:
            paginate['page'] = args['page']
        if 'per_page' in args:
            paginate['per_page'] = args['per_page']
    
        if 'keyword' in args:
            result = result.filter(Q(Title__icontains=args['keyword']))
        
        if 'order_by' in args:
            result = result.order_by(args['order_by'])

        if paginate != {}:
            result = result.paginate(**paginate) 
        
        a = result
        items = []
        for item in a.items:
            i = json.loads(item.to_json())
            i['_id'] = str(item['id'])
            if 'ParentId' in item:
                i['ParentId'] = str(item['ParentId'])
            if 'CreatedAt' in item:
                i['CreatedAt'] = mongodatetime_to_stringformat(item['CreatedAt'],'%Y-%m-%d %H:%M:%S')
            if 'UpdatedAt' in item:
                i['UpdatedAt'] = mongodatetime_to_stringformat(item['UpdatedAt'],'%Y-%m-%d %H:%M:%S')
            items.append(i)
        
        a.items = items
        return result