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

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

public class KeyStore {
	private final java.security.KeyStore delegate;

	public KeyStore(java.security.KeyStore delegate) {
		this.delegate = delegate;
	}

	public static KeyStore getInstance(String algorithm) {
		try {
			return new KeyStore(
					java.security.KeyStore.getInstance(algorithm));
		} catch (KeyStoreException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void load(InputStream is, char[] password) {
		try {
			this.delegate.load(is, password);
		} catch (Exception e){
			throw new IllegalStateException(e);
		}
	}

	public void store(OutputStream os, char[] password) {
		try {
			this.delegate.store(os, password);
		} catch (Exception e){
			throw new IllegalStateException(e);
		}
	}

	public Key getKey(String alias, char[] password) {
		try {
			return this.delegate.getKey(alias, password);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void setKeyEntry(String alias, Key key, char[] password, Certificate[] certificates) {
		try {
			this.delegate.setKeyEntry(alias, key, password, certificates);
		} catch (KeyStoreException e) {
			throw new IllegalStateException(e);
		}
	}
}
