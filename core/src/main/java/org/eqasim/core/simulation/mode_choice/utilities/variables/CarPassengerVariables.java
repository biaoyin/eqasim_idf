package org.eqasim.core.simulation.mode_choice.utilities.variables;

public class CarPassengerVariables implements BaseVariables {
	final public double travelTime_min;
	//final public double cost_MU;
	final public double euclideanDistance_km;
	final public double accessEgressTime_min;
	final public int trip_commuting;
	final public int trip_others;


	public CarPassengerVariables(double travelTime_min, double euclideanDistance_km,
                                 double accessEgressTime_min, int trip_commuting, int trip_others) {
		this.travelTime_min = travelTime_min;
		this.euclideanDistance_km = euclideanDistance_km;
		this.accessEgressTime_min = accessEgressTime_min;
		this.trip_commuting = trip_commuting;
		this.trip_others = trip_others;
	}

}
