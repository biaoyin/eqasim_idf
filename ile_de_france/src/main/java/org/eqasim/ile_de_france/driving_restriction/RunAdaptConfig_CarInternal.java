package org.eqasim.ile_de_france.driving_restriction;

import org.eqasim.core.components.config.ConfigAdapter;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModule;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;

import java.util.Arrays;

public class RunAdaptConfig_CarInternal {
    // set input-path and output-path
	private static final String scenarioID = "ile_de_france_1pm";

	static public void main(String[] args) throws ConfigurationException {

		args = new String[] {"--input-path", "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\" + scenarioID + "\\matsim_input\\ile_de_france_config.xml",
				"--output-path", "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\" + scenarioID +"\\matsim_input\\PlanA_Paris\\ile_de_france_config_carInternal.xml"};

		ConfigAdapter.run(args, IDFConfigurator.getConfigGroups(), RunAdaptConfig_CarInternal::adaptConfiguration);
	}

	static public void adaptConfiguration(Config config) {
		// Adjust eqasim config
		EqasimConfigGroup eqasimConfig = EqasimConfigGroup.get(config);

		eqasimConfig.setCostModel(TransportMode.car, IDFModeChoiceModule.CAR_COST_MODEL_NAME);
		eqasimConfig.setCostModel(TransportMode.pt, IDFModeChoiceModule.PT_COST_MODEL_NAME);

		eqasimConfig.setEstimator(TransportMode.car, IDFModeChoiceModule.CAR_ESTIMATOR_NAME);
		eqasimConfig.setEstimator(TransportMode.bike, IDFModeChoiceModule.BIKE_ESTIMATOR_NAME);


        //BYIN:
		// Routing config
		PlansCalcRouteConfigGroup routingConfig = config.plansCalcRoute();
		routingConfig.setNetworkModes(Arrays.asList("car", "car_passenger", "truck", "carInternal"));


        //BYIN: strategy settings:

		//1) changes innovative and selector strategies
		//1-1) for subpopulation = person
		StrategySettings strategySettings_mode = new StrategySettings();
		strategySettings_mode.setStrategyName("ChangeSingleTripMode");
		strategySettings_mode.setSubpopulation("person");
		strategySettings_mode.setWeight(0.05);

//		StrategySettings strategySettings_route = new StrategySettings();
//		strategySettings_route.setStrategyName("ReRoute");
//		strategySettings_route.setSubpopulation("person");
//		strategySettings_route.setWeight(0.05);

/*
		StrategySettings strategySettings_time = new StrategySettings();
		strategySettings_time.setStrategyName("TimeAllocationMutator");
		strategySettings_time.setSubpopulation("person");
		strategySettings_time.setWeight(0.05);
*/

		//Replace default strategy settings in eqasim
		for (StrategySettings ss : config.strategy().getStrategySettings()) {
			if (ss.getStrategyName().equals(DiscreteModeChoiceModule.STRATEGY_NAME)) {
				ss.setSubpopulation("person");
			}
			if (ss.getStrategyName().equals("KeepLastSelected")) {
				ss.setStrategyName("ChangeExpBeta");
				ss.setSubpopulation("person");
				ss.setWeight(0.90);
			}
		}

		//1-1) for subpopulation = personInternal
		StrategySettings strategySettings_mode_int = new StrategySettings();
		strategySettings_mode_int.setStrategyName("DiscreteModeChoiceInternal");
		strategySettings_mode_int.setSubpopulation("personInternal");
		strategySettings_mode_int.setWeight(0.05);

		StrategySettings strategySettings_mode_int2 = new StrategySettings();
		strategySettings_mode_int2.setStrategyName("ChangeSingleTripMode");
		strategySettings_mode_int2.setSubpopulation("personInternal");
		strategySettings_mode_int2.setWeight(0.05);

//		StrategySettings strategySettings_route_int = new StrategySettings();
//		strategySettings_route_int.setStrategyName("ReRoute");
//		strategySettings_route_int.setSubpopulation("personInternal");
//		strategySettings_route_int.setWeight(0.05);

/*		StrategySettings strategySettings_time_int = new StrategySettings();
		strategySettings_time_int.setStrategyName("TimeAllocationMutator");
		strategySettings_time_int.setSubpopulation("personInternal");
		strategySettings_time_int.setWeight(0.05);*/

		StrategySettings strategySettings_sel_int = new StrategySettings();
		strategySettings_sel_int.setStrategyName("ChangeExpBeta");
		strategySettings_sel_int.setSubpopulation("personInternal");
		strategySettings_sel_int.setWeight(0.90);


		StrategyConfigGroup strategyConfig = config.strategy();
		strategyConfig.addStrategySettings(strategySettings_mode);
//		strategyConfig.addStrategySettings(strategySettings_route);
//		strategyConfig.addStrategySettings(strategySettings_time);
		strategyConfig.addStrategySettings(strategySettings_mode_int);
		strategyConfig.addStrategySettings(strategySettings_mode_int2);
//		strategyConfig.addStrategySettings(strategySettings_route_int);
//		strategyConfig.addStrategySettings(strategySettings_time_int);
		strategyConfig.addStrategySettings(strategySettings_sel_int);
		//and others
		strategyConfig.setFractionOfIterationsToDisableInnovation(0.8);

		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		dmcConfig.setModeAvailability(IDFModeChoiceModule.MODE_AVAILABILITY_NAME);

		//BYIN: we consider subpopulation's mode choice
		dmcConfig.setEnforceSinglePlan(false);

		// Calibration results for 5%
		if (eqasimConfig.getSampleSize() == 0.05) {
			// Adjust flow and storage capacity
			config.qsim().setFlowCapFactor(0.045);
			config.qsim().setStorageCapFactor(0.045);
		}
	}
}
