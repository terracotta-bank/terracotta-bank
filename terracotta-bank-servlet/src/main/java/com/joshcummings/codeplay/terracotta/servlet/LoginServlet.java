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
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * This class makes Terracotta Bank vulnerable to Cross-site Scripting,
 * Open-Redirect, CRLF, Enumeration and CSRF attacks.
 *
 * It is vulnerable to Cross-site Scripting due to not validating and
 * encoding the {@code username} parameter.
 *
 * It is vulnerable to Open-Redirect and to CRLF due to not validating
 * and encoding the {@code relay} parameter.
 *
 * It is vulnerable to Enumeration at least due to the error messaging
 * being different between successful and unsuccessful user lookups.
 *
 * It is vulnerable to CSRF due to it naively calling {@code doPost}
 * in its {@code doGet} method.
 *
 * @author Josh Cummings
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AccountService accountService;
	private UserService userService;

	public LoginServlet(AccountService accountService, UserService userService) {
		this.accountService = accountService;
		this.userService = userService;
	}

	protected void doPost(
						HttpServletRequest request,
						HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");

		User user = this.userService.findByUsernameAndPassword(username, password);

		if ( user == null )
		{
			String error = "The username (" + username + ") or password you provided is incorrect.";
			this.error(request, response, error);
		}
		else
		{
			Set<Account> accounts = this.accountService.findByUsername(user.getUsername());

			request.getSession().setAttribute("authenticatedUser", user);
			request.getSession().setAttribute("authenticatedAccounts", accounts);
			
			String relay = request.getParameter("relay");
			if ( relay == null || relay.isEmpty() ) {
				response.sendRedirect(request.getContextPath());
			} else {
				response.sendRedirect(relay);
			}
		}
	}
	
	protected void doGet(
						HttpServletRequest request,
						HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);
	}

	private void error(
					HttpServletRequest request,
					HttpServletResponse response,
					String error) throws ServletException, IOException {

		request.setAttribute("loginErrorMessage", error);
		request.getRequestDispatcher(request.getContextPath() + "index.jsp").forward(request, response);
	}
}
