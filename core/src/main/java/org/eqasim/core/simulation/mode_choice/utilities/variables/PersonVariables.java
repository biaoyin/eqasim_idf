package org.eqasim.core.simulation.mode_choice.utilities.variables;

public class PersonVariables implements BaseVariables {
	public final int age_a;
	public final double income;

	public PersonVariables(int age_a, double income) {
		this.age_a = age_a;
		this.income = income;
	}
}
