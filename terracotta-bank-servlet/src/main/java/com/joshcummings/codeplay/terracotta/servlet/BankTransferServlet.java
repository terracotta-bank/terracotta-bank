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
import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.Client;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.ClientService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
}
