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
package com.joshcummings.codeplay.terracotta.model;

import java.net.URI;
import java.security.Key;

/**
 * @author Josh Cummings
 */
public class Client {
	private final String id;
	private final String clientId;
	private final Key clientSecret;
	private final Algorithm algorithm;
	private final URI keySetUri;

	public enum Algorithm {
		v1, v2;
	}

	public Client(String id, String clientId, Key clientSecret, Algorithm algorithm, URI keySetUri) {
		this.id = id;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.algorithm = algorithm;
		this.keySetUri = keySetUri;
	}

	public String getId() {
		return id;
	}

	public String getClientId() {
		return clientId;
	}

	public Key getClientSecret() {
		return clientSecret;
	}

	public Algorithm getAlgorithm() { return algorithm; }

	public URI getKeySetUri() {
		return keySetUri;
	}
}
