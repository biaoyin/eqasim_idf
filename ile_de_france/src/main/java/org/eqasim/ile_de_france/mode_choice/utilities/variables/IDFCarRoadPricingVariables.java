package org.eqasim.ile_de_france.mode_choice.utilities.variables;

import org.eqasim.core.simulation.mode_choice.utilities.variables.BaseVariables;

public class IDFCarRoadPricingVariables implements BaseVariables {
    // Toll
    final public double road_pricing_fee;


    public IDFCarRoadPricingVariables(double road_pricing_fee) {
        this.road_pricing_fee = road_pricing_fee;
    }
}
