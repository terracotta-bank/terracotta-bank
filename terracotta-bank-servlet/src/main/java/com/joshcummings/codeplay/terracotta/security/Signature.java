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
package com.joshcummings.codeplay.terracotta.security;

import java.security.PublicKey;

public class Signature {
	private final java.security.Signature delegate;

	public Signature(java.security.Signature delegate) {
		this.delegate = delegate;
	}

	public static Signature getInstance(String algorithm) {
		try {
			return new Signature(java.security.Signature.getInstance(algorithm));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void initVerify(PublicKey key) {
		try {
			this.delegate.initVerify(key);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void update(byte[] b) {
		try {
			this.delegate.update(b);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public boolean verify(byte[] signature) {
		try {
			return this.delegate.verify(signature);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
