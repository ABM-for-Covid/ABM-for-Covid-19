package abmforcovid;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import java.io.*;
import java.util.List;

import static java.lang.Math.pow;

public /*strictfp*/ class Env extends SimState {
    private static final long serialVersionUID = 1;

    //Constant environment variables here
    public static final double XMIN = 0;
    public static final double YMIN = 0;
    public static final double DIAMETER = 15;
    public static final double HYGIENE_CONST = 0.8; // it was 0.8 before
    public static final double I2toD_CONST = 0.1;
    public static final double INCUBATION_PERIOD_High = 14;
    public static final double INFECTION_DISTANCE = DIAMETER + 10; // it was +5 before
    public static final double INFECTION_DISTANCE_SQUARED = INFECTION_DISTANCE * INFECTION_DISTANCE;


    //scaling factors of environments
    public static int ini_num_agents = 100;
    public static double ini_agent_density = 0.0001;
    public static double ini_hospital_bed_per_agent = 0.1;
    public static double ini_icu_bed_per_hospital_bed = 0.05;

    public static double ini_essential_agent_percent = 0.05;

    public static double ini_infection_percent = 0.0;
    // age is a triangular distribution between 1, 90 with peak at 25
    public static double ini_distribution_age_min = 1;
    public static double ini_distribution_age_max = 90;
    public static double ini_distribution_age_peak = 25;

    //hygiene distribution
    public static double ini_distribution_hygiene_mean = 0.5;
    public static double ini_distribution_hygiene_var = 1;
    public static int ini_sim_cycle_per_day = 500;

    //experiments
    public static double discreatization = 25.0;
    public static double env_xmax;
    public static double env_ymax;
    public static double Q_XMAX;
    public static double Q_YMAX;

    /********* Capacities ******************************/
    public static int capacity_contact_trace = 10;
    public static int capacity_hospital_bed = (int) (ini_num_agents * ini_hospital_bed_per_agent);
    public static int capacity_icu_beds = (int) (ini_icu_bed_per_hospital_bed * capacity_hospital_bed);
    public static int capacity_testing = 5;

    /********* Policies ******************************/
    public static boolean policy_quarantine = false;
    public static boolean policy_daily_testing = false;
    public static boolean policy_contact_tracing = false;
    public static boolean policy_lockdown = false;
    public static boolean policy_social_distancing = false;
    public static boolean policy_close_borders = false;
    public static boolean policy_hospitalization = false; // if i2 and i3 be isolated in a hospital.

    /********** count **********************************/
    public static int num_traveler_Agents = 0;
    public static double social_distancing_efficiency = 0.6;

    /***********************************/
    // Model this to get death rate under control
    public static int infection_to_recovery_days = 15; //reduce it see the impact.
    public int expose_to_recovery_days = 12;
    public static int traveler_agent_count = 500;
    public static double i2ToDProbability = 0.6; //reduced it to 0.6



    public int getNum_Infected_Agents()
    //  return the count of infected agents to inspectors.*/
    {
        int infCount = 0;
        Bag un_objects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.infected) {
                    infCount++;
                }
            }
        }
        un_objects = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.infected) {
                    infCount++;
                }
            }

        }

        return infCount;
    }

    public int getNum_Exposed_Agents()
    //  return the count of exposed agents to inspectors.*/
    {
        int expoCount = 0;
        Bag un_objects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.exposed) {
                    expoCount++;
                }
            }
        }
        un_objects = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.exposed) {
                    expoCount++;
                }
            }
        }
        return expoCount;
    }

    public int getNum_Recovered_Agents()
    //  return the count of recovered agents to inspectors.*/
    {
        int recCount = 0;
        Bag un_objects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.recovered) {
                    recCount++;
                    ta.active = false;
                }
            }
        }
        un_objects = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.recovered) {
                    recCount++;
                    ta.active = false;
                }
            }
        }
        return recCount;
    }

    public int getNum_Death_Count()
    //  return the count of dead agents to inspectors.*/
    {
        int deadCount = 0;
        Bag un_objects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.dead) {
                    deadCount++;
                    ta.active = false;
                }
            }
        }
        un_objects = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.dead) {
                    deadCount++;
                    ta.active = false;
                }
            }
        }

        return deadCount;
    }

    public int getNum_Susceptible_agents()
    //  return the count of dead agents to inspectors.*/
    {
        int sus_count = 0;
        Bag un_objects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.susceptible) {
                    sus_count++;
                    ta.active = false;
                }
            }
        }
        un_objects = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.susceptible) {
                    sus_count++;
                    ta.active = false;
                }
            }
        }

        return sus_count;
    }

    public int getNum_Asymptomatic_Agents()
    //  return the count of asymptomatic agents to inspectors.*/
    {
        int infI0Count = 0;
        Bag un_object = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_object.numObjs; i++) {
            if (un_object.objs[i] != null) {
                Agent ta = (Agent) (un_object.objs[i]);
                if (ta.getInfectionState() == 0) {
                    infI0Count++;
                }
            }
        }
        un_object = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_object.numObjs; i++) {
            if (un_object.objs[i] != null) {
                Agent ta = (Agent) (un_object.objs[i]);
                if (ta.getInfectionState() == 0) {
                    infI0Count++;
                }
            }
        }
        return infI0Count;
    }

    public int getNum_Agents()
    //  return the number of total agents to inspectors.*/
    {
        Bag un_objects = HumansEnvironment.getAllObjects();
        return un_objects.numObjs;
    }

    public int getBlack_Box(){
        int count = getNum_Asymptomatic_Agents();
        int total = getNum_Infected_Agents();
        return (total- count);

    }

    public double getAvg_infection() {
        int inf_contacts = 0;
        double t_contacts = 0;
        Bag un_object = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_object.numObjs; i++) {
            if (un_object.objs[i] != null) {
                Agent ta = (Agent) (un_object.objs[i]);
                if (ta.once_infected) {
                    inf_contacts = inf_contacts + ta.infection_producing_contacts;
                    t_contacts++;
                }
            }
        }
        un_object = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_object.numObjs; i++) {
            if (un_object.objs[i] != null) {
                Agent ta = (Agent) (un_object.objs[i]);
                if (ta.infected) {
                    inf_contacts = inf_contacts + ta.infection_producing_contacts;
                    t_contacts++;
                }
            }
        }
        return inf_contacts / t_contacts;
    }

    public static double getIni_essential_agent_percent() {
        return ini_essential_agent_percent;
    }

    public static void setIni_essential_agent_percent(double ini_essential_agent_percent) {
        Env.ini_essential_agent_percent = ini_essential_agent_percent;
    }

    public static int getIni_sim_cycle_per_day() {
        return ini_sim_cycle_per_day;
    }

    public static void setIni_sim_cycle_per_day(int ini_sim_cycle_per_day) {
        Env.ini_sim_cycle_per_day = ini_sim_cycle_per_day;
    }

    public static int getNum_traveler_Agents() {
        return num_traveler_Agents;
    }

    public static double getIni_agent_density() {
        return ini_agent_density;
    }

    public static void setIni_agent_density(double ini_agent_density) {
        Env.ini_agent_density = ini_agent_density;
    }

    public static double getIni_hospital_bed_per_agent() {
        return ini_hospital_bed_per_agent;
    }

    public static void setIni_hospital_bed_per_agent(double ini_hospital_bed_per_agent) {
        Env.ini_hospital_bed_per_agent = ini_hospital_bed_per_agent;
    }

    public static double getIni_icu_bed_per_hospital_bed() {
        return ini_icu_bed_per_hospital_bed;
    }

    public static void setIni_icu_bed_per_hospital_bed(double ini_icu_bed_per_hospital_bed) {
        Env.ini_icu_bed_per_hospital_bed = ini_icu_bed_per_hospital_bed;
    }


    public static double getEnv_xmax() {
        return env_xmax;
    }

    public static void setEnv_xmax(double env_xmax) {
        Env.env_xmax = env_xmax;
    }

    public static double getEnv_ymax() {
        return env_ymax;
    }

    public static void setEnv_ymax(double env_ymax) {
        Env.env_ymax = env_ymax;
    }


    public static boolean isPolicy_close_borders() {
        return policy_close_borders;
    }

    public static void setPolicy_close_borders(boolean policy_close_borders) {
        Env.policy_close_borders = policy_close_borders;
    }


    public static boolean isPolicy_social_distancing() {

        return policy_social_distancing;
    }

    public static void setPolicy_social_distancing(boolean policy_social_distancing) {
        Env.policy_social_distancing = policy_social_distancing;
    }

    public static boolean isPolicy_daily_testing() {
        return policy_daily_testing;
    }

    public static void setPolicy_daily_testing(boolean policy_daily_testing) {
        Env.policy_daily_testing = policy_daily_testing;
    }

    public static boolean isPolicy_quarantine() {
        return policy_quarantine;
    }

    public static void setPolicy_quarantine(boolean policy_quarantine) {
        Env.policy_quarantine = policy_quarantine;
    }


    public static boolean isPolicy_lockdown() {
        return policy_lockdown;
    }

    public static void setPolicy_lockdown(boolean policy_lockdown) {
        Env.policy_lockdown = policy_lockdown;
    }


    /**************************
     Contact Tracing Variables
     **************************/
    //contact tracing hashMap
    public static Multimap<String, Human> contacts = LinkedListMultimap.create();


    public static boolean isPolicy_contact_tracing() {
        return policy_contact_tracing;
    }

    public static void setPolicy_contact_tracing(boolean policy_contact_tracing) {
        Env.policy_contact_tracing = policy_contact_tracing;
    }

    public static void setCapacity_contact_trace(int capacity_contact_trace) {
        Env.capacity_contact_trace = capacity_contact_trace;
    }

    public static int getCapacity_contact_trace() {
        return capacity_contact_trace;
    }

    public static int quarantine_day_limit = 21;

    public static void setQuarantine_day_limit(int quarantine_day_limit) {
        Env.quarantine_day_limit = quarantine_day_limit;
    }

    public static int getQuarantine_day_limit() {
        return quarantine_day_limit;
    }

    public static int uiIndent = 100;
    public static int q_dx = uiIndent;
    public static int q_dy = uiIndent;

    public static Double2D assignQurantineLocation(Human hu) {
        Double2D agentLocation;
        if (q_dx == uiIndent && q_dy == uiIndent)
            agentLocation = new Double2D(q_dx, q_dy);
        else {
            if (q_dx >= Q_XMAX || hu.prime) {
                q_dy = q_dy + uiIndent;
                q_dx = uiIndent;
            }
            agentLocation = new Double2D(q_dx, q_dy);

        }
        q_dx = q_dx + uiIndent;

        return agentLocation;
    }

    /****************************************************************/

    /*
    Testing related variables
     */

    public static double test_false_negative_percent = 0.3;
    public static int test_delay = 2;

    public static int getCapacity_testing() {
        return capacity_testing;
    }

    public static void setCapacity_testing(int capacity_testing) {
        Env.capacity_testing = capacity_testing;
    }

    public static double getTest_false_negative_percent() {
        return test_false_negative_percent;
    }

    public static void setTest_false_negative_percent(double test_false_negative_percent) {
        Env.test_false_negative_percent = test_false_negative_percent;
    }

    public static int getTest_delay() {
        return test_delay;
    }

    public static void setTest_delay(int test_delay) {
        Env.test_delay = test_delay;
    }

    public static int t_dx = uiIndent;
    public static int t_dy = uiIndent;

    public static Double2D assignTestLocation() {
        Double2D agentLocation;
        agentLocation = new Double2D(t_dx, t_dy);
        t_dy = t_dy + uiIndent;
        return agentLocation;
    }

    /***************************************************************/


    //    all model parameters here


    public static int getInfected_traveler_pday_limit() {
        return infected_traveler_pday_limit;
    }

    public static void setInfected_traveler_pday_limit(int infected_traveler_pday_limit) {
        Env.infected_traveler_pday_limit = infected_traveler_pday_limit;
    }

    // everyday maximum infection influx
    public static int infected_traveler_pday_limit = 5;

    // flag to see actual glass view ( This will show each patient tested and event I0, R etc. )
    public static boolean glassView = true;


    // incubation period distribution - Average 5 with a positive skew, so sampling from an exponential distribution
    public static double incubationMean = 5;


    public static void setGlassView(boolean glassView) {
        Env.glassView = glassView;
    }

    public static boolean isGlassView() {
        return glassView;
    }


    /********  Hospital related variables *************/
    public static boolean isPolicy_hospitalization() {
        return policy_hospitalization;
    }

    public static void setPolicy_hospitalization(boolean policy_hospitalization) {
        Env.policy_hospitalization = policy_hospitalization;
    }


    public static int getCapacity_hospital_bed() {
        return capacity_hospital_bed;
    }

    public static void setCapacity_hospital_bed(int capacity_hospital_bed) {
        Env.capacity_hospital_bed = capacity_hospital_bed;
    }

    public static int getCapacity_icu_beds() {
        return capacity_icu_beds;
    }

    public static void setCapacity_icu_beds(int capacity_icu_beds) {
        Env.capacity_icu_beds = capacity_icu_beds;
    }

    // recovery time - Uniform distribution between 21 and 42 days
    public static int recoveryTimeMin = 21;
    public static int recoveryTimeMax = 42;

    /***********************************************/

    public static Continuous2D HumansEnvironment = null;
    public static Continuous2D BlackBoxEnvironment = null;
    public static Continuous2D QuarantinedEnvironment = null;
    public static Continuous2D TestEnvironment = null;
    public static Continuous2D TravelerEnvironment = null;

    public static double getIni_distribution_hygiene_mean() {
        return ini_distribution_hygiene_mean;
    }

    public static double getIni_distribution_hygiene_var() {
        return ini_distribution_hygiene_var;
    }

    public static void setIni_distribution_hygiene_mean(double ini_distribution_hygiene_mean) {
        Env.ini_distribution_hygiene_mean = ini_distribution_hygiene_mean;
    }

    public static void setIni_distribution_hygiene_var(double ini_distribution_hygiene_var) {
        Env.ini_distribution_hygiene_var = ini_distribution_hygiene_var;
    }

    public static void setIni_distribution_age_min(double ini_distribution_age_min) {
        Env.ini_distribution_age_min = ini_distribution_age_min;
    }

    public static void setIni_distribution_age_max(double ini_distribution_age_max) {
        Env.ini_distribution_age_max = ini_distribution_age_max;
    }

    public static void setIni_distribution_age_peak(double ini_distribution_age_peak) {
        Env.ini_distribution_age_peak = ini_distribution_age_peak;
    }

    public static double getIni_distribution_age_min() {
        return ini_distribution_age_min;
    }

    public static double getIni_distribution_age_max() {
        return ini_distribution_age_max;
    }

    public static double getIni_distribution_age_peak() {
        return ini_distribution_age_peak;
    }

    public static int getIni_num_agents() {
        return ini_num_agents;
    }

    public static void setIni_num_agents(int ini_num_agents) {
        Env.ini_num_agents = ini_num_agents;
    }

    public static boolean checkICUAvailability() {
        return capacity_icu_beds > 0;
    }


    public static void setIni_infection_percent(double ini_infection_percent) {
        Env.ini_infection_percent = ini_infection_percent;
    }

    public static double getIni_infection_percent() {
        return ini_infection_percent;
    }

    /**
     * Creates a infection simulation with the given random number seed.
     */
    public Env(long seed) {
        super(seed);
    }

    public static double getSocial_distancing_efficiency() {
        return social_distancing_efficiency;
    }

    public static void setSocial_distancing_efficiency(double social_distancing_efficiency) {
        Env.social_distancing_efficiency = social_distancing_efficiency;
    }


    static boolean conflict(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        Double social_distance = DIAMETER;
        if (Env.policy_social_distancing) {
            if (Transitions.getRandomBoolean(social_distancing_efficiency))
                social_distance = INFECTION_DISTANCE + 3;
        }
        return ((a.x > b.x && a.x < b.x + social_distance) ||
                (a.x + social_distance > b.x && a.x + social_distance < b.x + social_distance)) &&
                ((a.y > b.y && a.y < b.y + social_distance) ||
                        (a.y + social_distance > b.y && a.y + social_distance < b.y + social_distance));
    }

    public boolean withinInfectionDistance(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        return ((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) <= INFECTION_DISTANCE_SQUARED);
    }

    static boolean acceptablePosition(final Agent agent, final Double2D location) {

        if (location.x < DIAMETER / 2 || location.x > (env_xmax - XMIN)/*HumansEnvironment.getXSize()*/ - DIAMETER / 2 ||
                location.y < DIAMETER / 2 || location.y > (env_ymax - YMIN)/*HumansEnvironment.getYSize()*/ - DIAMETER / 2)
            return false;
        Bag mysteriousObjects = HumansEnvironment.getNeighborsWithinDistance(location, 2 * DIAMETER);
        if (mysteriousObjects != null) {
            for (int i = 0; i < mysteriousObjects.numObjs; i++) {
                if (mysteriousObjects.objs[i] != null && mysteriousObjects.objs[i] != agent) {
                    Agent ta = (Agent) (mysteriousObjects.objs[i]);
                    if (conflict(agent, location, ta, HumansEnvironment.getObjectLocation(ta)))
                        return false;
                }
            }
        }
        return true;
    }


    public void start() {
        super.start();  // clear out the schedule
        setEnv_xmax((int)( pow( (ini_num_agents/ini_agent_density), 0.5)));
        setEnv_ymax(getEnv_xmax());
        Q_XMAX = getEnv_xmax()*2;
        Q_YMAX = getEnv_xmax()*2;

        System.out.println("Env "+getEnv_xmax());

        HumansEnvironment = new Continuous2D(discreatization, (getEnv_xmax() - XMIN), (getEnv_ymax() - YMIN));
        QuarantinedEnvironment = new Continuous2D(discreatization, (Q_XMAX - XMIN), (Q_YMAX - YMIN));
        BlackBoxEnvironment = new Continuous2D(discreatization, (getEnv_xmax() - XMIN), (getEnv_ymax() - YMIN));
        TestEnvironment = new Continuous2D(discreatization, (getEnv_xmax() - XMIN), (getEnv_ymax() - YMIN));
        TravelerEnvironment = new Continuous2D(discreatization, (getEnv_xmax() - XMIN), (getEnv_ymax() - YMIN));

        int step_int = 0;
        for (int x = 0; x < ini_num_agents + traveler_agent_count; x++) {
            Double2D loc;
            Human agent;
            int times = 0;
            do {
                loc = new Double2D(random.nextDouble() * (env_xmax - XMIN - DIAMETER) + XMIN + DIAMETER / 2,
                        random.nextDouble() * (env_ymax - YMIN - DIAMETER) + YMIN + DIAMETER / 2);


                agent = new Human("Human-" + x, loc);
                //add agent as infected according to initial value
                if (step_int < ini_num_agents * ini_infection_percent) {
                    agent.setInfected(true);
                    agent.setSusceptible(false);
                    agent.setInfectionState(0);
                }

                if (ini_infection_percent == 0) {
                    if (step_int < infected_traveler_pday_limit) {
                        agent.setInfected(true);
                        agent.setSusceptible(false);
                        agent.setInfectionState(0);
                    }
                }

                // set hygiene for every human
                agent.hygiene = ini_distribution_hygiene_mean + random.nextGaussian() * Math.sqrt(ini_distribution_hygiene_var);
                if (!(agent.hygiene > 0.0 && agent.hygiene <= 1))
                    continue;

                // set age
                agent.age = (int) (triangularDistribution(ini_distribution_age_min, ini_distribution_age_max, ini_distribution_age_peak));
                if (agent.age > 100 || agent.age <= 1)
                    continue;

                // immunity flag
                if (Transitions.getRandomBoolean(0.1))
                    agent.weakImmune = true;

                //co-morbidity
                if (Transitions.getRandomBoolean(0.1))
                    agent.coMorbid_score = -2;
                else if (Transitions.getRandomBoolean(0.23))
                    agent.coMorbid_score = -1;

                //overall health
                agent.overallHealth = random.nextInt(4);
                times++;

                if (times == 1000) {
                    // can't place agents, oh well
                    break;
                }
            } while (!acceptablePosition(agent, loc));

            agent.aindex = step_int;
            if (agent.aindex > 0 && agent.aindex <= ini_num_agents)
                HumansEnvironment.setObjectLocation(agent, loc);
            else if (agent.aindex > 0) {
                agent.id = "Traveler-" + agent.aindex;
                TravelerEnvironment.setObjectLocation(agent, loc); //add around 500 agents to traveler environment.
            }
            schedule.scheduleRepeating(agent);
            step_int++;
        }
        Transitions.mark_essential_agents();
    }

    public static void setStrategy(HashMap strategy) {
        Env.strategy = strategy;
    }

    public static HashMap strategy;


    public void checkAndInvokePolicy(Integer step) {
        if (strategy == null) return;

        double cycle_count = Env.ini_sim_cycle_per_day;

        double action_day = step / cycle_count;

        Policies policy = (Policies) strategy.get(action_day);
        if (policy != null) {
            System.out.println("Invoking policy " + action_day);
            System.out.println("Number of agents " + getNum_Agents());

            if (policy.p_lockdown == 0 || policy.p_lockdown == 1) {
                boolean b = policy.p_lockdown == 1;
                Env.setPolicy_lockdown(b);
            }
            if (policy.p_close_borders == 0 || policy.p_close_borders == 1) {
                boolean b = policy.p_close_borders == 1;
                Env.setPolicy_close_borders(b);
            }
            if (policy.p_contact_tracing == 0 || policy.p_contact_tracing == 1) {
                boolean b = policy.p_contact_tracing == 1;
                Env.setPolicy_contact_tracing(b);
            }
            if (policy.p_daily_testing == 0 || policy.p_daily_testing == 1) {
                boolean b = policy.p_daily_testing == 1;
                Env.setPolicy_daily_testing(b);
            }
            if (policy.p_hospitalization == 0 || policy.p_hospitalization == 1) {
                boolean b = policy.p_hospitalization == 1;
                Env.setPolicy_hospitalization(b);
            }
            if (policy.p_quarantine == 0 || policy.p_quarantine == 1) {
                boolean b = policy.p_quarantine == 1;
                Env.setPolicy_quarantine(b);
            }
            if (policy.p_social_distancing == 0 || policy.p_social_distancing == 1) {
                boolean b = policy.p_social_distancing == 1;
                Env.setPolicy_social_distancing(b);
            }

            //capacities
            if (policy.c_contact_trace > 0)
                Env.setCapacity_contact_trace(policy.c_contact_trace);
            if (policy.c_hospital_bed > 0)
                Env.setCapacity_hospital_bed(policy.c_hospital_bed);
            if (policy.c_icu_beds > 0)
                Env.setCapacity_icu_beds(policy.c_icu_beds);
            if (policy.c_testing > 0)
                Env.setCapacity_testing(policy.c_testing);


            // attributes
            if (policy.a_false_negative_percent > 0)
                Env.setTest_false_negative_percent(policy.a_false_negative_percent);

            if (policy.a_social_distancing_efficiency>0)
                Env.setSocial_distancing_efficiency(policy.a_social_distancing_efficiency);

            if (policy.p_exit > 0)
                finish();
        }
    }

    public static void setExperiment(String experiment) {
        Env.experiment = experiment;
    }

    public static String experiment;

    public static void setResultFile(String resultFile) {
        Env.resultFile = resultFile;
    }

    public static String resultFile;

    public void stream_data(Integer step_int) {

        String filename = resultFile;
        if (filename == null) return;

        File csvFile = new File(filename);
        try {
            if (csvFile.isFile()) {

                BufferedWriter file = new BufferedWriter(new FileWriter(filename, true));

                List<Integer> rowdata = new ArrayList<Integer>();
                rowdata.add(step_int);
                rowdata.add(getNum_Infected_Agents());
                rowdata.add(getNum_Exposed_Agents());
                rowdata.add(getNum_Recovered_Agents());
                rowdata.add(getNum_Death_Count());
                rowdata.add(getNum_Asymptomatic_Agents());
                rowdata.add(getNum_Susceptible_agents());
                rowdata.add(getCapacity_hospital_bed());
                rowdata.add(getCapacity_icu_beds());
                for (int i = 0; i < rowdata.size(); i++) {
                    Integer data = rowdata.get(i);
                    file.append(data.toString());
                    file.append(",");

                }
                file.append(String.format("%.2f", getAvg_infection()));
                file.newLine();
                file.flush();
                file.close();
            } else {
                BufferedWriter file = new BufferedWriter(new FileWriter(filename, true));
                file.append("step");
                file.append(",");
                file.append("infected_agents");
                file.append(",");
                file.append("exposed_agents");
                file.append(",");
                file.append("recovered_agents");
                file.append(",");
                file.append("dead_agents");
                file.append(",");
                file.append("asympt_agents");
                file.append(",");
                file.append("suscept_agents");
                file.append(",");
                file.append("hospital_beds");
                file.append(",");
                file.append("icu_beds");
                file.append(",");
                file.append("avg_infection");

                file.newLine();

                List<Integer> rowdata = new ArrayList<Integer>();
                rowdata.add(step_int);
                rowdata.add(getNum_Infected_Agents());
                rowdata.add(getNum_Exposed_Agents());
                rowdata.add(getNum_Recovered_Agents());
                rowdata.add(getNum_Death_Count());
                rowdata.add(getNum_Asymptomatic_Agents());
                rowdata.add(getNum_Susceptible_agents());
                rowdata.add(getCapacity_hospital_bed());
                rowdata.add(getCapacity_icu_beds());

                for (int i = 0; i < rowdata.size(); i++) {
                    Integer data = rowdata.get(i);
                    file.append(data.toString());
                    file.append(",");
                }
                file.append(String.format("%.2f", getAvg_infection()));
                file.newLine();
                file.flush();
                file.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public double triangularDistribution(double low, double high, double peak) {
        double F = (peak - low) / (high - low);
        double rand = random.nextDouble();
        if (rand < F) {
            return low + Math.sqrt(rand * (high - low) * (peak - low));
        } else {
            return high - Math.sqrt((1 - rand) * (high - low) * (high - peak));
        }
    }

    public static void main(String[] args) {
        doLoop(Env.class, args);
        System.exit(0);
    }
}
