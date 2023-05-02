package org.eqasim.core.analysis;

import com.google.inject.Singleton;
import org.eqasim.core.components.ParkRideManager;

import org.eqasim.core.tools.TestCarPtPara;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Singleton
public class CarPtEventHandler implements ActivityStartEventHandler{
    private long interactionAct = 0;
    private long intermodalCountCarPt = 0;
    private long intermodalCountPtCar = 0;
    private List<Id<Person>> personIdListCarPt = new ArrayList<Id<Person>>();
    private List<Id<Person>> personIdListPtCar = new ArrayList<Id<Person>>();

    HashMap<Id<Link>, Integer> prkIdListCarPt = new HashMap<Id<Link>, Integer>();
    HashMap<Id<Link>, Integer> prkIdListPtCar = new HashMap<Id<Link>, Integer>();
//    private List<Id<Link>> prkIdListCarPt = new ArrayList<Id<Link>>();
//    private List<Id<Link>> prkIdListPtCar = new ArrayList<Id<Link>>();

    private long carCount = 0;
    private long carPassengerCount = 0;
    private long ptCount = 0;
    private long walkCount = 0;
    private long bikeCount = 0;

    // Park and ride lot location
    private final List<Coord> parkRideCoords = ParkRideManager.getCoordinates();
    private final Network network = ParkRideManager.getNetwork();

    private  final double car_pt_constant = TestCarPtPara.getPara();

    @Override
    public void handleEvent(ActivityStartEvent event) {

        if(TripStructureUtils.isStageActivityType(event.getActType())){
            interactionAct += 1;

            if (event.getActType().equals("carPt interaction")) {
                intermodalCountCarPt += 1;
                if(!personIdListCarPt.contains(event.getPersonId())) {
                    personIdListCarPt.add(event.getPersonId());
                }
                /*if(!prkIdListCarPt.contains(event.getLinkId())) {
                    prkIdListCarPt.add(event.getLinkId());
                }*/
                Integer carPtCount = prkIdListCarPt.get(event.getLinkId());
                if(carPtCount !=null) {
                    prkIdListCarPt.put(event.getLinkId(), carPtCount + 1);
                } else {
                    prkIdListCarPt.put(event.getLinkId(), 1);
                }

            }

            if (event.getActType().equals("ptCar interaction")) {
                intermodalCountPtCar += 1;
//                if(!prkIdListPtCar.contains(event.getLinkId())) {
//                    prkIdListPtCar.add(event.getLinkId());
//                }
                Integer ptCarCount = prkIdListPtCar.get(event.getLinkId());
                if(ptCarCount !=null) {
                    prkIdListPtCar.put(event.getLinkId(), ptCarCount + 1);
                } else {
                    prkIdListPtCar.put(event.getLinkId(), 1);
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

        String counter4 = "Id of link car_pt interaction; count; Id of park&ride \n";

        String counter5 = "Id of link pt_car interaction; count; Id of park&ride \n";

        String counter6 = "Id of person in pt_car interaction \n";

        for (int i = 0; i < personIdListCarPt.size(); i++) {
            counter3+=personIdListCarPt.get(i) + "\n";
        }

        /*for (int i = 0; i < prkIdListCarPt.size(); i++) {
            counter4+=prkIdListCarPt.get(i) + "\n";
        }*/

        // Biao: find the nearest park&ride location
        for (Id<Link> name: prkIdListCarPt.keySet()) {
            String key = name.toString();
            String value = prkIdListCarPt.get(name).toString();

            double minDist = 999999999.0;
            double distance = 0.0;
            int minIndex = 0;
            if (iteration == 60) {
                Coord linkXY = network.getLinks().get(name).getCoord();
                for (int i = 0; i < parkRideCoords.size(); i++) {
                    distance = CoordUtils.calcEuclideanDistance(parkRideCoords.get(i), linkXY);
                    if (minDist > distance) {
                        minDist = distance;
                        minIndex = i;
                    }
                }
            }

            String IdParkRide = Integer.toString(minIndex + 1);

            counter4 += key + ";" + value + ";" + IdParkRide + "\n";
        }

        for (int i = 0; i < personIdListPtCar.size(); i++) {
            counter6+=personIdListPtCar.get(i) + "\n";
        }

        /*for (int i = 0; i < prkIdListPtCar.size(); i++) {
            counter5+=prkIdListPtCar.get(i) + "\n";
        }*/

        for (Id<Link> name: prkIdListPtCar.keySet()) {
            String key = name.toString();
            String value = prkIdListPtCar.get(name).toString();

            double minDist = 999999999.0;
            double distance = 0.0;
            int minIndex = 0;
            if (iteration==60) {
                Coord linkXY = network.getLinks().get(name).getCoord();
                for (int i = 0; i < parkRideCoords.size(); i++) {
                    distance = CoordUtils.calcEuclideanDistance(parkRideCoords.get(i), linkXY);
                    if (minDist > distance) {
                        minDist = distance;
                        minIndex = i;
                    }
                }
            }
            String IdParkRide = Integer.toString(minIndex + 1);

            counter5 += key + ";" + value + ";" + IdParkRide + "\n";
        }


        final String car_pt_savepath = TestCarPtPara.getCarPtSavePath();
        File outputFile = new File( car_pt_savepath +"/intermodalCount" + iteration + ".csv");
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
