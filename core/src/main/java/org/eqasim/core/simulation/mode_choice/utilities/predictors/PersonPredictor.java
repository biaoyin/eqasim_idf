package org.eqasim.core.simulation.mode_choice.utilities.predictors;

import java.util.List;

import org.eqasim.core.simulation.mode_choice.utilities.variables.PersonVariables;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

public class PersonPredictor extends CachedVariablePredictor<PersonVariables> {
	@Override
	public PersonVariables predict(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
		int age_a = PredictorUtils.getAge(person);
		//BYIN 2023-10 household income in dmc_1
		//double income = PredictorUtils.getHouseholdIncome(person);

		//BYIN 2025-01 equibalized income in dmc_2
		int gender = PredictorUtils.getGender(person);
		double income = PredictorUtils.getEquivalizedHouseholdIncome(person);
		return new PersonVariables(age_a, income, gender);
	}
}
