/*
 * Copyright 2015-2018 Josh Cummings
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

import com.joshcummings.codeplay.terracotta.model.Client;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class makes Terracotta Bank vulnerable to SQL injection
 * attacks because it concatenates queries instead of using
 * bind variables.
 *
 * @author Josh Cummings
 */
@Service
public class ClientService extends ServiceSupport {
	public Client findByClientId(String clientId) {
		Set<Client> clients =
				runQuery("SELECT id, client_id, client_secret, algorithm, key_set_uri " +
						"FROM clients WHERE client_id = '" + clientId + "'",
						rs -> {
							String id = rs.getString(1);
							String cId = rs.getString(2);
							byte[] bytes = Base64.getDecoder().decode(rs.getString(3).getBytes(UTF_8));
							Client.Algorithm algorithm = Client.Algorithm.valueOf(rs.getString(4));
							Key key = null;
							switch (algorithm) {
								case v1:
									key = new SecretKeySpec(bytes, "AES");
									break;
								case v2:
									key = publicKey(bytes);
									break;
							}
							return new Client(id, cId, key, algorithm, URI.create(rs.getString(5)));
						});
		return clients.isEmpty() ? null : clients.iterator().next();
	}

	public void addClient(Client client) {
		String encoded = encode(client.getClientSecret());
		runUpdate("INSERT INTO clients (id, client_id, client_secret, algorithm, key_set_uri) " +
				"VALUES ('" + client.getId() + "', '" + client.getClientId() + "', '" + encoded + "', '" + client.getAlgorithm() + "','" + client.getKeySetUri() + "')");
	}

	private String encode(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	private Key publicKey(byte[] bytes) {
		try {
			return KeyFactory.getInstance("RSA").generatePublic(
					new X509EncodedKeySpec(bytes));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
