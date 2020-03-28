package social_dist;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.awt.*;

public /*strictfp*/ class Human extends Agent {
    private static final long serialVersionUID = 1;

    protected boolean greedy = false;

    // agent health parameters
    public double hygiene = 0.5;
    public int age = 45;
    public boolean weakImmune = false;
    public int coMorbid_score = 0; // [-2,-1,0]
    public int overallHealth = 3; // health [0,1,2,3]

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
        env hb = (env) state;
        double distance2DesiredLocation = 1e30;

        // interaction of two agents when they are in infection distance
        Bag mysteriousObjects = hb.HumansEnvironment.getNeighborsWithinDistance(agentLocation, 10.0 * env.INFECTION_DISTANCE);
        if (mysteriousObjects != null) {
            for (int i = 0; i < mysteriousObjects.numObjs; i++) {
                if (mysteriousObjects.objs[i] != null &&
                        mysteriousObjects.objs[i] != this) {

                    if (!(((Agent) mysteriousObjects.objs[i]) instanceof Human))
                        continue;
                    Human ta = (Human) (mysteriousObjects.objs[i]);

                    if (hb.withinInfectionDistance(this, agentLocation, ta, ta.agentLocation)) {
                        //calculate S->E transition
                        if (ta.isInfected() && this.isSusceptible()) {
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
        steps--;


        // if social distancing is being practiced
        if (this.isolated && env.isSocialDistancing())
            return;
        if (this.dead) return;

        if (desiredLocation == null || steps <= 0) {
            desiredLocation = new Double2D((state.random.nextDouble() - 0.5) * ((env.XMAX - env.XMIN) / 5 - env.DIAMETER) +
                    //infection.XMIN
                    agentLocation.x
                    //+infection.DIAMETER/2
                    ,
                    (state.random.nextDouble() - 0.5) * ((env.YMAX - env.YMIN) / 5 - env.DIAMETER) +
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
            agentLocation = new Double2D(agentLocation.x + dx, agentLocation.y + dy);
            hb.HumansEnvironment.setObjectLocation(this, agentLocation);
        }

    }


    protected Color humanColor = new Color(15, 165, 189);
    protected Color exposedColor = new Color(192, 183, 76);
    protected Color infectedColorI0 = new Color(255, 122, 47);
    protected Color infectedColorI1 = new Color(230, 23, 255);
    protected Color infectedColorI2 = new Color(255, 27, 59);
    protected Color infectedColorI3 = new Color(255, 27, 59);
    protected Color deadColor = new Color(15, 19, 17);
    protected Color recoveredColor = new Color(9, 255, 42);


    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        double diamx = info.draw.width * env.DIAMETER;
        double diamy = info.draw.height * env.DIAMETER;
        if (env.glassView) {
            if (isExposed()) {
                exposedColor = new Color(192, 156, 30, getOpacity());
                graphics.setColor(exposedColor);
            } else if (this.getInfectionState() == 0) {
                infectedColorI0 = new Color(255, 122, 47, getOpacity());
                graphics.setColor(infectedColorI0);
            } else if (this.getInfectionState() == 1) {
                infectedColorI1 = new Color(230, 23, 255, getOpacity());
                graphics.setColor(infectedColorI1);
            } else if (this.getInfectionState() == 2 || this.getInfectionState() == 3 ) {
                graphics.setColor(infectedColorI3);
            } else if (this.dead) graphics.setColor(deadColor);
            else if (this.recovered) graphics.setColor(recoveredColor);
            else {
                humanColor = new Color(11, 172, 189, getOpacity());
                graphics.setColor(humanColor);
            }
        }
        else {
            if (this.getInfectionState() == 1 || this.getInfectionState() == 2 || this.getInfectionState() == 3){
                graphics.setColor(infectedColorI3);
            }
            else if (this.dead) graphics.setColor(deadColor);

            else {
                humanColor = new Color(11, 172, 189, getOpacity());
                graphics.setColor(humanColor);
            }
        }

        graphics.fillOval((int) (info.draw.x - diamx / 2), (int) (info.draw.y - diamy / 2), (int) (diamx), (int) (diamy));
    }


    public String getType() {
        if (isInfected())
            return "Infected Human";
        else
            return "Healthy Human";
    }
}
