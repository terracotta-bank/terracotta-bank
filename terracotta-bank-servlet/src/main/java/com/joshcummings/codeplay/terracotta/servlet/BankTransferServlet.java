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
package com.joshcummings.codeplay.terracotta.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.Client;
import com.joshcummings.codeplay.terracotta.security.Mac;
import com.joshcummings.codeplay.terracotta.security.Signature;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.ClientService;
import com.nimbusds.jose.Algorithm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class is vulnerable to Cross-site Scripting and other
 * types of reflected attacks since it reflects user input back
 * to the response. Also, it performs unsafe validation on the
 * dollar amount, making it vulnerable to ReDOS attacks. It fails
 * to validate any of the inputs, making so that an individual
 * can indicate to tranfer any amount from any bank to any account.
 *
 * @author Josh Cummings
 */
public class BankTransferServlet extends HttpServlet {
	private static final Pattern IS_DOLLAR = Pattern.compile("([0-9]+)*\\.([0-9]{0,2})");

	private final AccountService accountService;
	private final ClientService clientService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public BankTransferServlet(AccountService accountService, ClientService clientService) {
		this.accountService = accountService;
		this.clientService = clientService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<String> errors = new ArrayList<>();

		if (!verifySignature(request)) {
			response.setStatus(401);
			return;
		}

		Optional<String> clientId = tryParse(request.getParameter("clientId"), String::valueOf, errors);
		Optional<Integer> accountNumber = tryParse(request.getParameter("accountNumber"), Integer::parseInt, errors);
		Optional<BigDecimal> amount = tryParse(request.getParameter("amount"), this::parseAsDollar, errors);

		Client client = this.clientService.findByClientId(clientId.get());
		if (client == null) {
			errors.add("Couldn't find client id " + clientId);
		} else {
			try {
				Account account = this.accountService.makeDeposit(accountNumber.get(), amount.get());
				this.objectMapper.writeValue(response.getWriter(), account);
				return;
			} catch (Exception e) {
				errors.add(e.getMessage());
			}
		}

		this.objectMapper.writeValue(response.getWriter(), errors);
	}

	private Supplier<IllegalArgumentException> notFound(String parameter) {
		return () -> new IllegalArgumentException("Couldn't find required parameter " + parameter);
	}

	private BigDecimal parseAsDollar(String param) {
		if (IS_DOLLAR.matcher(param).matches()) {
			return new BigDecimal(param);
		} else {
			return null;
		}
	}

	private <E> Optional<E> tryParse(String possibleInteger, Function<String, E> parser, List<String> errors) {
		try {
			return Optional.of(parser.apply(possibleInteger));
		} catch ( Exception e ) {
			errors.add(possibleInteger + " is invalid");
			return Optional.empty();
		}
	}

	private static Cache<String, String> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofSeconds(120)).build();

	private boolean verifyMessage(HttpServletRequest request) {
		if (verifySignature(request)) {
			String id = Optional.ofNullable(request.getParameter("id"))
					.orElseThrow(notFound("id"));
			Instant created = Optional.ofNullable(request.getParameter("created"))
					.map(Long::parseLong).map(Instant::ofEpochSecond).orElseThrow(notFound("created"));
			Instant now = Instant.now();
			if (created.isAfter(now) || created.isBefore(now.minusSeconds(120))) {
				throw new IllegalArgumentException("message created outside usable window");
			}
			if (cache.asMap().putIfAbsent(id, id) != null) {
				throw new IllegalArgumentException("duplicate message");
			}
			return true;
		}

		return false;
	}

	private boolean verifySignature(HttpServletRequest request) {
		byte[] sender = Optional.ofNullable(request.getParameter("signature"))
				.map(Base64.getDecoder()::decode).orElseThrow(notFound("signature"));

		Client c = Optional.ofNullable(request.getParameter("clientId"))
				.map(this.clientService::findByClientId)
				.orElseThrow(notFound("clientId"));

		Client.Algorithm algorithm = Optional.ofNullable(request.getParameter("version"))
				.map(Client.Algorithm::valueOf).orElseThrow(notFound("version"));
		if (algorithm != c.getAlgorithm()) {
			throw new IllegalArgumentException("algorithm mismatch");
		}

		switch (c.getAlgorithm()) {
			case v1:
				return verifyMac(sender, c, request);
			case v2:
				return verifySignature(sender, c, request);
		}

		throw new IllegalArgumentException("Invalid algorithm");
	}

	private boolean verifyMac(byte[] sender, Client c, HttpServletRequest request) {
		Mac mac = Mac.getInstance("HMACSHA256");
		mac.init(c.getClientSecret());
		mac.update("v1".getBytes(UTF_8));
		Optional.ofNullable(request.getParameter("id"))
				.map(id -> id.getBytes(UTF_8)).ifPresent(mac::update);
		Optional.ofNullable(request.getParameter("created"))
				.map(id -> id.getBytes(UTF_8)).ifPresent(mac::update);
		mac.update(c.getClientId().getBytes(UTF_8));
		Optional.ofNullable(request.getParameter("accountNumber"))
				.map(number -> number.getBytes(UTF_8)).ifPresent(mac::update);
		Optional.ofNullable(request.getParameter("amount"))
				.map(amount -> amount.getBytes(UTF_8)).ifPresent(mac::update);
		byte[] recipient = mac.doFinal();

		return MessageDigest.isEqual(sender, recipient);
	}

	private boolean verifySignature(byte[] sender, Client c, HttpServletRequest request) {
		Signature signature = Signature.getInstance("SHA256WITHRSA");
		signature.initVerify((PublicKey) c.getClientSecret());
		signature.update("v2".getBytes(UTF_8));
		Optional.ofNullable(request.getParameter("id"))
				.map(id -> id.getBytes(UTF_8)).ifPresent(signature::update);
		Optional.ofNullable(request.getParameter("created"))
				.map(id -> id.getBytes(UTF_8)).ifPresent(signature::update);
		signature.update(c.getClientId().getBytes(UTF_8));
		Optional.ofNullable(request.getParameter("accountNumber"))
				.map(number -> number.getBytes(UTF_8)).ifPresent(signature::update);
		Optional.ofNullable(request.getParameter("amount"))
				.map(amount -> amount.getBytes(UTF_8)).ifPresent(signature::update);
		return signature.verify(sender);
	}
}
