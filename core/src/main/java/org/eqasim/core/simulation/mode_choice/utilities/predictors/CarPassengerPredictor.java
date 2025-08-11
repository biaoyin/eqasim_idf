package org.eqasim.core.simulation.mode_choice.utilities.predictors;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eqasim.core.simulation.mode_choice.cost.CostModel;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarPassengerVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.CarVariables;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class CarPassengerPredictor extends CachedVariablePredictor<CarPassengerVariables> {

	private final ModeParameters parameters;
	private boolean flag_multi_car_stage = true; //use multi_stage car trips or not
	private boolean flag_walk_time_const = false; //walk leg time is constant or not

	@Inject
	public CarPassengerPredictor(ModeParameters parameters) {
		this.parameters = parameters;
	}
	@Override
	public CarPassengerVariables predict(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		double euclideanDistance_km = 0.0;
		//double cost_MU = 0.0;
		double travelTime_min = 0.0;
		double accessEgressTime_min = 0.0;
		int trip_commuting = 0;
		int trip_others = 0;

		if (flag_multi_car_stage == false) {
			if (elements.size() > 1) {
				throw new IllegalStateException("We do not support multi-stage car trips yet.");
			} else{
				Leg leg = (Leg) elements.get(0);
				travelTime_min = leg.getTravelTime().seconds() / 60.0;
				euclideanDistance_km = PredictorUtils.calculateEuclideanDistance_km(trip);
				accessEgressTime_min = parameters.car.constantAccessEgressWalkTime_min;
			}
		} else {
			for (PlanElement element : elements) {
				if (element instanceof Leg) {
					Leg leg = (Leg) element;
					switch (leg.getMode()) {
						case TransportMode.walk:
							if (flag_walk_time_const == true) {
								accessEgressTime_min = parameters.car.constantAccessEgressWalkTime_min;
							} else{
								accessEgressTime_min += leg.getTravelTime().seconds() / 60.0;
							}
							break;
						case "carPassengerInternal": //BYIN 2025-06
						case "car_passenger":
							travelTime_min += leg.getTravelTime().seconds() / 60.0;

							break;
						default:
							throw new IllegalStateException("Unknown mode in passenger trip: " + leg.getMode());
					}
				}
			}
		}
		euclideanDistance_km = PredictorUtils.calculateEuclideanDistance_km(trip);
		trip_commuting = PredictorUtils.getCommutingTripPurpose(trip);
		if (trip_commuting == 1) {
			trip_others = 0;
		} else {
			trip_others = 1;
		}

		return new CarPassengerVariables(travelTime_min, euclideanDistance_km, accessEgressTime_min, trip_commuting, trip_others);
	}
}
