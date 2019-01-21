/*
 * Copyright 2015-2019 Josh Cummings
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
package com.joshcummings.codeplay.terracotta.repo;

import com.joshcummings.codeplay.terracotta.crypto.KeyFactory;
import com.joshcummings.codeplay.terracotta.crypto.SecretKeyFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyPair;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.HashMap;
import java.util.Map;

public class InMemoryKeyRepository implements KeyRepository {
	private final Map<String, KeySpec> specs = new HashMap<>();

	public void store(String alias, SecretKey key) {
		this.specs.put(alias, new SecretKeySpec(key.getEncoded(), key.getAlgorithm()));
	}

	public void store(String alias, KeyPair pair) {
		RSAPrivateCrtKeySpec keySpec = KeyFactory.getInstance("RSA")
				.getKeySpec(pair.getPrivate(), RSAPrivateCrtKeySpec.class);
		this.specs.put(alias, keySpec);
	}

	public <T extends Key> T load(String alias) {
		KeySpec spec = this.specs.get(alias);
		if (spec instanceof SecretKeySpec) {
			return (T) spec;
		} else {
			return (T) KeyFactory.getInstance("RSA").generatePrivate(spec);
		}
	}
}
