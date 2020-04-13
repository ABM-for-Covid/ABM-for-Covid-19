import subprocess
import os
import json
home = "/home/ubuntu/covid-19-Multiagent-Simulations"
exp_file = "/Users/rusty/Desktop/covid-19/covid19_abm/experiments/exp11.json"
print "running for exp", exp_file
try:
#     os.chdir("src/")
    command = "java abmforcovid/RunABM {}".format(exp_file)
    print "running command", command
    res = subprocess.check_output(command, shell=True)
    print (res)
except Exception as e:
    print (e)
