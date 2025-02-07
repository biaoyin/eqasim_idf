package org.eqasim.ile_de_france.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.CarPtUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.PtCarUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPtPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PtCarPredictor;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFModeParameters;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFPtPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.variables.IDFPtVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class IDFPtCarUtilityEstimator extends PtCarUtilityEstimator {
	private final IDFModeParameters parameters;
	private final IDFPtPredictor predictor;


	@Inject
	public IDFPtCarUtilityEstimator(IDFModeParameters parameters, PtCarPredictor ptCarPredictor, PersonPredictor personPredictor, IDFPtPredictor predictor) {
		super(parameters, ptCarPredictor, personPredictor);
		this.parameters = parameters;
		this.predictor = predictor;
	}

	//BYIN 2025-01:
	protected double estimateOnlyBusUtility(IDFPtVariables variables) {

		return variables.isOnlyBus ? (variables.trip_commuting * parameters.idfPt.onlyBus_u_commuting +
				variables.trip_others * parameters.idfPt.onlyBus_u_others): 0.0;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		IDFPtVariables variables = predictor.predictVariables(person, trip, elements);

		double utility = 0.0;
		utility += super.estimateUtility(person, trip, elements);
		utility += estimateOnlyBusUtility(variables);

		return utility;
	}
}
