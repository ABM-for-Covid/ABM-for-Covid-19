from flask import Flask, request, jsonify
from werkzeug.exceptions import HTTPException
import os
import json
from multiprocessing import Process
import subprocess
from Constants import *
from parse_exp import *
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


@app.route('/run')
def get_experiment():
    data = request.json
    env = data.get('env', 'prod')
    if env == 'prod':
        home = serve_home
    experiment = data.get('experiment')
    result_file = get_result_file(data)
    #create a resfile from the experiment name
    data['resultfile'] = result_file
    global p
    p = Process(target=export.run_abm_process, args=(data))
    p.start()
    #todo - change the filepath to filelink in server, which is also serverd and appended.
    return "Path of result_file {}".format(result_file), 200



if __name__ == '__main__':
    if Constant.env == 'prod':
        app.run(host='0.0.0.0', port=8080)
    else:
        app.run(host='0.0.0.0', port=8080, debug=True)



