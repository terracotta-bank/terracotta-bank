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
package com.joshcummings.codeplay.terracotta;

import com.joshcummings.codeplay.terracotta.model.RemoteAccount;
import com.joshcummings.codeplay.terracotta.service.RemoteAccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class is a vulnerable SSL server because it
 * includes credentials in the file and allows for older
 * TLS versions like TLSv1.0.
 *
 * @author Josh Cummings
 */
@SpringBootApplication
public class RemoteAccountServiceApplication {

	private static SSLServerSocket sslSocket() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		KeyStore keyStore = keyStore();
		KeyManagerFactory keyManagerFactory =
				KeyManagerFactory.getInstance("NewSunX509");
		keyManagerFactory.init(keyStore, "t3rrac0tta".toCharArray());
		sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
		SSLServerSocket server = (SSLServerSocket)
				sslContext.getServerSocketFactory().createServerSocket(8443);
		server.setEnabledProtocols(new String[] { "TLSv1.3" });
		server.setEnabledCipherSuites(new String[] { "TLS_AES_256_GCM_SHA384" });
		return server;
	}

	private static KeyStore keyStore() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(new ClassPathResource("server.jks").getInputStream(), "t3rrac0tta".toCharArray());
		return keyStore;
	}

	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringApplication.run(RemoteAccountServiceApplication.class, args);
		RemoteAccountService accountService = context.getBean(RemoteAccountService.class);

		SSLServerSocket server = sslSocket();
		ExecutorService workers = Executors.newFixedThreadPool(4);
		while (true) {
			Socket socket = server.accept();
			workers.submit(() -> {
				DataInputStream request = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				DataOutputStream response = new DataOutputStream(socket.getOutputStream());

				char method = request.readChar();
				Integer accountNumber = readAccountNumber(request);
				try {
					switch (method) {
						case 'r':
							writeAccount(response, accountService.findByAccountNumber(accountNumber));
							break;
						case 'w':
							BigDecimal amount = readAmount(request);
							writeAccount(response, accountService.makeDeposit(accountNumber, amount));
							break;
						default:
							writeAccount(response, new RemoteAccount("-1", BigDecimal.ZERO, -1L, "-1"));
					}
				} catch (Exception e) {
					writeAccount(response, new RemoteAccount("-1", BigDecimal.ZERO, -1L, "-1"));
				}
				response.flush();
				return null;
			});
		}
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

	private static void writeId(DataOutputStream dos, String id) throws IOException {
		writeBytes(dos, id.getBytes(UTF_8));
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

	private static void writeAccount(DataOutputStream dos, RemoteAccount account) throws IOException {
		writeId(dos, account.getId());
		writeAccountNumber(dos, account.getNumber().intValue());
		writeAmount(dos, account.getAmount());
		writeId(dos, account.getOwnerId());
	}
}
