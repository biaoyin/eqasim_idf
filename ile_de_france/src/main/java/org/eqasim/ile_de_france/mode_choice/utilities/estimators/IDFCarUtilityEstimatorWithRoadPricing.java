package org.eqasim.ile_de_france.mode_choice.utilities.estimators;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.CarUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CarPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFModeParameters;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFCarRoadPricingPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFSpatialPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.variables.IDFCarRoadPricingVariables;
import org.eqasim.ile_de_france.mode_choice.utilities.variables.IDFSpatialVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class IDFCarUtilityEstimatorWithRoadPricing extends CarUtilityEstimator {
	private final IDFModeParameters parameters;
	private final IDFSpatialPredictor spatialPredictor;
	private final PersonPredictor personPredictor;
	private final IDFCarRoadPricingPredictor roadPricingPredictor;

	@Inject
	public IDFCarUtilityEstimatorWithRoadPricing(IDFModeParameters parameters, IDFSpatialPredictor spatialPredictor,
												 CarPredictor carPredictor, PersonPredictor personPredictor, IDFCarRoadPricingPredictor roadPricingPredictor) {
		super(parameters, carPredictor, personPredictor);

		this.parameters = parameters;
		this.spatialPredictor = spatialPredictor;
		this.personPredictor = personPredictor;
		this.roadPricingPredictor = roadPricingPredictor;
	}

	protected double estimateUrbanUtility(IDFSpatialVariables variables) {
		double utility = 0.0;

		if (variables.hasUrbanOrigin && variables.hasUrbanDestination) {
			utility += parameters.idfCar.betaInsideUrbanArea;
		}

		if (variables.hasUrbanOrigin || variables.hasUrbanDestination) {
			utility += parameters.idfCar.betaCrossingUrbanArea;
		}

		return utility;
	}

	protected double estimateRoadPricing(IDFCarRoadPricingVariables variables) {
		double utility = 0.0;

		utility += parameters.betaCost_u_MU * variables.road_pricing_fee;  //BYIN:  betaCost_u_MU: what is the physical meaning of this parameter ?? 2023-04

		return utility;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		IDFSpatialVariables variables = spatialPredictor.predictVariables(person, trip, elements);
		IDFCarRoadPricingVariables carRoadPricingVariables = roadPricingPredictor.predictVariables(person, trip, elements);
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);

		double utility = 0.0;
		double coefficient_cost_income = Math.exp(parameters.lambda_cost * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);

		utility += super.estimateUtility(person, trip, elements);
		utility += estimateUrbanUtility(variables);
		utility += estimateRoadPricing(carRoadPricingVariables) * coefficient_cost_income;

		return utility;
	}
}
