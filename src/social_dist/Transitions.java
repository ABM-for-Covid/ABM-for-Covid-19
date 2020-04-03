package social_dist;

public class Transitions {
    public static double inf_score_scaler = 5.0;

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
        }
    }

    public static void calculateE_I0toI1(Human exposedHuman) {
        /*
        Just set age a parameter of expose to infection
         */
        int a_x = exposedHuman.get_age_score();

        // if weak immune person exposed, it will go to high chance to I1
        if (exposedHuman.weakImmune) {
            if (getRandomBooleanRange(1 / Env.incubationMean, 1/(1+a_x))) {
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
            if (getRandomBoolean(1 / Env.INCUBATION_PERIOD_High)) {
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


    public static void calculateI0Transition(Human i0Human) {

        // I0->R is based on the time duration agent stays in the i0 state probabilistically 
        if (i0Human.count_EI0 >= 12) {
            i0Human.setRecovered(true);
            i0Human.setInfected(false);
            i0Human.setInfectionState(-1);
            return;
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

        // score ranges from -2 to 6
        double score = a_x + human.overallHealth + human.coMorbid_score + 2;
        double score_days_spent = score - inf_score;

        // Normalize score to range from 0 to 10
        double prob_score = (score_days_spent + 2) / 10;

        if (prob_score < 0) prob_score = 0;

        // transition to R
        if (human.count_I1 > Env.i1Period && human.wantToMoveToI2 < 2) {
            human.setRecovered(true);
            human.setInfected(false);
            return;
        }

        // transition to I2
        if (human.wantToMoveToI2 > 2 || getRandomBoolean((1 - prob_score))) {
            human.wantToMoveToI2++;
            if (Env.HospitalBedCount > 0) {

                // if human is not isolated, then only do the contact tracing
                // this sets contact tracing to single level.
                if (Env.contactTracing && !human.quarantined){
                    human.findAndMarkTraces();
                    human.setPrime(true);
                    human.setQuarantined(true);
                }
                human.setInfectionState(2);
                Env.HospitalBedCount--;
                return;
            } else {
                //calculate transition to D
                if (getRandomBoolean((Env.I2toD_CONST * (1 - prob_score)))) {
                    human.setDead(true);
                    human.setInfected(false);
                    Env.HospitalBedCount++;
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
        if (getRandomBoolean(1 - prob_score)) {

            // check availability
            int icu_count = Env.getIcuCount();

            //probability to D
            if (icu_count <= 0) {
                // if not available transit to D with fix prob
                if (getRandomBoolean(Env.i2ToDProbability)) {
                    human.setDead(true);
                    human.setInfected(false);
                    Env.HospitalBedCount++;
                    return;
                }
            }
            //todo if already ICUed once, then increase the prob to D ? how?
            else {
                human.setInfectionState(3);
                Env.icuCount--;
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
            inf_score = human.count_I3 /inf_score_scaler;
        }

        // score ranges from -2 to 6
        double score = a_x + human.overallHealth + human.coMorbid_score - inf_score;
        // Normalize score to range from 0 to 10
        double prob_score = (score + 4) / 10;
        if (prob_score < 0) prob_score = 0;

        // probability to D
        if (getRandomBoolean(1 - prob_score)) {
            human.setDead(true);
            human.setInfected(false);
            Env.icuCount++;
            return;
        }

        // probability to I2
        score = a_x + human.overallHealth + human.coMorbid_score + 2;
        prob_score = score / (8 * (1 + human.count_I3));
        if (getRandomBoolean(prob_score)) {
            human.setInfectionState(2);
            human.count_I2 = 0;
            Env.icuCount++;
            return;
        }
        human.count_I3++;

    }

    public static void countQuarantinedDays(Human human) {
        if (human.count_iso >= Env.quarantine_day_limit)
            human.setQuarantined(false);
        else human.count_iso++;
    }
}


