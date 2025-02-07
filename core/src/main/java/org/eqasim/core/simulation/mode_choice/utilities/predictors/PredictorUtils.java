package org.eqasim.core.simulation.mode_choice.utilities.predictors;

import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.core.utils.geometry.CoordUtils;

public class PredictorUtils {
	private PredictorUtils() {
	}

	static public double calculateEuclideanDistance_km(DiscreteModeChoiceTrip trip) {
		return CoordUtils.calcEuclideanDistance(trip.getOriginActivity().getCoord(),
				trip.getDestinationActivity().getCoord()) * 1e-3;
	}

	static public int getAge(Person person) {
		return (int) (Integer) person.getAttributes().getAttribute("age");
	}

	// BYIN: 2023-10
	static public double getHouseholdIncome(Person person) { return (double) (Double) person.getAttributes().getAttribute("householdIncome");}

	//BYIN: 2025-01: equivalized household income
	static public double getEquivalizedHouseholdIncome(Person person) { return (double) (Double) person.getAttributes().getAttribute("unitIncome");}

	//BYIN: 2025-01
	static public int getGender(Person person) {
		String gender = (String) person.getAttributes().getAttribute("sex");
		if (gender.equals("m")) {
			return 0;
		} else {
			return 1;
		}
	}

	static public int getCommutingTripPurpose(DiscreteModeChoiceTrip trip) {
		String purpose = (String) trip.getDestinationActivity().getType();
		if (purpose.equals("work") || purpose.equals("study")) {
			return 1;
		} else {
			return 0;
		}
	}
}
