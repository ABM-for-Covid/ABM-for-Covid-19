package social_natural;

import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.awt.*;

public /*strictfp*/ class Human extends Agent {
    private static final long serialVersionUID = 1;

    protected boolean greedy = false;

    public final boolean getIsGreedy() {
        return greedy;
    }

    public final void setIsGreedy(final boolean b) {
        greedy = b;
    }


    public final boolean isInfected() {
        return infected;
    }


    public Human(String id, Double2D location) {
        super(id, location);
        try {
            intID = Integer.parseInt(id.substring(5)); // "Human"
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

    // squared distance between two points
    public final double distanceSquared(final double x1, final double y1, final double x2, final double y2) {
        return ((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**************************************************************
     * get nearBy humans and position of each human
     * @param state
     * @param pos
     * @param distance
     *************************************************************/
    void preprocessHumans(final infection state, Double2D pos, double distance) {
        nearbyHuman = infection.HumansEnvironment.getNeighborsWithinDistance(pos, distance);
        if (nearbyHuman == null) {
            return;
        }
        distSqrTo = new double[nearbyHuman.numObjs];
        for (int i = 0; i < nearbyHuman.numObjs; i++) {
            Human p = (Human) (nearbyHuman.objs[i]);
            distSqrTo[i] = distanceSquared(pos.x, pos.y, p.x, p.y);
        }
    }

    /************************************************************************************
     * Step the simulation
     ************************************************************************************/
    public void step(final SimState state) {
        infection hb = (infection) state;
        double distance2DesiredLocation = 1e30;

        Bag mysteriousObjects = hb.HumansEnvironment.getNeighborsWithinDistance(agentLocation, 10.0 * infection.INFECTION_DISTANCE);
        if (mysteriousObjects != null) {
            for (int i = 0; i < mysteriousObjects.numObjs; i++) {
                if (mysteriousObjects.objs[i] != null &&
                        mysteriousObjects.objs[i] != this) {

                    if (!(((Agent) mysteriousObjects.objs[i]) instanceof Human))
                        continue;
                    Human ta = (Human) (mysteriousObjects.objs[i]);

                    if (hb.withinInfectionDistance(this, agentLocation, ta, ta.agentLocation) && ta.isInfected())
                        this.setInfected(true);
                    else {
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
            desiredLocation = new Double2D((state.random.nextDouble() - 0.5) * ((infection.XMAX - infection.XMIN) / 5 - infection.DIAMETER) +
                    //infection.XMIN
                    agentLocation.x
                    //+infection.DIAMETER/2
                    ,
                    (state.random.nextDouble() - 0.5) * ((infection.YMAX - infection.YMIN) / 5 - infection.DIAMETER) +
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

        if (!hb.acceptablePosition(this, new Double2D(agentLocation.x + dx, agentLocation.y + dy))) {
            steps = 0;
        } else {
            agentLocation = new Double2D(agentLocation.x + dx, agentLocation.y + dy);
            hb.HumansEnvironment.setObjectLocation(this, agentLocation);
        }

    }

    protected Color humanColor = new Color(19, 189, 192);
    protected Color infectedColor = new Color(255, 27, 59);

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        double diamx = info.draw.width * infection.DIAMETER;
        double diamy = info.draw.height * infection.DIAMETER;

        if (isInfected())
            graphics.setColor(infectedColor);
        else graphics.setColor(humanColor);
        graphics.fillOval((int) (info.draw.x - diamx / 2), (int) (info.draw.y - diamy / 2), (int) (diamx), (int) (diamy));
    }


    public String getType() {
        if (isInfected())
            return "Infected Human";
        else
            return "Healthy Human";
    }
}
