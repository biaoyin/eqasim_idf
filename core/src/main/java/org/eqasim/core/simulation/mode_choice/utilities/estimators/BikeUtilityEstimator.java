package org.eqasim.core.simulation.mode_choice.utilities.estimators;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.UtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.BikePredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PersonPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.variables.BikeVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import com.google.inject.Inject;

public class BikeUtilityEstimator implements UtilityEstimator {
	private final ModeParameters parameters;
	private final BikePredictor bikePredictor;
	private final PersonPredictor personPredictor;
	private final static List<String> bikeTravelTimes = new LinkedList<>();
	private static boolean  RecordActive = false;
	@Inject
	public BikeUtilityEstimator(ModeParameters parameters, PersonPredictor personPredictor,
			BikePredictor bikePredictor) {
		this.parameters = parameters;
		this.bikePredictor = bikePredictor;
		this.personPredictor = personPredictor;
	}

	protected double estimateConstantUtility() {
		return parameters.bike.alpha_u;
	}

	protected double estimateTravelTimeUtility(BikeVariables variables) {
		return parameters.bike.betaTravelTime_u_min * variables.travelTime_min;
	}

	protected double estimateAgeOver18Utility(PersonVariables variables) {
		return parameters.bike.betaAgeOver18_u_a * Math.max(0.0, variables.age_a - 18);
	}

	// BYIN feb 24
	public List<String> getBikeTravelTimes() {
		return bikeTravelTimes;
	}
	public void setRecordActive() {
		this.RecordActive = true;
	}
	protected void saveTripTravelTime (BikeVariables variables, Person person, DiscreteModeChoiceTrip trip) {

		String personID = person.getId().toString();

		int totalSecs = (int) trip.getDepartureTime();
		int hours = (totalSecs / 3600);
		int minutes = (totalSecs % 3600) / 60;
		int seconds = totalSecs % 60;
		String tripDepTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

		double travelTime_min = 0.0;
		travelTime_min = variables.travelTime_min;
		travelTime_min = Precision.round(travelTime_min, 1);
		double euclideanDistance = 0.0;
		euclideanDistance = travelTime_min * 60 * 3.1 / 1.4 /1000;

		if (RecordActive) {
			bikeTravelTimes.add(personID + ";" + tripDepTime + ";" + travelTime_min + ";" + euclideanDistance);
		}
	}

	@Override
	public double estimateUtility(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		PersonVariables personVariables = personPredictor.predictVariables(person, trip, elements);
		BikeVariables bikeVariables = bikePredictor.predictVariables(person, trip, elements);

		// BYIN feb 24
		saveTripTravelTime (bikeVariables, person, trip);

		double utility = 0.0;

		utility += estimateConstantUtility();
		utility += estimateTravelTimeUtility(bikeVariables) * Math.exp(parameters.lambda_time * (personVariables.income - parameters.referenceHouseholdIncome)/parameters.referenceHouseholdIncome);
		utility += estimateAgeOver18Utility(personVariables);

		return utility;
	}
}
