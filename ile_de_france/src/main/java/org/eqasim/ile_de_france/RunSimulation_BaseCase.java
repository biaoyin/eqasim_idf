package org.eqasim.ile_de_france;


import ch.sbb.matsim.routing.pt.raptor.RaptorIntermodalAccessEgress;
import com.google.inject.Guice;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.eqasim.core.components.EqasimComponentsModule;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModule;
import org.eqasim.ile_de_france.IDFConfigurator;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModule;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.VehiclesSource;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.router.AnalysisMainModeIdentifier;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.scenario.ScenarioUtils;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressModeSelection;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import ch.sbb.matsim.routing.pt.raptor.IntermodalAwareRouterModeIdentifier;
import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;
import org.eqasim.core.components.EqasimMainModeIdentifier;
import org.matsim.extensions.pt.fare.intermodalTripFareCompensator.IntermodalTripFareCompensatorsModule;
import org.matsim.extensions.pt.routing.EnhancedRaptorIntermodalAccessEgress;
import org.matsim.extensions.pt.routing.ptRoutingModes.PtIntermodalRoutingModesModule;

import java.util.Arrays;

// to run saint-denis_1pm case, we can use the network of cut scenario of 10pct, which has more links
public class RunSimulation_BaseCase {

	static public void main(String[] args) throws ConfigurationException {
		args = new String[] {"--config-path", "ile_de_france/scenarios/saintdenis-cut-10pct/base_case/big-zone-ex2/SaintDenis_config.xml"};

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), IDFConfigurator.getConfigGroups());
		//modify some parameters in config file
		config.controler().setLastIteration(60);
		config.vehicles().setVehiclesFile("vehicle_types.xml");

		//////////////////////////////basic strategy setting////////////////////////////
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
		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModule());
		controller.addOverridingModule(new IDFModeChoiceModule(cmd));

		controller.run();
	}
}