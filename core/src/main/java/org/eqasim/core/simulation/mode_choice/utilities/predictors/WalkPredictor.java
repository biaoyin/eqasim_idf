package org.eqasim.core.simulation.mode_choice.utilities.predictors;

import java.util.List;

import org.eqasim.core.simulation.mode_choice.utilities.variables.WalkVariables;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

public class WalkPredictor extends CachedVariablePredictor<WalkVariables> {
	@Override
	public WalkVariables predict(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		double travelTime_min = ((Leg) elements.get(0)).getTravelTime().seconds() / 60.0;
		int trip_commuting = 0;
		int trip_others = 0;
		trip_commuting = PredictorUtils.getCommutingTripPurpose(trip);
		if (trip_commuting == 1) {
			trip_others = 0;
		} else {
			trip_others = 1;
		}
		return new WalkVariables(travelTime_min,trip_commuting, trip_others);
	}
}
