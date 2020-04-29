package abmforcovid;

import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.*;

public /*strictfp*/ class Human extends Agent {
    private static final long serialVersionUID = 1;

    protected boolean greedy = false;

    // agent health parameters
    public double hygiene = 0.5;
    public int age = 45;
    public boolean weakImmune = false;
    public int coMorbid_score = 0; // [-2,-1,0]
    public int overallHealth = 3; // health [0,1,2,3]
    public int sim_count = 0;

    public int get_age_score() {
        int a_x = 0;
        if (age <= 1)
            a_x = 1;
        else if (age <= 60 && age > 40)
            a_x = 2;
        else if (age <= 40)
            a_x = 3;
        return a_x;
    }

    public double getHygiene() {
        return hygiene;
    }

    public final boolean getIsGreedy() {
        return greedy;
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

        if (this.dead) {
            return;
        }

        // if in traveler env or eliminated don't run the simulation!
        Double2D t_location = Env.TravelerEnvironment.getObjectLocation(this);
        if (t_location != null) {
            return;
        }

        // run test once per day which means only on 1 virtual agent cycle
        if (this.aindex==0){

            hb.checkAndInvokePolicy(sim_count);
//            hb.stream_data(sim_count);

            if (sim_count % Env.ini_sim_cycle_per_day == 0){

                // run daily testing policy
                if (Env.policy_daily_testing)
                    Transitions.run_tests();

                // if borders are open - add more infectious agents daily
                if (!Env.policy_close_borders) {
                    Transitions.add_new_infectious_agents();
                }

                int day = sim_count/Env.ini_sim_cycle_per_day;
                hb.send_data(day);
                hb.daily_data(day);
            }
            sim_count++;
            return;
        }

        // interaction of two agents when they are in infection distance
        Bag mysteriousObjects = Env.HumansEnvironment.getNeighborsWithinDistance(agentLocation, Env.INFECTION_DISTANCE);
        if (mysteriousObjects != null) {
            for (int i = 0; i < mysteriousObjects.numObjs; i++) {
                if (mysteriousObjects.objs[i] != null &&
                        mysteriousObjects.objs[i] != this) {

                    if (!(mysteriousObjects.objs[i] instanceof Human))
                        continue;
                    Human ta = (Human) (mysteriousObjects.objs[i]);

                    if (hb.withinInfectionDistance(this, agentLocation, ta, ta.agentLocation)) {
                        //calculate S->E transition
                        if (Env.policy_contact_tracing && this.getInfectionState() >= 0)
                            Env.contacts.put(id, ta);

                        if ((ta.getInfectionState() == 0 || ta.getInfectionState() == 1 || ta.getInfectionState() == 2) && this.isSusceptible()) {
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
            desiredLocation = new Double2D((state.random.nextDouble() - 0.5) * ((Env.env_xmax - Env.XMIN) / 5 - Env.DIAMETER) +
                    //infection.XMIN
                    agentLocation.x
                    //+infection.DIAMETER/2
                    ,
                    (state.random.nextDouble() - 0.5) * ((Env.env_ymax - Env.YMIN) / 5 - Env.DIAMETER) +
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
            sim_count++;
            agentLocation = new Double2D(agentLocation.x + dx, agentLocation.y + dy);

            if (sim_count % Env.ini_sim_cycle_per_day == 0) {
                if (this.quarantined)
                    Transitions.countQuarantinedDays(this);
                if (this.isExposed())
                    Transitions.calculateE_I0toI1(this);
                else if (this.getInfectionState() == 0)
                    Transitions.calculateI0Transition(this, hb);
                else if (this.getInfectionState() == 1)
                    Transitions.calculateI1Transition(this);
                else if (this.getInfectionState() == 2)
                    Transitions.calculateI2Transition(this);
                else if (this.getInfectionState() == 3)
                    Transitions.calculateI3Transition(this);

            }

            if (this.isolated || this.quarantined) {
                Double2D q_location = Env.QuarantinedEnvironment.getObjectLocation(this);
                if (q_location != null) {
                    return;
                }
                Env.QuarantinedEnvironment.setObjectLocation(this, Env.assignQurantineLocation(this));
                Env.HumansEnvironment.remove(this);
                Env.BlackBoxEnvironment.remove(this);

            } else {
                //if lockdown then only essentials will run
                if (Env.policy_lockdown && !this.essential)
                    return;

                //if age based lockdown then resetrict people above the limit
                if(Env.policy_age_based_lockdown && this.age >= Env.age_based_lockdown_threshhold) {
                    if (Env.policy_quarantine)
                        this.setQuarantined(true);
                    return;
                }

                Env.HumansEnvironment.setObjectLocation(this, agentLocation);
                Env.BlackBoxEnvironment.setObjectLocation(this, agentLocation);
            }

        }
    }

    public String getType() {
        if (isInfected())
            return "Infected Human";
        else
            return "Healthy Human";
    }

    public void findAndMarkTraces() {
        List<Human> traces = fetch_contacts(Env.capacity_contact_trace);
        for (Human h : traces) {
            h.setQuarantined(true);
        }
    }

    public List<Human> fetch_contacts(int how_many) {
        Collection<Human> contacts = Env.contacts.get(this.id);
        List<Human> cList = new ArrayList<>(contacts);
        Collections.reverse(cList);

        Set<Human> set = new LinkedHashSet<>(cList);
        cList.clear();
        cList.addAll(set);
        int lsize = cList.size();
        // if we need more than the elements in the list, restrict how_many param.
        if (how_many > lsize) how_many = lsize;

        // return last how_many human contacts.
        return cList.subList(0, how_many);
    }
}
