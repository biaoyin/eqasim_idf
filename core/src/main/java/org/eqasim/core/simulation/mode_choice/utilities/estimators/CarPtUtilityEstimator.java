package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPtPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarPtVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.eqasim.core.tools.TestCarPtPara;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class CarPtUtilityEstimator implements UtilityEstimator{
    private final ModeParameters parameters;
    private final CarPtPredictor carPtPredictor;
    private final PersonPredictor personPredictor;

    //private final double car_pt_constant = TestCarPtPara.getPara();

    @Inject
    public CarPtUtilityEstimator(ModeParameters parameters,
                                 CarPtPredictor carPtPredictor,  PersonPredictor personPredictor) {
        this.parameters = parameters;
        this.carPtPredictor = carPtPredictor;
        this.personPredictor = personPredictor;
    }

    protected double estimateConstantUtility(CarPtVariables variables) {
        return variables.trip_commuting * parameters.car_pt.alpha_u_commuting +
                variables.trip_others * parameters.car_pt.alpha_u_others;
    }

    // BYIN 2025-01: add accessEgressTime to in_vehicle_time, instead of usinge stimateAccessEgressTimeUtility.
    protected double estimateTravelTimeUtility(CarPtVariables variables) {
        return variables.trip_commuting * parameters.car.betaTravelTime_u_min_commuting * (variables.travelTime_min + variables.accessEgressTime_min_car) +
                variables.trip_others * parameters.car.betaTravelTime_u_min_others * (variables.travelTime_min + variables.accessEgressTime_min_car);
    }

//    protected double estimateAccessEgressTimeUtility(CarPtVariables variables) {
//        double thetaWalkThreshold = 20.0;
//        double penaltyWalk = 0.0;
//        penaltyWalk = Math.exp(Math.log(101) * variables.accessEgressTime_min_car/thetaWalkThreshold) - 1;
//        return variables.trip_commuting * (parameters.walk.betaAccessEgressTravelTime_u_min * variables.accessEgressTime_min_car - penaltyWalk + parameters.pt.betaAccessEgressTime_u_min_commuting * variables.accessEgressTime_min_pt) +
//                variables.trip_others * (parameters.walk.betaAccessEgressTravelTime_u_min * variables.accessEgressTime_min_car - penaltyWalk + parameters.pt.betaAccessEgressTime_u_min_others * variables.accessEgressTime_min_pt);
//
//       /* return parameters.walk.betaTravelTime_u_min * variables.accessEgressTime_min_car
//                + parameters.pt.betaAccessEgressTime_u_min * variables.accessEgressTime_min_pt;*/
//    }

    //BYIN 2025-01: in new dmc, use betaInVehicleTime_u_min for all pt time consumed
    protected double estimateInVehicleTimeUtility(CarPtVariables variables) {
        return variables.trip_commuting * (parameters.pt.betaInVehicleTime_u_min_commuting * (variables.inVehicleTime_min + variables.accessEgressTime_min_pt + variables.waitingTime_min)) +
                variables.trip_others * (parameters.pt.betaInVehicleTime_u_min_others * (variables.inVehicleTime_min + variables.accessEgressTime_min_pt + variables.waitingTime_min));
    }
//    protected double estimateWaitingTimeUtility(CarPtVariables variables) {
//        return variables.trip_commuting * parameters.pt.betaWaitingTime_u_min_commuting * variables.waitingTime_min +
//                variables.trip_others * parameters.pt.betaWaitingTime_u_min_others * variables.waitingTime_min;
//    }

    protected double estimateLineSwitchUtility(CarPtVariables variables) {
        return variables.trip_commuting * parameters.pt.betaLineSwitch_u_commuting * variables.numberOfLineSwitches +
                variables.trip_others * parameters.pt.betaLineSwitch_u_others * variables.numberOfLineSwitches;
    }

    protected double estimateMonetaryCostUtility(CarPtVariables variables) {
        return variables.trip_commuting * parameters.betaCost_u_MU_commuting * EstimatorUtils.interaction(variables.euclideanDistance_km_car,
                parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU_car +
                variables.trip_others * parameters.betaCost_u_MU_others * EstimatorUtils.interaction(variables.euclideanDistance_km_car,
                        parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU_car +
                variables.trip_commuting * parameters.betaCost_u_MU_commuting * variables.cost_MU_pt +
                        variables.trip_others * parameters.betaCost_u_MU_others * variables.cost_MU_pt;
    }

    protected  double estimateParkingPressureUtility(CarPtVariables variables) {
        return variables.trip_commuting * parameters.car.betaParkingPressure_commuting * parameters.car.constantParkingPressure +
                variables.trip_others * parameters.car.betaParkingPressure_others * parameters.car.constantParkingPressure;
    }

    //BYIN 2025-01: car_pt or pt_car utility excludes variables of gender and income quartiles.
    @Override
    public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
        CarPtVariables variables = carPtPredictor.predictVariables(person, trip, elements);
        PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);
        double coefficient_income = Math.pow(personVariables.income/parameters.mean_equ_income, parameters.income_power);

        double utility = 0.0;

        utility += estimateConstantUtility(variables);
        utility += estimateTravelTimeUtility(variables);
        utility += estimateInVehicleTimeUtility(variables);
        utility += estimateLineSwitchUtility(variables);
        utility += estimateMonetaryCostUtility(variables) * coefficient_income;
        utility += estimateParkingPressureUtility(variables);

        return utility;
    }
}
