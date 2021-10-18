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

@Singleton
public class CarPtEventHandler implements ActivityStartEventHandler{
    private long interactionAct = 0;
    private long intermodalCountCarPt = 0;
    private long intermodalCountPtCar = 0;
    private List<Id<Person>> personIdListCarPt = new ArrayList<Id<Person>>();
    private List<Id<Person>> personIdListPtCar = new ArrayList<Id<Person>>();

    private List<Id<Link>> prkIdListCarPt = new ArrayList<Id<Link>>();
    private List<Id<Link>> prkIdListPtCar = new ArrayList<Id<Link>>();
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

            if (event.getActType().equals("carPt interaction")) {
                intermodalCountCarPt += 1;
                if(!personIdListCarPt.contains(event.getPersonId())) {
                    personIdListCarPt.add(event.getPersonId());
                }
                if(!prkIdListCarPt.contains(event.getLinkId())) {
                    prkIdListCarPt.add(event.getLinkId());
                }

            }

            if (event.getActType().equals("ptCar interaction")) {
                intermodalCountPtCar += 1;
                if(!prkIdListPtCar.contains(event.getLinkId())) {
                    prkIdListPtCar.add(event.getLinkId());
                }
                if(!personIdListPtCar.contains(event.getPersonId())) {
                    personIdListPtCar.add(event.getPersonId());
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

        String counter1 = "Number of carPt interaction = " + intermodalCountCarPt + "\n";

        String counter2 = "Number of ptCar interaction = " + intermodalCountPtCar + "\n";

        String counter3 = "Id of person in car_pt interaction \n";

        String counter4 = "Id of link car_pt interaction \n";

        String counter5 = "Id of link pt_car interaction \n";

        String counter6 = "Id of person in pt_car interaction \n";

        for (int i = 0; i < personIdListCarPt.size(); i++) {
            counter3+=personIdListCarPt.get(i) + "\n";
        }

        for (int i = 0; i < prkIdListCarPt.size(); i++) {
            counter4+=prkIdListCarPt.get(i) + "\n";
        }


        for (int i = 0; i < personIdListPtCar.size(); i++) {
            counter6+=personIdListPtCar.get(i) + "\n";
        }

        for (int i = 0; i < prkIdListPtCar.size(); i++) {
            counter5+=prkIdListPtCar.get(i) + "\n";
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
        intermodalCountCarPt = 0;
        intermodalCountPtCar = 0;
        personIdListCarPt.clear();
        personIdListPtCar.clear();
        prkIdListCarPt.clear();
        prkIdListPtCar.clear();
        carCount = 0;
        carPassengerCount = 0;
        ptCount = 0;
        walkCount = 0;
        bikeCount = 0;

    }
}
