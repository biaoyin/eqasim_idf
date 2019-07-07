package org.eqasim.ile_de_france.mode_choice.utilities.estimators;

import org.eqasim.ile_de_france.mode_choice.parameters.ModeChoiceParameters;
import org.eqasim.ile_de_france.mode_choice.utilities.variables.WalkVariables;

public class WalkEstimator {
	private final ModeChoiceParameters parameters;

	public WalkEstimator(ModeChoiceParameters parameters) {
		this.parameters = parameters;
	}

	public double estimateUtility(WalkVariables variables) {
		double utility = 0.0;

		utility += parameters.walk.alpha;
		utility += parameters.walk.betaTravelTime * variables.travelTime_min;

		return utility;
	}
}
