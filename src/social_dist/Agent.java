package social_dist;

import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

import java.awt.geom.Ellipse2D;

public abstract /*strictfp*/ class Agent extends SimplePortrayal2D implements Steppable {
    private static final long serialVersionUID = 1;

    public String id;

    public Double2D agentLocation;

    public int intID = -1;

    public Agent(String id, Double2D location) {
        this.id = id;
        this.agentLocation = location;
    }

    // check for SEIR states
    public int aindex = 0;
    protected boolean susceptible = true;
    protected boolean exposed = false;
    protected boolean infected = false;
    protected boolean recovered = false;
    protected boolean dead = false;
    public boolean prime = false;

    // agent mobility
    public boolean isolated = false;
    public boolean quarantined = false;

    // infectiion state (I0, I1, I2, I3)
    public int infectionState = -1;
    public int previousState = -1;
    public int wantToMoveToI2 = 0;

    // state counters ( considering 10 simulation count as 1 day)
    public int count_EI0 = 0;
    public int count_I1 = 0;
    public int count_I2 = 0;
    public int count_I3 = 0;
    public int count_iso = 0;

    public Continuous2D plotEnv = null;

    public void setQuarantined(boolean quarantined) {
        this.quarantined = quarantined;
        this.count_iso = 0;
    }

    public int getInfectionState() {
        return infectionState;
    }

    public void setSusToExposeProb(double susToExposeProb) {
        this.susToExposeProb = susToExposeProb;
    }

    public double getSusToExposeProb() {
        return susToExposeProb;
    }

    public double susToExposeProb = 0;

    public String getState() {
        return state;
    }

    public void setInfectionState(int infectionState) {
        this.infectionState = infectionState;
        if (infectionState == 2 || infectionState == 3) this.setIsolation(true);
        else this.setIsolation(false);

    }

    public void setState(String state) {
        this.state = state;
    }

    // choose states from  [S, E, I0, I1, I2, I3, R, D]
    protected String state = "S";

    public final void setIsolation(boolean b) {
        isolated = b;
    }

    public final void setPrime(boolean b) {
        prime = b;
    }

    public final void setInfected(boolean b) {
        infected = b;
        if (!infected)
            infectionState = -1;
    }

    public final void setExposed(boolean b) {
        exposed = b;
    }

    public final void setSusceptible(boolean b) {
        susceptible = b;
    }

    public final void setRecovered(boolean b) {
        recovered = b;
        infectionState = -1;
        infected = false;
    }

    public final void setDead(boolean b) {
        dead = b;
    }


    double distanceSquared(final Double2D loc1, Double2D loc2) {
        return ((loc1.x - loc2.x) * (loc1.x - loc2.x) + (loc1.y - loc2.y) * (loc1.y - loc2.y));
    }

    public abstract String getType();

    public boolean hitObject(Object object, DrawInfo2D info) {
        double diamx = info.draw.width * Env.DIAMETER;
        double diamy = info.draw.height * Env.DIAMETER;

        Ellipse2D.Double ellipse = new Ellipse2D.Double((int) (info.draw.x - diamx / 2), (int) (info.draw.y - diamy / 2), (int) (diamx), (int) (diamy));
        return (ellipse.intersects(info.clip.x, info.clip.y, info.clip.width, info.clip.height));
    }
}
