package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import java.util.List;

import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PtPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PtVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import com.google.inject.Inject;

public class PtUtilityEstimator implements UtilityEstimator {
	private final ModeParameters parameters;
	private final PtPredictor predictor;
	private final PersonPredictor personPredictor;

	@Inject
	public PtUtilityEstimator(ModeParameters parameters, PtPredictor predictor, PersonPredictor personPredictor) {
		this.parameters = parameters;
		this.predictor = predictor;
		this.personPredictor = personPredictor;
	}

	protected double estimateConstantUtility() {
		return parameters.pt.alpha_u;
	}

	protected double estimateAccessEgressTimeUtility(PtVariables variables) {
		return parameters.pt.betaAccessEgressTime_u_min * variables.accessEgressTime_min;
	}

	protected double estimateInVehicleTimeUtility(PtVariables variables) {
		return parameters.pt.betaInVehicleTime_u_min * variables.inVehicleTime_min;
	}

	protected double estimateWaitingTimeUtility(PtVariables variables) {
		return parameters.pt.betaWaitingTime_u_min * variables.waitingTime_min;
	}

	protected double estimateLineSwitchUtility(PtVariables variables) {
		return parameters.pt.betaLineSwitch_u * variables.numberOfLineSwitches;
	}

	protected double estimateMonetaryCostUtility(PtVariables variables) {
		return parameters.betaCost_u_MU * EstimatorUtils.interaction(variables.euclideanDistance_km,
				parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		PtVariables variables = predictor.predictVariables(person, trip, elements);
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);

		double utility = 0.0;
		double coefficient_time_income = Math.exp(parameters.lambda_time * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);
		double coefficient_cost_income = Math.exp(parameters.lambda_cost * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);

		utility += estimateConstantUtility();
		utility += estimateAccessEgressTimeUtility(variables) * coefficient_time_income;
		utility += estimateInVehicleTimeUtility(variables) * coefficient_time_income;
		utility += estimateWaitingTimeUtility(variables) * coefficient_time_income;
		utility += estimateLineSwitchUtility(variables);
		utility += estimateMonetaryCostUtility(variables) * coefficient_cost_income;

		return utility;
	}
}
