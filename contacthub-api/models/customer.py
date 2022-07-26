# mongo-engine packages
from bson.objectid import ObjectId
from flask_mongoengine import Document
from mongoengine import (StringField,DateTimeField,ListField,BooleanField,IntField,EmbeddedDocumentField,EmbeddedDocument)
from datetime import datetime,timedelta
from bson import json_util
import json, re
from mongoengine.queryset.visitor import Q
from helpers import get_random_alphanumeric_string,hash_login,hash_string
from .user import User 
from .token import Token 
from .credit import Credit 
from .contact import Contact 
from .affiliate import Affiliate 
from random import randint

class ContactSaveEmbedded(EmbeddedDocument):
    Type = StringField(required=True,choices=('random','interest','other'))
    InterestId = ListField(null=True)
    OtherKey = StringField(null=True)
    OtherValue = StringField(null=True)

class Customer(Document):
    Name = StringField(required=True)
    Greeting = StringField(null=True)
    Address = StringField(null=True)
    CityId = StringField(null=True)
    WhatsApp = StringField(null=True)
    Facebook = StringField(null=True)
    Instagram = StringField(null=True)
    Website = StringField(null=True)
    Gender = StringField(null=True)
    DateOfBirth = StringField(null=True)
    ProfessionId = StringField(null=True)
    Hoby = StringField(null=True)
    Religion = StringField(null=True)
    InterestId = ListField(null=True)
    RelationshipStatus = StringField(null=True)
    BusinessName = StringField(null=True)
    BusinessTypeId = StringField(null=True)
    Product = StringField(null=True)
    UserId = StringField(required=True)
    UpdatedAt = DateTimeField(null=True)
    CreatedAt = DateTimeField(null=True)
    IsOwner = BooleanField(null=True)
    AccountId = StringField(required=True)
    MarketingCode = StringField(required=True)
    AllowedShareProfile = BooleanField(null=True)
    SaveContactFriendBy = EmbeddedDocumentField(ContactSaveEmbedded,null=True) # menyimpan kontak teman
    SaveMyContactBy = EmbeddedDocumentField(ContactSaveEmbedded,null=True) # kontak saya disimpan oleh teman (dibagikan ke teman)
    LimitSaveMyContactDay = IntField(null=True)
    LimitSaveContactFriendDay = IntField(null=True)
    Foto = StringField(null=True)
    IsCompleted = BooleanField(null=True)
    Tokopedia = StringField(null=True)
    Bukalapak = StringField(null=True)
    Shopee = StringField(null=True)
    CoverContact = StringField(null=True)

    def count_all_member(self):
        pipeline = []
        pipeline.append({"$group" : {
            "_id" : 0,
            "Count" : {"$sum" : 1}
        }})
        result = Customer.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result

    def get_new_contact(self,UserId:str,BalanceMinimum:int,Type:str,Key=None,Value=None):
        customer_yang_akan_save = Customer.objects(UserId=UserId).first()
        my_interest = customer_yang_akan_save['InterestId']
        if my_interest is None:
            if type(my_interest) is list:
                my_interest = []
            else:
                print("Bukan List")
        # print(Type)
        if Type == 'random':
            # cari contact yang masih tersedia limit FriendDaynya, balance, AllowedShareProfile, IsCompleted
            # SaveContactFriendBy Type random
            list_balance = Credit.balance(self)
            if list_balance:
                customer_ready = []
                for balance in list_balance:
                    if balance['Balance'] >= BalanceMinimum:
                        customer_ready.append(ObjectId(balance['Customer']['CustomerId']))
                
                exclude_customer_id = []
                customer_contact = Contact.objects(UserId=UserId).all()
                for cus in customer_contact:
                    exclude_customer_id.append(ObjectId(cus['CustomerId']))
                profile_customer = None
                # cek apakah Profile memenuhi atau tidak

                # cari yang friend savenya berdasarkan interest dan interestnya sesuai dengan yang akan save
                profile_customer = None
                if len(my_interest) > 0:
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                        SaveMyContactBy__Type='interest',SaveMyContactBy__InterestId__in=my_interest).first()

                # cari yang friend savenya berdasarkan profile dan profilenya sesuai dengan yang akan save
                if profile_customer is None:
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                        SaveMyContactBy__Type='other').first()
                    if not profile_customer is None:
                        profile_key = profile_customer['SaveContactFriendBy']['OtherKey']
                        profile_value = profile_customer['SaveContactFriendBy']['OtherValue']
                        if profile_key == 'Gender' and profile_value == customer_yang_akan_save['Gender']:
                            pass
                        elif profile_key == 'Religion' and profile_value == customer_yang_akan_save['Religion']:
                            pass
                        elif profile_key == 'CityId' and profile_value == customer_yang_akan_save['CityId']:
                            pass
                        else:
                            profile_customer = None

                # cari yang friend savenya random
                if profile_customer is None:
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                        SaveMyContactBy__Type='random').first()

                if not profile_customer is None:
                    limit_share_contact = profile_customer['LimitSaveContactFriendDay']
                    # cek limitnya tersedia atau tidak
                    total_contact_share_today = Contact.total_contact_share(self,CustomerId=balance['Customer']['CustomerId'])
                    if len(total_contact_share_today) < limit_share_contact:
                        return profile_customer
                        
                
            else:
                return False
        elif Type == 'interest':
            # cari contact yang masih tersedia limit FriendDaynya, balance, AllowedShareProfile, IsCompleted
            # SaveContactFriendBy Type interest
            # InterestId.nya sesuai Value
            list_balance = Credit.balance(self)
            if list_balance:
                customer_ready = []
                for balance in list_balance:
                    if balance['Balance'] >= BalanceMinimum:
                        customer_ready.append(ObjectId(balance['Customer']['CustomerId']))

                exclude_customer_id = []
                customer_contact = Contact.objects(UserId=UserId).all()
                for cus in customer_contact:
                    exclude_customer_id.append(ObjectId(cus['CustomerId']))

                profile_customer = None
                # cek apakah Profile memenuhi atau tidak
                # cari friend yang savenya berdasarkan interest yang diinginkan dan interestnya sesuai dengan yang akan save
                profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                    LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                    SaveMyContactBy__Type='interest',SaveMyContactBy__InterestId__in=my_interest,InterestId__in=Value).first()
                
                # cari friend yang savenya berdasarkan profile dan profilenya sesuai dengan yang akan save dan friendnya memiliki interest yang diinginkan
                if profile_customer is None:
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                        SaveMyContactBy__Type='other',InterestId__in=Value).first()
                    if not profile_customer is None:
                        profile_key = profile_customer['SaveContactFriendBy']['OtherKey']
                        profile_value = profile_customer['SaveContactFriendBy']['OtherValue']
                        if profile_key == 'Gender' and profile_value == customer_yang_akan_save['Gender']:
                            pass
                        elif profile_key == 'Religion' and profile_value == customer_yang_akan_save['Religion']:
                            pass
                        elif profile_key == 'CityId' and profile_value == customer_yang_akan_save['CityId']:
                            pass
                        else:
                            profile_customer = None
                
                # cari friend yang tipenya random dan interestnya sesuai yang ingin disave
                if profile_customer is None:
                    # cari yang typenya random dan interestnya sesuai
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready,\
                        SaveMyContactBy__Type='random',InterestId__in=Value).first()

                if not profile_customer is None:
                    limit_share_contact = profile_customer['LimitSaveContactFriendDay']
                    # cek limitnya tersedia atau tidak
                    total_contact_share_today = Contact.total_contact_share(self,CustomerId=balance['Customer']['CustomerId'])
                    if len(total_contact_share_today) < limit_share_contact:
                        return profile_customer
                
            else:
                return False
        elif Type == 'other':
            # cari contact yang masih tersedia limit FriendDaynya, balance, AllowedShareProfile, IsCompleted
            # SaveContactFriendBy Type other
            list_balance = Credit.balance(self)
            if list_balance:
                customer_ready = []
                for balance in list_balance:
                    if balance['Balance'] >= BalanceMinimum:
                        customer_ready.append(ObjectId(balance['Customer']['CustomerId']))
                        
                exclude_customer_id = []
                customer_contact = Contact.objects(UserId=UserId).all()
                for cus in customer_contact:
                    exclude_customer_id.append(ObjectId(cus['CustomerId']))                        

                # cek apakah Profile memenuhi atau tidak
                # cari friend yang savenya berdasarkan interest yang diinginkan dan profilenya sesuai dengan yang akan save
                profile_customer = None
                if Key == 'CityId':
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                        SaveMyContactBy__Type='interest',SaveMyContactBy__InterestId__in=my_interest,
                        CityId=Value).first()
                elif Key == 'Religion':
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                        SaveMyContactBy__Type='interest',SaveMyContactBy__InterestId__in=my_interest,
                        Religion=Value).first()
                elif Key == 'Gender':
                    profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                        SaveMyContactBy__Type='interest',SaveMyContactBy__InterestId__in=my_interest,
                        Gender=Value).first()

                # cari friend yang savenya berdasarkan profile dan profilenya sesuai dengan yang akan save dan friendnya memiliki interest yang diinginkan
                if profile_customer is None:
                    if Key == 'CityId': 
                        # Gender
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='Gender',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['Gender'], \
                            CityId=Value).first()
                        # Religion
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='Religion',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['Religion'], \
                            CityId=Value).first()
                        # CityId
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='CityId',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['CityId'], \
                            CityId=Value).first()
                    elif Key == 'Religion': 
                        # Gender
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='Gender',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['Gender'], \
                            Religion=Value).first()

                        # Religion
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='Religion',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['Religion'], \
                            Religion=Value).first()

                        # CityId
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='CityId',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['CityId'], \
                            Religion=Value).first()

                    elif Key == 'Gender': 
                        # Gender
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='Gender',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['Gender'], \
                            Gender=Value).first()
                        # Religion
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='Religion',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['Religion'], \
                            Gender=Value).first()
                        # CityId
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready, \
                            SaveMyContactBy__Type='other',SaveMyContactBy__OtherKey='CityId',\
                            SaveMyContactBy__OtherValue=customer_yang_akan_save['CityId'], \
                            Gender=Value).first()

                    if not profile_customer is None:
                        profile_key = profile_customer['SaveContactFriendBy']['OtherKey']
                        profile_value = profile_customer['SaveContactFriendBy']['OtherValue']
                        if profile_key == 'Gender' and profile_value == customer_yang_akan_save['Gender']:
                            pass
                        elif profile_key == 'Religion' and profile_value == customer_yang_akan_save['Religion']:
                            pass
                        elif profile_key == 'CityId' and profile_value == customer_yang_akan_save['CityId']:
                            pass
                        else:
                            profile_customer = None
                
                # cari friend yang tipenya random dan profilenya sesuai yang ingin disave
                if profile_customer is None:
                    # cari yang typenya random dan profilenya sesuai
                    if Key == 'CityId': 
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready,\
                            SaveMyContactBy__Type='random',CityId=Value).first()
                    elif Key == 'Religion': 
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready,\
                            SaveMyContactBy__Type='random',Religion=Value).first()
                    elif Key == 'Gender': 
                        profile_customer = Customer.objects(id__nin=exclude_customer_id,UserId__ne=UserId,AllowedShareProfile=True,IsCompleted=True, \
                            LimitSaveMyContactDay__gt=0,id__in=customer_ready,\
                            SaveMyContactBy__Type='random',Gender=Value).first()
                    
                if not profile_customer is None:
                    limit_share_contact = profile_customer['LimitSaveContactFriendDay']
                    # cek limitnya tersedia atau tidak
                    total_contact_share_today = Contact.total_contact_share(self,CustomerId=balance['Customer']['CustomerId'])
                    if len(total_contact_share_today) < limit_share_contact:
                        return profile_customer
            else:
                return False
        
        return False


    def AvailableCreditSave(self, balanceMinimum=0):
        list_balance = Credit.balance(self)
        list_customer_available = []
        if list_balance:
            for balance in list_balance:
                if balance['Balance'] >= balanceMinimum:
                    # cek apakah Profile memenuhi atau tidak
                    profile_customer = Customer.objects(AllowedShareProfile=True,IsCompleted=True, \
                        LimitSaveMyContactDay__gt=0,id=ObjectId(balance['Customer']['CustomerId'])).first()
                    if not profile_customer is None:
                        limit_save_contact = profile_customer['LimitSaveMyContactDay']
                        # cek limitnya tersedia atau tidak
                        total_contact_save_today = Contact.total_contact_save(self,UserId=profile_customer['UserId'])
                        if len(total_contact_save_today) < limit_save_contact:
                            list_customer_available.append({
                                'Balance' : balance['Balance'],
                                'CustomerId' : balance['Customer']['CustomerId'],
                                'UserId' : profile_customer['UserId'],
                                'LimitSaveMyContactDay' : profile_customer['LimitSaveMyContactDay'],
                                'LimitSaveContactFriendDay' : profile_customer['LimitSaveContactFriendDay'],
                                'SaveContactFriendBy' : profile_customer['SaveContactFriendBy'],
                                'SaveMyContactBy' : profile_customer['SaveMyContactBy']
                            })
            
            return list_customer_available
        else:
            return False
        
    def get_data(self,**args_filter):
        pipeline = []
        for fil in args_filter:
            if fil == 'CustomerId':
                pipeline.append({"$match" : {
                    "$or"  : [
                        {'_id' : ObjectId(args_filter[fil])},
                        {"UserId" : args_filter[fil]}
                    ]
                }})
            else:    
                pipeline.append({"$match" : {fil : args_filter[fil]}})

        pipeline.append({"$addFields" : {"CustomerId" : {"$toString" : "$_id"}}})
        pipeline.append({"$addFields" : {"UserId" : {"$toObjectId" : "$UserId"}}})
        pipeline.append({
            "$addFields" : {
                "CityId": { "$convert": { "input": "$CityId", "to": "int", "onError": "$CityId", "onNull": "$CityId" } }
            }
        })

        pipeline.append({"$addFields" : {
            "LimitSaveMyContactDay": { "$convert": { "input": "$LimitSaveMyContactDay", "to": "int", "onError": "$LimitSaveMyContactDay", "onNull": "$LimitSaveMyContactDay" } }
            }
        })
        pipeline.append({"$addFields" : {
            "LimitSaveContactFriendDay": { "$convert": { "input": "$LimitSaveContactFriendDay", "to": "int", "onError": "$LimitSaveContactFriendDay", "onNull": "$LimitSaveContactFriendDay" } }
            }
        })
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
                "from": "city",
                "localField": "CityId",
                "foreignField": "_id",
                "as": "City"
            }
        })
        
        pipeline.append({"$unwind" : "$User"})
        pipeline.append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        pipeline.append({"$addFields" : {"CityElemAt" : {"$arrayElemAt" : ["$City",0]}}})

        pipeline.append({
            "$addFields" : {
                "CityName" : {
                    "$cond" : [
                        {"$gt" : [{"$size" : "$City"},0]},
                        "$CityElemAt.City",
                        None
                    ]
                }
            }
        })
        pipeline.append({
            "$addFields" : {
                "ProvinceId" : {
                    "$cond" : [
                        {"$gt" : [{"$size" : "$City"},0]},
                        "$CityElemAt.ProvinceId",
                        None
                    ]
                }
            }
        })
        pipeline.append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        pipeline.append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        pipeline.append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        pipeline.append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        pipeline.append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})

        pipeline.append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "CityElemAt" : 0,
            "City" : 0,
            "User.Password" : 0
        }})
        result = Customer.objects.aggregate(pipeline)
        result = json.loads(json_util.dumps(result))

        return result

    def login(self,Username:str,Password:str,RoleId:str):
        password = hash_login(Password)
        user = User.objects((Q(Username=Username) | Q(Email=Username)) & Q(Password=password) & Q(RoleId=RoleId)).first()
        if user is None:
            return False
        else:
            customer =  Customer.get_data(self,**{'UserId':str(user.id)})
            if len(customer) > 0:
                return customer[0]
            else:
                return False

    def delete(self,CustomerId:str):
        customer = Customer.get_data(self,**{'CustomerId' : CustomerId})
        if len(customer) > 0:
            UserId = customer[0]['UserId']
            Customer.objects(id=ObjectId(CustomerId)).delete()
            User.objects(id=ObjectId(UserId)).delete()
            Credit.objects(UserId=UserId).delete()
            Contact.objects(Q(UserId=UserId) | Q(CustomerId=UserId) ).delete()
            Affiliate.objects(UserId=UserId).delete()
            return True
        else:
            return False
    def create_token(self,UserId:str,Platform:str,AccountId:str,IpAddresses=None,IsExpired=False):
        data = {}
        now = datetime.now()
        new_token = randint(1000,9999)
        new_token = str(new_token) + str(now.strftime('%Y%m%d%H%M%S'))
        data['IsExpiredToken'] = IsExpired
        data['Platform'] = Platform
        data['UserId'] = UserId
        data['Key'] = hash_string(new_token)
        data['CreatedAt'] = now
        data['Status'] = 1
        data['IpAddresses'] = IpAddresses
        data['AccountId'] = AccountId

        if 'IsExpiredToken' in data:
            if data['IsExpiredToken']:
                expired_token  = now + timedelta(hours=48)
                data['ExpiredToken'] = expired_token
        
        result = Token.create(self,**data)
        disable_access_token_other = Token.objects(id__ne=ObjectId(result['_id']),UserId=data['UserId']).update(set__Status=2)
        if result:
            return result
        else:
            return False

    def generate_marketing_code(self) -> str:
        is_exist = True
        code = ""
        while(is_exist):
            code = get_random_alphanumeric_string(10)
            affiliate = Customer.objects(MarketingCode=code)
            if affiliate:
                is_exist = True
            else:
                is_exist = False
        return code

    def update(self,UserId:str,**data):
        user_param = {}
        customer_param = {}

        if 'Username' in data: user_param['set__Username'] = data['Username']
        if 'Nickname' in data: user_param['set__Nickname'] = data['Nickname']
        if 'Email' in data: user_param['set__Email'] = data['Email']
        if 'Password' in data: user_param['set__Password'] = hash_login(data['Password'])
        if 'Phone' in data: user_param['set__Phone'] = data['Phone']
        if 'RoleId' in data: user_param['set__RoleId'] = data['RoleId']
        if 'Status' in data: user_param['set__Status'] = data['Status']
        
        if 'Email' in data:
            get_user = User.objects(id=ObjectId(UserId)).first()
            if get_user['Email'].strip() !=  data['Email'].strip():
                get_user = User.objects(id__ne=ObjectId(UserId),Email=data['Email'].strip()).first()
                if not get_user is None:
                    return "email_already"

        key_customer = ['Name','Greeting','Address','CityId','WhatsApp','Facebook', \
            'Instagram','Website','Gender','DateOfBirth','ProfessionId','Hoby', \
            'Religion','InterestId','RelationshipStatus','BusinessName','BusinessTypeId', \
            'Product','UpdatedAt','AllowedShareProfile','SaveContactFriendBy', \
            'SaveMyContactBy','LimitSaveMyContactDay','LimitSaveContactFriendDay','Foto','IsCompleted', \
            'SaveMyContactInterest','SaveContactFriendInterest','SaveMyContactOtherKey','SaveMyContactOtherValue', \
            'SaveContactFriendOtherKey','SaveContactFriendOtherValue', 'Tokopedia','Bukalapak','Shopee']
            
        for x in key_customer:
            if x in data:
                if x == 'SaveMyContactBy' :
                    customer_param['set__%s' % x]  = {
                        'Type' : data[x],
                        'InterestId' : data['SaveMyContactInterest'],
                        'OtherKey' : data['SaveMyContactOtherKey'],
                        'OtherValue' : data['SaveMyContactOtherValue']

                    }
                elif x == 'SaveContactFriendBy':
                    customer_param['set__%s' % x]  = {
                        'Type' : data[x],
                        'InterestId' : data['SaveContactFriendInterest'],
                        'OtherKey' : data['SaveContactFriendOtherKey'],
                        'OtherValue' : data['SaveContactFriendOtherValue']
                    }
                elif x == 'SaveMyContactInterest' or x == 'SaveContactFriendInterest' or \
                    x == 'SaveMyContactOtherKey' or x == 'SaveMyContactOtherValue' or \
                    x == 'SaveContactFriendOtherKey' or x == 'SaveContactFriendOtherValue':
                    pass
                else:
                    if x == 'LimitSaveMyContactDay' or x == 'LimitSaveContactFriendDay':
                        if str( data[x]).strip() == "":
                            data[x] = int(0) 
                        else:
                            data[x] = str(data[x]).split(".")
                            if len(data[x]) > 0:
                                data[x] = data[x][0]
                                
                            data[x] = int(data[x])
                            
                    customer_param['set__%s' % x]  = data[x]

        if user_param != {}:
            user_param['UpdatedAt'] = datetime.now
            update_user = User.objects(id=ObjectId(UserId)).update(**user_param)
            if update_user:
                pass
            else:
                return False
        else:
            pass
        if customer_param != {}:
            customer_param['UpdatedAt'] = datetime.now
            update_customer = Customer.objects(UserId=UserId).update(**customer_param)
            if update_customer:
                pass
            else:
                return False
            
        return Customer.get_data(self, **{'UserId' : UserId})[0]
    def register(self,Nickname:str,Email:str,Password:str, \
        AccountId:str,IsOwner:bool,Platform:str,RoleId:str,Status="inactive",Name=None,Address=None, \
            Greeting=None,CityId=None, WhatsApp=None,Facebook=None,Instagram=None,Website=None, \
            Gender=None,DateOfBirth=None,ProfessionId=None,Hoby=None,Religion=None,InterestId=None, \
            RelationshipStatus=None,BusinessName=None,BusinessTypeId=None,Product=None,
            AllowedShareProfile=None,SaveContactFriendBy=None,SaveMyContactBy=None,
            LimitSaveMyContactDay=None,LimitSaveContactFriendDay=None,Username=None):
        # check username
        check_username = User.objects(Username=Username).first()
        if not check_username is None:
            return "username_already"
        
        # check email
        check_email = User.objects(Email=Email).first()
        if not check_email is None:
            return "email_already"
        
        data_user = {
            'Nickname' : Nickname,
            'Username' : Username,
            'Email' : Email,
            'Password' : hash_login(Password),
            'RoleId' : RoleId,
            'Status' : Status, # 1 = tidak aktif, 2 = aktif, 3=blokir
            'CreatedAt' : datetime.now,
        }
        register_user = User(**data_user).save()

        if IsOwner:
            AccountId = str(ObjectId())
        
        if register_user:
            MarketingCode = Customer.generate_marketing_code(self)
            data_customer = {
                "Name" : Name,                
                "Greeting" : Greeting,          
                "Address" : Address,                   
                "CityId" : CityId,                
                "WhatsApp" : WhatsApp,                
                "Facebook" : Facebook,                
                "Instagram" : Instagram,                
                "Website" : Website,                
                "Gender" : Gender,                   
                "DateOfBirth" : DateOfBirth,                   
                "ProfessionId" : ProfessionId,                   
                "Hoby" : Hoby,                   
                "Religion" : Religion,                   
                "InterestId" : InterestId,                   
                "RelationshipStatus" : RelationshipStatus,                   
                "BusinessName" : BusinessName,                
                "BusinessTypeId" : BusinessTypeId,                
                "Product" : Product,                
                "MarketingCode" : MarketingCode,                
                "AllowedShareProfile" : AllowedShareProfile,                
                "SaveContactFriendBy" : SaveContactFriendBy,                
                "SaveMyContactBy" : SaveMyContactBy,                
                "LimitSaveMyContactDay" : LimitSaveMyContactDay,                
                "LimitSaveContactFriendDay" : LimitSaveContactFriendDay,                
                "UserId" : str(register_user.id),                
                "CreatedAt" : datetime.now,         
                "IsOwner" : IsOwner,                
                "AccountId" : AccountId,                
            }
            register_customer = Customer(**data_customer).save()
            if register_customer:
                # update status User
                User.objects(id=ObjectId(data_customer['UserId'])).update(set__Status="active")
                customer = Customer.get_data(self,**{'UserId' : data_customer['UserId']})
                if len(customer) > 0:
                    return customer[0]
                else:
                    return False
            else:
                return False
        else:
            return False
    
    
    
    def get_paginate(self,**args_filter):
        pipeline = []

        if 'keyword' in args_filter and not args_filter['keyword'] is None:
            regex = re.compile('.*%s.*' % args_filter['keyword'],re.IGNORECASE)
            pipeline.append({"$match" : {
                "$or" : [
                    {"Name" : regex},
                    {"Gender" : regex},
                    {"WhatsApp" : regex}
                ] 
            }})

        if 'filter' in args_filter:
            for fil in args_filter['filter']:
                if fil == 'CustomerId':
                    pipeline.append({"$match" : {'_id' : ObjectId(args_filter['filter'][fil])}})
                elif fil == 'WhereIn':
                    filter_where_in = []
                    for whereIn in args_filter['filter']['WhereIn']:
                        filter_where_in.append({whereIn : args_filter['filter']['WhereIn'][whereIn]})
                    pipeline.append({"$match" : {
                        "$and" : filter_where_in
                    }})
                else:    
                    pipeline.append({"$match" : {fil : args_filter['filter'][fil]}})

        
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
        
        facet['$facet']['data'].append({"$addFields" : {"CustomerId" : {"$toString" : "$_id"}}})
        facet['$facet']['data'].append({"$addFields" : {"UserId" : {"$toObjectId" : "$UserId"}}})
        facet['$facet']['data'].append({"$addFields" : {
            "CityId": { "$convert": { "input": "$CityId", "to": "int", "onError": "$CityId", "onNull": "$CityId" } }
            }
        })
        facet['$facet']['data'].append({"$addFields" : {
            "LimitSaveMyContactDay": { "$convert": { "input": "$LimitSaveMyContactDay", "to": "int", "onError": "$LimitSaveMyContactDay", "onNull": "$LimitSaveMyContactDay" } }
            }
        })
        facet['$facet']['data'].append({"$addFields" : {
            "LimitSaveContactFriendDay": { "$convert": { "input": "$LimitSaveContactFriendDay", "to": "int", "onError": "$LimitSaveContactFriendDay", "onNull": "$LimitSaveContactFriendDay" } }
            }
        })
        facet['$facet']['data'].append({
            "$lookup" :  {
                "from": "user",
                "localField": "UserId",
                "foreignField": "_id",
                "as": "User"
            }
        })

        facet['$facet']['data'].append({
            "$lookup" :  {
                "from": "city",
                "localField": "CityId",
                "foreignField": "_id",
                "as": "City"
            }
        })
        
        facet['$facet']['data'].append({"$unwind" : "$User"})
        facet['$facet']['data'].append({"$addFields" : {"UserId" : {"$toString" : "$UserId"}}})
        facet['$facet']['data'].append({"$addFields" : {"CityElemAt" : {"$arrayElemAt" : ["$City",0]}}})

        facet['$facet']['data'].append({
            "$addFields" : {
                "CityName" : {
                    "$cond" : [
                        {"$gt" : [{"$size" : "$City"},0]},
                        "$CityElemAt.City",
                        None
                    ]
                }
            }
        })
        facet['$facet']['data'].append({
            "$addFields" : {
                "ProvinceId" : {
                    "$cond" : [
                        {"$gt" : [{"$size" : "$City"},0]},
                        "$CityElemAt.ProvinceId",
                        None
                    ]
                }
            }
        })
        facet['$facet']['data'].append({"$addFields" : {"User.UserId" : {"$toString" : "$User._id"}}})
        facet['$facet']['data'].append({"$addFields" : {"CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$CreatedAt"}}}})
        facet['$facet']['data'].append({"$addFields" : {"UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$UpdatedAt"}}}})
        facet['$facet']['data'].append({"$addFields" : {"User.CreatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.CreatedAt"}}}})
        facet['$facet']['data'].append({"$addFields" : {"User.UpdatedAt" : {"$dateToString":{"format": "%Y-%m-%d %H:%M:%S","date":"$User.UpdatedAt"}}}})

        facet['$facet']['data'].append({"$project" : {
            "_id" : 0,
            "User._id" : 0,
            "City" : 0,
            "CityElemAt" : 0,
            "User.Password" : 0

        }})
        pipeline.append(facet)
        pipeline.append({"$project" : {
            "data" :1,
            "totalCount": { "$arrayElemAt": [ '$metadata.total', 0 ] }
        }})
        
        result = Customer.objects.aggregate(*pipeline,allowDiskUse=True)
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