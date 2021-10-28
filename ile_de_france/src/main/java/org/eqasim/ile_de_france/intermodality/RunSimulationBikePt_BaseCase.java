package org.eqasim.ile_de_france.intermodality;

import org.eqasim.core.components.bike_pt.routing.EqasimBikePtModule;
import org.eqasim.core.components.bike_pt.routing.EqasimPtBikeModule;
import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModuleBikePt;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModuleBikePt;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.*;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.api.core.v01.Coord;

import java.io.IOException;
import java.util.*;

public class RunSimulationBikePt_BaseCase {
	static public void main(String[] args) throws ConfigurationException, IOException {
		args = new String[] {"--config-path", "ile_de_france/scenarios/saintdenis-cut-10pct/base_case/small-zone-ex3/SaintDenis_config.xml"};
//		String locationFile = "ile_de_france/scenarios/saint-denis-bike-location_1.csv";
		String locationFile = "ile_de_france/scenarios/saint-denis-bike-location_2.csv";

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), IDFConfigurator.getConfigGroups());

		//modify some parameters in config file
		config.controler().setLastIteration(3);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		config.vehicles().setVehiclesFile("vehicle_types.xml");
		config.strategy().setMaxAgentPlanMemorySize(5);
		config.strategy().setPlanSelectorForRemoval("WorstPlanSelector");
		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		dmcConfig.setEnforceSinglePlan(false);
		for (StrategyConfigGroup.StrategySettings ss : config.strategy().getStrategySettings()) {
			if (ss.getStrategyName().equals("KeepLastSelected")) {
				ss.setStrategyName("ChangeExpBeta");
				ss.setWeight(0.90);
			}
		}
		StrategyConfigGroup.StrategySettings strategySettings_mode = new StrategyConfigGroup.StrategySettings();
		strategySettings_mode.setStrategyName("ChangeSingleTripMode");
		strategySettings_mode.setWeight(0.05);
		StrategyConfigGroup strategyConfig = config.strategy();
		strategyConfig.addStrategySettings(strategySettings_mode);
		strategyConfig.setFractionOfIterationsToDisableInnovation(0.8);
		PlansCalcRouteConfigGroup routingConfig = config.plansCalcRoute();
		routingConfig.setNetworkModes(Arrays.asList("car", "car_passenger", "truck"));


		// Eqasim config definition to add the mode bike_pt estimation
		EqasimConfigGroup eqasimConfig = EqasimConfigGroup.get(config);
		eqasimConfig.setEstimator("bike_pt", "BikePtUtilityEstimator");
		eqasimConfig.setEstimator("pt_bike", "PtBikeUtilityEstimator");

		// Scoring config definition to add the mode bike_pt parameters
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore();
		ModeParams bikePtParams = new ModeParams("bike_pt");
		ModeParams ptBikeParams = new ModeParams("pt_bike");
		scoringConfig.addModeParams(bikePtParams);
		scoringConfig.addModeParams(ptBikeParams);

		// "bike_pt interaction" definition
		ActivityParams paramsbikePtInterAct = new ActivityParams("bikePt interaction");
		paramsbikePtInterAct.setTypicalDuration(100.0);
		paramsbikePtInterAct.setScoringThisActivityAtAll(false);

		// "pt_bike interaction" definition
		ActivityParams paramsPtBikeInterAct = new ActivityParams("ptBike interaction");
		paramsPtBikeInterAct.setTypicalDuration(100.0);
		paramsPtBikeInterAct.setScoringThisActivityAtAll(false);

		// Adding "bike_pt interaction" to the scoring
		scoringConfig.addActivityParams(paramsbikePtInterAct);
		scoringConfig.addActivityParams(paramsPtBikeInterAct);

		// DMC config definition
		// Adding the mode "bike_pt" and "pt_bike" to CachedModes
		Collection<String> cachedModes = new HashSet<>(dmcConfig.getCachedModes());
		cachedModes.add("bike_pt");
		cachedModes.add("pt_bike");
		dmcConfig.setCachedModes(cachedModes);



		// Activation of constraint intermodal modes Using
	/*	Collection<String> tourConstraints = new HashSet<>(dmcConfig.getTourConstraints());
		tourConstraints.add("IntermodalModesConstraint");
		dmcConfig.setTourConstraints(tourConstraints);
*/
		/*for (StrategyConfigGroup.StrategySettings strategy : config.strategy().getStrategySettings()) {
			if(strategy.getStrategyName().equals("DiscreteModeChoice")) {
				strategy.setWeight(10000);// all weights from this innovative strategy
			}
		}*/
//
		cmd.applyConfiguration(config);

		Scenario scenario = ScenarioUtils.createScenario(config);
		IDFConfigurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);

		Controler controller = new Controler(scenario);
		IDFConfigurator.configureController(controller);

		List<Coord> parkRideCoords;
		readParkRideCoordsFromFile readFile = new readParkRideCoordsFromFile(locationFile);
		parkRideCoords = readFile.readCoords;
		ParkRideManager parkRideManager = new ParkRideManager();
		parkRideManager.setParkRideCoords(parkRideCoords);
		Network network = scenario.getNetwork();
		parkRideManager.setNetwork(network);

		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModuleBikePt());
		controller.addOverridingModule(new IDFModeChoiceModuleBikePt(cmd, parkRideCoords, scenario.getNetwork(), scenario.getPopulation().getFactory()));
		controller.addOverridingModule(new EqasimBikePtModule(parkRideCoords));
		controller.addOverridingModule(new EqasimPtBikeModule(parkRideCoords));

		controller.run();
	}

}