package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PtCarPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PtCarVariables;
import org.eqasim.core.tools.TestCarPtPara;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class PtCarUtilityEstimator implements UtilityEstimator {
    private final ModeParameters parameters;
    private final PtCarPredictor ptCarPredictor;
    private final PersonPredictor personPredictor;

    private final double car_pt_constant = TestCarPtPara.getPara();
    @Inject
    public PtCarUtilityEstimator(ModeParameters parameters,
                                 PtCarPredictor ptCarPredictor, PersonPredictor personPredictor) {
        this.parameters = parameters;
        this.ptCarPredictor = ptCarPredictor;
        this.personPredictor = personPredictor;
    }

    protected double estimateConstantUtility() {
        //return parameters.car.alpha_u + parameters.pt.alpha_u;
        //return 1.25;
        return car_pt_constant;

    }

    protected double estimateTravelTimeUtility(PtCarVariables variables) {
        return parameters.car.betaTravelTime_u_min * variables.travelTime_min;
    }

    protected double estimateAccessEgressTimeUtility(PtCarVariables variables) {
        double thetaWalkThreshold = 20.0;
        double penaltyWalk = 0.0;
        penaltyWalk = Math.exp(Math.log(101) * variables.accessEgressTime_min_car/thetaWalkThreshold) - 1;
        return parameters.walk.betaAccessEgressTravelTime_u_min * variables.accessEgressTime_min_car - penaltyWalk
                + parameters.pt.betaAccessEgressTime_u_min * variables.accessEgressTime_min_pt;

        /*return parameters.walk.betaTravelTime_u_min * variables.accessEgressTime_min_car
                + parameters.pt.betaAccessEgressTime_u_min * variables.accessEgressTime_min_pt;*/
    }

    protected double estimateInVehicleTimeUtility(PtCarVariables variables) {
        return parameters.pt.betaInVehicleTime_u_min * variables.inVehicleTime_min;
    }

    protected double estimateWaitingTimeUtility(PtCarVariables variables) {
        return parameters.pt.betaWaitingTime_u_min * variables.waitingTime_min;
    }

    protected double estimateLineSwitchUtility(PtCarVariables variables) {
        return parameters.pt.betaLineSwitch_u * variables.numberOfLineSwitches;
    }

    protected double estimateMonetaryCostUtility(PtCarVariables variables) {
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
        PtCarVariables variables = ptCarPredictor.predictVariables(person, trip, elements);
        PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);

        double utility = 0.0;
        double coefficient_time_income = Math.exp(parameters.lambda_time * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);
        double coefficient_cost_income = Math.exp(parameters.lambda_cost * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);

        utility += estimateConstantUtility();
        utility += estimateTravelTimeUtility(variables) * coefficient_time_income;
        utility += estimateAccessEgressTimeUtility(variables) * coefficient_time_income;

        utility += estimateInVehicleTimeUtility(variables)* coefficient_time_income;
        utility += estimateWaitingTimeUtility(variables)* coefficient_time_income;
        utility += estimateLineSwitchUtility(variables);
        utility += estimateMonetaryCostUtility(variables)*coefficient_cost_income;


        return utility;
    }
}
