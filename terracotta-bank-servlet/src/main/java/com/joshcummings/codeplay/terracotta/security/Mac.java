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

import java.security.Key;

public class Mac {
	private final javax.crypto.Mac delegate;

	public Mac(javax.crypto.Mac delegate) {
		this.delegate = delegate;
	}

	public static Mac getInstance(String algorithm) {
		try {
			return new Mac(javax.crypto.Mac.getInstance(algorithm));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void init(Key key) {
		try {
			this.delegate.init(key);
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

	public byte[] doFinal() {
		try {
			return this.delegate.doFinal();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
