package org.eqasim.core.simulation.mode_choice.utilities.predictors;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.components.ParkingFinder;
import org.eqasim.core.simulation.mode_choice.cost.CostModel;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PtCarVariables;
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

import java.util.LinkedList;
import java.util.List;

public class PtCarPredictor extends CachedVariablePredictor<PtCarVariables>{
    private final RoutingModule carRoutingModule;
    private final RoutingModule ptRoutingModule;
    private final CostModel carCostModel;
    private final CostModel ptCostModel;
    private final ModeParameters parameters;
    //private final List<Coord> parkRideCoords;
    private final Network network;
    private final PopulationFactory populationFactory;
    private final ParkRideManager parkRideMana;
    private boolean flag_multi_car_stage = true; //use multi_stage car trips or not
    private boolean flag_walk_time_const = false; //walk leg time is constant or not

    @Inject
    public PtCarPredictor(ModeParameters parameters, Network network, @Named("car") RoutingModule carRoutingModule,
                          @Named("pt") RoutingModule ptRoutingModule, PopulationFactory populationFactory, @Named("car") CostModel carCostModel,
                          @Named("pt") CostModel ptCostModel, ParkRideManager parkRideMana) {
        this.carCostModel = carCostModel;
        this.ptCostModel = ptCostModel;
        this.parameters = parameters;
        //this.parkRideCoords = parkRideCoords;
        this.network = network;
        this.carRoutingModule = carRoutingModule;
        this.ptRoutingModule = ptRoutingModule;
        this.populationFactory = populationFactory;
        this.parkRideMana = parkRideMana;
    }

    @Override
    public PtCarVariables predict(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {

        List<PlanElement> carElements = new LinkedList<>();
        List<PlanElement> ptElements = new LinkedList<>();
        boolean flag = true;
        for (PlanElement element : elements) {
            if (flag == true) {
                ptElements.add(element);
            } else {
                carElements.add(element);
            }
            if(element instanceof Activity) {
                Activity act = (Activity) element;
                if (act.getType().equals("ptCar interaction")) {
                    flag = false;
                }
            }

        }

        PlanElement last_element = ptElements.get(ptElements.size()-1);
        Activity car_pt = (Activity) last_element;
        DiscreteModeChoiceTrip trip_pt = new DiscreteModeChoiceTrip(trip.getOriginActivity(), car_pt, "pt",
                ptElements, person.hashCode(),ptElements.get(0).hashCode(), 1000);

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
        double cost_MU_pt = carCostModel.calculateCost_MU(person, trip_pt, ptElements);
        double euclideanDistance_km_pt = PredictorUtils.calculateEuclideanDistance_km(trip_pt);

        double timeToAccessCar = 5;
        double euclideanDistance_km_car = 0.0;
        double cost_MU_car= 0.0;
        double vehicleTravelTime = 0.0;
        double accessEgressTime_min_car = 0.0;
        vehicleTravelTime += timeToAccessCar;
        DiscreteModeChoiceTrip trip_car = new DiscreteModeChoiceTrip(car_pt, trip.getDestinationActivity(), "car",
                carElements, person.hashCode(), carElements.get(0).hashCode(), 1000);

        for (PlanElement element : carElements) {
            if (element instanceof Leg) {
                Leg leg = (Leg) element;
                switch (leg.getMode()) {
                    case TransportMode.walk:
                        accessEgressTime_min_car += leg.getTravelTime().seconds() / 60.0;
                        break;
                    case "carInternal":
                    case TransportMode.car:
                        vehicleTravelTime += leg.getTravelTime().seconds() / 60.0 + parameters.car.constantParkingSearchPenalty_min;
                        cost_MU_car += ptCostModel.calculateCost_MU(person, trip_car, carElements);
                        euclideanDistance_km_car = PredictorUtils.calculateEuclideanDistance_km(trip_car);
                        break;
                    default:
                        throw new IllegalStateException("Unknown mode in car trip: " + leg.getMode());
                }
            }
        }

        return new PtCarVariables(vehicleTravelTime, euclideanDistance_km_car, accessEgressTime_min_car, cost_MU_car,
                inVehicleTime_min, waitingTime_min, numberOfLineSwitches, euclideanDistance_km_pt,
                accessEgressTime_min_pt, cost_MU_pt);
    }
}