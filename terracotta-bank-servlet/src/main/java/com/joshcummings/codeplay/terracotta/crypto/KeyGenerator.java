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
package com.joshcummings.codeplay.terracotta.crypto;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class KeyGenerator {
	private final javax.crypto.KeyGenerator delegate;

	public KeyGenerator(javax.crypto.KeyGenerator delegate) {
		this.delegate = delegate;
	}

	public static KeyGenerator getInstance(String algorithm) {
		try {
			return new KeyGenerator(javax.crypto.KeyGenerator.getInstance(algorithm));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public SecretKey generateKey() {
		return this.delegate.generateKey();
	}

	public void init(int i) {
		this.delegate.init(i);
	}
}
