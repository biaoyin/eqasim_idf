package org.eqasim.core.simulation.mode_choice.utilities.predictors;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.components.ParkingFinder;
import org.eqasim.core.simulation.mode_choice.cost.CostModel;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.variables.BikePtVariables;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.RoutingModule;
import org.matsim.facilities.Facility;
import org.matsim.pt.routes.TransitPassengerRoute;

import java.util.List;

public class BikePtPredictor extends CachedVariablePredictor<BikePtVariables>{
    private final RoutingModule bikeRoutingModule;
    private final RoutingModule ptRoutingModule;
//    private final CostModel bikeCostModel;
    private final CostModel ptCostModel;
    private final ModeParameters parameters;
    // private final List<Coord> parkRideCoords;
    private final Network network;
    private final PopulationFactory populationFactory;
    private final ParkRideManager parkRideMana;

    @Inject
    public BikePtPredictor(ModeParameters parameters, Network network, @Named("bike") RoutingModule bikeRoutingModule,
                           @Named("pt") RoutingModule ptRoutingModule, PopulationFactory populationFactory,
                           @Named("pt") CostModel ptCostModel, ParkRideManager parkRideMana) {
//        this.bikeCostModel = bikeCostModel;
        this.ptCostModel = ptCostModel;
        this.parameters = parameters;
        // this.parkRideCoords = parkRideCoords;
        this.network = network;
        this.bikeRoutingModule = bikeRoutingModule;
        this.ptRoutingModule = ptRoutingModule;
        this.populationFactory = populationFactory;
        this.parkRideMana = parkRideMana;
    }

    @Override
    public BikePtVariables predict(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
        // double travelTime_min = ((Leg) elements.get(0)).getTravelTime() / 60.0;

        ParkingFinder prFinder = new ParkingFinder(parkRideMana.getCoordinates());
        //ParkingFinder prFinder = new ParkingFinder(parkRideCoords);
        Facility prkFacility = prFinder.getParking2(person, trip.getOriginActivity(), trip.getDestinationActivity(),
                network);

        // Creation of a bike leg from Origin to the PR facility
        Link fromLink = NetworkUtils.getNearestLink(network, trip.getOriginActivity().getCoord());
        Facility fromFacility = new LinkWrapperFacility(fromLink);
        List<? extends PlanElement> bikeElements = bikeRoutingModule.calcRoute(fromFacility, prkFacility,
                trip.getDepartureTime(), null);
        if (bikeElements.size() > 1) {
            throw new IllegalStateException("We do not support multi-stage bike trips yet.");
        }

        double bikeTravelTime = Double.NaN;
        Leg leg_bike = (Leg) bikeElements.get(0);
//        bikeTravelTime = leg_bike.getRoute().getTravelTime().seconds() / 60.0 + parameters.car.constantParkingSearchPenalty_min;
        bikeTravelTime = leg_bike.getRoute().getTravelTime().seconds() / 60.0;
        // We take 5 min to park the car and access to PT (transfer time)
        double timeToAccessPt = 5;
        bikeTravelTime += timeToAccessPt;

        // "car_pt interaction" definition
        // Other way to define Activity
        // Activity interactionActivtyCarPt =
        // PopulationUtils.createActivityFromCoordAndLinkId("carPt interaction",
        // prkFacility.getCoord(), prkFacility.getLinkId());

        Activity bike_pt = (Activity) populationFactory.createActivityFromCoord("bike_pt interaction",
                prkFacility.getCoord());
        bike_pt.setMaximumDuration(600);// 10 min
        bike_pt.setLinkId(prkFacility.getLinkId());

        DiscreteModeChoiceTrip trip_bike = new DiscreteModeChoiceTrip(trip.getOriginActivity(), bike_pt, "bike",
                bikeElements, person.hashCode(), leg_bike.hashCode(),1000);

//        double cost_MU_car = carCostModel.calculateCost_MU(person, trip_car, bikeElements);
//        double euclideanDistance_km_car = PredictorUtils.calculateEuclideanDistance_km(trip_car);
//        double accessEgressTime_min_car = parameters.car.constantAccessEgressWalkTime_min;

        // Creation of a pt leg from the PR facility to Destination
        double ptDepartureTime = trip.getDepartureTime() + bikeTravelTime * 60;
        Link toLink = NetworkUtils.getNearestLink(network, trip.getDestinationActivity().getCoord());
        Facility toFacility = new LinkWrapperFacility(toLink);

        List<? extends PlanElement> ptElements = ptRoutingModule.calcRoute(prkFacility, toFacility, ptDepartureTime,
                person);

        DiscreteModeChoiceTrip trip_pt = new DiscreteModeChoiceTrip(bike_pt, trip.getDestinationActivity(), "pt",
                ptElements, person.hashCode(), ptElements.get(0).hashCode(), 1000);

        //DiscreteModeChoiceTrip(Activity originActivity, Activity destinationActivity, String initialMode,
        //        List<? extends PlanElement> initialElements, int personHash, int tripHash, int index)

        int numberOfVehicularTrips = 0;
        boolean isFirstWaitingTime = true;

        // Track relevant variables
        double inVehicleTime_min = 0.0;
        double waitingTime_min = 0.0;
        double accessEgressTime_min_pt = 0.0;

        for (PlanElement element : ptElements) {
            if (element instanceof Leg) {
                Leg leg = (Leg) element;

                switch (leg.getMode()) {
                    case TransportMode.walk:
                    case TransportMode.non_network_walk:
                        accessEgressTime_min_pt += leg.getTravelTime().seconds() / 60.0;
                        break;
                    case TransportMode.transit_walk:
                        waitingTime_min += leg.getTravelTime().seconds() / 60.0;
                        break;
                    case TransportMode.pt:
                        TransitPassengerRoute route = (TransitPassengerRoute) leg.getRoute();

                        double departureTime = leg.getDepartureTime().seconds();
                        double waitingTime = route.getBoardingTime().seconds() - departureTime;
                        double inVehicleTime = leg.getTravelTime().seconds() - waitingTime;

                        inVehicleTime_min += inVehicleTime / 60.0;
                        /*
                        if (!isFirstWaitingTime) {
                            waitingTime_min += route.getWaitingTime() / 60.0;
                        } else {
                            isFirstWaitingTime = false;
                        }
                        */
                        if (!isFirstWaitingTime) {
                            waitingTime_min += waitingTime / 60.0;
                        } else {
                            isFirstWaitingTime = false;
                        }

                        numberOfVehicularTrips++;
                        break;
                    default:
                        throw new IllegalStateException("Unknown mode in PT trip: " + leg.getMode());
                }
            }
        }

        int numberOfLineSwitches = Math.max(0, numberOfVehicularTrips - 1);

        // Calculate cost
        double cost_MU_pt = ptCostModel.calculateCost_MU(person, trip_pt, ptElements);

        double euclideanDistance_km_pt = PredictorUtils.calculateEuclideanDistance_km(trip_pt);

        return new BikePtVariables(bikeTravelTime,
                inVehicleTime_min, waitingTime_min, numberOfLineSwitches, euclideanDistance_km_pt,
                accessEgressTime_min_pt, cost_MU_pt);
    }
}
