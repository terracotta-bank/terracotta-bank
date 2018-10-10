/*
 * Copyright 2015-2018 Josh Cummings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshcummings.codeplay.terracotta;

import com.joshcummings.codeplay.terracotta.testng.HttpSupport;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by joshcummings on 2/17/18.
 */
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 20, time = 1)
public class LoginEnumerationBenchmark {
	HttpSupport http = new HttpSupport();

	@Param({ "admin", "admidpifns" }) String username;

	@Benchmark
	public HttpEntity attemptLogin() throws IOException {
		try ( CloseableHttpResponse response =
			http.post("/login", new BasicNameValuePair("username", username), new BasicNameValuePair("password", "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdc")); ) {
			return response.getEntity();
		}
	}

	@Benchmark
	public HttpEntity baseline() throws IOException {
		try ( CloseableHttpResponse response =
				  http.getForEntity("/index.jsp"); ) {
			return response.getEntity();
		}
	}

	@Test
	public void runBenchmark() throws RunnerException {
		Options opt = new OptionsBuilder()
			.include(LoginEnumerationBenchmark.class.getCanonicalName())
			.jvmArgs("-Xmx512M")
			.forks(1)
			.build();

		Collection<RunResult> results = new Runner(opt).run();

		List<Double> scoreForOtherUsernames = new ArrayList<>();

		Map<String, Double> scoresByBenchmarkParam = new HashMap<>();

		for ( RunResult result : results ) {
			scoresByBenchmarkParam.put(result.getParams().getBenchmark() + ":" + result.getParams().getParam("username"), result.getPrimaryResult().getScore());
		}

		double scoreForExistingUsername =
			scoresByBenchmarkParam
				.get(LoginEnumerationBenchmark.class.getCanonicalName() + ".attemptLogin:admin") -
			scoresByBenchmarkParam
				.get(LoginEnumerationBenchmark.class.getCanonicalName() + ".baseline:admin");

		double scoreForFakeUsername =
			scoresByBenchmarkParam
				.get(LoginEnumerationBenchmark.class.getCanonicalName() + ".attemptLogin:admidpifns") -
				scoresByBenchmarkParam
					.get(LoginEnumerationBenchmark.class.getCanonicalName() + ".baseline:admidpifns");


		Assert.assertTrue(Math.abs(1 - scoreForFakeUsername / scoreForExistingUsername) < .2);
	}
}
