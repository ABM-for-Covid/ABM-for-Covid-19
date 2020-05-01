import conf
import pyrebase

firebase = pyrebase.initialize_app(conf.FireBaseConfig)
db = firebase.database()

def write_result(data):
    experiment = data.pop('experiment')
    day = data.get("day")
    db.child('experiments').child(experiment).child('res').child(day).update(data)

def write_exp(experiment, data):
    db.child('experiments').child(experiment).child('exp').update(data)