package org.eqasim.ile_de_france.mode_choice;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.eqasim.core.analysis.CarPtEventHandler;
import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.mode_choice.AbstractEqasimExtension;
import org.eqasim.core.simulation.mode_choice.ParameterDefinition;
import org.eqasim.core.simulation.mode_choice.constraints.IntermodalModesConstraint;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.BikeUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.CarPassengerUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.CarPtUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.PtCarUtilityEstimator;
import org.eqasim.ile_de_france.mode_choice.costs.IDFCarCostModel;
import org.eqasim.ile_de_france.mode_choice.costs.IDFPtCostModel;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFCostParameters;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFModeParameters;
import org.eqasim.ile_de_france.mode_choice.utilities.estimators.*;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFPersonPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFPtPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFCarRoadPricingPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFSpatialPredictor;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contribs.discrete_mode_choice.components.utils.home_finder.HomeFinder;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.contribs.discrete_mode_choice.modules.config.VehicleTourConstraintConfigGroup;
import org.matsim.core.config.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class IDFModeChoiceModuleCarPtRoadPricing extends AbstractEqasimExtension {
    private final CommandLine commandLine;

    public static final String MODE_AVAILABILITY_NAME = "IDFModeAvailability";

    public static final String CAR_COST_MODEL_NAME = "IDFCarCostModel";
    public static final String PT_COST_MODEL_NAME = "IDFPtCostModel";

    public static final String CAR_ESTIMATOR_NAME = "IDFCarUtilityEstimator";
    public static final String BIKE_ESTIMATOR_NAME = "IDFBikeUtilityEstimator";
    public static final String PT_ESTIMATOR_NAME = "IDFPtUtilityEstimator";

    public static final String CAR_PT_ESTIMATOR_NAME = "IDFCarPtUtilityEstimator";
    public static final String PT_CAR_ESTIMATOR_NAME = "IDFPtCarUtilityEstimator";

    public final List<Coord> parkRideCoords;
    public final Network network;
    private final PopulationFactory populationFactory ;

    public IDFModeChoiceModuleCarPtRoadPricing(CommandLine commandLine, List<Coord> parkRideCoords, Network network,
                                               PopulationFactory populationFactory) {
        this.commandLine = commandLine;
        this.parkRideCoords = parkRideCoords;
        this.network = network;
        this.populationFactory = populationFactory;
    }

    @Override
    protected void installEqasimExtension() {
        //IDF scenario
        bindModeAvailability(MODE_AVAILABILITY_NAME).to(IDFModeAvailabilityCarPt.class);
        bind(IDFPersonPredictor.class);
        bind(IDFPtPredictor.class);

        // road pricing case
        bind(IDFCarRoadPricingPredictor.class);

        bindCostModel(CAR_COST_MODEL_NAME).to(IDFCarCostModel.class);
        bindCostModel(PT_COST_MODEL_NAME).to(IDFPtCostModel.class);

        //road pricing case: BYIN 2023-04-28
        bindUtilityEstimator(CAR_ESTIMATOR_NAME).to(IDFCarUtilityEstimatorWithRoadPricing.class);
        bindUtilityEstimator(BIKE_ESTIMATOR_NAME).to(IDFBikeUtilityEstimator.class);
        // BYIN 2025-01: add the IDFPtUtilityEstimator.class with new added OnlyBus utility
        bindUtilityEstimator(PT_ESTIMATOR_NAME).to(IDFPtUtilityEstimator.class);
        // BYIN 2025-01: add the IDFCarPtUtilityEstimator.class with new added OnlyBus utility
        bindUtilityEstimator(CAR_PT_ESTIMATOR_NAME).to(IDFCarPtUtilityEstimator.class);
        // BYIN 2025-01: add the IDFPtCarUtilityEstimator.class with new added OnlyBus utility
        bindUtilityEstimator(PT_CAR_ESTIMATOR_NAME).to(IDFPtCarUtilityEstimator.class);

        bind(IDFSpatialPredictor.class);
        bind(ModeParameters.class).to(IDFModeParameters.class);
        // Constraint register
        bindTourConstraintFactory("IntermodalModesConstraint").to(IntermodalModesConstraint.Factory.class);
        // Intermodal count: issue of excuted plan eventhandler here: might it only record the results before tourconstraint validation.
        addEventHandlerBinding().to(CarPtEventHandler.class);
    }


    @Provides
    @Singleton
    public IDFModeParameters provideModeChoiceParameters(EqasimConfigGroup config)
            throws IOException, CommandLine.ConfigurationException {
        IDFModeParameters parameters = IDFModeParameters.buildDefault();

        if (config.getModeParametersPath() != null) {
            ParameterDefinition.applyFile(new File(config.getModeParametersPath()), parameters);
        }

        ParameterDefinition.applyCommandLine("mode-choice-parameter", commandLine, parameters);
        return parameters;
    }

    @Provides
    @Singleton
    public IDFCostParameters provideCostParameters(EqasimConfigGroup config) {
        IDFCostParameters parameters = IDFCostParameters.buildDefault();

        if (config.getCostParametersPath() != null) {
            ParameterDefinition.applyFile(new File(config.getCostParametersPath()), parameters);
        }

        ParameterDefinition.applyCommandLine("cost-parameter", commandLine, parameters);
        return parameters;
    }

    @Provides
    @Singleton
    public IntermodalModesConstraint.Factory provideIntermodalModesConstraintFactory(
            DiscreteModeChoiceConfigGroup dmcConfig, HomeFinder homeFinder) {
        VehicleTourConstraintConfigGroup config = dmcConfig.getVehicleTourConstraintConfig();
        return new IntermodalModesConstraint.Factory(config.getRestrictedModes(), homeFinder, parkRideCoords, network);
    }
}
