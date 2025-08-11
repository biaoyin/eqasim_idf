package org.eqasim.ile_de_france.driving_restriction;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.*;
import java.util.*;
// BYIN: add carPassengerInternal for LTN scenario 06/25
public class PopulationModifier {

    private static final Logger LOG = LogManager.getLogger(PopulationModifier.class);
    private static final String scenarioID = "ile-de-france-5pct";
    public static void main(String[] args) throws IOException {
    // Input and output files

        String plansInputFile =  "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\synpop_IdF_egt5pct_egt2020_LTN\\ile_de_france_population.xml.gz";
        String plansOutputFile =  "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\synpop_IdF_egt5pct_egt2020_LTN\\ile_de_france_population_carInternal_carPassengerInternal_residentOnly.xml.gz";
        String outputFile_ResidentsReader = "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\synpop_IdF_egt5pct_egt2020_LTN\\personInternalIDsList.txt";

        //String RedStreets = "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\synpop_IdF_egt5pct_egt2020_LTN\\RedLinksID.txt";
        //String GreenStreets ="ile_de_france\\scenarios\\" + scenarioID + "\\driving_restriction_paris_4arr\\GreenLinksID.txt";
        //String YellowStreets = "ile_de_france\\scenarios\\" + scenarioID + "\\driving_restriction_paris_4arr\\YellowLinksID.txt";
        String InternalStreets = "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\synpop_IdF_egt5pct_egt2020_LTN\\Paris_linkIDs.txt";

        //1) preparation: switch to list
        // RedStreets
//        BufferedReader bfrRedStreets = new BufferedReader(new FileReader(RedStreets));
//        ArrayList<String> RedStreetsList = new ArrayList<>();
//        while (true){
//            String s = bfrRedStreets.readLine();
//            if(s==null){
//                break;
//            }
//            RedStreetsList.add(s);
//        }
//        bfrRedStreets.close();
        // GreenStreets
//        BufferedReader bfrGreenStreets = new BufferedReader(new FileReader(GreenStreets));
//        ArrayList<String> GreenStreetsList = new ArrayList<>();
//        while (true){
//            String s = bfrGreenStreets.readLine();
//            if(s==null){
//                break;
//            }
//            GreenStreetsList.add(s);
//        }
//        bfrGreenStreets.close();
        // YellowStreets
//        BufferedReader bfrYellowStreets = new BufferedReader(new FileReader(YellowStreets));
//        ArrayList<String> YellowStreetsList = new ArrayList<>();
//        while (true){
//            String s = bfrYellowStreets.readLine();
//            if(s==null){
//                break;
//            }
//            YellowStreetsList.add(s);
//        }
//        bfrYellowStreets.close();
        // InternalStreets
        BufferedReader bfrInternalStreets = new BufferedReader(new FileReader(InternalStreets));
        ArrayList<String> InternalStreetsList = new ArrayList<>();
        while (true){
            String s = bfrInternalStreets.readLine();
            if(s==null){
                break;
            }
            InternalStreetsList.add(s);
        }
        bfrInternalStreets.close();

        //Step 2: Get population
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(plansInputFile);

        HashSet<String> personInternalLinkList = new HashSet<>();
        personInternalLinkList.addAll(InternalStreetsList);
        //ersonInternalLinkList.addAll(YellowStreetsList);
        //personInternalLinkList.addAll(RedStreetsList);

//        System.out.println(personInternalLinkList);
        System.out.println(personInternalLinkList.size());

        int count = 0;
        //prepare List
        ArrayList<String> personInternalIDsList = new ArrayList<>();
        //2.1 Substitute car mode by carInternal mode for people inside relevant area
        for (Person person : scenario.getPopulation().getPersons().values()) {
            //option 1 for only Residents
            String homeActAsLink = null;
            Activity homeActivity = (Activity) person.getPlans().get(0).getPlanElements().get(0); // first activity is from home: attention, the first act may be not home
            if (homeActivity.getType().equals("home")) {
                homeActAsLink = homeActivity.getLinkId().toString();
            }

            //option 2: add people with all activities in this area
            List<PlanElement> planElements = person.getPlans().get(0).getPlanElements();
            List<Activity> activities = TripStructureUtils.getActivities(planElements,
                    TripStructureUtils.StageActivityHandling.ExcludeStageActivities);
            List<String> ActAsLinkList =  new ArrayList<String>();
            for (Activity act: activities) {
                ActAsLinkList.add(act.getLinkId().toString());
            }

            if (personInternalLinkList.contains(homeActAsLink)){  //option 1: only residents
//            if (personInternalLinkList.stream().anyMatch(ActAsLinkList::contains)){  //option 2: add people with all activities
                count ++;
                person.getAttributes().putAttribute("subpopulation", "personInternal");
                personInternalIDsList.add(person.getId().toString());
                for (PlanElement pe : person.getPlans().get(0).getPlanElements()) {
                    if (pe instanceof Leg) {
                        Leg leg = (Leg) pe;
                        if (leg.getMode().equals(TransportMode.car)) {
                            leg.setMode("carInternal");
                            leg.setRoutingMode("carInternal");
                            //leg.getAttributes().putAttribute("routingMode", "carInternal");
                        }

                        if (leg.getMode().equals("car_passenger")) {
                            leg.setMode("carPassengerInternal");
                            leg.setRoutingMode("carPassengerInternal");
                        }


                        String routingMode = leg.getRoutingMode();
                        if (leg.getMode().equals(TransportMode.walk) && (routingMode.equals(TransportMode.car)))
                        {
                           leg.setRoutingMode("carInternal");
                            //leg.getAttributes().putAttribute("routingMode", "carInternal");
                             // as well as for "car_passenger"
                        }
                        if (leg.getMode().equals(TransportMode.walk) && (routingMode.equals("car_passenger")))
                        {
                            leg.setRoutingMode("carPassengerInternal");
                        }
                    } else if (pe instanceof Activity) {
                        Activity activity = (Activity) pe;
                        if (activity.getType().equals("car interaction")) {
                            activity.setType("carInternal interaction");
                        }
                        if (activity.getType().equals("car_passenger interaction")) {
                            activity.setType("carPassengerInternal interaction");
                        }
                    }
                }
            } else{
                person.getAttributes().putAttribute("subpopulation", "personExternal"); //Added for the rest of people
            }
        }
        LOG.info("the nb of internal residents is:");
        System.out.println(count);

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile_ResidentsReader));
        // traverses the collection
        for (String s : personInternalIDsList) {
            // write data
            bw.write(s);
            bw.newLine();
            bw.flush();
        }
        // release resource
        bw.close();

        // Write modified population to file
        PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation());
        populationWriter.write(plansOutputFile);
    }

}
