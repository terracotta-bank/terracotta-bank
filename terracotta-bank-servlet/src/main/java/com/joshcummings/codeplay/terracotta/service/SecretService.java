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
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.core.VaultTransitTemplate;
import org.springframework.vault.support.RawTransitKey;
import org.springframework.vault.support.TransitKeyType;
import org.springframework.vault.support.VaultTransitKeyCreationRequest;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class SecretService {
	private final VaultTransitOperations vaultOperations;

	public SecretService(VaultOperations vaultOperations) {
		this.vaultOperations = new VaultTransitTemplate(vaultOperations, "transit");
	}

	public void generateSymmetric(String alias) {
		this.vaultOperations.createKey(alias,
				VaultTransitKeyCreationRequest.builder()
						.exportable(true).build());
	}

	public SecretKey loadSymmetric(String alias) {
		RawTransitKey raw = this.vaultOperations.exportKey(alias,
				TransitKeyType.ENCRYPTION_KEY);
		List<String> versions = new ArrayList<>(raw.getKeys().values());
		String base64 = CollectionUtils.lastElement(versions);
		byte[] encoded = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(encoded, "AES");
	}

	public void generateAsymmetric(String alias) {
		this.vaultOperations.createKey(alias,
				VaultTransitKeyCreationRequest.builder()
						.type("rsa-2048")
						.exportable(true)
						.build());
	}

	public KeyPair loadAsymmetric(String alias) {
		RawTransitKey raw = this.vaultOperations.exportKey(alias,
				TransitKeyType.ENCRYPTION_KEY);
		List<String> versions = new ArrayList<>(raw.getKeys().values());
		String pkcs1 = CollectionUtils.lastElement(versions);
		String base64 = new BufferedReader(new StringReader(pkcs1))
				.lines()
				.filter(line -> !line.contains("PRIVATE KEY"))
				.collect(Collectors.joining());
		byte[] encoded = Base64.getDecoder().decode(base64);
		RSAPrivateKey key = RSAPrivateKey.getInstance(encoded);

		RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(
				key.getModulus(), key.getPublicExponent());
		RSAPrivateCrtKeySpec privateSpec = new RSAPrivateCrtKeySpec(
				key.getModulus(), key.getPublicExponent(), key.getPrivateExponent(),
				key.getPrime1(), key.getPrime2(), key.getExponent1(), key.getExponent2(),
				key.getCoefficient());

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(publicSpec);
		PrivateKey privateKey = keyFactory.generatePrivate(privateSpec);
		return new KeyPair(publicKey, privateKey);
	}

	@Scheduled(cron = "5 4 * * *")
	public void rotateKeys() {
		this.vaultOperations.getKeys().stream()
				.forEach(alias -> this.vaultOperations.rotate(alias));
	}
}
