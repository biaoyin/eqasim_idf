package org.eqasim.ile_de_france.driving_restriction;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class NetworkModifier {
    private static final Logger LOG = Logger.getLogger(NetworkModifier.class);

    public static void main (String[] args) throws IOException {
        // Input and output files
        String networkInputFile = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\ile_de_france_1pm\\matsim_input\\ile_de_france_network.xml.gz";
        String networkOutputFile = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\ile_de_france_1pm\\matsim_input\\PlanA_Paris\\ile_de_france_network_carInternal.xml.gz";


        String areaShapeFile = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\qgis\\Paris\\paris.shp";


        String RedStreets = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\mobility_model\\link_specification_Paris\\RedLinksID.txt";
        String GreenStreets ="C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\mobility_model\\link_specification_Paris\\GreenLinksID.txt";
        String YellowStreets = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\mobility_model\\link_specification_Paris\\YellowLinksID.txt";
        String InternalStreets = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\mobility_model\\link_specification_Paris\\InternalLinksID.txt";

        //1) preparation: switch to list
        // RedStreets
        BufferedReader bfrRedStreets = new BufferedReader(new FileReader(RedStreets));
        ArrayList<String> RedStreetsList = new ArrayList<>();
        while (true){
            String s = bfrRedStreets.readLine();
            if(s==null){
                break;
            }
            RedStreetsList.add(s);
        }
        bfrRedStreets.close();
        // GreenStreets
        BufferedReader bfrGreenStreets = new BufferedReader(new FileReader(GreenStreets));
        ArrayList<String> GreenStreetsList = new ArrayList<>();
        while (true){
            String s = bfrGreenStreets.readLine();
            if(s==null){
                break;
            }
            GreenStreetsList.add(s);
        }
        bfrGreenStreets.close();
        // YellowStreets
        BufferedReader bfrYellowStreets = new BufferedReader(new FileReader(YellowStreets));
        ArrayList<String> YellowStreetsList = new ArrayList<>();
        while (true){
            String s = bfrYellowStreets.readLine();
            if(s==null){
                break;
            }
            YellowStreetsList.add(s);
        }
        bfrYellowStreets.close();
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

        //no-car mode list (no redstreets - keep no change)
        List<String> notCarModeList = new ArrayList<String>();
        notCarModeList.addAll(InternalStreetsList);
        notCarModeList.addAll(YellowStreetsList);
        notCarModeList.addAll(GreenStreetsList);
        System.out.println(notCarModeList.size());

        //carInternal mode: combine and clean LinksLists for those having carInternal mode in focused Zone: no greenstreets -only pt mode)
        List<String> carInternalList = new ArrayList<String>();
        carInternalList.addAll(InternalStreetsList);
        carInternalList.addAll(YellowStreetsList);
        carInternalList.addAll(RedStreetsList);
//        carInternalList = new ArrayList<String>(new LinkedHashSet<>(carInternalList));
        System.out.println(carInternalList.size());

        // step 2 ——————MetworkModeModifier: link settings for driving restrictions——————
        //Get network
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        MatsimNetworkReader reader = new MatsimNetworkReader(scenario.getNetwork());
        reader.readFile(networkInputFile);
        LOG.info("number of original network links");
        System.out.println(scenario.getNetwork().getLinks().size());
        LOG.info("number of original network nodes");
        System.out.println(scenario.getNetwork().getNodes().size());

        // Modify the network by adding carInternal for all car links and removing car in the driving restriction zone
        for (Link link : scenario.getNetwork().getLinks().values()) {
            Set<String> allowedModesBefore = link.getAllowedModes();
            Set<String> allowedModesAfter = new HashSet<>();

            if (GreenStreetsList.contains(link.getId().toString())) {// green streets without car mode
                allowedModesAfter.add(TransportMode.pt);
                allowedModesAfter.add(TransportMode.bike);
                allowedModesAfter.add(TransportMode.walk);
            }else {
                for (String mode : allowedModesBefore) {
                    if (mode.equals(TransportMode.car)) {
                        allowedModesAfter.add("carInternal");
                        if (!notCarModeList.contains(link.getId().toString())) {
                            allowedModesAfter.add(TransportMode.car);
                        }
                    } else {
                        allowedModesAfter.add(mode);
                    }
                }
            }
            link.setAllowedModes(allowedModesAfter);

        }
        LOG.info("Finished modifying the network with carInternal mode and filtered car mode in the area");

        //////step 3: the following steps are considered to clean the network (car and carInternal subnetworks) because of link continuity for cars, carInternal,...
        //3.1 Get car subnetwork
        Scenario carScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        TransportModeNetworkFilter transportModeNetworkFilterCar = new TransportModeNetworkFilter(scenario.getNetwork());
        transportModeNetworkFilterCar.filter(carScenario.getNetwork(), new HashSet<>(Arrays.asList(TransportMode.car)));
        LOG.info("number of car network links before clearning");
        System.out.println(carScenario.getNetwork().getLinks().size());

        //3.2: clean car subnetwork
        (new NetworkCleaner()).run(carScenario.getNetwork()); //  why clean it?, it seems necessary to run but...
        LOG.info("Finished creating and cleaning car subnetwork in xx; the nb of car links is:");
        System.out.println(carScenario.getNetwork().getLinks().size());
        // Store remaining car links after cleaning in list
        Set<Id<Link>> remainingCarlinksAfterCleaning = new HashSet<>();
        for (Link link : carScenario.getNetwork().getLinks().values()) {
            remainingCarlinksAfterCleaning.add(link.getId());
        }

        //3.3 Get carInternal subnetwork
        Scenario carInternalScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        TransportModeNetworkFilter transportModeNetworkFilterCarInternal = new TransportModeNetworkFilter(scenario.getNetwork());
        transportModeNetworkFilterCarInternal.filter(carInternalScenario.getNetwork(), new HashSet<>(Arrays.asList("carInternal")));
        LOG.info("number of carInternal network links before clearning");
        System.out.println(carInternalScenario.getNetwork().getLinks().size());
        //3.4: clean car subnetwork
        (new NetworkCleaner()).run(carInternalScenario.getNetwork());
        LOG.info("Finished creating and clearning carInternal subnetwork; the nb of carInternal links is:");
        System.out.println(carInternalScenario.getNetwork().getLinks().size());
        Set<Id<Link>> remainingCarInternallinksAfterCleaning = new HashSet<>();
        for (Link link : carInternalScenario.getNetwork().getLinks().values()) {
            remainingCarInternallinksAfterCleaning.add(link.getId());
        }

        // step 4, update the network with cleaned subnetworks
        int count = 0;
        for (Link link : scenario.getNetwork().getLinks().values()) {
            if ((!remainingCarlinksAfterCleaning.contains(link.getId()) && link.getAllowedModes().contains(TransportMode.car)) ||
                    (!remainingCarInternallinksAfterCleaning.contains(link.getId()) && (link.getAllowedModes().contains("carInternal")))) {
                scenario.getNetwork().removeLink(link.getId());
                count++;
            }
        }
        LOG.info("remove nb of links for updating the network: ");
        System.out.println(count);

        //——————SpeedModifier if needed——————
        HashSet<String> cleanedNetworkLinkIDList = new HashSet<>();// faster than ArrayList
        for (Link link : scenario.getNetwork().getLinks().values()) {
            cleanedNetworkLinkIDList.add(link.getId().toString());
        }
        // for InternalStreets
        for (int i = 0; i < InternalStreetsList.size(); i++) {
            if(cleanedNetworkLinkIDList.contains(InternalStreetsList.get(i))) {
                scenario.getNetwork().getLinks().get(Id.createLinkId(InternalStreetsList.get(i))).setFreespeed(8.33);
            }
        }
        // for YellowStreets
        for (int i = 0; i < YellowStreetsList.size(); i++) {
            if(cleanedNetworkLinkIDList.contains(YellowStreetsList.get(i))) {
                scenario.getNetwork().getLinks().get(Id.createLinkId(YellowStreetsList.get(i))).setFreespeed(8.33);
            }
        }

        // for redStreets
        for (int i = 0; i < RedStreetsList.size(); i++) {
            if(cleanedNetworkLinkIDList.contains(RedStreetsList.get(i))) {
                scenario.getNetwork().getLinks().get(Id.createLinkId(RedStreetsList.get(i))).setFreespeed(19.44);
            }
        }

        LOG.info("after modification, the number of new network links");
        System.out.println(scenario.getNetwork().getLinks().size());
        LOG.info("after modification, the number of new network nodes");
        System.out.println(scenario.getNetwork().getNodes().size());
        // Write modified network to file
        NetworkWriter writer = new NetworkWriter(scenario.getNetwork());
        writer.write(networkOutputFile);

    }
}
