package com.joshcummings.codeplay.terracotta.service.passwords;

import java.util.regex.Pattern;

public class WeakPasswordEntropyEvaluator implements PasswordEntropyEvaluator {
	private static final String SPECIAL_CHARACTERS = "!@#^";

	private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
	private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
	private static final Pattern HAS_NUMBER = Pattern.compile("[0-9]");
	private static final Pattern HAS_SPECIAL = Pattern.compile("[" + SPECIAL_CHARACTERS + "]");
	private static final Pattern HAS_ONLY = Pattern.compile("[a-zA-Z0-9" + SPECIAL_CHARACTERS + "]{6,20}");

	@Override
	public Evaluation evaluate(String password) {
		boolean matches =
				HAS_LOWER.matcher(password).find() &&
				HAS_UPPER.matcher(password).find() &&
				HAS_NUMBER.matcher(password).find() &&
				HAS_SPECIAL.matcher(password).find() &&
				HAS_ONLY.matcher(password).matches();

		if ( !matches ) {
			return Evaluation.failure(
					"Make sure it has 6 to 20 characters",
					"And a lower-case letter, upper-case letter, and number",
					"And at least one of !@#^");
		} else {
			return Evaluation.success();
		}
	}
}
