package org.eqasim.ile_de_france.flow.calibration;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;

public class CapacityAdjustment {
	private final double majorFactor;
	private final double intermediateFactor;
	private final double minorFactor;
	private final double linkFactor;

	public CapacityAdjustment(CommandLine cmd) throws NumberFormatException, ConfigurationException {
		double majorFactor = 1.0;
		double intermediateFactor = 1.0;
		double minorFactor = 1.0;
		double linkFactor = 1.0;

		for (String option : cmd.getAvailableOptions()) {
			if (option.startsWith("capacity:")) {
				String slot = option.replace("capacity:", "");

				switch (slot) {
				case "major":
					majorFactor = Double.parseDouble(cmd.getOptionStrict("capacity:major"));
					break;
				case "intermediate":
					intermediateFactor = Double.parseDouble(cmd.getOptionStrict("intermediate"));
					break;
				case "minor":
					minorFactor = Double.parseDouble(cmd.getOptionStrict("minor"));
					break;
				case "link":
					linkFactor = Double.parseDouble(cmd.getOptionStrict("link"));
					break;
				default:
					throw new IllegalStateException("Unknown slot: " + slot);
				}
			}
		}

		this.majorFactor = majorFactor;
		this.intermediateFactor = intermediateFactor;
		this.minorFactor = minorFactor;
		this.linkFactor = linkFactor;
	}

	public void apply(Config config, Network network) {
		for (Link link : network.getLinks().values()) {
			String osmType = (String) link.getAttributes().getAttribute("osm:way:highway");

			if (osmType != null) {
				if (osmType.contains("motorway") || osmType.contains("trunk")) {
					link.setCapacity(link.getCapacity() * majorFactor);
				} else if (osmType.contains("primary") || osmType.contains("secondary")) {
					link.setCapacity(link.getCapacity() * intermediateFactor);
				} else {
					link.setCapacity(link.getCapacity() * minorFactor);
				}

				if (osmType.contains("_link")) {
					link.setCapacity(link.getCapacity() * linkFactor);
				}
			}
		}
	}
}