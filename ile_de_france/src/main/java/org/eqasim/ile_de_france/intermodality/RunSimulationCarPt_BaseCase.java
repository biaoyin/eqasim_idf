package org.eqasim.ile_de_france.intermodality;

import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.components.car_pt.routing.EqasimCarPtModule;
import org.eqasim.core.components.car_pt.routing.EqasimPtCarModule;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModuleCarPt;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModuleCarPt;
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
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RunSimulationCarPt_BaseCase {

	static public void main(String[] args) throws ConfigurationException, IOException {
		args = new String[] {"--config-path", "ile_de_france/scenarios/ile-de-france-1pm/base_case/ile_de_france_config.xml"};
		String locationFile = "ile_de_france/scenarios/parcs-relais-idf_2021plus.csv";

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), IDFConfigurator.getConfigGroups());

		//modify some parameters in config file
		config.controler().setLastIteration(100);
		config.strategy().setMaxAgentPlanMemorySize(1);// be subject to the setEnforceSinglePlan to ensure vehicle tour constraint
//		config.strategy().setPlanSelectorForRemoval("ChangeExpBetaForRemoval");
		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		dmcConfig.setEnforceSinglePlan(true);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		// multistage car trips
		config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
		config.qsim().setUsingTravelTimeCheckInTeleportation( true );

		config.vehicles().setVehiclesFile("vehicle_types.xml");
		config.plans().setInputFile("ile_de_france_population_test_100p.xml.gz");
		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);

		for (StrategyConfigGroup.StrategySettings ss : config.strategy().getStrategySettings()) {
			if (ss.getStrategyName().equals("KeepLastSelected")) {
				ss.setWeight(0.80);
			}
			if (ss.getStrategyName().equals("DiscreteModeChoice")) {
				ss.setWeight(0.20);
			}
		}

		// Eqasim config definition to add the mode car_pt estimation
		EqasimConfigGroup eqasimConfig = EqasimConfigGroup.get(config);
		eqasimConfig.setEstimator("car_pt", "CarPtUtilityEstimator");
		eqasimConfig.setEstimator("pt_car", "PtCarUtilityEstimator");

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

		// Adding "car_pt interaction" to the scoring
		scoringConfig.addActivityParams(paramscarPtInterAct);
		scoringConfig.addActivityParams(paramsPtCarInterAct);

		// DMC config definition
		// Adding the mode "car_pt" and "pt_car" to CachedModes
		Collection<String> cachedModes = new HashSet<>(dmcConfig.getCachedModes());
		cachedModes.add("car_pt");
		cachedModes.add("pt_car");
		dmcConfig.setCachedModes(cachedModes);


		// Activation of constraint intermodal modes Using
		Collection<String> tourConstraints = new HashSet<>(dmcConfig.getTourConstraints());
		tourConstraints.add("IntermodalModesConstraint");
		dmcConfig.setTourConstraints(tourConstraints);

		cmd.applyConfiguration(config);
		Scenario scenario = ScenarioUtils.createScenario(config);
		IDFConfigurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);

		Controler controller = new Controler(scenario);
		IDFConfigurator.configureController(controller);

		//set Park and ride lot locations
		List<Coord> parkRideCoords;
		readParkRideCoordsFromFile readFile = new readParkRideCoordsFromFile(locationFile);
		parkRideCoords = readFile.readCoords;
		ParkRideManager parkRideManager = new ParkRideManager();
		parkRideManager.setParkRideCoords(parkRideCoords);
		Network network = scenario.getNetwork();
		parkRideManager.setNetwork(network);
//////////
		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModuleCarPt());
		controller.addOverridingModule(new IDFModeChoiceModuleCarPt(cmd, parkRideCoords, scenario.getNetwork(), scenario.getPopulation().getFactory()));
		controller.addOverridingModule(new EqasimCarPtModule(parkRideCoords));
		controller.addOverridingModule(new EqasimPtCarModule(parkRideCoords));

		controller.run();
	}

}