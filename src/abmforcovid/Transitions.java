package abmforcovid;
import sim.util.Bag;
import sim.util.Double2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Transitions {
    public static double inf_score_scaler = 5.0;
    public static double inf_i1_transition_scaler = 4.0;

    public static Random ran_1 = new Random();

    static boolean getRandomBoolean(double probability) {
        double randomValue = Math.random();  //0.0 to 0.99
        return randomValue <= probability;
    }

    static boolean getRandomBooleanRange(double low_prob, double high_prob) {
        double randomValue = Math.random();  //0.0 to 0.99
        return (randomValue <= high_prob && randomValue >= low_prob);
    }

    public static void calculateStoE(Human human, Human infectedHuman) {
        /*
        Depends on the hygiene of both the agents
        calculate the prob of human getting infected
        p_t(human, infectedHuman) = [1-human.hygiene][1-infectedHuman.hygiene]
         */
        double transition_prob = Env.HYGIENE_CONST * (1 - human.hygiene) * (1 - infectedHuman.hygiene);
        if (getRandomBoolean(transition_prob)) {
            human.setExposed(true);
            human.setSusceptible(false);
            infectedHuman.infection_producing_contacts++;
        }
    }

    public static void calculateE_I0toI1(Human exposedHuman) {
        /*
        Just set age a parameter of expose to infection
         */
        int a_x = exposedHuman.get_age_score();

        // if weak immune person exposed, it will go to high chance to I1
        if (exposedHuman.weakImmune) {
            if (getRandomBooleanRange(1 / Env.incubationMean, 1.0 / (1 + a_x))) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(1);
                exposedHuman.setExposed(false);
                exposedHuman.count_I1 = 1;
                return;
            }
            // otherwise it try to go to I0 with some latent period probability type A disease!
            else if (getRandomBooleanRange(1 / (Env.incubationMean - 2), 1 / (Env.incubationMean - 3))
                    && exposedHuman.isExposed()) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(0);
                exposedHuman.setExposed(false);
                exposedHuman.count_EI0++;
                return;
            }
        } else {
            if (getRandomBoolean( 1 /(inf_i1_transition_scaler * Env.INCUBATION_PERIOD_High))) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(1);
                exposedHuman.setExposed(false);
                exposedHuman.count_I1 = 1;
                return;
            }
            // otherwise it try to go to I0 with some latent period probability type A disease!
            else if (getRandomBooleanRange(1 / (Env.incubationMean - 2), 1 / (Env.incubationMean - 3))
                    && exposedHuman.isExposed()) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(0);
                exposedHuman.setExposed(false);
                exposedHuman.count_EI0++;
                return;
            }

        }

        exposedHuman.count_EI0++;
    }


    public static void calculateI0Transition(Human i0Human, Env hb) {

        // I0->R is based on the time duration agent stays in the i0 state probabilistically 
        if (i0Human.count_EI0 >= hb.expose_to_recovery_days) {
            i0Human.setRecovered(true);
        }
        // I0-> I1 -> transition into I1 depend on weakImmune and age
        // stay in I0
        else calculateE_I0toI1(i0Human);

    }

    public static void calculateI1Transition(Human human) {
        // age score
        int a_x = human.get_age_score();
        double inf_score = 0;

        // no of days stayed in I1 has an impact on the score
        if (human.count_I1 > 4) {
            inf_score = human.count_I1 / inf_score_scaler;
        }

        if (human.count_I1 == 2 && Env.policy_trace_and_test && !human.quarantined){
                human.setPrime(true);
                human.setQuarantined(true);
                human.recursiveTraceandTest();

        }

        // score ranges from -2 to 6
        double score = a_x + human.overallHealth + human.coMorbid_score + 2;
        double score_days_spent = score - inf_score;

        // Normalize score to range from 0 to 10
        double prob_score = (score_days_spent + 2) / 10;

        if (prob_score < 0) prob_score = 0;

        // transition to R
        if (human.count_EI0 + human.count_I1 >= Env.infection_to_recovery_days && human.wantToMoveToI2 < 2) {
            human.setRecovered(true);
            return;
        }

        // transition to I2
        if (human.wantToMoveToI2 > 2 || getRandomBoolean(Env.I2toD_CONST *(1 - prob_score))) {
            human.wantToMoveToI2++;
            if (Env.capacity_hospital_bed > 0) {

                // if human is not isolated, then only do the contact tracing
                // this sets contact tracing to single level.
                if (Env.policy_contact_tracing && !human.quarantined) {
                    human.findAndMarkTraces();
                }
                if (Env.policy_quarantine) {
                    human.setPrime(true);
                    human.setQuarantined(true);
                }
                human.setInfectionState(2);
                Env.capacity_hospital_bed--;
                return;
            } else {
                //calculate transition to D
                if (getRandomBoolean((Env.I2toD_CONST * (1 - prob_score)))) {
                    human.setDead(true);
                    human.setInfected(false);
                    Env.capacity_hospital_bed++;
                    return;
                }

            }
        }
        //stay in I1
        human.count_I1++;
    }

    public static void calculateI2Transition(Human human) {
        int a_x = human.get_age_score();
        double inf_score = 0;

        // no of days stayed in I2 has an impact on the score
        if (human.count_I2 > 4) {
            inf_score = human.count_I2 / inf_score_scaler;
        }

        // score ranges from -2 to 6
        double score = a_x + human.overallHealth + human.coMorbid_score - inf_score;
        // Normalize score to range from 0 to 10
        double prob_score = (score + 4) / 10;
        if (prob_score < 0) prob_score = 0;

        // probability to I3
        if (getRandomBoolean((Env.I2toD_CONST * (1 - prob_score)))) {

            // check availability
            int icu_count = Env.getCapacity_icu_beds();

            //probability to D
            if (icu_count <= 0) {
                // if not available transit to D with fix prob
                if (getRandomBoolean(Env.i2ToDProbability)) {
                    human.setDead(true);
                    human.setInfected(false);
                    Env.capacity_hospital_bed++;
                    return;
                }
            }
            //todo if already ICUed once, then increase the prob to D ? how?
            else {
                human.setInfectionState(3);
                Env.capacity_icu_beds--;
                return;
            }
        }

        // probability to I1
        score = a_x + human.overallHealth + human.coMorbid_score + 2;
        prob_score = score / (8 * (1 + human.count_I2));
        if (getRandomBoolean(prob_score)) {
            human.setInfectionState(1);
            human.count_I1 = 0;
            return;
        }

        human.count_I2++;

    }

    public static void calculateI3Transition(Human human) {
        int a_x = human.get_age_score();
        double inf_score = 0;

        // no of days stayed in I3 has an impact on the score
        if (human.count_I3 > 4) {
            inf_score = human.count_I3 / inf_score_scaler;
        }

        // score ranges from -2 to 6
        double score = a_x + human.overallHealth + human.coMorbid_score - inf_score;
        // Normalize score to range from 0 to 10
        double prob_score = (score + 4) / 10;
        if (prob_score < 0) prob_score = 0;

        // probability to D
        if (getRandomBoolean((Env.I2toD_CONST * (1 - prob_score)))) {
            human.setDead(true);
            human.setInfected(false);
            Env.capacity_icu_beds++;
            return;
        }

        // probability to I2
        score = a_x + human.overallHealth + human.coMorbid_score + 2;
        prob_score = score / (8 * (1 + human.count_I3));
        if (getRandomBoolean(prob_score)) {
            human.setInfectionState(2);
            human.count_I2 = 0;
            Env.capacity_icu_beds++;
            return;
        }
        human.count_I3++;

    }


    public static void countQuarantinedDays(Human human) {
        if (human.count_iso >= Env.quarantine_day_limit && human.infectionState <= 0)
            human.setQuarantined(false);
        else human.count_iso++;
    }

    public static  void  add_new_infectious_agents(){
        Bag all_agents = Env.TravelerEnvironment.getAllObjects();
        all_agents.shuffle(ran_1);
        int agents_to_add = ran_1.nextInt(Env.infected_traveler_pday_limit);
        if (all_agents.numObjs < agents_to_add) agents_to_add = all_agents.numObjs;

//        System.out.println("Adding "+agents_to_add+" travellers");
        // add new infectious agents to environment
        for (int i = 0; i < agents_to_add; i++) {
            if (all_agents.objs[i] != null) {

                if (!(all_agents.objs[i] instanceof Human))
                    continue;
                Human traveler = (Human) (all_agents.objs[i]);
                traveler.setInfected(true);
                traveler.setInfectionState(0);
                Double2D loc;
                do {
                    loc = new Double2D(ran_1.nextDouble() * (Env.env_xmax - Env.XMIN - Env.DIAMETER) + Env.XMIN + Env.DIAMETER / 2,
                            ran_1.nextDouble() * (Env.env_ymax - Env.YMIN - Env.DIAMETER) + Env.YMIN + Env.DIAMETER / 2);
                } while (!Env.acceptablePosition(traveler, loc));
//                System.out.println("removing traveler " + traveler.id + " from traveler env");
                try {
                    Env.TravelerEnvironment.remove(traveler);
//                    System.out.println("Adding traveler " + traveler.id + " to the human env");
                    Env.HumansEnvironment.setObjectLocation(traveler, loc);
                    Env.num_traveler_Agents++;
                } catch (Exception e) {
                   continue;
                }
            }
        }
    }


    public static void run_tests() {
        // reset the env each day for fresh testing
        Env.TestEnvironment.clear();
        Env.t_dx = Env.uiIndent;
        Env.t_dy = Env.uiIndent;

        // if env says no tests now
        if (Env.capacity_testing == 0)
            return;
        ArrayList<Human> sample_list = new ArrayList<>();
        Bag all_agents = Env.HumansEnvironment.getAllObjects();
        for (int i = 0; i < all_agents.numObjs; i++) {
            if (all_agents.objs[i] != null) {
                Human ta = (Human) (all_agents.objs[i]);
                if (ta.infectionState <= 1 && !ta.tested) {
                    sample_list.add(ta);
                }
            }
        }
        if (sample_list.size() == 0) return;

        //shuffle the list to pick random agents
        Collections.shuffle(sample_list);
        int test_to_perform = Env.capacity_testing;
        if (sample_list.size() < Env.capacity_testing) test_to_perform = sample_list.size();

        int count_false_negative = (int) (test_to_perform * Env.test_false_negative_percent);
        for (int i = 0; i < test_to_perform; i++) {
            Human hu = sample_list.get(i);

            // generate result based on false negative %
            if (i < count_false_negative) {
                if (hu.infected) hu.test_result_positive = false;
                hu.tested = true;
            } else {
                if (hu.infected) {
                    hu.test_result_positive = true;
                    hu.tested = true;
                    if (Env.policy_quarantine) {
                        hu.setPrime(true);
                        hu.setQuarantined(true);
                    }
                    if (Env.policy_contact_tracing)
                        hu.findAndMarkTraces();
                }
            }
            Env.TestEnvironment.setObjectLocation(hu, Env.assignTestLocation());
        }

    }

    public static void mark_essential_agents(){
        // run it only once to mark a percentage of agents essential
        ArrayList<Human> sample_list = new ArrayList<>();
        Bag all_agents = Env.HumansEnvironment.getAllObjects();
        for (int i = 0; i < all_agents.numObjs; i++) {
            if (all_agents.objs[i] != null) {
                Human ta = (Human) (all_agents.objs[i]);

                // if not having infection and age is working age
                if (ta.infectionState <= 1 && ta.age<60 && ta.age > 18) {
                    sample_list.add(ta);
                }
            }
        }
        if (sample_list.size() == 0) return;

        //shuffle the list to pick random agents
        Collections.shuffle(sample_list);
        int agents_to_mark = (int)(all_agents.size() * Env.ini_essential_agent_percent);
        if (sample_list.size() < agents_to_mark) agents_to_mark = sample_list.size();

        for (int i = 0; i < agents_to_mark; i++) {
            Human hu = sample_list.get(i);
            hu.essential = true;
        }
    }

    public static void mark_recovered_agents() {
        // run it only once to mark a percentage of agents essential
        ArrayList<Human> sample_list = new ArrayList<>();
        Bag all_agents = Env.HumansEnvironment.getAllObjects();
        for (int i = 0; i < all_agents.numObjs; i++) {
            if (all_agents.objs[i] != null) {
                Human ta = (Human) (all_agents.objs[i]);
                    sample_list.add(ta);
            }
        }
        if (sample_list.size() == 0) return;
        //shuffle the list to pick random agents
        Collections.shuffle(sample_list);
        int agents_to_mark = (int)(all_agents.size() * Env.ini_recovery_percent);
        if (sample_list.size() < agents_to_mark) agents_to_mark = sample_list.size();

        for (int i = 0; i < agents_to_mark; i++) {
            Human hu = sample_list.get(i);
            hu.setRecovered(true);
        }
    }

    public static void mark_weak_immune_agents() {
        // run it only once to mark a percentage of agents essential
        ArrayList<Human> sample_list = new ArrayList<>();
        Bag all_agents = Env.HumansEnvironment.getAllObjects();
        for (int i = 0; i < all_agents.numObjs; i++) {
            if (all_agents.objs[i] != null) {
                Human ta = (Human) (all_agents.objs[i]);
                    sample_list.add(ta);
            }
        }
        if (sample_list.size() == 0) return;
        //shuffle the list to pick random agents
        Collections.shuffle(sample_list);
        int agents_to_mark = (int)(all_agents.size() * Env.ini_weak_immune_percent);
        if (sample_list.size() < agents_to_mark) agents_to_mark = sample_list.size();

        for (int i = 0; i < agents_to_mark; i++) {
            Human hu = sample_list.get(i);
            hu.weakImmune = true;
        }
    }
}


