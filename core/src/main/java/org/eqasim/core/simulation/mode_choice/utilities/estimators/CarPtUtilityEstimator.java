package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPtPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarPtVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class CarPtUtilityEstimator implements UtilityEstimator{
    private final ModeParameters parameters;
    private final CarPtPredictor carPtPredictor;
    // private final PersonPredictor personPredictor;

    @Inject
    public CarPtUtilityEstimator(ModeParameters parameters,
                                 CarPtPredictor carPtPredictor) {
        this.parameters = parameters;
        this.carPtPredictor = carPtPredictor;
        // this.personPredictor = personPredictor;
    }

    protected double estimateConstantUtility() {
        //return parameters.car.alpha_u + parameters.pt.alpha_u;
        return 1.25;

    }

    protected double estimateTravelTimeUtility(CarPtVariables variables) {
        return parameters.car.betaTravelTime_u_min * variables.travelTime_min;
    }

    protected double estimateAccessEgressTimeUtility(CarPtVariables variables) {
        return parameters.walk.betaTravelTime_u_min * variables.accessEgressTime_min_car
                + parameters.pt.betaAccessEgressTime_u_min * variables.accessEgressTime_min_pt;
    }

    protected double estimateInVehicleTimeUtility(CarPtVariables variables) {
        return parameters.pt.betaInVehicleTime_u_min * variables.inVehicleTime_min;
    }

    protected double estimateWaitingTimeUtility(CarPtVariables variables) {
        return parameters.pt.betaWaitingTime_u_min * variables.waitingTime_min;
    }

    protected double estimateLineSwitchUtility(CarPtVariables variables) {
        return parameters.pt.betaLineSwitch_u * variables.numberOfLineSwitches;
    }

    protected double estimateMonetaryCostUtility(CarPtVariables variables) {
        return parameters.betaCost_u_MU
                * EstimatorUtils.interaction(variables.euclideanDistance_km_car,
                parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance)
                * variables.cost_MU_car
                + parameters.betaCost_u_MU
                * EstimatorUtils.interaction(variables.euclideanDistance_km_pt,
                parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance)
                * variables.cost_MU_pt;
    }

    @Override
    public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
        CarPtVariables variables = carPtPredictor.predictVariables(person, trip, elements);

        //double utility = 1000000.0;

        double utility = 0.0;

        utility += estimateConstantUtility();
        utility += estimateTravelTimeUtility(variables);
        utility += estimateAccessEgressTimeUtility(variables);
        utility += estimateInVehicleTimeUtility(variables);
        utility += estimateWaitingTimeUtility(variables);
        utility += estimateLineSwitchUtility(variables);
        utility += estimateMonetaryCostUtility(variables);

        return utility;
    }
}
