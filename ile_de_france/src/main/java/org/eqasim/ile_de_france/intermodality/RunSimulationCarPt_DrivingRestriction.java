package org.eqasim.ile_de_france.intermodality;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.components.car_pt.routing.EqasimCarPtModule;
import org.eqasim.core.components.car_pt.routing.EqasimPtCarModule;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModuleCarPt;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModule;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModuleCarPt;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceModel;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.contribs.discrete_mode_choice.replanning.CheckConsistentRoutingReplanningModule;
import org.matsim.contribs.discrete_mode_choice.replanning.DiscreteModeChoiceReplanningModule;
import org.matsim.contribs.discrete_mode_choice.replanning.TripListConverter;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.*;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.algorithms.PermissibleModesCalculator;
import org.matsim.core.population.algorithms.PermissibleModesCalculatorImpl;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.modules.SubtourModeChoice;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;

import java.io.IOException;
import java.util.*;

public class RunSimulationCarPt_DrivingRestriction {
	static String outputPath = "E:/lvmt_BY/simulation_output/eqasim_idf/ile-de-france-1pct/PTCar_DRZ_rer_train";

	static public void main(String[] args) throws ConfigurationException, IOException {
		args = new String[] {"--config-path", "ile_de_france/scenarios/ile-de-france-1pct/driving_restriction/ile_de_france_config_carInternal.xml"};
		String locationFile = "ile_de_france/scenarios/parcs-relais-idf_rer_train.csv";

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();
		IDFConfigurator configurator = new IDFConfigurator();
		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), configurator.getConfigGroups());

		//modify some parameters in config file
		config.controler().setLastIteration(60);
		/*config.controler().setFirstIteration(60);
		config.controler().setLastIteration(100);*/

		config.controler().setOutputDirectory(outputPath);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		// multi-stage car trips
		config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
		config.qsim().setUsingTravelTimeCheckInTeleportation( true );

		//1) driving restriction setting
		config.network().setInputFile("ile_de_france_network_carInternal.xml.gz");
		config.plans().setInputFile("ile_de_france_population_carInternal_residentOnly.xml.gz");  //ile_de_france_population_carInternal_residentOnly.xml.gz
		config.vehicles().setVehiclesFile("vehicle_types.xml");
		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);  //original value is defaultVehicle
		//BYIN: qsim visulasation (can be shown in via) : can also put this setting in RunAdaptConfig_CarInternal.java
		config.qsim().setMainModes(Arrays.asList("car","carInternal"));//attention: car_passenger is excluded, corresponding adds in emissionRunner

		// add carInternal to traveltimeCalculator
		Set<String> analyzedModes = new HashSet<> (config.travelTimeCalculator().getAnalyzedModes());
		analyzedModes.add("carInternal");
		config.travelTimeCalculator().setAnalyzedModes(analyzedModes);

		for (StrategyConfigGroup.StrategySettings ss : config.strategy().getStrategySettings()) {
			if (ss.getStrategyName().equals("KeepLastSelected")) {
				ss.setWeight(0.95);
			}
			if (ss.getStrategyName().equals("DiscreteModeChoice")) {
				ss.setWeight(0.05);
			}
		}
		//add parameters of the new mode and related: discrete mode choice in eqasim
		// Scoring config
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore();
		ModeParams carInternalParams = new ModeParams("carInternal");
		scoringConfig.addModeParams(carInternalParams);

		// consider carInternal as a special car, using the same parameters of car and the same others
		EqasimConfigGroup eqasimConfig = EqasimConfigGroup.get(config);
		eqasimConfig.setCostModel("carInternal", IDFModeChoiceModule.CAR_COST_MODEL_NAME);
		eqasimConfig.setEstimator("carInternal", IDFModeChoiceModule.CAR_ESTIMATOR_NAME);

		// Eqasim config definition to add the mode car_pt estimation
		eqasimConfig.setEstimator("car_pt", "CarPtUtilityEstimator");
		eqasimConfig.setEstimator("pt_car", "PtCarUtilityEstimator");

		// Scoring config definition to add the mode car_pt parameters
		//PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore();
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
		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		Collection<String> cachedModes = new HashSet<>(dmcConfig.getCachedModes());
		cachedModes.add("car_pt");
		cachedModes.add("pt_car");
		cachedModes.add("carInternal");
		dmcConfig.setCachedModes(cachedModes);

		// Activation of constraint intermodal modes Using
		Collection<String> tourConstraints = new HashSet<>(dmcConfig.getTourConstraints());
		tourConstraints.add("IntermodalModesConstraint");
		dmcConfig.setTourConstraints(tourConstraints);

		dmcConfig.getVehicleTourConstraintConfig().setRestrictedModes(Arrays.asList("car", "carInternal", "bike"));

		cmd.applyConfiguration(config);
		Scenario scenario = prepareScenario( config, configurator );
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
		controller.addOverridingModule(new IDFModeChoiceModuleCarPt(cmd, parkRideCoords, scenario.getNetwork(), scenario.getPopulation().getFactory()));
		controller.addOverridingModule(new EqasimCarPtModule(parkRideCoords));
		controller.addOverridingModule(new EqasimPtCarModule(parkRideCoords));

		controller.run();
	}

	private static Scenario prepareScenario(Config config, IDFConfigurator configurator) {
		final Scenario scenario = ScenarioUtils.createScenario( config );

		// Add carInternal vehicle type
		VehiclesFactory vehiclesFactory = scenario.getVehicles().getFactory();
		VehicleType carInternalVehicleType = vehiclesFactory.createVehicleType(Id.create("carInternal", VehicleType.class));
		scenario.getVehicles().addVehicleType(carInternalVehicleType);

		configurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);

		// Delete all initial links and routes in the plan
		for (Person person : scenario.getPopulation().getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				for (PlanElement pe : plan.getPlanElements()) {
					if (pe instanceof Activity) {
						((Activity) pe).setLinkId(null);
					} else if (pe instanceof Leg) {
						((Leg) pe).setRoute(null);
					} else {
						throw new RuntimeException("Plan element can either be activity or leg.");
					}
				}
			}
		}

		return scenario;
	}

}