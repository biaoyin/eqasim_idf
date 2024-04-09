package org.eqasim.core.simulation.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eqasim.core.analysis.DistanceUnit;
import org.eqasim.core.analysis.IntermediateDMCWriter;
import org.eqasim.core.analysis.TripListener;
import org.eqasim.core.analysis.TripWriter;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.BikeUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.CarUtilityEstimator;
import org.eqasim.core.simulation.mode_choice.utilities.estimators.PtUtilityEstimator;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AnalysisOutputListener implements IterationStartsListener, IterationEndsListener, ShutdownListener {
	private static final String TRIPS_FILE_NAME = "eqasim_trips.csv";
	private static final String DMC_FILE_NAME = "dmc_travel_time";
	private final OutputDirectoryHierarchy outputDirectory;

	private final TripListener tripAnalysisListener;

	private final CarUtilityEstimator carUtilityEstimator;
	private final PtUtilityEstimator ptUtilityEstimator;
	private final BikeUtilityEstimator bikeUtilityEstimator;

	private final int tripAnalysisInterval;
	private boolean isTripAnalysisActive = false;

	private final DistanceUnit scenarioDistanceUnit;
	private final DistanceUnit analysisDistanceUnit;


	@Inject
	public AnalysisOutputListener(EqasimConfigGroup config, OutputDirectoryHierarchy outputDirectory,
								  TripListener tripListener,
								  CarUtilityEstimator carUtilityEstimator,
								  PtUtilityEstimator ptUtilityEstimator,
								  BikeUtilityEstimator bikeUtilityEstimator) {
		this.outputDirectory = outputDirectory;

		this.scenarioDistanceUnit = config.getDistanceUnit();
		this.analysisDistanceUnit = config.getTripAnalysisDistanceUnit();

		this.tripAnalysisInterval = config.getTripAnalysisInterval();
		this.tripAnalysisListener = tripListener;

		this.carUtilityEstimator = carUtilityEstimator;
		this.ptUtilityEstimator = ptUtilityEstimator;
		this.bikeUtilityEstimator = bikeUtilityEstimator;



	}


	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		boolean writeAnalysisAtAll = tripAnalysisInterval > 0;
		isTripAnalysisActive = false;

		if (writeAnalysisAtAll) {
			if (event.getIteration() % tripAnalysisInterval == 0 || event.isLastIteration()) {
				isTripAnalysisActive = true;
				event.getServices().getEvents().addHandler(tripAnalysisListener);
			}
			//BYIN  save modal travel time at the last iteration: feb 2024
			if (event.isLastIteration()) {
				carUtilityEstimator.setRecordActive();
				ptUtilityEstimator.setRecordActive();
				bikeUtilityEstimator.setRecordActive();
			}
		}

	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		try {
			if (isTripAnalysisActive) {
				event.getServices().getEvents().removeHandler(tripAnalysisListener);

				String path = outputDirectory.getIterationFilename(event.getIteration(), TRIPS_FILE_NAME);
				new TripWriter(tripAnalysisListener.getTripItems(), scenarioDistanceUnit, analysisDistanceUnit)
						.write(path);

            	//BYIN  save modal travel time feb2024
				if (event.isLastIteration()) {
					String path2 = outputDirectory.getIterationFilename(event.getIteration(), DMC_FILE_NAME);
					new IntermediateDMCWriter(carUtilityEstimator.getCarTravelTimes()).write(path2 + "_car.csv");
					new IntermediateDMCWriter(ptUtilityEstimator.getPtTravelTimes()).write(path2 + "_pt.csv");
					new IntermediateDMCWriter(bikeUtilityEstimator.getBikeTravelTimes()).write(path2 + "_bike.csv");
				}


			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		try {
			File iterationPath = new File(outputDirectory.getIterationFilename(event.getIteration(), TRIPS_FILE_NAME));
			File outputPath = new File(outputDirectory.getOutputFilename(TRIPS_FILE_NAME));
			Files.copy(iterationPath.toPath(), outputPath.toPath());
		} catch (IOException e) {
		}
	}
}
