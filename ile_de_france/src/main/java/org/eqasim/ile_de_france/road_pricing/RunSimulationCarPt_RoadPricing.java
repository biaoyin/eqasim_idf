package org.eqasim.ile_de_france.road_pricing;

import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.components.car_pt.routing.EqasimCarPtModule;
import org.eqasim.core.components.car_pt.routing.EqasimPtCarModule;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModuleCarPt;
import org.eqasim.core.tools.TestCarPtPara;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModuleCarPt;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModuleCarPtRoadPricing;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModuleRoadPricing;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFModeParameters;
import org.eqasim.ile_de_france.mode_choice.parameters.TestTollFee;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;
import java.util.*;

//BYIN2025-01: Run Rayane's new dmc (version 2, see dmc_variables_and_equations.pdf. Attention: new scenario inputs need to use,
// due to the equivalized unit income generated in the new synthetic population.
public class RunSimulationCarPt_RoadPricing {
	static String outputPath =
			"F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\simout_IdF_egt5pct_egt2020_DCM_ref";
			// "E:\\lvmt_BY\\simulation_output\\eqasim_idf\\ile-de-france-5pct\\New_DMC_2\\base_case";

	static public void main(String[] args) throws ConfigurationException, IOException {
		args = new String[] {
				"--config-path", "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\synpop_IdF_egt5pct_egt2020\\ile_de_france_config.xml"};
				// "--config-path", "ile_de_france/scenarios/ile-de-france-5pct/base_case/ile_de_france_config.xml"};
        // set car-pt parameters
		String locationFile = "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\data_IdF_RP\\parcs-relais-idf_rer_train_outside_paris.csv";
		// set road pricing parameters
		String areaShapeFile = "F:\\Rayane-MATSIM-Synth-pop_2020-DCM-2020\\data_IdF_RP\\gis\\paris_inner.shp";
		double fee_toll = 0; //euros

		TestCarPtPara tp = new TestCarPtPara();
		tp.setCarPtSavePath(outputPath + fee_toll);
		TestTollFee tf = new TestTollFee();
		tf.setTollFee(fee_toll);
		tf.setTollAreaFilePath(areaShapeFile);

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();
		IDFConfigurator configurator = new IDFConfigurator();
		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), configurator.getConfigGroups());

		//modify some parameters in config file
		config.controler().setLastIteration(80); //80
		config.controler().setOutputDirectory(outputPath + fee_toll);
		//config.plans().setInputFile("ile_de_france_population_test_100p.xml.gz");
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
		config.qsim().setUsingTravelTimeCheckInTeleportation( true );

		for (StrategyConfigGroup.StrategySettings ss : config.strategy().getStrategySettings()) {
			if (ss.getStrategyName().equals("KeepLastSelected")) {
				ss.setWeight(0.95);
			}
			if (ss.getStrategyName().equals("DiscreteModeChoice")) {
				ss.setWeight(0.05);
			}
		}

		// Eqasim config definition to add the mode car_pt estimation
		EqasimConfigGroup eqasimConfig = EqasimConfigGroup.get(config);
		eqasimConfig.setEstimator("car_pt", "IDFCarPtUtilityEstimator");//BYIN 2025-01: we use the IDF car_pt (pt_car) definition in the new dmc
		eqasimConfig.setEstimator("pt_car", "IDFPtCarUtilityEstimator");

		eqasimConfig.removeEstimator("car_passenger");
		eqasimConfig.setEstimator("car_passenger", "IDFCarPassengerUtilityEstimatorRoadPricing");
		eqasimConfig.removeEstimator("pt");
		eqasimConfig.setEstimator("pt", "IDFPtUtilityEstimator"); //BYIN 2025-01: we use the IDF pt definition rather than the default pt in the config file.

		// Scoring config definition to add the mode car_pt parameters
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore();
		ModeParams carPtParams = new ModeParams("car_pt");
		ModeParams ptCarParams = new ModeParams("pt_car");
		scoringConfig.addModeParams(carPtParams);
		scoringConfig.addModeParams(ptCarParams);

		// "car_pt interaction" definition
		ActivityParams paramscarPtInterAct = new ActivityParams("carPt interaction");
		paramscarPtInterAct.setTypicalDuration(100.0);
		paramscarPtInterAct.setScoringThisActivityAtAll(false);

		// "pt_car interaction" definition
		ActivityParams paramsPtCarInterAct = new ActivityParams("ptCar interaction");
		paramsPtCarInterAct.setTypicalDuration(100.0);
		paramsPtCarInterAct.setScoringThisActivityAtAll(false);

		// this interaction doesn't work
		ActivityParams toll_interaction = new ActivityParams("toll interaction");
		toll_interaction.setTypicalDuration(100.0);
		toll_interaction.setScoringThisActivityAtAll(false);

		// Adding "car_pt interaction" to the scoring
		scoringConfig.addActivityParams(paramscarPtInterAct);
		scoringConfig.addActivityParams(paramsPtCarInterAct);
		scoringConfig.addActivityParams(toll_interaction);

		// DMC config definition
		// Adding the mode "car_pt" and "pt_car" to CachedModes
		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		Collection<String> cachedModes = new HashSet<>(dmcConfig.getCachedModes());
		cachedModes.add("car_pt");
		cachedModes.add("pt_car");
		dmcConfig.setCachedModes(cachedModes);
		// BYIN 2025-01: we add passenger utility now. So here delete the PassengerConstraint
		Collection<String> tripConstraints = new HashSet<>(dmcConfig.getTripConstraints());
		if (tripConstraints.contains("PassengerConstraint")){
			tripConstraints.remove("PassengerConstraint");
		}
		dmcConfig.setTripConstraints(tripConstraints);

		// Activation of constraint intermodal modes Using
		Collection<String> tourConstraints = new HashSet<>(dmcConfig.getTourConstraints());
		tourConstraints.add("IntermodalModesConstraint");
		dmcConfig.setTourConstraints(tourConstraints);

		cmd.applyConfiguration(config);
		Scenario scenario = ScenarioUtils.createScenario(config);
		configurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);

		Controler controller = new Controler(scenario);
		configurator.configureController(controller);

		//set Park and ride lot locations
		List<Coord> parkRideCoords;
		readParkRideCoordsFromFile readFile = new readParkRideCoordsFromFile(locationFile);
		parkRideCoords = readFile.readCoords;
		ParkRideManager parkRideManager = new ParkRideManager();
		parkRideManager.setParkRideCoords(parkRideCoords);
		Network network = scenario.getNetwork();
		parkRideManager.setNetwork(network);

		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModuleCarPt());
		controller.addOverridingModule(new IDFModeChoiceModuleCarPtRoadPricing(cmd, parkRideCoords, scenario.getNetwork(), scenario.getPopulation().getFactory()));
		controller.addOverridingModule(new EqasimCarPtModule(parkRideCoords));
		controller.addOverridingModule(new EqasimPtCarModule(parkRideCoords));

		controller.run();
	}

}