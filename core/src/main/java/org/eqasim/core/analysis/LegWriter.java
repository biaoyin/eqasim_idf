package org.eqasim.core.analysis;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

public class LegWriter {
	final private Collection<LegItem> legs;
	final private String delimiter;

	final private DistanceUnit inputUnit;
	final private DistanceUnit outputUnit;

	public LegWriter(Collection<LegItem> legs, DistanceUnit inputUnit, DistanceUnit outputUnit) {
		this(legs, inputUnit, outputUnit, ";");
	}

	public LegWriter(Collection<LegItem> legs, DistanceUnit inputUnit, DistanceUnit outputUnit, String delimiter) {
		this.legs = legs;
		this.delimiter = delimiter;
		this.inputUnit = inputUnit;
		this.outputUnit = outputUnit;
	}

	public void write(String outputPath) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

		writer.write(formatHeader() + "\n");
		writer.flush();

		for (LegItem leg : legs) {
			writer.write(formatLeg(leg) + "\n");
			writer.flush();
		}

		writer.flush();
		writer.close();
	}

	private String normalizeActivityType(String activityType) {
		return activityType.replaceAll("_[0-9]+$", "");
	}

	private String formatHeader() {
		return String.join(delimiter, new String[] { //
				"person_id", //
				"person_trip_id", //
				"leg_id", //
				"origin_x", //
				"origin_y", //
				"destination_x", //
				"destination_y", "start_time", //
				"travel_time", //
				"network_distance", //
				"mode", //
				"preceedingPurpose", //
				"followingPurpose", //
				"returning", //
				"crowfly_distance" //
		});
	}

	/**
	 * Gets a factor to convert any other unit to meters
	 */
	private double getUnitFactor(DistanceUnit unit) {
		double factor = Double.NaN;

		switch (inputUnit) {
		case foot:
			factor = 0.3048;
			break;
		case kilometer:
			factor = 1e3;
			break;
		case meter:
			factor = 1.0;
			break;
		case mile:
			factor = 1609.344;
			break;
		default:
			throw new IllegalStateException("Unknown input unit");
		}

		return factor;
	}

	private String formatLeg(LegItem leg) {
		double inputFactor = getUnitFactor(inputUnit);
		double outputFactor = 1.0 / getUnitFactor(outputUnit);

		return String.join(delimiter, new String[] { //
				leg.personId.toString(), //
				String.valueOf(leg.tripId), //
				String.valueOf(leg.legId), //
				String.valueOf(leg.origin.getX()), //
				String.valueOf(leg.origin.getY()), //
				String.valueOf(leg.destination.getX()), //
				String.valueOf(leg.destination.getY()), //
				String.valueOf(leg.startTime), //
				String.valueOf(leg.travelTime), //
				String.valueOf(leg.networkDistance * inputFactor * outputFactor), //
				String.valueOf(leg.mode), //
				normalizeActivityType(String.valueOf(leg.preceedingPurpose)), //
				normalizeActivityType(String.valueOf(leg.followingPurpose)), //
				String.valueOf(leg.returning), //
				String.valueOf(leg.crowflyDistance * inputFactor * outputFactor) //
		});
	}
}