# ABM for COVID-19 🦠

Modeling the Spread and Control of COVID-19.


# Agent Parameters 
**Age** - An integer attribute sampled from a triangular distribution between 1 and 90 with a peak of 25.

**Hygiene** A value between 0 and 1, where 1 corresponds to perfect hygiene. High hygine level indicate in real life washing hands, wearing masks etc., which leads to less exposure of virus.

**Overall health** - An integer value between 0 and 3, both inclusive. A higher value corresponds to strong health and a lower value cor-responds to poor health.

**Comorbidity** An integer value between 0 and -2, both inclusive.  A co-morbidity value of zero corresponds to an agent with nocomorbid  conditions,  and  a  lower  value  corresponds  toprevalence of more serious comorbid conditions.

**Immunity** A value of either 0 or 1, where 0 corresponds to weak immunity and 1 corresponds to strong immunity.


# Policies
Many policies can be applied at any time during the simulation. These policies change and control agent behaviour and also update environmental resources. 

**Boarder Close** This policy is applied to stop the incoming infectious agents in the environment.

**Lockdown** Lockdown is applied to control the movement of the agents in the simulation. When applied it can make some agents static in the environment. 

**Age Based Lockdown** This lockdown is applied with a threshhold age. All the agents above the threshhold will be under lockdown.

**Quarantine** This policy, if applied -- can remove certain agents from the environment. Sick and positively tested agents can be quarantined. 

**Daily Testing** When this policy is applied -- Daily, a random number of agents are being sampled and tested. There are  parameters which can be modified to control capcity of daily testing and false negative percentage of test results. 

**Contact Tracing** This policy trace back to a certain number of contacts of an agent when it is tested positive. If Quarantine is also invoked, the traced agents will be isolated from the environment. 

**Social Distancing** This policy when applied -- It controls how close any one agent come to another one. 

**Hospitalization** If this policy is invoked, very sick agents goes gets isolated and moved to hospital environment. Subject to availability of hospital beds. 

**Reinforcement** Hospital beds and ICU beds can be always modified during the simulation. 



## How to start
- Compile all java classes. 

```
cd src
javac abmforcovid/*.java
```

- Start the server in background. 
```
nohup python server.py &
```
- Assuming server running at `http://localhost:8080`

- Define an experiment in json file (you can see example experiment file in `experiments/` dir)

- Check the `notebooks/` dir for jupyter notebooks with different experiments.

- Call function with the json
`run_exp`

- Check the result `csv` with the same name as the experiment in `results/` dir. 


- Note - Have Java in your machine; Adding all the jar files here for quick setup.

### If you want to run with the UI. 

- Compile all the java classes
- Run EnvUI class
- It will open all the views related to the simulation.

## Future work
- Create environment and define mobility.
- Improve the code and suggestions to scale.


# Reference
Using Multi agent simulator [MASON](https://cs.gmu.edu/~eclab/projects/mason/)
