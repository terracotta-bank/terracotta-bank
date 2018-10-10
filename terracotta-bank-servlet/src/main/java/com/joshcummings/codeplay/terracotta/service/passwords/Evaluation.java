package com.joshcummings.codeplay.terracotta.service.passwords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Evaluation {
	private static final Evaluation SUCCESS = new Evaluation(Collections.emptyList());

	private final List<String> details;

	private Evaluation(List<String> details) {
		this.details = new ArrayList<>(details);
	}

	public boolean isSuccess() {
		return this.details.isEmpty();
	}

	public List<String> getDetails() {
		return this.details;
	}

	public static Evaluation success() {
		return SUCCESS;
	}

	public static Evaluation failure(String... details) {
		return failure(Arrays.asList(details));
	}

	public static Evaluation failure(List<String> details) {
		if ( details.isEmpty() ) {
			return SUCCESS;
		} else {
			return new Evaluation(details);
		}
	}
}
