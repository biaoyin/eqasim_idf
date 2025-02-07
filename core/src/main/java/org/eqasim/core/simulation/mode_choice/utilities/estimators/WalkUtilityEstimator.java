package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import java.util.List;

import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.WalkPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.WalkVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import com.google.inject.Inject;

public class WalkUtilityEstimator implements UtilityEstimator {
	private final ModeParameters parameters;
	private final WalkPredictor predictor;
	private final PersonPredictor personPredictor;

	@Inject
	public WalkUtilityEstimator(ModeParameters parameters, WalkPredictor predictor, PersonPredictor personPredictor) {
		this.parameters = parameters;
		this.predictor = predictor;
		this.personPredictor = personPredictor;
	}

//	protected double estimateConstantUtility() {
//		return parameters.walk.alpha_u;
//	}

	protected double estimateTravelTimeUtility(WalkVariables variables) {
		return variables.trip_commuting * parameters.walk.betaTravelTime_u_min_commuting * variables.travelTime_min +
				variables.trip_others * parameters.walk.betaTravelTime_u_min_others * variables.travelTime_min;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		WalkVariables variables = predictor.predictVariables(person, trip, elements);
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);
		double utility = 0.0;
		utility += estimateTravelTimeUtility(variables);
		utility += (variables.trip_commuting * parameters.betaWalk_gender_commuting * personVariables.gender +
				variables.trip_others * parameters.betaWalk_gender_others * personVariables.gender);
		return utility;
	}
}
