package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.apache.commons.math3.util.Precision;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPassengerPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarPassengerVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.core.population.PersonUtils;

import java.util.ArrayList;
import java.util.List;

public class CarPassengerUtilityEstimator implements UtilityEstimator {
	private final ModeParameters parameters;
	private final CarPassengerPredictor predictor;

	@Inject
	public CarPassengerUtilityEstimator(ModeParameters parameters, CarPassengerPredictor predictor) {
		this.parameters = parameters;
		this.predictor = predictor;
	}

	protected double estimateConstantUtility(CarPassengerVariables variables) {
		return variables.trip_commuting * parameters.car_passenger.alpha_u_commuting +
				variables.trip_others * parameters.car_passenger.alpha_u_others;
	}

	protected double estimateTravelTimeUtility(CarPassengerVariables variables) {
		return variables.trip_commuting * parameters.car_passenger.betaTravelTime_u_min_commuting * variables.travelTime_min +
				variables.trip_others * parameters.car_passenger.betaTravelTime_u_min_others * variables.travelTime_min;
	}

	protected double estimateAvailabilityUtility(Person person, CarPassengerVariables variables) {
		Integer carPassengerAvailability = 1;
		if ("no".equals(PersonUtils.getLicense(person))) {
			carPassengerAvailability = 0;
		}
		if ("none".equals((String) person.getAttributes().getAttribute("carAvailability"))) {
			carPassengerAvailability = 0;
		}
		return variables.trip_commuting * parameters.car_passenger.betaAvailability_commuting * carPassengerAvailability +
				variables.trip_others * parameters.car_passenger.betaAvailability_others * carPassengerAvailability;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		CarPassengerVariables variables = predictor.predictVariables(person, trip, elements);
		double utility = 0.0;
		utility += estimateConstantUtility(variables);
		utility += estimateTravelTimeUtility(variables);
		utility += estimateAvailabilityUtility(person, variables);
		return utility;
	}
}
