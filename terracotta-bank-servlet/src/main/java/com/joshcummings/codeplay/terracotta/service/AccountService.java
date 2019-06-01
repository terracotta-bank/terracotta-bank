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

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.Check;
import com.joshcummings.codeplay.terracotta.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.crypto.Data;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class makes Terracotta Bank vulnerable to SQL injection
 * attacks because it concatenates queries instead of using
 * bind variables.
 *
 * Also, it is vulnerable to man-in-the-middle in the section
 * where it is making an outbound SSL call because it doesn't
 * mandate hostname verification and trusts all certificates.
 *
 * @author Josh Cummings
 */
@Service
public class AccountService extends ServiceSupport {
	public Account findById(String id) {
		Set<Account> accounts = runQuery("SELECT * FROM accounts WHERE id = " + id, (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
					rs.getLong(3), rs.getString(4)));
		return accounts.size() > 0 ? accounts.iterator().next() : null;
	}
	
	public Set<Account> findByUsername(String username) {
		Set<Account> accounts = runQuery("SELECT accounts.* FROM accounts, users WHERE users.username = '" + username + "' AND accounts.owner_id = users.id", (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
					rs.getLong(3), rs.getString(4)));
		return accounts;
	}
	
	public Account findByAccountNumber(Integer accountNumber) {
		return findAccountRemotely(accountNumber);
	}
	
	public Set<Account> findAll() {
		return runQuery("SELECT * FROM accounts", (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
				rs.getLong(3), rs.getString(4)));
	}
	
	public Account findDefaultAccountForUser(User user) {
		Set<Account> accounts = runQuery("SELECT * FROM accounts WHERE owner_id = '" + user.getId() + "'", (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
				rs.getLong(3), rs.getString(4)));
		
		return accounts.size() > 0 ? accounts.iterator().next() : null;		
	}

	public Integer count() {
		return super.count("accounts");
	}
	
	public void addAccount(Account account) {
		runUpdate("INSERT INTO accounts (id, amount, number, owner_id)"
				+ " VALUES ('" + account.getId() + "','" + account.getAmount() + 
				"','" + account.getNumber() + "','" + account.getOwnerId() + "')");
	}

	public Account makeDeposit(Account account, Check check) {
		return makeDeposit(account, check.getAmount());
	}

	public Account makeDeposit(Integer accountNumber, BigDecimal amount) {
		return makeDepositRemotely(accountNumber, amount);
	}

	public Account makeDeposit(Account account, BigDecimal amount) {
		runUpdate("UPDATE accounts SET amount = " + account.getAmount().add(amount).toString() + " WHERE id = " + account.getId());
		return findById(account.getId());
	}
	
	public Account transferMoney(Account from, Account to, BigDecimal amount) {
		runUpdate("UPDATE accounts SET amount = " + from.getAmount().subtract(amount).toString() + " WHERE id = " + from.getId());
		runUpdate("UPDATE accounts SET amount = " + to.getAmount().add(amount).toString() + " WHERE id = " + to.getId());
		return findById(from.getId());
	}

	private Account findAccountRemotely(Integer accountNumber) {
		try {
			return doFindAccountRemotely(accountNumber);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Account makeDepositRemotely(Integer accountNumber, BigDecimal amount) {
		try {
			return doMakeDepositRemotely(accountNumber, amount);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Account doFindAccountRemotely(Integer accountNumber) throws Exception {
		SSLSocket socket = sslSocket();
		DataOutputStream request = new DataOutputStream(socket.getOutputStream());
		DataInputStream response = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		request.writeChar('r');
		writeAccountNumber(request, accountNumber);
		return readAccount(response);
	}

	private Account doMakeDepositRemotely(Integer accountNumber, BigDecimal amount) throws Exception {
		SSLSocket socket = sslSocket();
		DataOutputStream request = new DataOutputStream(socket.getOutputStream());
		DataInputStream response = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		request.writeChar('w');
		writeAccountNumber(request, accountNumber);
		writeAmount(request, amount);
		return readAccount(response);
	}

	@Value("${keystoreLocation}") String keystoreLocation;
	@Value("${keystorePassword}") String keystorePassword;

	private SSLSocket sslSocket() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		TrustManagerFactory factory = TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm());
		factory.init(keyStore(this.keystoreLocation, this.keystorePassword.toCharArray()));
		TrustManager[] trustMyCA = factory.getTrustManagers();
		sslContext.init(null, trustMyCA, null);

		SSLSocket socket =
				(SSLSocket) sslContext.getSocketFactory().createSocket("localhost", 8443);
		socket.setEnabledProtocols(new String[] { "TLSv1.2" });
		socket.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384" });

		SSLParameters sslParameters = new SSLParameters();
		sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
		socket.setSSLParameters(sslParameters);

		return socket;
	}

	private KeyStore keyStore(String name, char[] password) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(new ClassPathResource(name).getInputStream(), password);
		return keyStore;
	}

	private static Account readAccount(DataInputStream dis) throws IOException {
		String id = readId(dis);
		Integer accountNumber = readAccountNumber(dis);
		BigDecimal amount = readAmount(dis);
		String ownerId = readId(dis);
		return new Account(id, amount, new Long(accountNumber), ownerId);
	}

	private static String readId(DataInputStream dis) throws IOException {
		return new String(readBytes(dis));
	}

	private static Integer readAccountNumber(DataInputStream dis) throws IOException {
		return Integer.parseInt(new String(readBytes(dis)));
	}

	private static BigDecimal readAmount(DataInputStream dis) throws IOException {
		return new BigDecimal(new String(readBytes(dis)));
	}

	private static byte[] readBytes(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		byte[] value = new byte[length];
		dis.read(value);
		return value;
	}

	private static void writeAccountNumber(DataOutputStream dos, Integer accountNumber) throws IOException {
		writeBytes(dos, String.valueOf(accountNumber).getBytes(UTF_8));
	}

	private static void writeAmount(DataOutputStream dos, BigDecimal amount) throws IOException {
		writeBytes(dos, amount.setScale(2).toString().getBytes(UTF_8));
	}

	private static void writeBytes(DataOutputStream dos, byte[] value) throws IOException {
		dos.writeInt(value.length);
		dos.write(value);
	}
}
