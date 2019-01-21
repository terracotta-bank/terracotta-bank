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

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class KeyPairGenerator {
	private final java.security.KeyPairGenerator delegate;

	public KeyPairGenerator(java.security.KeyPairGenerator delegate) {
		this.delegate = delegate;
	}

	public static KeyPairGenerator getInstance(String algorithm) {
		try {
			return new KeyPairGenerator(
					java.security.KeyPairGenerator.getInstance(algorithm));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public KeyPair generateKeyPair() {
		return this.delegate.generateKeyPair();
	}

	public void init(int keySize) {
		this.delegate.initialize(keySize);
	}
}
