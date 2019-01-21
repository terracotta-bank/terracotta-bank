package com.joshcummings.codeplay.terracotta.service;

import com.joshcummings.codeplay.terracotta.crypto.KeyFactory;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;
import java.net.URI;
import java.security.KeyPair;
import java.security.spec.RSAPrivateCrtKeySpec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Note the docker command to start vault with the appropriate dev settings:
 *
 * <pre>
 *     docker run --name=dev-vault --cap-add=IPC_LOCK -p 8200:8200 -d vault
 * </pre>
 */
public class SecretServiceTest {
	private SecretService secretService;

	@BeforeMethod
	public void setUp() {
		this.secretService = new SecretService(
				new VaultTemplate(
						VaultEndpoint.from(URI.create("http://localhost:8200")),
						new TokenAuthentication("s.hymcqtETQbagyeNmnya4jfiS")
				));
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
