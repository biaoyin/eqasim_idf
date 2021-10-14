package org.eqasim.core.analysis;

import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.TripStructureUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


// attention: the id of person, or link of bike_pt iteraction is not the final selected in plans (might be the first plan related events).  Biao oct. 21
@Singleton
public class BikePtEventHandler implements ActivityStartEventHandler{
    private long interactionAct = 0;
    private long intermodalCountBikePt = 0;
    private long intermodalCountPtBike = 0;
    private List<Id<Person>> personIdListBikePt = new ArrayList<Id<Person>>();
    private List<Id<Person>> personIdListPtBike = new ArrayList<Id<Person>>();

    private List<Id<Link>> prkIdListBikePt = new ArrayList<Id<Link>>();
    private List<Id<Link>> prkIdListPtBike = new ArrayList<Id<Link>>();
    //private OutputDirectoryHierarchy outputDirectory;

    private long carCount = 0;
    private long carPassengerCount = 0;
    private long ptCount = 0;
    private long walkCount = 0;
    private long bikeCount = 0;
    @Override
    public void handleEvent(ActivityStartEvent event) {
        if(TripStructureUtils.isStageActivityType(event.getActType())){
            interactionAct += 1;

            if (event.getActType().equals("bikePt interaction")) {
                intermodalCountBikePt += 1;

                if(!personIdListBikePt.contains(event.getPersonId())) {
                    personIdListBikePt.add(event.getPersonId());
                }
                if(!prkIdListBikePt.contains(event.getLinkId())) {
                    prkIdListBikePt.add(event.getLinkId());
                }

            }

            if (event.getActType().equals("ptBike interaction")) {
                intermodalCountPtBike += 1;
                if(!prkIdListPtBike.contains(event.getLinkId())) {
                    prkIdListPtBike.add(event.getLinkId());
                }
                if(!personIdListPtBike.contains(event.getPersonId())) {
                    personIdListPtBike.add(event.getPersonId());
                }
            }
        }






        if (event.getActType().equals("car interaction")) {
            carCount += 1;

        }

        if (event.getActType().equals("car_passenger interaction")) {
            carPassengerCount += 1;

        }

        if (event.getActType().equals("pt interaction")) {
            ptCount += 1;

        }

        if (event.getActType().equals("bike interaction")) {
            bikeCount += 1;

        }

        if (event.getActType().equals("walking interaction")) {
            walkCount += 1;

        }

    }

    @Override
    public void reset(int iteration) {
        String counter = "Number of interaction activities = " + interactionAct + "\n";

        String counter1 = "Number of bikePt interaction = " + intermodalCountBikePt + "\n";

        String counter2 = "Number of ptBike interaction = " + intermodalCountPtBike + "\n";

        String counter3 = "Id of person in bike_pt interaction \n";

        String counter4 = "Id of link bike_pt interaction \n";

        String counter5 = "Id of link pt_bike interaction \n";

        String counter6 = "Id of person in pt_bike interaction \n";

        for (int i = 0; i < personIdListBikePt.size(); i++) {
            counter3+= personIdListBikePt.get(i) + "\n";
        }

        for (int i = 0; i < prkIdListBikePt.size(); i++) {
            counter4+= prkIdListBikePt.get(i) + "\n";
        }


        for (int i = 0; i < personIdListPtBike.size(); i++) {
            counter6+= personIdListPtBike.get(i) + "\n";
        }

        for (int i = 0; i < prkIdListPtBike.size(); i++) {
            counter5+= prkIdListPtBike.get(i) + "\n";
        }

//        File outputFile = new File("C:\\Users\\azise.oumar.diallo\\Documents\\AziseThesis\\GenerationPopulationSynthetique\\MEL_Simulations\\output_0.1p\\count_car_pt\\intermodalCount" + iteration + ".csv");
        File outputFile = new File("simulation_output\\intermodalCount" + iteration + ".csv");
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));

            writer.write(counter);
            writer.flush();
            writer.write(counter1);
            writer.flush();
            writer.write(counter2);
            writer.flush();
            writer.write(counter3);
            writer.flush();
            writer.write(counter4);
            writer.flush();
            writer.write(counter6);
            writer.flush();
            writer.write(counter5);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //BufferedWriter writer = new BufferedWriter("/home/dialloaziseoumar/AziseThesis/GenerationPopulationSynthetique/mel/output/CarPtInteractionCount.json");

        //Initialization
        interactionAct = 0;
        intermodalCountBikePt = 0;
        intermodalCountPtBike = 0;
        personIdListBikePt.clear();
        personIdListPtBike.clear();
        prkIdListBikePt.clear();
        prkIdListPtBike.clear();
        carCount = 0;
        carPassengerCount = 0;
        ptCount = 0;
        walkCount = 0;
        bikeCount = 0;

    }
}
