package social_dist;

public class Transitions {

    public static void calculateStoE(Human human, Human infectedHuman) {
        /*
        Depends on the hygiene of both the agents
        calculate the prob of human getting infected
        p_t(human, infectedHuman) = [1-human.hygiene][1-infectedHuman.hygiene]
         */
        double transition_prob = env.HYGIENE_CONST * (1 - human.hygiene) * (1 - infectedHuman.hygiene);
        if (getRandomBoolean(transition_prob)) {
            human.setExposed(true);
            human.setSusceptible(false);
        }
    }

    public static void calculateE_I0toI1(Human exposedHuman) {
        /*
        Just set age a parameter of expose to infection
         */
        // if weak immune person exposed, it will go to high chance to I1
        if (exposedHuman.weakImmune) {
            // todo - do scaling based on age!
            if (getRandomBooleanRange(1 / env.incubationMean, 1)) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(1);
                exposedHuman.setExposed(false);
                exposedHuman.count_I1++;
                return;
            }
            // otherwise it try to go to I0 with some latent period probability type A disease!
            else if (getRandomBooleanRange(1 / (env.incubationMean - 2), 1 / (env.incubationMean - 3))) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(0);
                exposedHuman.setExposed(false);
                exposedHuman.count_EI0++;
                return;
            }
        } else {
            if (getRandomBooleanRange(1 / env.INCUBATION_PERIOD_High, 1 / env.incubationMean)) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(1);
                exposedHuman.setExposed(false);
                exposedHuman.count_I1++;
                return;
            }
            // otherwise it try to go to I0 with some latent period probability type A disease!
            else if (getRandomBooleanRange(1 / (env.incubationMean - 2), 1 / (env.incubationMean - 3))) {
                exposedHuman.setInfected(true);
                exposedHuman.setInfectionState(0);
                exposedHuman.setExposed(false);
                exposedHuman.count_EI0++;
                return;
            }

        }

        exposedHuman.count_EI0++;
    }

    static boolean getRandomBoolean(double probability) {
        double randomValue = Math.random();  //0.0 to 0.99
        return randomValue <= probability;
    }

    static boolean getRandomBooleanRange(double low_prob, double high_prob) {
        double randomValue = Math.random();  //0.0 to 0.99
        return (randomValue <= high_prob && randomValue >= low_prob);
    }

    public static void calculateI0Transition(Human i0Human) {

        // I0->R is based on the time duration agent stays in the i0 state probabilistically 
        if (i0Human.count_EI0 >= 14) {
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
            inf_score = human.count_I1 / 3.0;
        }

        // score ranges from -2 to 6
        double score = a_x + human.overallHealth + human.coMorbid_score - inf_score;
        // Normalize score to range from 0 to 10
        double prob_score = (score + 4) / 10;

        if (prob_score < 0) prob_score = 0;

        // transition to R
        if (human.count_I1 > env.i1Period) {
            human.setRecovered(true);
            human.setInfected(false);
            human.setInfectionState(-1);
            return;
        }

        // transition to I2
        if ( human.wantToMoveToI2 || getRandomBoolean(1 - prob_score)) {
            human.wantToMoveToI2 = true;
            if (env.hospitalCount > 0 ) {
                human.setInfectionState(2);
                env.hospitalCount--;
                return;
            }else {
                //calculate transition to D
                if (getRandomBoolean(1 - prob_score)){
                    human.setDead(true);
                    human.setInfected(false);
                    env.hospitalCount++;
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
            inf_score = human.count_I2 / 3.0;
        }

        // score ranges from -2 to 6
        double score = a_x + human.overallHealth + human.coMorbid_score - inf_score;
        // Normalize score to range from 0 to 10
        double prob_score = (score + 4) / 10;
        if (prob_score < 0) prob_score = 0;

        // probability to I3
        if (getRandomBoolean(1 - prob_score)) {

            // check availability
            int icu_count = env.getIcuCount();

            //probability to D
            if (icu_count <= 0) {
                // if not available transit to D with fix prob
                if (getRandomBoolean(env.i2ToDProbability)) {
                    human.setDead(true);
                    human.setInfected(false);
                    env.hospitalCount++;
                    return;
                }
            }
            //todo if already ICUed once, then increase the prob to D ? how?
            human.setInfectionState(3);
            env.icuCount--;
            return;
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
            inf_score = human.count_I3 / 3.0;
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
            env.icuCount++;
            return;
        }

        // probability to I2
        score = a_x + human.overallHealth + human.coMorbid_score + 2;
        prob_score = score / (8 * (1 + human.count_I3));
        if (getRandomBoolean(prob_score)) {
            human.setInfectionState(1);
            human.count_I2 = 0;
            env.hospitalCount--;
            return;
        }

        human.count_I3++;

    }
}


