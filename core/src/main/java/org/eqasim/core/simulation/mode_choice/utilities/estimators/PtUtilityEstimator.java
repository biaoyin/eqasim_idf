package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.eqasim.core.analysis.IntermediateDMCWriter;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PtPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarVariables;
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

	protected double estimateConstantUtility(PtVariables variables) {
		return variables.trip_commuting * parameters.pt.alpha_u_commuting +
				variables.trip_others * parameters.pt.alpha_u_others;
	}

	protected double estimateAffordabilityUtility(PersonVariables pv, PtVariables variables) {
		int quartile_1 = 0;
		int quartile_2 = 0;
		int quartile_3 = 0;
		int quartile_4 = 0;
		double equ_income = pv.income;
		if (equ_income <= 1222) {
			quartile_1 = 1;
		}
		if (equ_income > 1222 && equ_income <= 1800) {
			quartile_2 = 1;
		}
		if (equ_income > 1800 && equ_income <= 2667) {
			quartile_3 = 1;
		}
		if (equ_income > 2667) {
			quartile_4 = 1;
		}
		return variables.trip_commuting * (parameters.pt.betaQ2_commuting*quartile_2 +
				parameters.pt.betaQ3_commuting*quartile_3 + parameters.pt.betaQ4_commuting*quartile_4) +
				variables.trip_others * (parameters.pt.betaQ2_others*quartile_2 +
						parameters.pt.betaQ3_others*quartile_3 + parameters.pt.betaQ4_others*quartile_4);
	}
	//BYIN 2025-01: in new dmc, use betaInVehicleTime_u_min for all pt time consumed
	protected double estimateAccessEgressTimeUtility(PtVariables variables) {
		return variables.trip_commuting * (parameters.pt.betaInVehicleTime_u_min_commuting * variables.accessEgressTime_min) +
				variables.trip_others * (parameters.pt.betaInVehicleTime_u_min_others * variables.accessEgressTime_min);
	}

	protected double estimateInVehicleTimeUtility(PtVariables variables) {
		return variables.trip_commuting * (parameters.pt.betaInVehicleTime_u_min_commuting * variables.inVehicleTime_min) +
				variables.trip_others * (parameters.pt.betaInVehicleTime_u_min_others * variables.inVehicleTime_min);
	}
	//BYIN 2025-01: in new dmc, use betaInVehicleTime_u_min for all pt time consumed
	protected double estimateWaitingTimeUtility(PtVariables variables) {
		return variables.trip_commuting * parameters.pt.betaInVehicleTime_u_min_commuting * variables.waitingTime_min +
				variables.trip_others * parameters.pt.betaInVehicleTime_u_min_others * variables.waitingTime_min;
	}

	protected double estimateLineSwitchUtility(PtVariables variables) {
		return variables.trip_commuting * parameters.pt.betaLineSwitch_u_commuting * variables.numberOfLineSwitches +
				variables.trip_others * parameters.pt.betaLineSwitch_u_others * variables.numberOfLineSwitches;
	}

/*	protected double estimateMonetaryCostUtility(PtVariables variables) {
		return parameters.betaCost_u_MU * EstimatorUtils.interaction(variables.euclideanDistance_km,
				parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU;
	}*/
	//BYIN 2025-01: pt cost is constant of 0.8 euros
	protected double estimateMonetaryCostUtility(PtVariables variables) {
		return variables.trip_commuting * parameters.betaCost_u_MU_commuting * variables.cost_MU +
				variables.trip_others * parameters.betaCost_u_MU_others * variables.cost_MU;
	}


	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		PtVariables variables = predictor.predictVariables(person, trip, elements);
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);

		double coefficient_income = Math.pow(personVariables.income/parameters.mean_equ_income, parameters.income_power);
		double utility = 0.0;
		utility += estimateConstantUtility(variables);
		utility += estimateAffordabilityUtility(personVariables, variables);
		utility += estimateAccessEgressTimeUtility(variables);
		utility += estimateInVehicleTimeUtility(variables);
		utility += estimateWaitingTimeUtility(variables);
		utility += estimateLineSwitchUtility(variables);
		utility += estimateMonetaryCostUtility(variables) * coefficient_income;

		return utility;
	}
}
