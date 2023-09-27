package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import java.util.List;

import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import com.google.inject.Inject;

public class CarUtilityEstimator implements UtilityEstimator {
	private final ModeParameters parameters;
	private final CarPredictor predictor;
	private final PersonPredictor personPredictor;

	@Inject
	public CarUtilityEstimator(ModeParameters parameters, CarPredictor predictor, PersonPredictor personPredictor) {
		this.parameters = parameters;
		this.predictor = predictor;
		this.personPredictor = personPredictor;
	}

	protected double estimateConstantUtility() {
		return parameters.car.alpha_u;
	}

	protected double estimateTravelTimeUtility(CarVariables variables) {
		return parameters.car.betaTravelTime_u_min * variables.travelTime_min;
	}

	protected double estimateAccessEgressTimeUtility(CarVariables variables) {
		double thetaWalkThreshold = 20.0;
		double penaltyWalk = 0.0;
		penaltyWalk = Math.exp(Math.log(101) * variables.accessEgressTime_min/thetaWalkThreshold) - 1; // BYIN
		//return parameters.walk.betaTravelTime_u_min * variables.accessEgressTime_min; // reference_0
		return parameters.walk.betaAccessEgressTravelTime_u_min * variables.accessEgressTime_min - penaltyWalk;
	}

	protected double estimateMonetaryCostUtility(CarVariables variables) {
		return parameters.betaCost_u_MU * EstimatorUtils.interaction(variables.euclideanDistance_km,
				parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		CarVariables variables = predictor.predictVariables(person, trip, elements);
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);

		double utility = 0.0;
		double coefficient_time_income = Math.exp(parameters.lambda_time * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);
		double coefficient_cost_income = Math.exp(parameters.lambda_cost * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);


		utility += estimateConstantUtility();
		utility += estimateTravelTimeUtility(variables) * coefficient_time_income;
		utility += estimateAccessEgressTimeUtility(variables) * coefficient_time_income;
		utility += estimateMonetaryCostUtility(variables) * coefficient_cost_income;

		return utility;
	}
}
