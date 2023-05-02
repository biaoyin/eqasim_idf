package org.eqasim.ile_de_france;


import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModule;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModule;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.VehiclesSource;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;

import java.util.*;

public class RunSimulation_BaseCase_test_carInternal {

	static public void main(String[] args) throws ConfigurationException {
		args = new String[] {"--config-path", "ile_de_france/scenarios/ile-de-france-1pm/base_case/ile_de_france_config_test.xml"};

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();
		IDFConfigurator configurator = new IDFConfigurator();
		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), configurator.getConfigGroups());

		//modify some parameters in config file
		config.controler().setLastIteration(100);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		// multistage car trips
		config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
		config.qsim().setUsingTravelTimeCheckInTeleportation( true );

		//////////////////////////////basic strategy setting////////////////////////////
		config.vehicles().setVehiclesFile("./vehicle_types.xml");
		config.network().setInputFile("ile_de_france_network_carInternal.xml.gz");
		config.plans().setInputFile("ile_de_france_population_test_200p_allcarInternal.xml");
		config.qsim().setVehiclesSource(VehiclesSource.modeVehicleTypesFromVehiclesData);  //original value is defaultVehicle
		config.qsim().setMainModes(Arrays.asList("car","carInternal"));

		//
		PlansCalcRouteConfigGroup routingConfig = config.plansCalcRoute();
		routingConfig.setNetworkModes(Arrays.asList("car", "car_passenger", "truck", "carInternal"));
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
		// Scoring config
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore();
		PlanCalcScoreConfigGroup.ModeParams carInternalParams = new PlanCalcScoreConfigGroup.ModeParams("carInternal");
		carInternalParams.setMarginalUtilityOfTraveling(-1.0);
		scoringConfig.addModeParams(carInternalParams);

		// consider carInternal as a special car, using the same parameters of car and the same others
		EqasimConfigGroup eqasimConfig = EqasimConfigGroup.get(config);
		eqasimConfig.setCostModel("carInternal", IDFModeChoiceModule.CAR_COST_MODEL_NAME);
		eqasimConfig.setEstimator("carInternal", IDFModeChoiceModule.CAR_ESTIMATOR_NAME);

		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		Collection<String> cachedModes = new HashSet<>(dmcConfig.getCachedModes());
		cachedModes.add("carInternal");
		dmcConfig.setCachedModes(cachedModes);
		dmcConfig.getVehicleTourConstraintConfig().setRestrictedModes(Arrays.asList("car","carInternal", "bike"));

		//
		cmd.applyConfiguration(config);
		Scenario scenario = prepareScenario( config, configurator );
		Controler controller = new Controler(scenario);

		configurator.configureController(controller);
		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModule());
		controller.addOverridingModule(new IDFModeChoiceModule(cmd));

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
/*		for (Person person : scenario.getPopulation().getPersons().values()) {
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
		}*/
		return scenario;
	}
}