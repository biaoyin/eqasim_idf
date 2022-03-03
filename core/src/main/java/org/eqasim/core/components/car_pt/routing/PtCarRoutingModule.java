package org.eqasim.core.components.car_pt.routing;

import com.google.inject.Inject;
import org.eqasim.core.components.ParkingFinder;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.RoutingModule;
import org.matsim.facilities.Facility;

import java.util.LinkedList;
import java.util.List;


//import org.matsim.core.router.StageActivityTypes;
//import org.matsim.core.router.StageActivityTypesImpl;

public class PtCarRoutingModule implements RoutingModule{
    private final RoutingModule carRoutingModule;
    private final Network network;
    // Create an object of a ptRoutingModule
    private final RoutingModule ptRoutingModule;
    private final List<Coord> parkRideCoords;

    @Inject
    public PtCarRoutingModule(RoutingModule ptRoutingModule, RoutingModule roadRoutingModule, Network network, List<Coord> parkRideCoords) {
        this.carRoutingModule = roadRoutingModule;
        this.ptRoutingModule = ptRoutingModule;
        this.network = network;
        this.parkRideCoords = parkRideCoords;

    }

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
                                                 Person person) {
        // Park and ride lot location

        ParkingFinder prFinder = new ParkingFinder(parkRideCoords);
        Facility prkFacility = prFinder.getParking(person, fromFacility, toFacility, network);

        // Creation of a PT trip from the destination point to PR facility
        List<? extends PlanElement> ptElements = ptRoutingModule.calcRoute(fromFacility, prkFacility, departureTime,
                person);

        // double vehicleDistance = Double.NaN;
        double vehicleTravelTime = 0.0;
        // double price = Double.NaN;

       /* Leg leg = (Leg) ptElements.get(0);
        // vehicleDistance = leg.getRoute().getDistance();
        vehicleTravelTime = leg.getRoute().getTravelTime().seconds();
*/
        // when considering multi-stage car trips, replace above by
        boolean flag_walk_time_const = false; //walk leg time is constant or not
        for (PlanElement element : ptElements) {
            if (element instanceof Leg) {
                Leg leg = (Leg) element;
                switch (leg.getMode()) {
                    case TransportMode.walk:
                        vehicleTravelTime += leg.getRoute().getTravelTime().seconds();
                        break;
                    case TransportMode.pt:
                        vehicleTravelTime += leg.getRoute().getTravelTime().seconds();
                        break;
                    default:
                        throw new IllegalStateException("Unknown mode in pt trip: " + leg.getMode());
                }
            }
        }


        // Given the request time, we can calculate the waiting time
        double timeToAccessCar = 300; // We take 5 min to park the car and access to PT

        double carDepartureTime = departureTime + vehicleTravelTime + timeToAccessCar;

        // Creation of a  car trip from the PR facility to the origin point (home)
        List<? extends PlanElement> carElements = carRoutingModule.calcRoute(prkFacility, toFacility, carDepartureTime,
                person); // Biao: why is this null in default setting?

        // Creation interaction between pt and car
        Link prLink = NetworkUtils.getNearestLink(network, prkFacility.getCoord());
        Activity interactionActivtyPtCar = PopulationUtils.createActivityFromCoordAndLinkId("ptCar interaction",
                prkFacility.getCoord(), prLink.getId());
        interactionActivtyPtCar.setMaximumDuration(timeToAccessCar);// 5 min

        // Creation full trip
        List<PlanElement> allElements = new LinkedList<>();
        allElements.addAll(ptElements);
        allElements.add(interactionActivtyPtCar);
        allElements.addAll(carElements);

        return allElements;
    }
/*
    @Override
    public StageActivityTypes getStageActivityTypes() {
        return new StageActivityTypesImpl("pt interaction", "ptCar interaction", "car interaction");
    }
 */
}
