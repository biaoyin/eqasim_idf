package org.eqasim.core.components.bike_pt.routing;

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

public class PtBikeRoutingModule implements RoutingModule{
    private final RoutingModule bikeRoutingModule;
    private final Network network;

    // Create an object of a ptRoutingModule
    private final RoutingModule ptRoutingModule;

    private final List<Coord> parkRideCoords;

    public PtBikeRoutingModule(RoutingModule ptRoutingModule, RoutingModule bikeRoutingModule, Network network, List<Coord> parkRideCoords) {
        this.bikeRoutingModule = bikeRoutingModule;
        this.ptRoutingModule = ptRoutingModule;
        this.network = network;
        this.parkRideCoords = parkRideCoords;

    }

    @Override
/*
	 public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility
	 toFacility, double departureTime, Person person) {
	//Id<AVOperator> operatorId = choiceStrategy.chooseRandomOperator();
	//return calcRoute(fromFacility, toFacility, departureTime, person, operatorId);
	 Leg leg = PopulationUtils.createLeg("car_pt"); leg.setTravelTime(600.0);
	 Route route = new GenericRouteImpl(fromFacility.getLinkId(), toFacility.getLinkId()); route.setTravelTime(600.0);
	 route.setDistance(100.0);
	 leg.setRoute(route);
	 return Collections.singletonList(leg);
	 }
	 */

    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
                                                 Person person) {
        // Park and ride lot location

        ParkingFinder prFinder = new ParkingFinder(parkRideCoords);
        //Facility prFacility = prFinder.getParking(person, fromFacility, toFacility, network);


        Facility prkFacility = prFinder.getParking(person, fromFacility, toFacility, network);

        // Creation of a PT trip from the destination point to PR facility
        List<? extends PlanElement> ptElements = ptRoutingModule.calcRoute(fromFacility, prkFacility, departureTime,
                person);

        // double vehicleDistance = Double.NaN;
        double vehicleTravelTime = Double.NaN;
        // double price = Double.NaN;

        Leg leg = (Leg) ptElements.get(0);
        // vehicleDistance = leg.getRoute().getDistance();
        vehicleTravelTime = leg.getRoute().getTravelTime().seconds();

        // Given the request time, we can calculate the waiting time
        double timeToAccessBike = 300; // We take 5 min to park the car and access to PT

        double bikeDepartureTime = departureTime + vehicleTravelTime + timeToAccessBike;

        // Creation of a the bike trip from the PR facility to the origin point (home)
        List<? extends PlanElement> bikeElements = bikeRoutingModule.calcRoute(prkFacility, toFacility, bikeDepartureTime,
                null);

        // Creation interaction between pt and bike
        Link prLink = NetworkUtils.getNearestLink(network, prkFacility.getCoord());
        Activity interactionActivtyPtBike = PopulationUtils.createActivityFromCoordAndLinkId("ptBike interaction",
                prkFacility.getCoord(), prLink.getId());
        interactionActivtyPtBike.setMaximumDuration(300);// 5 min

        // Creation full trip
        List<PlanElement> allElements = new LinkedList<>();
        allElements.addAll(ptElements);
        allElements.add(interactionActivtyPtBike);
        allElements.addAll(bikeElements);

        return allElements;
    }
/*
    @Override
    public StageActivityTypes getStageActivityTypes() {
        return new StageActivityTypesImpl("pt interaction", "ptCar interaction", "car interaction");
    }
 */
}
