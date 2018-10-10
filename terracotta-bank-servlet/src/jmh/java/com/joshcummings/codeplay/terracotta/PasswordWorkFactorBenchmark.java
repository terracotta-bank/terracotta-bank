package com.joshcummings.codeplay.terracotta;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 20, time = 1)
public class PasswordWorkFactorBenchmark {
	private ConfigurableApplicationContext context;
	private UserService userService = new UserService();

	@Setup(value = Level.Trial)
	public void initializeContext() {
		context = SpringApplication.run(Mainer.class);
		this.userService = context.getBean(UserService.class);
	}

	@TearDown
	public void closeContext() {
		context.close();
	}

	@Benchmark
	public User retrieveUser() {
		return this.userService.findByUsername("josh.cummings");
	}

	@Benchmark
	public Object retrieveUserAndVerifyPassword() {
		return this.userService.findByUsernameAndPassword("josh.cummings", "j0sh");
	}

	@Test
	public void testPasswordHashHasSufficientWorkFactor() throws Exception {
		double minimumWorkFactorRatio = Math.pow(2, 10);
		Collection<RunResult> results = run();

		double retrieveUserScore =
				getScore(results, "retrieveUser");
		double retrieveUserAndVerifyPasswordScore =
				getScore(results, "retrieveUserAndVerifyPassword");

		double workFactorRatio = retrieveUserAndVerifyPasswordScore / retrieveUserScore;
		Assert.assertTrue(workFactorRatio > minimumWorkFactorRatio);
	}

	private Collection<RunResult> run() throws Exception {
		Options opt = new OptionsBuilder()
				.include(PasswordWorkFactorBenchmark.class.getCanonicalName())
				.jvmArgs("-Xmx512M")
				.forks(1)
				.build();

		return new Runner(opt).run();
	}

	private double getScore(Collection<RunResult> results, String benchmarkName) {
		return results.stream()
				.filter(result -> benchmarkName(result).equals(benchmarkName))
				.map(result -> result.getPrimaryResult().getScore())
				.findFirst().orElse(0d);
	}

	private String benchmarkName(RunResult result) {
		return result.getParams().getBenchmark().substring(this.getClass().getSimpleName().length() + 1);
	}
}
