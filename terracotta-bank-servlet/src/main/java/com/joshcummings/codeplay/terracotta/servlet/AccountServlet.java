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

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.service.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author Josh Cummings
 */
//@WebServlet("/showAccounts")
public class AccountServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AccountService accountService;

	public AccountServlet(AccountService accountService) {
		this.accountService = accountService;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( request.getAttribute("authenticatedUser") == null ) {
			response.sendRedirect(request.getContextPath() + "/employee.jsp?relay=" + request.getRequestURL().toString());
		} else {
			Set<Account> accounts = this.accountService.findAll();
			request.setAttribute("accounts", accounts);
			request.getRequestDispatcher("/WEB-INF/accounts.jsp").forward(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
