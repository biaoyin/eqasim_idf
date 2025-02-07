package org.eqasim.core.simulation.mode_choice.parameters;

import org.eqasim.core.simulation.mode_choice.ParameterDefinition;

public class ModeParameters implements ParameterDefinition {
	public class CarParameters {
		public double alpha_u_commuting = 0.0;
		public double betaTravelTime_u_min_commuting = 0.0;
		public double alpha_u_others = 0.0;
		public double betaTravelTime_u_min_others = 0.0;
		public double constantAccessEgressWalkTime_min = 0.0;
		public double constantParkingSearchPenalty_min = 0.0;
		public double betaParkingPressure_commuting = 0.0;
		public double betaParkingPressure_others = 0.0;
		public double constantParkingPressure = 0.0;
	}

	public class CarPassengerParameters {
		public double alpha_u_commuting = 0.0;
		public double betaTravelTime_u_min_commuting = 0.0;
		public double betaAvailability_commuting = 0.0;

		public double alpha_u_others = 0.0;
		public double betaTravelTime_u_min_others = 0.0;
		public double betaAvailability_others = 0.0;

	}

	public class PtParameters {
		public double alpha_u_commuting = 0.0;
		public double betaLineSwitch_u_commuting = 0.0;
		public double betaInVehicleTime_u_min_commuting = 0.0;
		public double betaWaitingTime_u_min_commuting = 0.0;
		public double betaAccessEgressTime_u_min_commuting = 0.0;
		public double betaQ1_commuting = 0.0;
		public double betaQ2_commuting = 0.0;
		public double betaQ3_commuting = 0.0;
		public double betaQ4_commuting = 0.0;

		public double alpha_u_others = 0.0;
		public double betaLineSwitch_u_others = 0.0;
		public double betaInVehicleTime_u_min_others = 0.0;
		public double betaWaitingTime_u_min_others = 0.0;
		public double betaAccessEgressTime_u_min_others = 0.0;
		public double betaQ1_others = 0.0;
		public double betaQ2_others = 0.0;
		public double betaQ3_others = 0.0;
		public double betaQ4_others = 0.0;

		public double cost_MU_constant = 0.0;

	}

	public class BikeParameters {
		public double alpha_u_commuting = 0.0;
		public double betaTravelTime_u_min_commuting = 0.0;

		public double alpha_u_others = 0.0;
		public double betaTravelTime_u_min_others = 0.0;
		public double betaAgeOver18_u_a = 0.0;
	}

	public class WalkParameters {
		public double betaTravelTime_u_min_commuting = 0.0;
		public double betaTravelTime_u_min_others = 0.0;
		public double betaAccessEgressTravelTime_u_min = 0.0;
	}

	public class CarPTParameters {
		public double alpha_u_commuting = 0.0;
		public double alpha_u_others = 0.0;
		public double parkingTimeToAccessPt = 0.0;
		public double pickupTimeToAccessCar = 0.0;

	}

	public double lambdaCostEuclideanDistance = 0.0;
	public double referenceEuclideanDistance_km = 0.0;

	public double betaCost_u_MU_commuting = 0.0;
	public double betaCost_u_MU_others = 0.0;

	public final CarParameters car = new CarParameters();
	public final PtParameters pt = new PtParameters();
	public final BikeParameters bike = new BikeParameters();
	public final WalkParameters walk = new WalkParameters();
	public final CarPTParameters car_pt = new CarPTParameters();
	public final CarPassengerParameters car_passenger = new CarPassengerParameters();


	//new DMC with income: BYIN 2025-01
	public double mean_equ_income = 0.0;
	public double income_power = 0.0;
	public double betaCar_gender_commuting = 0.0;
	public double betaBike_gender_commuting = 0.0;
	public double betaWalk_gender_commuting = 0.0;
	public double betaCar_gender_others = 0.0;
	public double betaBike_gender_others = 0.0;
	public double betaWalk_gender_others = 0.0;
}
