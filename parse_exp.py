from Constant import *
import json
import pandas as pd
import os
import subprocess

class Policies:
    close_borders = "p_close_borders"  
    hospitalization = "p_hospitalization"
    lockdown = "p_lockdown"
    quarantine = "p_quarantine"
    daily_testing = "p_daily_testing"
    contact_tracing = "p_contact_tracing"
    social_distancing = "p_social_distancing"
    exit = "p_exit"
    
class Capacity:
    contact_trace = "c_contact_trace"
    testing = "c_testing"
    hospital_bed = "c_hospital_bed"
    icu_beds = "c_icu_beds"
    
class ATTR:
    false_negative_percent = "a_false_negative_percent"
    social_distancing_efficiency = "a_social_distancing_efficiency"

class Param:
    num_agents = "num_agents" #100;
    sim_cycle_per_day =  "sim_cycle_per_day" #500;
    agent_density = "agent_density" #0.0001;
    hospital_bed_per_agent = "hospital_bed_per_agent" #0.1;
    icu_bed_per_hospital_bed = "icu_bed_per_hospital_bed" #0.05;
    infection_percent = "infection_percent" #0.0;
    distribution_age_min = "distribution_age_min" #1;
    distribution_age_max = "distribution_age_max" #90;
    distribution_age_peak = "distribution_age_peak" #25;
    distribution_hygiene_mean = "distribution_hygiene_mean" #0.5;
    distribution_hygiene_var = "distribution_hygiene_var" #1;

def write_exp_file(d):
    name = d.get('experiment')
    with open('{}/experiments/{}.json'.format(home,name), 'w') as fp:
        json.dump(d, fp)
    exp_file = "{}/experiments/{}.json".format(home, name)
    print(exp_file)
    return exp_file


def get_result_file(d):
    name = d.get('experiment')
    res_file = "/results/{}.csv".format(name)
    return res_file

def run_abm_process(d):
#     d = json.loads(data)
    exp_file = write_exp_file(d)
    print "running for exp", exp_file
    try:
        os.chdir("src/")
        command = "java abmforcovid/RunABM {}".format(exp_file)
        print "running command", command
        res = subprocess.check_output(command, shell=True)
        print (res)
    except Exception as e:
        print (e)

def plot_curve(d):
    res_file = d.get('resultfile')
    df = pd.read_csv(res_file)
    stgy = d.get('strategy')
    sim_cycle = d.get(Param.sim_cycle_per_day, 500)
    agents = d.get(Param.num_agents)
    days = list(stgy.keys())
    days.sort()
    last_day = days[-1]

    ax = plt.gca()
    fig_size = plt.gcf().get_size_inches() #Get current size
    sizefactor = 2.7  
    
    # Modify the current size by the factor
    plt.gcf().set_size_inches(sizefactor * fig_size) 

    df.plot(kind='line',x='step',y='infected_agents',  color='red', ax=ax)
    df.plot(kind='line',x='step',y='asympt_agents', color='orange', ax=ax)
    df.plot(kind='line',x='step',y='exposed_agents', color='#948f00', ax=ax)
    df.plot(kind='line',x='step',y='recovered_agents', color='green', ax=ax)
#     df.plot(kind='line',x='step',y='dead_agents', color='black', ax=ax)
    plt.xlabel("days")
    plt.ylabel("count")

    # add points of interests
    for k in stgy.keys():
        i = k*sim_cycle
        text = ""
        for k,v in stgy.get(k).items():
            x = ""
            if k.find('p_') > -1:
                if v == 0:
                    x = ' revoke '
                else: x = " invoke "
                t = k.replace('p_', '')
                t = t.replace('_', '-')
                t = x+t
                text += t+"\n"
            elif k.find('c_')> -1:
                t = k.replace('c_', '')
                t = t.replace('_', '-')
                t = t+" ={}".format(v)
                text += t+"\n"
            elif k.find('a_')> -1:
                t = k.replace('a_', ' ')
                t = t.replace('_', '-')
                t = t+" ={}".format(v)
                text += t+"\n"
                      
        y = df.get_value(index=i, col='infected_agents')
        plt.annotate(text, (i, agents-random.choice([200,400,600]) ))
        axvline(x=i, ymin=0, ymax=y, ls=":", lw=1.5)
        axhline(y=y, ls=":", lw=0.5)

    # mark x label with days
    x = np.array( list(stgy.keys()) )*sim_cycle
    my_xticks = np.array( list(stgy.keys()) )
    plt.xticks(x, my_xticks)
    
    plt.xticks(np.arange(1,last_day+5,2)*sim_cycle, range(1,last_day+5,2))
    plt.yticks(np.arange(0,agents+50,100), range(0,agents+50,100))
    
    plt.ylim(top=agents+50)
    plt.show()
    plt.subplots_adjust(left=0.16, bottom=0.39, top=0.82)