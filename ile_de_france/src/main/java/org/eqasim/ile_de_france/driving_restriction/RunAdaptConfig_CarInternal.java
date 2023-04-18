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
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;

import java.util.Arrays;

public class RunAdaptConfig_CarInternal {
    // set input-path and output-path
	private static final String scenarioID = "ile-de-france-5pct";

	static public void main(String[] args) throws ConfigurationException {

		args = new String[] {"--input-path", "ile_de_france\\scenarios\\" + scenarioID + "\\base_case\\ile_de_france_config_adapt.xml",
				"--output-path", "ile_de_france\\scenarios\\" + scenarioID +"\\driving_restriction_paris_4arr\\ile_de_france_config_carInternal.xml"};
		IDFConfigurator configurator = new IDFConfigurator();
		ConfigAdapter.run(args, configurator.getConfigGroups(), RunAdaptConfig_CarInternal::adaptConfiguration);
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
		//Replace default strategy settings in eqasim considering DRZ
		for (StrategySettings ss : config.strategy().getStrategySettings()) {
			if (ss.getStrategyName().equals(DiscreteModeChoiceModule.STRATEGY_NAME)) {
				ss.setSubpopulation("personExternal");
				ss.setWeight(0.05);
			}
			if (ss.getStrategyName().equals("KeepLastSelected")) {
//				ss.setStrategyName("ChangeExpBeta");
				ss.setSubpopulation("personExternal");
				ss.setWeight(0.95);
			}
		}

		//1-1) for subpopulation = personInternal
		StrategySettings strategySettings_mode_int = new StrategySettings();
		strategySettings_mode_int.setStrategyName("DiscreteModeChoice");  //others: ReRoute, TimeAllocationMutator
		strategySettings_mode_int.setSubpopulation("personInternal");
		strategySettings_mode_int.setWeight(0.05);

		StrategySettings strategySettings_mode_int2 = new StrategySettings();
		strategySettings_mode_int2.setStrategyName("KeepLastSelected");  //ChangeExpBeta
		strategySettings_mode_int2.setSubpopulation("personInternal");
		strategySettings_mode_int2.setWeight(0.95);

		StrategyConfigGroup strategyConfig = config.strategy();
		strategyConfig.addStrategySettings(strategySettings_mode_int);
		strategyConfig.addStrategySettings(strategySettings_mode_int2);
		//and others
		//strategyConfig.setFractionOfIterationsToDisableInnovation(0.8);

		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		dmcConfig.setModeAvailability(IDFModeChoiceModule.MODE_AVAILABILITY_NAME);

		//BYIN: we consider mode choice strategy without sizeofMemories = 1
//		dmcConfig.setEnforceSinglePlan(false);
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore();
		scoringConfig.setMarginalUtlOfWaitingPt_utils_hr(-1.0);

		// Calibration results for 5%
		if (eqasimConfig.getSampleSize() == 0.05) {
			// Adjust flow and storage capacity
			config.qsim().setFlowCapFactor(0.045);
			config.qsim().setStorageCapFactor(0.045);
		}
	}
}
