package com.joshcummings.codeplay.terracotta.service;

import com.joshcummings.codeplay.terracotta.crypto.KeyFactory;
import com.joshcummings.codeplay.terracotta.repo.InMemoryKeyRepository;
import com.joshcummings.codeplay.terracotta.repo.KeyStoreKeyRepository;
import com.joshcummings.codeplay.terracotta.service.SecretService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.spec.RSAPrivateCrtKeySpec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class SecretServiceTest {
	private SecretService secretService;

	@BeforeMethod
	public void setUp() {
		this.secretService = new SecretService(new KeyStoreKeyRepository());
	}

	@Test
	public void testSymmetricGenerateAndLoadRoundtrip() {
		this.secretService.generateSymmetric("symmetric");
		SecretKey key = this.secretService.loadSymmetric("symmetric");
		assertNotNull(key);
		assertEquals("AES", key.getAlgorithm());
		assertEquals(256, 8 * key.getEncoded().length);
	}

	@Test
	public void testAsymmetricGenerateAndLoadRoundtrip() {
		this.secretService.generateAsymmetric("asymmetric");
		KeyPair pair = this.secretService.loadAsymmetric("asymmetric");
		assertNotNull(pair);
		assertEquals("RSA", pair.getPrivate().getAlgorithm());

		RSAPrivateCrtKeySpec keySpec = KeyFactory.getInstance("RSA")
				.getKeySpec(pair.getPrivate(), RSAPrivateCrtKeySpec.class);
		assertEquals(1024, keySpec.getPrimeP().bitLength());
	}
}
