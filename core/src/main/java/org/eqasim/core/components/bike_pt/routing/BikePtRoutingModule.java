package org.eqasim.core.components.bike_pt.routing;

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

public class BikePtRoutingModule implements RoutingModule{
    private final RoutingModule bikeRoutingModule; // we may not use bikeRoutingModule, but we first consider this.
    private final Network network;

    // Create an object of a ptRoutingModule
    private final RoutingModule ptRoutingModule;

    private final List<Coord> parkRideCoords;

//    @Inject
    public BikePtRoutingModule(RoutingModule bikeRoutingModule, RoutingModule ptRoutingModule, Network network, List<Coord> parkRideCoords) {
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

        ParkingFinder prFinder = new ParkingFinder(parkRideCoords);

        Facility prkFacility = prFinder.getParking(person, fromFacility, toFacility, network);

        // Creation of a bike trip to the PR facility
        List<? extends PlanElement> bikeElements = bikeRoutingModule.calcRoute(fromFacility, prkFacility, departureTime,
                null);

        // double vehicleDistance = Double.NaN;
        double bikeTravelTime = Double.NaN;
        // double price = Double.NaN;

        Leg leg = (Leg) bikeElements.get(0);
        // vehicleDistance = leg.getRoute().getDistance();
        bikeTravelTime = leg.getRoute().getTravelTime().seconds(); // can not invoke seconds() in this context

        // Given the request time, we can calculate the waiting time
        double timeToAccessPt = 300; // We take 5 min to park the car and access to PT

        double ptDepartureTime = departureTime + bikeTravelTime + timeToAccessPt;

        // Creation of a PT trip from the PR facility to the destination
        List<? extends PlanElement> ptElements = ptRoutingModule.calcRoute(prkFacility, toFacility, ptDepartureTime,
                person);

        // Creation interaction between bike and pt
        Link prLink = NetworkUtils.getNearestLink(network, prkFacility.getCoord());
        Activity interactionActivtyBikePt = PopulationUtils.createActivityFromCoordAndLinkId("bikePt interaction",
                prkFacility.getCoord(), prLink.getId());
        interactionActivtyBikePt.setMaximumDuration(300);// 5 min

        // Creation full trip
        List<PlanElement> allElements = new LinkedList<>();
        allElements.addAll(bikeElements);
        allElements.add(interactionActivtyBikePt);
        allElements.addAll(ptElements);

        return allElements;

    }
/*
    @Override
    public StageActivityTypes getStageActivityTypes() {

        return new StageActivityTypesImpl("car interaction", "carPt interaction", "pt interaction");
    }

    public static Activity createStageActivityFromCoordLinkIdAndModePrefix(final Coord interactionCoord, final Id<Link> interactionLink, String modePrefix ) {
		Activity act = createActivityFromCoordAndLinkId(PlanCalcScoreConfigGroup.createStageActivityType(modePrefix), interactionCoord, interactionLink);
		act.setMaximumDuration(0.0);
		return act;
	}

	 private Activity getStageActivityTypes() {
        Activity activity = PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(prkFacility.getCoord(),
                stopFacility.getLinkId(), mode);
        return activity;
    }

*/

}
