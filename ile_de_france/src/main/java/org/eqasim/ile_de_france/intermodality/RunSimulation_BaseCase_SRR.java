package org.eqasim.ile_de_france.intermodality;


import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import org.eqasim.core.components.EqasimComponentsModule;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModule;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModule;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.VehiclesSource;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Arrays;


// to run saint-denis_1pm case, we can use the network of cut scenario of 10pct, which has more links
public class RunSimulation_BaseCase_SRR {
//	private static final String scenarioID = "saint_denis_1pm";
//	private static final String networkID = "sd_shp_ex3"; // ex1: extended LP; ex2: box covers the SD;  ex3: box covers the extended LP;

	static public void main(String[] args) throws ConfigurationException {
		args = new String[] {"--config-path", "ile_de_france/scenarios/saintdenis-cut-1pm/SaintDenis_config.xml"};

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), IDFConfigurator.getConfigGroups());

		//modify some parameters in config file
		config.controler().setLastIteration(1);
		config.vehicles().setVehiclesFile("vehicle_types.xml");

		////////////////////////////////////////////////////////////////////////////////
		//new add: modify strategy to compare with driving restriction scenario
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
//		StrategyConfigGroup.StrategySettings strategySettings_route = new StrategyConfigGroup.StrategySettings();
//		strategySettings_route.setStrategyName("ReRoute");
//		strategySettings_route.setWeight(0.05);

		StrategyConfigGroup strategyConfig = config.strategy();
		strategyConfig.addStrategySettings(strategySettings_mode);
//		strategyConfig.addStrategySettings(strategySettings_route);

		strategyConfig.setFractionOfIterationsToDisableInnovation(0.8);

		PlansCalcRouteConfigGroup routingConfig = config.plansCalcRoute();
		routingConfig.setNetworkModes(Arrays.asList("car", "car_passenger", "truck"));

		//// intermodality configuration
//		config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
//		SwissRailRaptorConfigGroup srrConfig =  (SwissRailRaptorConfigGroup) config.getModules().get(SwissRailRaptorConfigGroup.GROUP);
//		srrConfig.setUseIntermodalAccessEgress(true);
//		srrConfig.setIntermodalAccessEgressModeSelection(IntermodalAccessEgressModeSelection.CalcLeastCostModePerStop);
//		IntermodalAccessEgressParameterSet paramSetWalk = new IntermodalAccessEgressParameterSet();
//		paramSetWalk.setMode(TransportMode.walk);
//		paramSetWalk.setInitialSearchRadius(50);
//		paramSetWalk.setMaxRadius(1000);
//		paramSetWalk.setSearchExtensionRadius(100);
//		srrConfig.addIntermodalAccessEgress(paramSetWalk);

		/////////////////////////////////////////////////////////////////
		config.qsim().setVehiclesSource(VehiclesSource.modeVehicleTypesFromVehiclesData);  //original value is defaultVehicle
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

		cmd.applyConfiguration(config);


		Scenario scenario = ScenarioUtils.createScenario(config);
		IDFConfigurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);

		int originalNumberOfLinks = scenario.getNetwork().getLinks().size();
		System.out.println (originalNumberOfLinks);

		Controler controller = new Controler(scenario);
		IDFConfigurator.configureController(controller);
		//
		// add SBB router
		// To use the deterministic pt simulation (Part 1 of 2):
//		controller.addOverridingModule(new SBBTransitModule());

//		controller.addOverridingModule(new SwissRailRaptorModule());

//		controller.addOverridingModule(new AbstractModule() {
//			@Override
//			public void install() {
////				install(Modules.override(new SwissRailRaptorModule()).with(new EqasimComponentsModule()));
////				install(new SwissRailRaptorModule());
//
////				bind(MainModeIdentifier.class).to(IDFEqasimMainModeIdentifier.class);
//				bind(AnalysisMainModeIdentifier.class).to(IDFEqasimMainModeIdentifier.class);
////				bind(IDFEqasimMainModeIdentifier.class);
//			}
//		});
//		https://stackoverflow.com/questions/483087/overriding-binding-in-guice
		Guice.createInjector(Modules.override(new SwissRailRaptorModule()).with(new EqasimComponentsModule()));


//		// To use the deterministic pt simulation (Part 2 of 2):
//		controller.configureQSimComponents(components -> {
//			SBBTransitEngineQSimModule.configure(components);
//			// if you have other extensions that provide QSim components, call their configure-method here
//		});

		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModule());
		controller.addOverridingModule(new IDFModeChoiceModule(cmd));


		controller.run();
	}
}