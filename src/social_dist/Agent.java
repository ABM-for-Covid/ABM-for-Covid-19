package social_dist;

import sim.engine.Steppable;
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

    protected boolean infected = false;

    protected boolean isolated = true;

    public final void setIsolation(boolean b) {
        isolated = b;
    }

    public final void setInfected(boolean b) {
        infected = b;
    }

    double distanceSquared(final Double2D loc1, Double2D loc2) {
        return ((loc1.x - loc2.x) * (loc1.x - loc2.x) + (loc1.y - loc2.y) * (loc1.y - loc2.y));
    }

    public abstract String getType();

    public boolean hitObject(Object object, DrawInfo2D info) {
        double diamx = info.draw.width * infection.DIAMETER;
        double diamy = info.draw.height * infection.DIAMETER;

        Ellipse2D.Double ellipse = new Ellipse2D.Double((int) (info.draw.x - diamx / 2), (int) (info.draw.y - diamy / 2), (int) (diamx), (int) (diamy));
        return (ellipse.intersects(info.clip.x, info.clip.y, info.clip.width, info.clip.height));
    }
}
