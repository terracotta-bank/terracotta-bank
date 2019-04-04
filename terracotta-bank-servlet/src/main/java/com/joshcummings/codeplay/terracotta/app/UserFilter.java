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
package com.joshcummings.codeplay.terracotta.app;

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.UserService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

/**
 * @author Josh Cummings
 */
public class UserFilter implements Filter {

	private AccountService accountService;
	private UserService userService;

	public UserFilter(AccountService accountService, UserService userService) {
		this.accountService = accountService;
		this.userService = userService;
	}

	@Override
	public void init(FilterConfig filterConfig) { }

	@Override
	public void doFilter(
					ServletRequest req,
					ServletResponse resp,
					FilterChain chain)
			throws IOException, ServletException {

		try {
			if ( req instanceof HttpServletRequest ) {
				HttpServletRequest request = (HttpServletRequest) req;

				User user = Optional.ofNullable(request.getSession(false))
						.map(session -> session.getAttribute("authenticatedUser"))
						.map(User.class::cast)
						.orElse(null);

				if ( user == null ) {
					String authorization = request.getHeader("Authorization");
					if ( authorization != null && authorization.startsWith("Basic ") ) {
						String basic =  authorization.substring(6);
						String[] up = new String(Base64.getDecoder().decode(basic)).split(":");

						user = this.userService.findByUsernameAndPassword(up[0], up[1]);
						if ( user == null ) {
							((HttpServletResponse) resp).setStatus(403);
						}
					}
				}

				if ( user != null ) {
					User refreshed = this.userService.findByUsername(user.getUsername());
					if ( refreshed != null ) {
						Set<Account> accounts = this.accountService.findByUsername(user.getUsername());
						request.setAttribute("authenticatedUser", refreshed);
						request.setAttribute("authenticatedAccounts", accounts);
					} else {
						request.setAttribute("authenticatedUser", user);
					}
				}
			}
		} finally {
			chain.doFilter(req, resp);
		}
	}

	@Override
	public void destroy() { }

}
