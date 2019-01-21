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
package com.joshcummings.codeplay.terracotta.service;

import com.joshcummings.codeplay.terracotta.crypto.KeyFactory;
import com.joshcummings.codeplay.terracotta.crypto.KeyGenerator;
import com.joshcummings.codeplay.terracotta.crypto.KeyPairGenerator;
import com.joshcummings.codeplay.terracotta.repo.KeyRepository;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class SecretService {
	private final KeyRepository keyRepository;

	public SecretService(KeyRepository keyRepository) {
		this.keyRepository = keyRepository;
	}

	public void generateSymmetric(String alias) {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(256);
		SecretKey key = generator.generateKey();

		this.keyRepository.store(alias, key);
	}

	public SecretKey loadSymmetric(String alias) {
		return this.keyRepository.load(alias);
	}

	public void generateAsymmetric(String alias) {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.init(2048);
		KeyPair pair = generator.generateKeyPair();

		this.keyRepository.store(alias, pair);
	}

	public KeyPair loadAsymmetric(String alias) {
		PrivateKey privateKey = this.keyRepository.load(alias);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateCrtKeySpec s = keyFactory
				.getKeySpec(privateKey, RSAPrivateCrtKeySpec.class);
		RSAPublicKeySpec spec = new RSAPublicKeySpec(
				s.getModulus(), s.getPublicExponent());
		PublicKey publicKey = keyFactory.generatePublic(spec);
		return new KeyPair(publicKey, privateKey);
	}
}
