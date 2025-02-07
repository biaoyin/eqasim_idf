package org.eqasim.core.simulation.mode_choice.filters;

import java.util.List;

import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.contribs.discrete_mode_choice.model.tour_based.TourFilter;

public class TourLengthFilter implements TourFilter {
	@Override
	public boolean filter(Person person, List<DiscreteModeChoiceTrip> tour) {
		return tour.size() <= 6;
	}// BYIN. for purpose of modal time comparison, set non-constraint to tour.size with big value (default is 6)
}
