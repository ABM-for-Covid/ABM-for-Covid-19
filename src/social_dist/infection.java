package social_dist;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;


public /*strictfp*/ class infection extends SimState {
    private static final long serialVersionUID = 1;

    public static final double XMIN = 0;
    public static final double XMAX = 1000;
    public static final double YMIN = 0;
    public static final double YMAX = 800;

    public static final double DIAMETER = 15;

    public static final double HEALING_DISTANCE = 20;
    public static final double HEALING_DISTANCE_SQUARED = HEALING_DISTANCE * HEALING_DISTANCE;
    public static final double INFECTION_DISTANCE = 20;
    public static final double INFECTION_DISTANCE_SQUARED = INFECTION_DISTANCE * INFECTION_DISTANCE;

    public static final int NUM_HUMANS = 150;

    public static Continuous2D HumansEnvironment = null;

    /**
     * Creates a infection simulation with the given random number seed.
     */
    public infection(long seed) {
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

    public boolean withinHealingDistance(final Agent agent1, final Double2D a, final Agent agent2, final Double2D b) {
        return ((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) <= HEALING_DISTANCE_SQUARED);
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
        for (int x = 0; x < NUM_HUMANS; x++) {
            Double2D loc = null;
            Agent agent = null;
            int times = 0;
            do {
                loc = new Double2D(random.nextDouble() * (XMAX - XMIN - DIAMETER) + XMIN + DIAMETER / 2,
                        random.nextDouble() * (YMAX - YMIN - DIAMETER) + YMIN + DIAMETER / 2);

                agent = new Human("Human" + x, loc);

                if (times % 100 == 1) {
                    agent.setInfected(true);
                }

//                TODO mark some agents static
                if (step_int % 10 == 2) {
                    agent.setIsolation(false);
                }

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

    public static void main(String[] args) {
        doLoop(infection.class, args);
        System.exit(0);
    }
}
