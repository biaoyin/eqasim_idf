package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.BikePtPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.BikePtVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class BikePtUtilityEstimator implements UtilityEstimator{
    private final ModeParameters parameters;
    private final BikePtPredictor bikePtPredictor;
    private final PersonPredictor personPredictor;

    @Inject
    public BikePtUtilityEstimator(ModeParameters parameters, PersonPredictor personPredictor,
                                  BikePtPredictor bikePtPredictor) {
        this.parameters = parameters;
        this.bikePtPredictor = bikePtPredictor;
        this.personPredictor = personPredictor;
    }

    protected double estimateConstantUtility() {
        return parameters.bike.alpha_u + parameters.pt.alpha_u;
//        return 1.25;
    }

    protected double estimateTravelTimeUtility(BikePtVariables variables) {
        return parameters.bike.betaTravelTime_u_min * variables.travelTime_min;
    }

    protected double estimateAgeOver18Utility(PersonVariables variables) {
        return parameters.bike.betaAgeOver18_u_a * Math.max(0.0, variables.age_a - 18);
    }

    // pt utility
    protected double estimateAccessEgressTimeUtility(BikePtVariables variables) {
//        return parameters.walk.betaTravelTime_u_min * variables.accessEgressTime_min_car
//                + parameters.pt.betaAccessEgressTime_u_min * variables.accessEgressTime_min_pt;
        return  parameters.pt.betaAccessEgressTime_u_min * variables.accessEgressTime_min_pt;
    }

    protected double estimateInVehicleTimeUtility(BikePtVariables variables) {
        return parameters.pt.betaInVehicleTime_u_min * variables.inVehicleTime_min;
    }

    protected double estimateWaitingTimeUtility(BikePtVariables variables) {
        return parameters.pt.betaWaitingTime_u_min * variables.waitingTime_min;
    }

    protected double estimateLineSwitchUtility(BikePtVariables variables) {
        return parameters.pt.betaLineSwitch_u * variables.numberOfLineSwitches;
    }

    protected double estimateMonetaryCostUtility(BikePtVariables variables) {
        /*return parameters.betaCost_u_MU
                * EstimatorUtils.interaction(variables.euclideanDistance_km_car,
                parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance)
                * variables.cost_MU_car
                + parameters.betaCost_u_MU
                * EstimatorUtils.interaction(variables.euclideanDistance_km_pt,
                parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance)
                * variables.cost_MU_pt;*/
        return  parameters.betaCost_u_MU * EstimatorUtils.interaction(variables.euclideanDistance_km_pt,
                parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU_pt;
    }

    @Override
    public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
        PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);
        BikePtVariables variables = bikePtPredictor.predictVariables(person, trip, elements);

        //double utility = 1000000.0;

        double utility = 0.0;

        utility += estimateConstantUtility();
        utility += estimateTravelTimeUtility(variables);
        utility += estimateAgeOver18Utility(personVariables);

        utility += estimateAccessEgressTimeUtility(variables);
        utility += estimateInVehicleTimeUtility(variables);
        utility += estimateWaitingTimeUtility(variables);
        utility += estimateLineSwitchUtility(variables);
        utility += estimateMonetaryCostUtility(variables);

        return utility;
    }
}
