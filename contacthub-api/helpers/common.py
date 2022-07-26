
from datetime import datetime
from bson import json_util
import hashlib
import json
import random
import string

def mongodatetime_to_stringformat(dt,format:str) -> str:
    createdAt = json.dumps(dt, default=json_util.default)
    createdAt = int(json.loads(createdAt)['$date']) / 1000
    return datetime.utcfromtimestamp(createdAt).strftime(format)

def hash_string(string,security_key=''):
    """
    Return a SHA-256 hash of the given string
    """
    string.join(security_key)
    return hashlib.sha256(string.encode('utf-8')).hexdigest()

def hash_login(string):
    return hash_string(string,'-contact0hub-')
    
def sha1(string):
    return hashlib.sha1(string.encode('utf-8')).hexdigest()

def validate_key(obj,keys=[],empty=True):
    validate = True
    for x in keys:
        if not x in obj:
            return x
        elif str(obj[x]).strip() == '' and empty==False:
            return x
    return validate
    
def lang(key:str,filename='english'):
    try:
        with open('language/'+filename + '.json') as f:
            language = json.load(f)
            if key in language:
                return language[key]
            else:
                return '{' + key + '} tidak dapat diterjemahkan'
    except Exception as e:
        return str(e)

def load_config(filename='config-local'):
    try:
        with open('config/'+filename+'.json') as f:
            return json.load(f)
    except Exception as e:
        return str(e)

def get_random_alphanumeric_string(length):
    letters_and_digits = string.ascii_letters + string.digits
    result_str = ''.join((random.choice(letters_and_digits) for i in range(length)))
    return result_str