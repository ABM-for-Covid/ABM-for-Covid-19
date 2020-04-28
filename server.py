from flask import Flask, request, jsonify, Response
from werkzeug.exceptions import HTTPException
import json
from multiprocessing import Process
import time
import conf

import Constant
from parse_exp import *

app = Flask(__name__)

try:
    from flask_cors import CORS, cross_origin  # The typical way to import flask-cors

    CORS(app)
except ImportError:
    import os
    parentdir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    os.sys.path.insert(0, parentdir)
    from flask_cors import CORS, cross_origin

    CORS(app)


@app.route('/')
def hello():
    """Return a friendly HTTP greeting."""
    msg = "Dev release April 13 2020"
    return msg, 200


@app.errorhandler(500)
def server_error(e):
    # logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500


@app.route('/run', methods=['POST'])
def get_experiment():
    data = json.loads(request.data)
    experiment = data.get('experiment')
    result_file = get_result_file(data)
    #create a resfile from the experiment name
    data['resultfile'] = "{}/{}".format(home, result_file)
    data['dailyfile'] = "{}/{}".format(home, get_daily_res_file(data))
    global p
    p = Process(target=run_abm_process, args=(data,))
    p.start()
    #todo - change the filepath to filelink in server, which is also serverd and appended.
    return "Path of result_file {}".format(result_file), 200

@app.route('/res')
def get_res():
    experiment = request.args.get('name')
    res_file = "{}/results/{}.csv".format(home, experiment)

    def generate(res_file):
        with open(res_file, 'r') as fp:
            data = fp.readlines()
            last_10 = data[-100:]
            time.sleep(1)
        yield jsonify(last_10)

    return Response(generate(res_file), mimetype='text/csv')



if __name__ == '__main__':
    if Constant.env == 'prod':
        app.run(host='0.0.0.0', port=8080)
    else:
        app.run(host='0.0.0.0', port=8080, debug=True)



