package org.eqasim.ile_de_france.mode_choice.parameters;

import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;

public class IDFModeParameters extends ModeParameters {
	public class IDFCarParameters {
		public double betaInsideUrbanArea;
		public double betaCrossingUrbanArea;
	}

	public class IDFBikeParameters {
		public double betaInsideUrbanArea;
	}

	public class IDFPtParameters {
		public double onlyBus_u_commuting;
		public double onlyBus_u_others;
	}

	public final IDFCarParameters idfCar = new IDFCarParameters();
	public final IDFBikeParameters idfBike = new IDFBikeParameters();
	public final IDFPtParameters idfPt = new IDFPtParameters();

	public static IDFModeParameters buildDefault() {
		IDFModeParameters parameters = new IDFModeParameters();

		// Cost
		parameters.betaCost_u_MU_commuting = -0.050972;
		parameters.betaCost_u_MU_others = -0.115523;
		parameters.lambdaCostEuclideanDistance = 0;// BYIN 2025-01: need a calibration in the new dmc?
		parameters.referenceEuclideanDistance_km = 40.0;

		// new DMC with income: BYIN 2025-01
		parameters.mean_equ_income = 2415.0;
		parameters.income_power = -0.1588;

		//car
		parameters.car.alpha_u_commuting = -2.308873;
		parameters.car.betaTravelTime_u_min_commuting = -0.034261;
		parameters.betaCar_gender_commuting = -0.502989;
		parameters.car.betaParkingPressure_commuting = -3.293412;

		parameters.car.alpha_u_others = -2.074074;
		parameters.car.betaTravelTime_u_min_others = -0.012874;
		parameters.car.betaParkingPressure_others = -4.073834;
		parameters.betaCar_gender_others = -0.564531;

		parameters.car.constantParkingPressure = 0.15; //Rayane : average value

		parameters.car.constantAccessEgressWalkTime_min = 4.0;
		parameters.car.constantParkingSearchPenalty_min = 4.0;

		// pt
		parameters.pt.alpha_u_commuting = -0.582576;
		parameters.pt.betaLineSwitch_u_commuting = -0.495175;
		parameters.pt.betaInVehicleTime_u_min_commuting = -0.036074;
		//parameters.pt.betaWaitingTime_u_min_commuting = -0.036074;
		//parameters.pt.betaAccessEgressTime_u_min_commuting = -0.036074;
		//parameters.pt.betaQ1_commuting = 0.;
		parameters.pt.betaQ2_commuting = -0.659760;
		parameters.pt.betaQ3_commuting = -0.441588;
		parameters.pt.betaQ4_commuting = -0.595877;

		parameters.pt.alpha_u_others = -1.629294;
		parameters.pt.betaLineSwitch_u_others = -0.382428;
		parameters.pt.betaInVehicleTime_u_min_others = -0.024024;
		//parameters.pt.betaWaitingTime_u_min_others = -0.024024;
		//parameters.pt.betaAccessEgressTime_u_min_others = -0.024024;
		//parameters.pt.betaQ1_others = 0.;
		parameters.pt.betaQ2_others = -0.443545;
		parameters.pt.betaQ3_others = -0.792043;
		parameters.pt.betaQ4_others = -0.685524;

		parameters.pt.cost_MU_constant = 0.80;
		parameters.idfPt.onlyBus_u_commuting = -1.745111;
		parameters.idfPt.onlyBus_u_others = -1.908632;

		////Car_passenger
		parameters.car_passenger.alpha_u_commuting = -3.065954;
		parameters.car_passenger.betaTravelTime_u_min_commuting = -0.089086;
		parameters.car_passenger.betaAvailability_commuting = -2.212501;
		parameters.car_passenger.alpha_u_others = -3.809383;
		parameters.car_passenger.betaTravelTime_u_min_others = -0.015732;
		parameters.car_passenger.betaAvailability_others = -1.095952;

		//bike
		parameters.bike.alpha_u_commuting = -4.295694;
		parameters.bike.betaTravelTime_u_min_commuting = -0.055221;
		parameters.betaBike_gender_commuting = -0.648454;

		parameters.bike.alpha_u_others = -4.896731;
		parameters.bike.betaTravelTime_u_min_others = -0.038537;
		parameters.betaBike_gender_others = -1.111201;

		//walk
		parameters.walk.betaTravelTime_u_min_commuting = -0.133902;
		parameters.betaWalk_gender_commuting = -0.152070;
		parameters.walk.betaTravelTime_u_min_others = -0.137024;
		parameters.betaWalk_gender_others = -0.427766;

		//car_pt
		parameters.car_pt.alpha_u_commuting = -1.30;
		parameters.car_pt.alpha_u_others = -2.20;

		parameters.car_pt.parkingTimeToAccessPt = 5.0; // Default
		parameters.car_pt.pickupTimeToAccessCar = 5.0;

		return parameters;
	}
}
