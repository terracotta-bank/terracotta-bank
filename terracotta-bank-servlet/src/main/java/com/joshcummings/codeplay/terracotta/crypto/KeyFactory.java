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

import javax.crypto.SecretKeyFactory;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class KeyFactory {
	private final java.security.KeyFactory delegate;

	public KeyFactory(java.security.KeyFactory delegate) {
		this.delegate = delegate;
	}

	public static KeyFactory getInstance(String algorithm) {
		try {
			return new KeyFactory(java.security.KeyFactory.getInstance(algorithm));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public PublicKey generatePublic(KeySpec keySpec) {
		try {
			return this.delegate.generatePublic(keySpec);
		} catch (InvalidKeySpecException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public PrivateKey generatePrivate(KeySpec keySpec) {
		try {
			return this.delegate.generatePrivate(keySpec);
		} catch (InvalidKeySpecException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public <T extends KeySpec> T getKeySpec(Key key, Class<T> clazz) {
		try {
			return this.delegate.getKeySpec(key, clazz);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
