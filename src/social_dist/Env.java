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
    public static final double XMAX = 1000;
    public static final double YMIN = 0;
    public static final double YMAX = 800;

    public static final double Q_XMAX = 2000;
    public static final double Q_YMAX = 1600;

    public static final double DIAMETER = 15;
    public static final double HYGIENE_CONST = 0.2;
    public static final double I2toD_CONST = 0.1;

    public static final double INCUBATION_PERIOD_High = 14;
    public static final double INFECTION_DISTANCE = DIAMETER + 3;
    public static final double INFECTION_DISTANCE_SQUARED = INFECTION_DISTANCE * INFECTION_DISTANCE;

    /**************************
     Contact Tracing Variables
     **************************/
    //contact tracing hashMap
    public static Multimap<String, Human> contacts = LinkedListMultimap.create();
    public static boolean contactTracing = true;

    public static boolean isContactTracing() {
        return contactTracing;
    }

    public static void setContactTracing(boolean contactTracing) {
        Env.contactTracing = contactTracing;
    }

    //how many contact traces you can pull up.
    public static int contact_trace_capacity = 15;

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

    public static int i1Period = 5;
    public static double i2ToDProbability = 0.8;

    public static int getI1Period() {
        return i1Period;
    }

    public static void setI1Period(int i1Period) {
        Env.i1Period = i1Period;
    }

    //    all model parameters here
    public static int num_humans = 50;
    public static double initialInfectionPercent = 0.1;

    // implement distancing
    public static boolean socialDistancing = false;


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

    public static int HospitalBedCount = 150;
    public static int icuCount = (int) (0.05 * HospitalBedCount);


    public static void setGlassView(boolean glassView) {
        Env.glassView = glassView;
    }

    public static boolean isGlassView() {
        return glassView;
    }

    public static int getHospitalBedCount() {
        return HospitalBedCount;
    }

    public static void setHospitalBedCount(int hospitalBedCount) {
        Env.HospitalBedCount = hospitalBedCount;
    }

    public static int getIcuCount() {
        return icuCount;
    }

    public static void setIcuCount(int icuCount) {
        Env.icuCount = icuCount;
    }

    // recovery time - Uniform distribution between 21 and 42 days
    public static int recoveryTimeMin = 21;
    public static int recoveryTimeMax = 42;


    public static Continuous2D HumansEnvironment = null;
    public static Continuous2D BlackBoxEnvironment = null;
    public static Continuous2D QuarantinedEnvironment = null;

    /**
     * Add all the inspectors here
     */
    public static void setSocialDistancing(boolean socialDistancing) {
        Env.socialDistancing = socialDistancing;
    }

    public static boolean isSocialDistancing() {
        return socialDistancing;
    }

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

    public static int getNum_humans() {
        return num_humans;
    }

    public static void setNum_humans(int num_humans) {
        Env.num_humans = num_humans;
    }

    public static boolean checkICUAvailability() {
        return icuCount > 0;
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


    public static void setInitialInfectionPercent(double initialInfectionPercent) {
        Env.initialInfectionPercent = initialInfectionPercent;
    }

    public static double getInitialInfectionPercent() {
        return initialInfectionPercent;
    }

    /**
     * Creates a infection simulation with the given random number seed.
     */
    public Env(long seed) {
        super(seed);
    }

    boolean conflict(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        return ((a.x > b.x && a.x < b.x + DIAMETER) ||
                (a.x + DIAMETER > b.x && a.x + DIAMETER < b.x + DIAMETER)) &&
                ((a.y > b.y && a.y < b.y + DIAMETER) ||
                        (a.y + DIAMETER > b.y && a.y + DIAMETER < b.y + DIAMETER));
    }

    public boolean withinInfectionDistance(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        return ((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) <= INFECTION_DISTANCE_SQUARED);
    }

    boolean acceptablePosition(final Agent agent, final Double2D location) {

        if (location.x < DIAMETER / 2 || location.x > (XMAX - XMIN)/*HumansEnvironment.getXSize()*/ - DIAMETER / 2 ||
                location.y < DIAMETER / 2 || location.y > (YMAX - YMIN)/*HumansEnvironment.getYSize()*/ - DIAMETER / 2)
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
        HumansEnvironment = new Continuous2D(25.0, (XMAX - XMIN), (YMAX - YMIN));
        QuarantinedEnvironment = new Continuous2D(25.0, (Q_XMAX - XMIN), (Q_YMAX - YMIN));
        BlackBoxEnvironment = new Continuous2D(25.0, (XMAX - XMIN), (YMAX - YMIN));

        int step_int = 0;
        for (int x = 0; x < num_humans; x++) {
            Double2D loc;
            Human agent;
            int times = 0;
            do {
                loc = new Double2D(random.nextDouble() * (XMAX - XMIN - DIAMETER) + XMIN + DIAMETER / 2,
                        random.nextDouble() * (YMAX - YMIN - DIAMETER) + YMIN + DIAMETER / 2);


                agent = new Human("Human-" + x, loc);
                //add agent as infected according to initial value
                if (step_int < num_humans * initialInfectionPercent) {
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

                times++;

                if (times == 1000) {
                    // can't place agents, oh well
                    break;
                }
            } while (!acceptablePosition(agent, loc));
            HumansEnvironment.setObjectLocation(agent, loc);
            agent.aindex = step_int;
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
