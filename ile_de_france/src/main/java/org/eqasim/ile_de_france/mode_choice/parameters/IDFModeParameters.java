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
		parameters.betaCost_u_MU_commuting = -0.069986;
		parameters.betaCost_u_MU_others = -0.157372;
		parameters.lambdaCostEuclideanDistance = 0;// BYIN 2025-01: need a calibration in the new dmc?
		parameters.referenceEuclideanDistance_km = 40.0;

		// new DMC with income: BYIN 2025-01
		parameters.mean_equ_income = 2415.0;
		parameters.income_power = -0.1588;

		//car
		parameters.car.alpha_u_commuting = -3.839839;
		parameters.car.betaTravelTime_u_min_commuting = -0.038951;
		parameters.betaCar_gender_commuting = -0.402126;
		parameters.car.betaParkingPressure_commuting = -3.384471;

		parameters.car.alpha_u_others = -0.885067;
		parameters.car.betaTravelTime_u_min_others = -0.014549;
		parameters.car.betaParkingPressure_others = -3.824464;
		parameters.betaCar_gender_others = -0.608134;

		parameters.car.constantParkingPressure = 0.15; //Rayane : average value

		parameters.car.constantAccessEgressWalkTime_min = 4.0;
		parameters.car.constantParkingSearchPenalty_min = 4.0;

		// pt
		parameters.pt.alpha_u_commuting = -2.596137; //
		parameters.pt.betaLineSwitch_u_commuting = -0.469255;
		parameters.pt.betaInVehicleTime_u_min_commuting = -0.035997;
		//parameters.pt.betaWaitingTime_u_min_commuting = -0.036074;
		//parameters.pt.betaAccessEgressTime_u_min_commuting = -0.036074;
		//parameters.pt.betaQ1_commuting = 0.;
		parameters.pt.betaQ2_commuting = -0.484810;
		parameters.pt.betaQ3_commuting = -0.370425;
		parameters.pt.betaQ4_commuting = -0.475788;

		parameters.pt.alpha_u_others = -0.586071;//
		parameters.pt.betaLineSwitch_u_others = -0.449813;
		parameters.pt.betaInVehicleTime_u_min_others = -0.022682;
		//parameters.pt.betaWaitingTime_u_min_others = -0.024024;
		//parameters.pt.betaAccessEgressTime_u_min_others = -0.024024;
		//parameters.pt.betaQ1_others = 0.;
		parameters.pt.betaQ2_others = -0.510510;
		parameters.pt.betaQ3_others = -0.870120;
		parameters.pt.betaQ4_others = -0.768457;

		parameters.pt.cost_MU_constant = 0.80;
		parameters.idfPt.onlyBus_u_commuting = -1.715637;
		parameters.idfPt.onlyBus_u_others = -2.093884;

		////Car_passenger
		parameters.car_passenger.alpha_u_commuting = -10 ;//
		parameters.car_passenger.betaTravelTime_u_min_commuting = -0.084630;
		parameters.car_passenger.betaAvailability_commuting = -2.161480;
		parameters.car_passenger.alpha_u_others = -3.35;//
		parameters.car_passenger.betaTravelTime_u_min_others = -0.023233;
		parameters.car_passenger.betaAvailability_others = -1.091392;

		//bike
		parameters.bike.alpha_u_commuting = -4.8;//
		parameters.bike.betaTravelTime_u_min_commuting = -0.051972;
		parameters.betaBike_gender_commuting = -0.579307;

		parameters.bike.alpha_u_others = -2.42;//
		parameters.bike.betaTravelTime_u_min_others = -0.043448;
		parameters.betaBike_gender_others = -1.038523;

		//walk
		parameters.walk.betaTravelTime_u_min_commuting = -0.132587;//
		parameters.betaWalk_gender_commuting = -0.105292;
		parameters.walk.betaTravelTime_u_min_others = -0.137037;//
		parameters.betaWalk_gender_others = -0.436807;

		//car_pt
		parameters.car_pt.alpha_u_commuting = -1.3;//
		parameters.car_pt.alpha_u_others = -2.2;//

		parameters.car_pt.parkingTimeToAccessPt = 5.0; // Default
		parameters.car_pt.pickupTimeToAccessCar = 5.0;

		return parameters;
	}
}
