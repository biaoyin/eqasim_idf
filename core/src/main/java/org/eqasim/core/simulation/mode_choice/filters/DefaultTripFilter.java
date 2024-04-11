package org.eqasim.core.simulation.mode_choice.filters;

import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.contribs.discrete_mode_choice.model.tour_based.TripFilter;

import java.util.List;

public class DefaultTripFilter implements TripFilter {

	@Override
	public boolean filter(Person person, DiscreteModeChoiceTrip Trip) {

		if (Trip.getOriginActivity().getType().equals("outside")) {
			return false;
		}

		return true;
	}
}
