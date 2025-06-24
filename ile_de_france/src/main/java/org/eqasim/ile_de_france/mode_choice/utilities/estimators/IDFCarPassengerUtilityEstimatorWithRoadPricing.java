package org.eqasim.ile_de_france.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.CarPassengerUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.CarUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPassengerPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFModeParameters;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFCarPassengerRoadPricingPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFCarRoadPricingPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFSpatialPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.variables.IDFCarPassengerRoadPricingVariables;
import org.eqasim.ile_de_france.mode_choice.utilities.variables.IDFCarRoadPricingVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class IDFCarPassengerUtilityEstimatorWithRoadPricing extends CarPassengerUtilityEstimator {
	private final IDFModeParameters parameters;
	private final IDFSpatialPredictor spatialPredictor;
	private final PersonPredictor personPredictor;
	private final IDFCarPassengerRoadPricingPredictor roadPricingPredictor;

	@Inject
	public IDFCarPassengerUtilityEstimatorWithRoadPricing(IDFModeParameters parameters, IDFSpatialPredictor spatialPredictor,
														  CarPassengerPredictor carPassengerPredictor, PersonPredictor personPredictor, IDFCarPassengerRoadPricingPredictor roadPricingPredictor) {
		super(parameters, carPassengerPredictor);

		this.parameters = parameters;
		this.spatialPredictor = spatialPredictor;
		this.personPredictor = personPredictor;
		this.roadPricingPredictor = roadPricingPredictor;
	}

//	protected double estimateUrbanUtility(IDFSpatialVariables variables) {
//		double utility = 0.0;
//
//		if (variables.hasUrbanOrigin && variables.hasUrbanDestination) {
//			utility += parameters.idfCar.betaInsideUrbanArea;
//		}
//
//		if (variables.hasUrbanOrigin || variables.hasUrbanDestination) {
//			utility += parameters.idfCar.betaCrossingUrbanArea;
//		}
//
//		return utility;
//	}

	protected double estimateRoadPricing(IDFCarPassengerRoadPricingVariables variables) {
		double utility = 0.0;

		utility += (variables.trip_commuting * parameters.betaCost_u_MU_commuting * variables.road_pricing_fee +
		variables.trip_others * parameters.betaCost_u_MU_others * variables.road_pricing_fee);  //BYIN 2023-04:  betaCost_u_MU: what is the physical meaning of this parameter ??

		return utility;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		//IDFSpatialVariables variables = spatialPredictor.predictVariables(person, trip, elements);
		IDFCarPassengerRoadPricingVariables carPassengerRoadPricingVariables = roadPricingPredictor.predictVariables(person, trip, elements);
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);
		double coefficient_income = Math.pow(personVariables.income/parameters.mean_equ_income, parameters.income_power);

		double utility = 0.0;
		utility += super.estimateUtility(person, trip, elements);
		//utility += estimateUrbanUtility(variables); // BYIN 2025-01: not considered in the new dmc model
		utility += estimateRoadPricing(carPassengerRoadPricingVariables) * coefficient_income;
		return utility;
	}
}
