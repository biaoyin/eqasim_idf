package org.eqasim.ile_de_france.mode_choice;

import java.util.List;


import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.network.Network;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.mode_choice.AbstractEqasimExtension;
import org.eqasim.core.simulation.mode_choice.ParameterDefinition;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.eqasim.ile_de_france.mode_choice.costs.IDFCarCostModel;
import org.eqasim.ile_de_france.mode_choice.costs.IDFPtCostModel;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFCostParameters;
import org.eqasim.ile_de_france.mode_choice.parameters.IDFModeParameters;
import org.eqasim.ile_de_france.mode_choice.utilities.estimators.IDFBikeUtilityEstimator;
import org.eqasim.ile_de_france.mode_choice.utilities.estimators.IDFCarUtilityEstimator;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFPersonPredictor;
import org.eqasim.ile_de_france.mode_choice.utilities.predictors.IDFSpatialPredictor;

import org.matsim.contribs.discrete_mode_choice.components.utils.home_finder.HomeFinder;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.contribs.discrete_mode_choice.modules.config.VehicleTourConstraintConfigGroup;

import org.matsim.core.config.CommandLine;

import org.eqasim.core.components.ParkRideManager;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.BikePtUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.PtBikeUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.BikePtPredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PtBikePredictor;

import org.eqasim.core.analysis.BikePtEventHandler;

import org.eqasim.core.simulation.mode_choice.constraints.IntermodalModesConstraint;
//import org.eqasim.core.simulation.mode_choice.constraints.VehicleTourConstraintWithCar_Pt;

import java.io.File;
import java.io.IOException;

public class IDFModeChoiceModuleBikePt extends AbstractEqasimExtension {
    private final CommandLine commandLine;

    public static final String MODE_AVAILABILITY_NAME = "IDFModeAvailability";

    public static final String CAR_COST_MODEL_NAME = "IDFCarCostModel";
    public static final String PT_COST_MODEL_NAME = "IDFPtCostModel";

    public static final String CAR_ESTIMATOR_NAME = "IDFCarUtilityEstimator";
    public static final String BIKE_ESTIMATOR_NAME = "IDFBikeUtilityEstimator";

    public static final String BIKE_PT_ESTIMATOR_NAME = "BikePtUtilityEstimator";
    public static final String PT_BIKE_ESTIMATOR_NAME = "PtBikeUtilityEstimator";

    public final List<Coord> parkRideCoords;
    public final Network network;
    private final PopulationFactory populationFactory ;

    public IDFModeChoiceModuleBikePt(CommandLine commandLine, List<Coord> parkRideCoords, Network network,
                                     PopulationFactory populationFactory) {
        this.commandLine = commandLine;
        this.parkRideCoords = parkRideCoords;
        this.network = network;
        this.populationFactory = populationFactory;
    }

    @Override
    protected void installEqasimExtension() {
        bindModeAvailability(MODE_AVAILABILITY_NAME).to(IDFModeAvailabilityBikePt.class);

        bind(IDFPersonPredictor.class);

        bindCostModel(CAR_COST_MODEL_NAME).to(IDFCarCostModel.class);
        bindCostModel(PT_COST_MODEL_NAME).to(IDFPtCostModel.class);

        bindUtilityEstimator(CAR_ESTIMATOR_NAME).to(IDFCarUtilityEstimator.class);
        bindUtilityEstimator(BIKE_ESTIMATOR_NAME).to(IDFBikeUtilityEstimator.class);

        // Register the estimator
        bindUtilityEstimator(BIKE_PT_ESTIMATOR_NAME).to(BikePtUtilityEstimator.class);
        bindUtilityEstimator(PT_BIKE_ESTIMATOR_NAME).to(PtBikeUtilityEstimator.class);

        bind(IDFSpatialPredictor.class);

        // Register the predictor
        bind(ParkRideManager.class);
        bind(BikePtPredictor.class);
        bind(PtBikePredictor.class);

        bind(ModeParameters.class).to(IDFModeParameters.class);

        // Constraint register
        bindTourConstraintFactory("IntermodalModesConstraint").to(IntermodalModesConstraint.Factory.class);

        // Intermodal count: issue of excuted plan eventhandler here:  might it only record the results before tourconstraint validation.
        addEventHandlerBinding().to(BikePtEventHandler.class);
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
