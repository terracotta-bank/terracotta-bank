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
package com.joshcummings.codeplay.terracotta.repo;

import com.joshcummings.codeplay.terracotta.crypto.KeyStore;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Function;

public class KeyStoreKeyRepository implements KeyRepository {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private final JcaX509CertificateConverter converter =
			new JcaX509CertificateConverter()
					.setProvider(Security.getProvider("BC"));
	private final String subjectDN = "CN=self";
	private final String name = "keystore.p12";
	private final char[] password = "password".toCharArray();

	public void store(String alias, SecretKey key) {
		doWithKeyStore(keyStore -> {
			keyStore.setKeyEntry(alias, key, this.password, null);
			return null;
		});
	}

	public void store(String alias, KeyPair pair) {
		Certificate[] certificates = new Certificate[] { selfSign(pair) };
		doWithKeyStore(keyStore -> {
			keyStore.setKeyEntry(alias, pair.getPrivate(), this.password, certificates);
			return null;
		});
	}

	public <T extends Key> T load(String alias) {
		return doWithKeyStore(keyStore ->
			(T) keyStore.getKey(alias, this.password));
	}

	private Certificate selfSign(KeyPair keyPair)
	{
		try {
			ContentSigner contentSigner = signer(keyPair.getPrivate());
			X509CertificateHolder holder = builder(keyPair.getPublic()).build(contentSigner);
			return converter.getCertificate(holder);
		} catch (CertificateException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private ContentSigner signer(PrivateKey privateKey) {
		try {
			return new JcaContentSignerBuilder("SHA256WithRSA")
					.build(privateKey);
		} catch (OperatorCreationException e) {
			throw new IllegalStateException(e);
		}
	}

	private X509v3CertificateBuilder builder(PublicKey publicKey) {
		try {
			ZonedDateTime now = ZonedDateTime.now();
			X500Name dnName = new X500Name(subjectDN);
			BigInteger certSerialNumber = BigInteger.valueOf(now.toEpochSecond());
			Date startDate = Date.from(now.toInstant());
			Date endDate = Date.from(now.plusYears(1).toInstant());
			X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder
					(dnName, certSerialNumber, startDate, endDate, dnName, publicKey);

			BasicConstraints basicConstraints = new BasicConstraints(false);
			certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);

			return certBuilder;
		} catch (CertIOException e) {
			throw new IllegalStateException(e);
		}
	}

	private <T> T doWithKeyStore(Function<KeyStore, T> c) {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		try (FileInputStream fis = new FileInputStream(this.name)) {
			keyStore.load(fis, this.password);
			return c.apply(keyStore);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			try (FileOutputStream fos = new FileOutputStream(this.name)) {
				keyStore.store(fos, this.password);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
