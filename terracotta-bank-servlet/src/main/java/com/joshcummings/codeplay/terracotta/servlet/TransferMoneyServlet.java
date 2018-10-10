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
package com.joshcummings.codeplay.terracotta.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.AccountService;

/**
 * This class makes Terracotta Bank vulnerable to Cross-site Scripting
 * by not validating and encoding its various fields.
 *
 * @author Josh Cummings
 */
//@WebServlet("/transferMoney")
public class TransferMoneyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AccountService accountService;

	public TransferMoneyServlet(AccountService accountService) {
		this.accountService = accountService;
	}

	protected void doPost(
						HttpServletRequest request,
						HttpServletResponse response) throws ServletException, IOException {

		User user = (User)request.getAttribute("authenticatedUser");

		List<String> errors = new ArrayList<>();
		
		Function<String, Account> accountParser = (possibleInteger) -> {
			if ( possibleInteger != null ) {
				Integer accountNumber = tryParse(possibleInteger, Integer::parseInt, errors).get();
				return this.accountService.findByAccountNumber(accountNumber);
			} else {
				return this.accountService.findDefaultAccountForUser(user);
			}
		};
		
		Optional<Account> from = tryParse(request.getParameter("fromAccountNumber"), accountParser, errors);
		Optional<Account> to = tryParse(request.getParameter("toAccountNumber"), accountParser, errors);
		Optional<BigDecimal> transferAmount = tryParse(request.getParameter("transferAmount"), BigDecimal::new, errors);
		
		if ( errors.isEmpty() ) {
			Account acct = this.accountService.transferMoney(from.get(), to.get(), transferAmount.get());
			request.setAttribute("account", acct);
			request.getRequestDispatcher("/WEB-INF/json/account.jsp").forward(request, response);
		} else {
			response.setStatus(400);
			request.setAttribute("message", errors.stream().findFirst().get());
			request.getRequestDispatcher("/WEB-INF/json/error.jsp").forward(request, response);
		}
	}
	
	private <E> Optional<E> tryParse(
								String possibleInteger,
								Function<String, E> parser,
								List<String> errors) {

		try {
			return Optional.of(parser.apply(possibleInteger));
		} catch ( Exception e ) {
			errors.add(possibleInteger + " is invalid");
			return Optional.empty();
		}
	}
}
