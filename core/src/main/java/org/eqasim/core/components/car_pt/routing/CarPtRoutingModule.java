package org.eqasim.core.components.car_pt.routing;

import org.eqasim.core.components.ParkingFinder;
import org.matsim.api.core.v01.Coord;
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

public class CarPtRoutingModule implements RoutingModule{
    private final RoutingModule carRoutingModule;
    private final Network network;

    // Create an object of a ptRoutingModule
    private final RoutingModule ptRoutingModule;
    private final List<Coord> parkRideCoords;

    // @Inject
    public CarPtRoutingModule(RoutingModule roadRoutingModule, RoutingModule ptRoutingModule, Network network, List<Coord> parkRideCoords) {
        this.carRoutingModule = roadRoutingModule;
        this.ptRoutingModule = ptRoutingModule;
        this.network = network;
        this.parkRideCoords = parkRideCoords;

    }

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
                                                 Person person) {

        ParkingFinder prFinder = new ParkingFinder(parkRideCoords);

        Facility prkFacility = prFinder.getParking(person, fromFacility, toFacility, network);

        // Creation of a car trip to the PR facility
        List<? extends PlanElement> carElements = carRoutingModule.calcRoute(fromFacility, prkFacility, departureTime,
                null);

        // double vehicleDistance = Double.NaN;
        double vehicleTravelTime = Double.NaN;
        // double price = Double.NaN;

        Leg leg = (Leg) carElements.get(0);
        // vehicleDistance = leg.getRoute().getDistance();
        vehicleTravelTime = leg.getRoute().getTravelTime().seconds(); // can not invoke seconds() in this context

        // Given the request time, we can calculate the waiting time
        double timeToAccessPt = 300; // We take 5 min to park the car and access to PT

        double ptDepartureTime = departureTime + vehicleTravelTime + timeToAccessPt;

        // Creation of a PT trip from the PR facility to the destination
        List<? extends PlanElement> ptElements = ptRoutingModule.calcRoute(prkFacility, toFacility, ptDepartureTime,
                person);

        // Creation interaction between car and pt
        Link prLink = NetworkUtils.getNearestLink(network, prkFacility.getCoord());
        Activity interactionActivtyCarPt = PopulationUtils.createActivityFromCoordAndLinkId("carPt interaction",
                prkFacility.getCoord(), prLink.getId());
        interactionActivtyCarPt.setMaximumDuration(300);// 5 min

        // Creation full trip
        List<PlanElement> allElements = new LinkedList<>();
        allElements.addAll(carElements);
        allElements.add(interactionActivtyCarPt);
        allElements.addAll(ptElements);

        return allElements;

    }

}
