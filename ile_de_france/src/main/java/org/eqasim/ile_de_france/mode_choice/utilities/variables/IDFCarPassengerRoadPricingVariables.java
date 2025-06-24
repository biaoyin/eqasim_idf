package org.eqasim.ile_de_france.mode_choice.utilities.variables;

import org.eqasim.core.simulation.mode_choice.utilities.variables.BaseVariables;

public class IDFCarPassengerRoadPricingVariables implements BaseVariables {
    // Toll
    final public double road_pricing_fee;
    final public int trip_commuting;
    final public int trip_others;


    public IDFCarPassengerRoadPricingVariables(double road_pricing_fee, int trip_commuting, int trip_others) {
        this.road_pricing_fee = road_pricing_fee;
        this.trip_commuting = trip_commuting;
        this.trip_others = trip_others;
    }
}
