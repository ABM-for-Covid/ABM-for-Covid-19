package social_dist;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public /*strictfp*/ class Env extends SimState {
    private static final long serialVersionUID = 1;

    //    all environment variables here
    public static final double XMIN = 0;
    public static final double YMIN = 0;

    public static final double DIAMETER = 15;
    public static final double HYGIENE_CONST = 0.2;
    public static final double I2toD_CONST = 0.1;

    public static final double INCUBATION_PERIOD_High = 14;
    public static final double INFECTION_DISTANCE = DIAMETER + 3;
    public static final double INFECTION_DISTANCE_SQUARED = INFECTION_DISTANCE * INFECTION_DISTANCE;


    //scaling factors of environments
    public static double agent_density = 0.0001;
    public static double hospital_bed_per_agent = 0.1;
    public static double icu_bed_per_hospital_bed = 0.05;


    //experiment
    public static int num_agents = 100;
    public static double discreatization = 25.0;
    public static  double ENV_XMAX = (int)Math.pow( (num_agents / agent_density), 0.5);
    public static  double ENV_YMAX = ENV_XMAX;
    public static final double Q_XMAX = ENV_XMAX*2;
    public static final double Q_YMAX = ENV_YMAX *2;


    public static double getAgent_density() {
        return agent_density;
    }

    public static void setAgent_density(double agent_density) {
        Env.agent_density = agent_density;
    }

    public static double getHospital_bed_per_agent() {
        return hospital_bed_per_agent;
    }

    public static void setHospital_bed_per_agent(double hospital_bed_per_agent) {
        Env.hospital_bed_per_agent = hospital_bed_per_agent;
    }

    public static double getIcu_bed_per_hospital_bed() {
        return icu_bed_per_hospital_bed;
    }

    public static void setIcu_bed_per_hospital_bed(double icu_bed_per_hospital_bed) {
        Env.icu_bed_per_hospital_bed = icu_bed_per_hospital_bed;
    }


    public static int hospital_bed_capacity = (int) (num_agents * hospital_bed_per_agent);
    public static int icu_capacity = (int) (icu_bed_per_hospital_bed * hospital_bed_capacity);


    public static double getEnvXmax() {
        return ENV_XMAX;
    }

    public static void setEnvXmax(double envXmax) {
        ENV_XMAX = envXmax;
    }

    public static double getEnvYmax() {
        return ENV_YMAX;
    }

    public static void setEnvYmax(double envYmax) {
        Env.ENV_YMAX = envYmax;
    }

    /********* Policies ******************************/
    public static boolean policy_quarantine = false;
    public static boolean policy_daily_testing = false;
    public static boolean policy_contact_tracing = false;
    public static boolean policy_lockdown = false;
    public static boolean policy_social_distancing = false;
    public static boolean policy_hospitalization = false; // if i2 and i3 be isolated in a hospital.


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

    //how many contact traces you can pull up.
    public static int contact_trace_capacity = 10;


    public static void setContact_trace_capacity(int contact_trace_capacity) {
        Env.contact_trace_capacity = contact_trace_capacity;
    }

    public static int getContact_trace_capacity() {
        return contact_trace_capacity;
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
    public static int testing_capacity = 5;
    public static double test_false_negative = 0.3;
    public static int test_delay = 2;

    public static int getTesting_capacity() {
        return testing_capacity;
    }

    public static void setTesting_capacity(int testing_capacity) {
        Env.testing_capacity = testing_capacity;
    }

    public static double getTest_false_negative() {
        return test_false_negative;
    }

    public static void setTest_false_negative(double test_false_negative) {
        Env.test_false_negative = test_false_negative;
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

    // number of days after expose when agent should move to recovery.
    public static int infection_to_recovery_days = 21;
    public static int expose_to_recovery_days = 12;

    public static double i2ToDProbability = 0.7;

    //    all model parameters here
    public static double initial_infection_percent = 0.1;

    // flag to see actual glass view ( This will show each patient tested and event I0, R etc. )
    public static boolean glassView = true;

    // age is a triangular distribution between 1, 90 with peak at 25
    public static double ageMin = 1;
    public static double ageMax = 90;
    public static double agePeak = 25;

    //hygiene distribution
    public static double hygieneMean = 0.5;
    public static double hygieneVariance = 1;

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


    public static int getHospital_bed_capacity() {
        return hospital_bed_capacity;
    }

    public static void setHospital_bed_capacity(int hospital_bed_capacity) {
        Env.hospital_bed_capacity = hospital_bed_capacity;
    }

    public static int getIcu_capacity() {
        return icu_capacity;
    }

    public static void setIcu_capacity(int icu_capacity) {
        Env.icu_capacity = icu_capacity;
    }

    // recovery time - Uniform distribution between 21 and 42 days
    public static int recoveryTimeMin = 21;
    public static int recoveryTimeMax = 42;

    /***********************************************/

    public static Continuous2D HumansEnvironment = null;
    public static Continuous2D BlackBoxEnvironment = null;
    public static Continuous2D QuarantinedEnvironment = null;
    public static Continuous2D TestEnvironment = null;

    public static double getHygieneMean() {
        return hygieneMean;
    }

    public static double getHygieneVariance() {
        return hygieneVariance;
    }

    public static void setHygieneMean(double hygieneMean) {
        Env.hygieneMean = hygieneMean;
    }

    public static void setHygieneVariance(double hygieneVariance) {
        Env.hygieneVariance = hygieneVariance;
    }

    public static void setAgeMin(double ageMin) {
        Env.ageMin = ageMin;
    }

    public static void setAgeMax(double ageMax) {
        Env.ageMax = ageMax;
    }

    public static void setAgePeak(double agePeak) {
        Env.agePeak = agePeak;
    }

    public static double getAgeMin() {
        return ageMin;
    }

    public static double getAgeMax() {
        return ageMax;
    }

    public static double getAgePeak() {
        return agePeak;
    }

    public static int getNum_agents() {
        return num_agents;
    }

    public static void setNum_agents(int num_agents) {
        Env.num_agents = num_agents;
    }

    public static boolean checkICUAvailability() {
        return icu_capacity > 0;
    }

    public int getInfected_Agents()
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

    public int getExposed_Agents()
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

    public int getRecovered_Agents()
    //  return the count of recovered agents to inspectors.*/
    {
        int recCount = 0;
        Bag un_objects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.recovered) {
                    recCount++;
                }
            }
        }
        un_objects = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.recovered) {
                    recCount++;
                }
            }
        }
        return recCount;
    }

    public int getDeath_Count()
    //  return the count of dead agents to inspectors.*/
    {
        int deadCount = 0;
        Bag un_objects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.dead) {
                    deadCount++;
                }
            }
        }
        un_objects = QuarantinedEnvironment.getAllObjects();
        for (int i = 0; i < un_objects.numObjs; i++) {
            if (un_objects.objs[i] != null) {
                Agent ta = (Agent) (un_objects.objs[i]);
                if (ta.dead) {
                    deadCount++;
                }
            }
        }

        return deadCount;
    }

    public int getAsymptomatic_Agents()
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


    public static void setInitial_infection_percent(double initial_infection_percent) {
        Env.initial_infection_percent = initial_infection_percent;
    }

    public static double getInitial_infection_percent() {
        return initial_infection_percent;
    }

    /**
     * Creates a infection simulation with the given random number seed.
     */
    public Env(long seed) {
        super(seed);
    }

    boolean conflict(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        Double social_distance = DIAMETER;
        if (Env.policy_social_distancing)
            if(Transitions.getRandomBoolean(0.6))
                social_distance = DIAMETER+3;

        return ((a.x > b.x && a.x < b.x + social_distance) ||
                (a.x + social_distance > b.x && a.x + social_distance < b.x + social_distance)) &&
                ((a.y > b.y && a.y < b.y + social_distance) ||
                        (a.y + social_distance > b.y && a.y + social_distance < b.y + social_distance));
    }

    public boolean withinInfectionDistance(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        return ((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) <= INFECTION_DISTANCE_SQUARED);
    }

    boolean acceptablePosition(final Agent agent, final Double2D location) {

        if (location.x < DIAMETER / 2 || location.x > (ENV_XMAX - XMIN)/*HumansEnvironment.getXSize()*/ - DIAMETER / 2 ||
                location.y < DIAMETER / 2 || location.y > (ENV_YMAX - YMIN)/*HumansEnvironment.getYSize()*/ - DIAMETER / 2)
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
        HumansEnvironment = new Continuous2D(discreatization, (ENV_XMAX - XMIN), (ENV_YMAX - YMIN));
        QuarantinedEnvironment = new Continuous2D(discreatization, (Q_XMAX - XMIN), (Q_YMAX - YMIN));
        BlackBoxEnvironment = new Continuous2D(discreatization, (ENV_XMAX - XMIN), (ENV_YMAX - YMIN));
        TestEnvironment = new Continuous2D(discreatization, (ENV_XMAX - XMIN), (ENV_YMAX - YMIN));

        int step_int = 0;
        for (int x = 0; x < num_agents; x++) {
            Double2D loc;
            Human agent;
            int times = 0;
            do {
                loc = new Double2D(random.nextDouble() * (ENV_XMAX - XMIN - DIAMETER) + XMIN + DIAMETER / 2,
                        random.nextDouble() * (ENV_YMAX - YMIN - DIAMETER) + YMIN + DIAMETER / 2);


                agent = new Human("Human-" + x, loc);
                //add agent as infected according to initial value
                if (step_int < num_agents * initial_infection_percent) {
                    agent.setInfected(true);
                    agent.setSusceptible(false);
                    agent.setInfectionState(0);
                }

                // set hygiene for every human
                agent.hygiene = hygieneMean + random.nextGaussian() * Math.sqrt(hygieneVariance);
                if (!(agent.hygiene > 0.0 && agent.hygiene <= 1))
                    continue;

                // set age
                agent.age = (int) (triangularDistribution(ageMin, ageMax, agePeak));
                if (agent.age > 100 || agent.age <= 1)
                    continue;

                // imunity flag
                if (Transitions.getRandomBoolean(0.1))
                    agent.weakImmune = true;

                //co-morbidity
                if (Transitions.getRandomBoolean(0.1))
                    agent.coMorbid_score = -2;
                else if (Transitions.getRandomBoolean(0.23))
                    agent.coMorbid_score = -1;

                //overall health
                agent.overallHealth = random.nextInt(4);

                //essential agents 10% of the population
                if (Transitions.getRandomBoolean(0.3))
                    agent.essential = true;

                times++;

                if (times == 1000) {
                    // can't place agents, oh well
                    break;
                }
            } while (!acceptablePosition(agent, loc));
            agent.aindex = step_int;
            if (agent.aindex> 0 )
                HumansEnvironment.setObjectLocation(agent, loc);
            schedule.scheduleRepeating(agent);
            step_int++;
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
