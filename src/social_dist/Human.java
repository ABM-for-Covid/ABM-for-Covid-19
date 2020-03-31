package social_dist;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;

public /*strictfp*/ class Human extends Agent {
    private static final long serialVersionUID = 1;

    protected boolean greedy = false;

    // agent health parameters
    public double hygiene = 0.5;
    public int age = 45;
    public boolean weakImmune = false;
    public int coMorbid_score = 0; // [-2,-1,0]
    public int overallHealth = 3; // health [0,1,2,3]
    public int sim_count = 0; // health [0,1,2,3]

    public int get_age_score(){
        int a_x = 0;
        if (age <= 1)
            a_x = 1;
        else  if (age <= 60 && age >40)
            a_x = 2;
        else if (age <= 40 && age > 1)
            a_x = 3;
        return a_x;
    }


    public double getHygiene() {
        return hygiene;
    }

    public final boolean getIsGreedy() {
        return greedy;
    }

    public final void setIsGreedy(final boolean b) {
        greedy = b;
    }

    public final boolean isInfected() {
        return infected;
    }

    public final boolean isExposed() {
        return exposed;
    }

    public final boolean isSusceptible() {
        return susceptible;
    }

    public final int getOpacity() {
        if (hygiene >= 0.8)
            return 100;
        else if (hygiene < 0.8 && hygiene > 0.6)
            return 180;
        else if (hygiene <= 0.6 && hygiene > 0.3)
            return 200;
        else if (hygiene <= 0.3)
            return 255;
        else return 255;
    }

    // todo - create a detailed constructor for human
    public Human(String id, Double2D location) {
        super(id, location);
        try {
            intID = Integer.parseInt(id.substring(5));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Double2D desiredLocation = null;
    Double2D suggestedLocation = null;
    Bag nearbyHuman;
    double[] distSqrTo;
    public double x;
    public double y;
    int steps = 0;

    public String toString() {
        return System.identityHashCode(this) + "\nHygiene: " + getHygiene() + "\nS->E" + this.getSusToExposeProb();
    }

    /************************************************************************************
     * Step the simulation
     ************************************************************************************/
    public void step(final SimState state) {
        Env hb = (Env) state;
        double distance2DesiredLocation = 1e30;

        if (this.dead) return;

        // interaction of two agents when they are in infection distance
        Bag mysteriousObjects = hb.HumansEnvironment.getNeighborsWithinDistance(agentLocation, 10.0 * Env.INFECTION_DISTANCE);
        if (mysteriousObjects != null) {
            for (int i = 0; i < mysteriousObjects.numObjs; i++) {
                if (mysteriousObjects.objs[i] != null &&
                        mysteriousObjects.objs[i] != this) {

                    if (!(((Agent) mysteriousObjects.objs[i]) instanceof Human))
                        continue;
                    Human ta = (Human) (mysteriousObjects.objs[i]);

                    if (hb.withinInfectionDistance(this, agentLocation, ta, ta.agentLocation)) {
                        //calculate S->E transition
                        if ((ta.getInfectionState() == 0 || ta.getInfectionState() ==1 || ta.getInfectionState() == 2) && this.isSusceptible()) {
                            Transitions.calculateStoE(this, ta);
                        }

                    } else {
                        if (getIsGreedy()) {
                            double tmpDist = distanceSquared(agentLocation, ta.agentLocation);
                            if (tmpDist < distance2DesiredLocation) {
                                desiredLocation = ta.agentLocation;
                                distance2DesiredLocation = tmpDist;
                            }
                        }
                    }
                }
            }
        }

        steps--;


        if (desiredLocation == null || steps <= 0) {
            desiredLocation = new Double2D((state.random.nextDouble() - 0.5) * ((Env.XMAX - Env.XMIN) / 5 - Env.DIAMETER) +
                    //infection.XMIN
                    agentLocation.x
                    //+infection.DIAMETER/2
                    ,
                    (state.random.nextDouble() - 0.5) * ((Env.YMAX - Env.YMIN) / 5 - Env.DIAMETER) +
                            agentLocation.y
                    //infection.YMIN
                    //+infection.DIAMETER/2
            );
            steps = 50 + state.random.nextInt(50);
        }


        double dx = desiredLocation.x - agentLocation.x;
        double dy = desiredLocation.y - agentLocation.y;

        {
            double temp = /*Strict*/Math.sqrt(dx * dx + dy * dy);
            if (temp < 1) {
                steps = 0;
            } else {
                dx /= temp;
                dy /= temp;
            }
        }

        if (!hb.acceptablePosition(this, new

                Double2D(agentLocation.x + dx, agentLocation.y + dy))) {
            steps = 0;
        } else {
            sim_count++;
            agentLocation = new Double2D(agentLocation.x + dx, agentLocation.y + dy);
            if (sim_count %500 == 0){
                if (this.isExposed())
                    Transitions.calculateE_I0toI1(this);
                else if (this.getInfectionState() == 0)
                    Transitions.calculateI0Transition(this);
                else if (this.getInfectionState() == 1)
                    Transitions.calculateI1Transition(this);
                else if (this.getInfectionState() == 2)
                    Transitions.calculateI2Transition(this);
                else if (this.getInfectionState() == 3)
                    Transitions.calculateI3Transition(this);
            }

            if (this.isolated)
                return;
            hb.HumansEnvironment.setObjectLocation(this, agentLocation);
            hb.BlackBoxEnvironment.setObjectLocation(this, agentLocation);
        }
    }

    public String getType() {
        if (isInfected())
            return "Infected Human";
        else
            return "Healthy Human";
    }
}
