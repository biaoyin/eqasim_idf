package org.eqasim.core.simulation.mode_choice.utilities.variables;

public class CarPtVariables implements BaseVariables{
    // CAR
    final public double travelTime_min;
    final public double euclideanDistance_km_car;
    final public double accessEgressTime_min_car;
    final public double cost_MU_car;

    // PT
    public final double inVehicleTime_min;
    public final double waitingTime_min;
    public final int numberOfLineSwitches;
    final public double euclideanDistance_km_pt;
    final public double accessEgressTime_min_pt;
    final public double cost_MU_pt;

    // To do: Add park and ride facility characteristic such as capacity

    public CarPtVariables(double travelTime_min, double euclideanDistance_km_car, double accessEgressTime_min_car,
                          double cost_MU_car, double inVehicleTime_min, double waitingTime_min, int numberOfLineSwitches,
                          double euclideanDistance_km_pt, double accessEgressTime_min_pt, double cost_MU_pt) {
        this.travelTime_min = travelTime_min;
        this.cost_MU_car = cost_MU_car;
        this.euclideanDistance_km_car = euclideanDistance_km_car;
        this.accessEgressTime_min_car = accessEgressTime_min_car;
        this.inVehicleTime_min = inVehicleTime_min;
        this.waitingTime_min = waitingTime_min;
        this.numberOfLineSwitches = numberOfLineSwitches;
        this.cost_MU_pt = cost_MU_pt;
        this.euclideanDistance_km_pt = euclideanDistance_km_pt;
        this.accessEgressTime_min_pt = accessEgressTime_min_pt;
    }
}
