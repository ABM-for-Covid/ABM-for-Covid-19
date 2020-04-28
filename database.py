import firebase_admin
from firebase_admin import credentials
import conf
import pyrebase

firebase = pyrebase.initialize_app(conf.FireBaseConfig)
db = firebase.database()

def write_result(experiment, data):
    day = data.get("day")
    db.child('experiments').child(experiment).child(day).update(data)
