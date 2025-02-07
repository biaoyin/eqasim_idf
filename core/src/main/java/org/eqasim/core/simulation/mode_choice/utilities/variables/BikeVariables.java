package org.eqasim.core.simulation.mode_choice.utilities.variables;

public class BikeVariables implements BaseVariables {
	final public double travelTime_min;
	final public int trip_commuting;
	final public int trip_others;

	public BikeVariables(double travelTime_min, int trip_commuting, int trip_others) {
		this.travelTime_min = travelTime_min;
		this.trip_commuting = trip_commuting;
		this.trip_others = trip_others;
	}
}
