package social_dist;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public /*strictfp*/ class env extends SimState {
    private static final long serialVersionUID = 1;

    //    all environment variables here
    public static final double XMIN = 0;
    public static final double XMAX = 1000;
    public static final double YMIN = 0;
    public static final double YMAX = 800;
    public static final double DIAMETER = 15;
    public static final double HYGIENE_CONST = 0.2;
    public static final double INCUBATION_PERIOD_LOW = 1;
    public static final double INCUBATION_PERIOD_High = 14;
    public static final double INCUBATION_PERIOD_High_Weak = 5;
    public static final double INFECTION_DISTANCE = 20;
    public static final double INFECTION_DISTANCE_SQUARED = INFECTION_DISTANCE * INFECTION_DISTANCE;

    // todo - set some random locations as hospitals
    // if I2 and I3 ->  update location of agent to hospital loc
    // don't move the agent


    //Inefection state related parameters
    // period in which agent goes to R from I1, if doesn't transit to I2
    public static int i1Period = 10;
    public static double i2ToDProbability = 0.8;


    //    all model parameters here
    public static int num_humans = 150;
    public static double initialInfectionPercent = 0.1;

    // implement distancing
    public static  boolean socialDistancing = false;

    // flag to see actual glass view ( This will show each patient tested and event I0, R etc. )
    public static  boolean glassView = true;

    // age is a triangular distribution between 1, 90 with peak at 25
    public static double ageMin = 1;
    public static double ageMax = 90;
    public static double agePeak = 25;

    //hygiene distribution
    public static double hygieneMean = 0.5;
    public static double hygieneVariance = 1;

    // incubation period distribution - Average 5 with a positive skew, so sampling from an exponential distribution
    public static double incubationMean = 5;

    public static int hospitalCount = 100;
    public static int icuCount = (int) 0.5 * hospitalCount;

    public static void setGlassView(boolean glassView) {
        env.glassView = glassView;
    }

    public static boolean isGlassView() {
        return glassView;
    }

    public static int getHospitalCount() {
        return hospitalCount;
    }

    public static void setHospitalCount(int hospitalCount) {
        env.hospitalCount = hospitalCount;
    }

    public static int getIcuCount() {
        return icuCount;
    }

    public static void setIcuCount(int icuCount) {
        env.icuCount = icuCount;
    }

    // recovery time - Uniform distribution between 21 and 42 days
    public static int recoveryTimeMin = 21;
    public static int recoveryTimeMax = 42;


    public static Continuous2D HumansEnvironment = null;

    /**
     * Add all the inspectors here
     */
    public static void setSocialDistancing(boolean socialDistancing) {
        env.socialDistancing = socialDistancing;
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
        env.hygieneMean = hygieneMean;
    }

    public static void setHygieneVariance(double hygieneVariance) {
        env.hygieneVariance = hygieneVariance;
    }

    public static void setAgeMin(double ageMin) {
        env.ageMin = ageMin;
    }

    public static void setAgeMax(double ageMax) {
        env.ageMax = ageMax;
    }

    public static void setAgePeak(double agePeak) {
        env.agePeak = agePeak;
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
        env.num_humans = num_humans;
    }

    public static boolean checkICUAvailability() {
        if (icuCount > 0)
            return true;
        else return false;
    }

    public int getInfectedHumans()
    //  return the count of infected humans to inspectors.*/
    {
        int infCount = 0;

        Bag mysteriousObjects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < mysteriousObjects.numObjs; i++) {
            if (mysteriousObjects.objs[i] != null) {
                Agent ta = (Agent) (mysteriousObjects.objs[i]);
                if (ta.infected) {
                    infCount++;
                }
            }

        }
        return infCount;
    }

    public int getExposedHumans()
    //  return the count of exposed humans to inspectors.*/
    {
        int expoCount = 0;

        Bag mysteriousObjects = HumansEnvironment.getAllObjects();
        for (int i = 0; i < mysteriousObjects.numObjs; i++) {
            if (mysteriousObjects.objs[i] != null) {
                Agent ta = (Agent) (mysteriousObjects.objs[i]);
                if (ta.exposed) {
                    expoCount++;
                }
            }
        }
        return expoCount;
    }


    public static void setInitialInfectionPercent(double initialInfectionPercent) {
        env.initialInfectionPercent = initialInfectionPercent;
    }

    public static double getInitialInfectionPercent() {
        return initialInfectionPercent;
    }

    /**
     * Creates a infection simulation with the given random number seed.
     */
    public env(long seed) {
        super(seed);
    }

    boolean conflict(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        if (((a.x > b.x && a.x < b.x + DIAMETER) ||
                (a.x + DIAMETER > b.x && a.x + DIAMETER < b.x + DIAMETER)) &&
                ((a.y > b.y && a.y < b.y + DIAMETER) ||
                        (a.y + DIAMETER > b.y && a.y + DIAMETER < b.y + DIAMETER))) {
            return true;
        }
        return false;
    }

    public boolean withinInfectionDistance(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        return ((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) <= INFECTION_DISTANCE_SQUARED);
    }

//    public boolean withinHealingDistance(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
//        return ((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) <= HEALING_DISTANCE_SQUARED);
//    }

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

    public void setObjectLocation(final Human hu, Double2D location) {

        HumansEnvironment.setObjectLocation(hu, location);

        // to speed up the simulation, each Human knows where it is located (gets rid of a hash get call)
        hu.x = location.x;
        hu.y = location.y;
    }

    public void start() {
        super.start();  // clear out the schedule

        HumansEnvironment = new Continuous2D(25.0, (XMAX - XMIN), (YMAX - YMIN));

        // Schedule the agents -- we could instead use a RandomSequence, which would be faster,
        // but this is a good test of the scheduler
        int step_int = 0;
        for (int x = 0; x < num_humans; x++) {
            Double2D loc = null;
            Human agent = null;
            int times = 0;
            do {
                loc = new Double2D(random.nextDouble() * (XMAX - XMIN - DIAMETER) + XMIN + DIAMETER / 2,
                        random.nextDouble() * (YMAX - YMIN - DIAMETER) + YMIN + DIAMETER / 2);

                // todo - calculate all the things for this new agent here and pass to constructor Human.
                agent = new Human("Human" + x, loc);


                //add agent as infected according to initial value
                if (step_int < num_humans * initialInfectionPercent) {
                    agent.setInfected(true);
                    agent.setSusceptible(false);
                    agent.setInfectionState(0);
                }

                // mark some agents static
                if (step_int % 10 == 2) {
                    agent.setIsolation(false);
                }

                // set hygiene for every human
                agent.hygiene = hygieneMean + random.nextGaussian() * Math.sqrt(hygieneVariance) ;
                if (!(agent.hygiene > 0.0 && agent.hygiene <=1))
                        continue;

                // set age
                agent.age = (int) (triangularDistribution(ageMin, ageMax, agePeak));
                if (agent.age > 100 || agent.age <=1)
                    continue;
                System.out.println("Age of the agent "+agent.age);

                // imunity flag
                if (Transitions.getRandomBoolean(0.1))
                        agent.weakImmune = true;

                //co-morbidity
                if (Transitions.getRandomBoolean(0.1))
                    agent.coMorbid_score = -2;
                else  if (Transitions.getRandomBoolean(0.23))
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
        doLoop(env.class, args);
        System.exit(0);
    }
}
