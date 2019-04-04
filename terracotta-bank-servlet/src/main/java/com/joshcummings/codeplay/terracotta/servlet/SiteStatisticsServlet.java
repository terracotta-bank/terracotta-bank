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

import com.joshcummings.codeplay.terracotta.app.InMemoryAppender;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * This class is vulernable to Privilege Escalation attacks
 * because the site bases authority on a backdoor user.
 *
 * Further, it bases authority on the username as opposed to a
 * set of privileges granted by the site.
 *
 * @author Josh Cummings
 */
public class SiteStatisticsServlet extends HttpServlet {
	private AccountService accountService;
	private UserService userService;

	public SiteStatisticsServlet(AccountService accountService, UserService userService) {
		this.accountService = accountService;
		this.userService = userService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		User user = (User)req.getAttribute("authenticatedUser");

		if ( user != null && "system".equals(user.getUsername()) ) {
			if (req.getParameter("clear") != null) {
				InMemoryAppender.clear();
			}
			int userCount = this.userService.count();
			int accountCount = this.accountService.count();
			String logs = InMemoryAppender.take(20).stream()
					.collect(Collectors.joining("<br>"));

			resp.setContentType("text/html");
			resp.getWriter().printf(
					"<h2>Welcome, " + user.getUsername() + ".</h2>" +
					"<dl>" +
					"    <dt>Number of users:</dt>" +
					"    <dd>%d</dd>" +
					"    <dt>Number of accounts:</dt>" +
					"    <dd>%d</dd>" +
					"    <dt>Recent Activity <a href='?clear'>(clear)</a>:</dt>" +
					"    <dd>%s</dd>" +
					"</dl>", userCount, accountCount, logs);
		} else {
			resp.setStatus(403);
		}
	}
}
