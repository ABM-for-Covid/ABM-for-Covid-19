package abmforcovid;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import sun.util.locale.provider.FallbackLocaleProviderAdapter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class RunABM {

    public String experiment;
    public String resultfile;
    public String dailyfile;
    public boolean stream_data = true;
    //scaling factors of environments
    public  int num_agents = 100;
    public  int sim_cycle_per_day = 500;
    public  double essential_agent_percent = 0.05;
    public  double recovery_percent = 0.0;
    public  double agent_density = 0.0001;
    public  double hospital_bed_per_agent = 0.1;
    public  double icu_bed_per_hospital_bed = 0.05;

    public  double infection_percent = 0.0;
    // age is a triangular distribution between 1, 90 with peak at 25
    public  double distribution_age_min = 1;
    public  double distribution_age_max = 90;
    public  double distribution_age_peak = 25;

    //hygiene distribution
    public  double distribution_hygiene_mean = 0.5;
    public  double distribution_hygiene_var = 1;

    //frontend
    public double a_false_negative_percent = 0.3;
    public int c_contact_trace = 5;
    public double a_social_distancing_efficiency = 0.6;

    public  HashMap<Double, Policies> strategy;

    public void set_parameters() {
        System.out.println("Setting parameters for experiment "+ experiment);
        Env.setExperiment(experiment);
        Env.setResultFile(resultfile);
        Env.setDailyFile(dailyfile);
        Env.setStream_data(stream_data);
        Env.setIni_sim_cycle_per_day(sim_cycle_per_day);
        Env.setIni_essential_agent_percent(essential_agent_percent);
        Env.setIni_recovery_percent(recovery_percent);
        Env.setIni_num_agents(num_agents);
        Env.setIni_agent_density(agent_density);
        Env.setIni_hospital_bed_per_agent(hospital_bed_per_agent);
        Env.setIni_icu_bed_per_hospital_bed(icu_bed_per_hospital_bed);
        int capacity_hospital_bed = (int) (num_agents * hospital_bed_per_agent);
        Env.setCapacity_hospital_bed(capacity_hospital_bed);
        Env.setCapacity_icu_beds((int) (icu_bed_per_hospital_bed * capacity_hospital_bed));
        Env.setIni_infection_percent(infection_percent);
        Env.setIni_distribution_age_min(distribution_age_min);
        Env.setIni_distribution_age_max(distribution_age_max);
        Env.setIni_distribution_age_peak(distribution_age_peak);
        Env.setIni_distribution_hygiene_mean(distribution_hygiene_mean);
        Env.setIni_distribution_hygiene_var(distribution_hygiene_var);

        //frontend
        Env.setCapacity_contact_trace(c_contact_trace);
        Env.setTest_false_negative_percent(a_false_negative_percent);
        Env.setSocial_distancing_efficiency(a_social_distancing_efficiency);

        Env.setStrategy(strategy);

    }

    public void reset_parameters() {
       RunABM base = new RunABM();
       base.set_parameters();
       System.out.println("After reset "+ Env.strategy.toString());
    }

    public void check_policies(){
        Policies first =  (Policies) strategy.get(5);
        if (first == null)
            return;
        System.out.println(first);
        if (first.p_quarantine != -1)
            System.out.println("invoking policy qurantine "+first.p_quarantine);
        if (first.c_testing!=0)
            System.out.println("contact trace capacity is "+ first.c_testing);
    }

    public static void main(String[] args) throws FileNotFoundException {
        // run a for loop on all the experiment files
            RunABM config = null;
            try {
                //read a config file and create an object of the class
                if (args.length == 0)
                    System.out.println("Please provide the config file absolute path");

                String filename =  args[0];
                JsonReader reader = new JsonReader(new FileReader(filename));
                Gson gson = new Gson();
                config = gson.fromJson(reader, RunABM.class);

                // set initial parameters
                config.set_parameters();

                // set policy at marker steps
                config.check_policies();

                try {
                    Env.main(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("reset parameters");
                config.reset_parameters();
            } catch (FileNotFoundException e) {
                System.out.println("File exp" + args[0] + " was not found");
            } catch (JsonIOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
    }
}