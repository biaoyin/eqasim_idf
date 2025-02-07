package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.eqasim.core.analysis.IntermediateDMCWriter;
import org.eqasim.core.analysis.TripItem;
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

	protected double estimateConstantUtility(CarVariables variables) {
		return variables.trip_commuting * parameters.car.alpha_u_commuting +
				variables.trip_others * parameters.car.alpha_u_others;
	}
	// BYIN 2025-01: add accessEgressTime to in_vehicle_time, instead of usinge stimateAccessEgressTimeUtility.
	protected double estimateTravelTimeUtility(CarVariables variables) {
		return variables.trip_commuting * parameters.car.betaTravelTime_u_min_commuting * (variables.travelTime_min + variables.accessEgressTime_min) +
				variables.trip_others * parameters.car.betaTravelTime_u_min_others * (variables.travelTime_min + variables.accessEgressTime_min);
	}

//	protected double estimateAccessEgressTimeUtility(CarVariables variables) {
//		double thetaWalkThreshold = 20.0;
//		double penaltyWalk = 0.0;
//		penaltyWalk = Math.exp(Math.log(101) * variables.accessEgressTime_min/thetaWalkThreshold) - 1; // BYIN
//		//return parameters.walk.betaTravelTime_u_min * variables.accessEgressTime_min; // reference_0
//		return variables.trip_commuting * (parameters.walk.betaAccessEgressTravelTime_u_min * variables.accessEgressTime_min - penaltyWalk) +
//				variables.trip_others * (parameters.walk.betaAccessEgressTravelTime_u_min * variables.accessEgressTime_min - penaltyWalk);
//	}

	protected double estimateMonetaryCostUtility(CarVariables variables) {
		return variables.trip_commuting * parameters.betaCost_u_MU_commuting * EstimatorUtils.interaction(variables.euclideanDistance_km,
				parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU +
				variables.trip_others * parameters.betaCost_u_MU_others * EstimatorUtils.interaction(variables.euclideanDistance_km,
						parameters.referenceEuclideanDistance_km, parameters.lambdaCostEuclideanDistance) * variables.cost_MU;
	}

	protected  double estimateParkingPressureUtility(CarVariables variables) {
		return variables.trip_commuting * parameters.car.betaParkingPressure_commuting * parameters.car.constantParkingPressure +
				variables.trip_others * parameters.car.betaParkingPressure_others * parameters.car.constantParkingPressure;
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		CarVariables variables = predictor.predictVariables(person, trip, elements);
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);

		double coefficient_income = Math.pow(personVariables.income/parameters.mean_equ_income, parameters.income_power);

		double utility = 0.0;
		utility += estimateConstantUtility(variables);
		utility += estimateTravelTimeUtility(variables);
		utility += estimateMonetaryCostUtility(variables) * coefficient_income;
		utility += estimateParkingPressureUtility(variables);
		utility += (variables.trip_commuting * parameters.betaCar_gender_commuting * personVariables.gender +
				variables.trip_others * parameters.betaCar_gender_others * personVariables.gender);

		return utility;
	}
}
