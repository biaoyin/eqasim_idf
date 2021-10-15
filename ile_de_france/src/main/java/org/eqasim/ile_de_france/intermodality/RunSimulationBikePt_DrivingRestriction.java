package org.eqasim.ile_de_france.intermodality;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eqasim.core.components.bike_pt.routing.EqasimBikePtModule;
import org.eqasim.core.components.bike_pt.routing.EqasimPtBikeModule;
import org.eqasim.core.components.bike_pt.routing.ParkRideManager;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModuleBikePt;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModule;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModuleBikePt;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
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
import org.matsim.core.population.algorithms.PermissibleModesCalculator;
import org.matsim.core.population.algorithms.PermissibleModesCalculatorImpl;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RunSimulationBikePt_DrivingRestriction {
	static public void main(String[] args) throws ConfigurationException, IOException {
		args = new String[] {"--config-path", "ile_de_france/scenarios/saintdenis-cut-10pct/driving_restriction/big-zone-ex2/SaintDenis_config_carInternal.xml"};

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), IDFConfigurator.getConfigGroups());
		//modify some parameters in config file
		config.controler().setLastIteration(60);
		config.strategy().setMaxAgentPlanMemorySize(5);
		config.strategy().setPlanSelectorForRemoval("WorstPlanSelector");
		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		dmcConfig.setEnforceSinglePlan(false);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		//1) driving restriction setting
		config.vehicles().setVehiclesFile("vehicle_types.xml");
		config.network().setInputFile("SaintDenis_network_carInternal.xml.gz");
		config.plans().setInputFile("SaintDenis_population_carInternal_residentOnly.xml.gz");
		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);  //original value is defaultVehicle
		//BYIN: qsim visulasation (can be shown in via) : can also put this setting in RunAdaptConfig_CarInternal.java
		config.qsim().setMainModes(Arrays.asList("car","car_passenger","carInternal"));// corresponding adds in emissionRunner
		//add parameters of the new mode and related: discrete mode choice in eqasim
		// Scoring config
		PlanCalcScoreConfigGroup scoringConfig_DRZ = config.planCalcScore();
		ModeParams carInternalParams = new ModeParams("carInternal");
		scoringConfig_DRZ.addModeParams(carInternalParams);
		// consider carInternal as a special car, using the same parameters of car and the same others
		EqasimConfigGroup eqasimConfig_DRZ = EqasimConfigGroup.get(config);
		eqasimConfig_DRZ.setCostModel("carInternal", IDFModeChoiceModule.CAR_COST_MODEL_NAME);
		eqasimConfig_DRZ.setEstimator("carInternal", IDFModeChoiceModule.CAR_ESTIMATOR_NAME);

		// Intermodality setting
		//set Park and ride lot locations
//		String locationFile = "ile_de_france/scenarios/saint-denis-bike-location_1.csv";
		String locationFile = "ile_de_france/scenarios/saint-denis-bike-location_2.csv";
		List<Coord> parkRideCoords;
		readParkRideCoordsFromFile readFile = new readParkRideCoordsFromFile(locationFile);
		parkRideCoords = readFile.readCoords;

        ParkRideManager parkRideManager = new ParkRideManager();
		parkRideManager.setParkRideCoords(parkRideCoords);

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
		}
*/
//
		cmd.applyConfiguration(config);
		
		Scenario scenario = prepareScenario( config );
		Controler controller = new Controler(scenario);
		IDFConfigurator.configureController(controller);

		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModuleBikePt());
		controller.addOverridingModule(new IDFModeChoiceModuleBikePt(cmd, parkRideCoords, scenario.getNetwork(), scenario.getPopulation().getFactory()));
		controller.addOverridingModule(new EqasimBikePtModule(parkRideCoords));
		controller.addOverridingModule(new EqasimPtBikeModule(parkRideCoords));

		// BYIN: Add a new plan strategy module with mode choice: considering the mode carInternal for subpopulation: residents
		controller.addOverridingModule( new AbstractModule() {
			@Override
			public void install() {
				bind(PermissibleModesCalculator.class).to(PermissibleModesCalculatorImpl.class);// for subTourModeChoice in v13
				// define second mode choice strategy:
				this.addPlanStrategyBinding("DiscreteModeChoiceInternal").toProvider(new Provider<PlanStrategy>(){
					@Inject
					private GlobalConfigGroup globalConfigGroup;
					@Inject
					private Provider<TripRouter> tripRouterProvider;
					@Inject
					private ActivityFacilities activityFacilities;
					@Inject
					private Provider<DiscreteModeChoiceModel> modeChoiceModelProvider;
					@Inject
					private Provider<TripListConverter> tripListConverterProvider;
					@Inject
					private PopulationFactory populationFactory;
					@Inject
					private PermissibleModesCalculator permissibleModesCalculator;

					@Override  //here is the option of discrete mode choice
					public PlanStrategy get() {

						DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
								.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);

						Collection<String> cachedModes = new HashSet<>(dmcConfig.getCachedModes());
						cachedModes.add("carInternal");
						dmcConfig.setCachedModes(cachedModes);
						dmcConfig.getVehicleTourConstraintConfig().setRestrictedModes(Arrays.asList("carInternal", "bike"));

						PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new RandomPlanSelector<>());
						builder.addStrategyModule(new DiscreteModeChoiceReplanningModule(globalConfigGroup, modeChoiceModelProvider,
								tripListConverterProvider, populationFactory));
						if (dmcConfig.getPerformReroute()) {
							builder.addStrategyModule(new ReRoute(activityFacilities, tripRouterProvider, globalConfigGroup));
						} else {
							builder.addStrategyModule(new CheckConsistentRoutingReplanningModule(globalConfigGroup));
						}

						return builder.build();
					}

					// here is the option of subTourModeChoice.
					/*public PlanStrategy get() {

						SubtourModeChoiceConfigGroup modeChoiceConfig = new SubtourModeChoiceConfigGroup() ;
						modeChoiceConfig.setModes( new String[] {TransportMode.walk,TransportMode.pt,"carInternal","car_passenger","bike"} );
						modeChoiceConfig.setChainBasedModes( new String[] {"carInternal","bike"});

						PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new RandomPlanSelector<>());
						builder.addStrategyModule(new SubtourModeChoice(globalConfigGroup, modeChoiceConfig, permissibleModesCalculator));
						builder.addStrategyModule(new ReRoute(activityFacilities, tripRouterProvider, globalConfigGroup));

						return builder.build();
					}*/
				} ) ;
			}
		} ) ;

		controller.run();
	}




	private static Scenario prepareScenario(Config config) {
		final Scenario scenario = ScenarioUtils.createScenario( config );

		// Add carInternal vehicle type
		VehiclesFactory vehiclesFactory = scenario.getVehicles().getFactory();
		VehicleType carInternalVehicleType = vehiclesFactory.createVehicleType(Id.create("carInternal", VehicleType.class));
		scenario.getVehicles().addVehicleType(carInternalVehicleType);

		IDFConfigurator.configureScenario(scenario);
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