package org.eqasim.ile_de_france.driving_restriction;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

//import static org.matsim.core.population.PopulationUtils.createPopulation;

//BYIN: this is for specific test of driving restriction scenario with small size of population;
//version 2: both outside workers and local residents are considered.

public class PopulationCapture_2 {

    private static final Logger LOG = Logger.getLogger(PopulationCapture_2.class);
    private static final String scenarioID = "/ile-de-france-1pm";
    private static final String strategyID = "/driving_restriction";
    public static void main(String[] args) throws IOException {
        String networkInputFile ;
        if (strategyID.equals("/base_case") ) {
            networkInputFile = "./ile_de_france/scenarios" + scenarioID + strategyID + "/ile_de_france_network.xml.gz";
        } else {
            networkInputFile = "./ile_de_france/scenarios" + scenarioID + strategyID + "/ile_de_france_network_carInternal.xml.gz";
        }
        String plansInputFile = "./ile_de_france/scenarios" + scenarioID + strategyID + "/ile_de_france_population.xml.gz";
        String plansOutputFile = "./ile_de_france/scenarios"  + scenarioID + strategyID + "/ile_de_france_population_test_200p.xml.gz";
        String outputFile_ResidentsReader = "./ile_de_france/scenarios" + scenarioID + strategyID + "/personTestIDsList_200p.txt";
        //step 1: get internal and outside links of paris
        // for internal links
        String InternalLinksIDFile = "./ile_de_france/scenarios" + scenarioID + strategyID + "/InternalLinksID.txt";
        BufferedReader bfrInternalLinks = new BufferedReader(new FileReader(InternalLinksIDFile));
        HashSet<String> internalLinksIDs = new HashSet<>();
        ArrayList<String> internalLinksIDsList = new ArrayList<>();
        while (true){
            String s = bfrInternalLinks.readLine();
            if(s==null){
                break;
            }
            internalLinksIDsList.add(s);
        }
        bfrInternalLinks.close();
        internalLinksIDs.addAll(internalLinksIDsList);
        System.out.println(internalLinksIDs.size());
        // for outside link list
        // Get network
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        MatsimNetworkReader reader = new MatsimNetworkReader(scenario.getNetwork());
        reader.readFile(networkInputFile);
        ArrayList<String> outsideLinksIDsList = new ArrayList<>();
        for (Link link : scenario.getNetwork().getLinks().values()) {
            if(!internalLinksIDs.contains(link.getId().toString())) {
                outsideLinksIDsList.add(link.getId().toString());
            }

        }
        HashSet<String> outsideLinksIDs = new HashSet<>();
        outsideLinksIDs.addAll(outsideLinksIDsList);
        System.out.println(outsideLinksIDs.size());

        //Step 2: Get the population
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(plansInputFile);
        // for workers
        int count = 0;
        boolean modeIsCar = false;
        boolean actIsWork = false;
        //prepare List
        ArrayList<String> personIDsList = new ArrayList<>();
        // for residents
        int count2 = 0;
        boolean modeIsCar2 = false;
        ArrayList<String> residentIDsList = new ArrayList<>();

        for (Person person : scenario.getPopulation().getPersons().values()) {
            String homeActAsLink = null;
            String workActAsLink = null;
            Activity firstActivity = (Activity) person.getPlans().get(0).getPlanElements().get(0); // first activity is from home: attention, the first act may be not home
            if (firstActivity.getType().equals("home")) {
                homeActAsLink = firstActivity.getLinkId().toString();
            }

            //option 1: add people with specific activity in this area: i.e. work
            List<PlanElement> planElements = person.getPlans().get(0).getPlanElements();
            List<Activity> activities = TripStructureUtils.getActivities(planElements,
                    TripStructureUtils.StageActivityHandling.ExcludeStageActivities);

            for (Activity act: activities) {
                if (act.getType().equals("work")) {
                    workActAsLink = act.getLinkId().toString();
                    break;
                }
            }

            if (outsideLinksIDs.contains(homeActAsLink) && internalLinksIDs.contains(workActAsLink)){
                if (strategyID.equals("/base_case") ) {
                    continue;
                } else {
                    person.getAttributes().putAttribute("subpopulation", "personExternal");
                }
                for (PlanElement pe : person.getPlans().get(0).getPlanElements()) {
                    if (pe instanceof Leg) {
                        Leg leg = (Leg) pe;
                        if (leg.getMode().equals(TransportMode.car)) {
                            modeIsCar = true;
                        } else{
                            actIsWork = false;
                            modeIsCar = false;
                        }
                    } else if(pe instanceof Activity){
                        Activity activity = (Activity) pe;
                        if (activity.getType().equals("work")) {
                            actIsWork = true;
                        } else {
                            actIsWork = false;
                            modeIsCar = false;
                        }
                    }

                    if (modeIsCar == true && actIsWork == true){
                        count ++;
                        personIDsList.add(person.getId().toString());
                        break;
                    }

                }
            }


            //option 2: add people who are residents
            if (internalLinksIDs.contains(homeActAsLink)) {
                if (strategyID.equals("/base_case") ) {
                    continue;
                } else {
                    person.getAttributes().putAttribute("subpopulation", "personInternal");
                }
                for (PlanElement pe : person.getPlans().get(0).getPlanElements()) {
                    if (pe instanceof Leg) {
                        Leg leg = (Leg) pe;
                        if (leg.getMode().equals(TransportMode.car)) {
                            leg.setMode("carInternal");
                            leg.getAttributes().putAttribute("routingMode", "carInternal");
                            modeIsCar2 = true;
                        } else {
                            modeIsCar2 = false;
                        }
                    } else if (pe instanceof Activity) {
                        modeIsCar2 = false;
                        Activity activity = (Activity) pe;
                        if (activity.getType().equals("car interaction")) {
                            activity.setType("carInternal interaction");
                            break;
                        }
                    }
                    if (modeIsCar2 == true) {
                        count2++;
                        residentIDsList.add(person.getId().toString());
                    }

                }
            }
        }
        LOG.info("the nb of concerned population is:");
        System.out.println(count);
        System.out.println(count2);
        ArrayList<String> personSampleIDsList = new ArrayList<>();

        int nbOfElements = 100;
        Random rand = new Random();
        for (int i = 0; i < nbOfElements; i++) {
            int indice = rand.nextInt(personIDsList.size());
            personSampleIDsList.add(personIDsList.get(indice));
            personIDsList.remove(indice);
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile_ResidentsReader));
        // traverses the collection 1: 100 who are related to DRZ no residents
        for (String s : personSampleIDsList) {
            // write data
            bw.write(s);
            bw.newLine();
            bw.flush();
        }

        // traverses the collection 2: 100 who are related to residents
        ArrayList<String> residentSampleIDsList = new ArrayList<>();
        int nbOfElements2 = 100;
        Random rand2 = new Random();
        for (int i = 0; i < nbOfElements2; i++) {
            int indice2 = rand2.nextInt(residentIDsList.size());
            residentSampleIDsList.add(residentIDsList.get(indice2));
            residentIDsList.remove(indice2);
        }
        for (String s : residentSampleIDsList) {
            // write data
            bw.write(s);
            bw.newLine();
            bw.flush();
        }

        // release resource
        bw.close();

        // Write new population to file
        Population population = scenario.getPopulation();
        PopulationWriter populationWriter = new PopulationWriter(population);
        population.getPersons().values().removeIf(person -> !(personSampleIDsList.contains(person.getId().toString()) || residentSampleIDsList.contains(person.getId().toString())));
        populationWriter.write(plansOutputFile);
    }

}
