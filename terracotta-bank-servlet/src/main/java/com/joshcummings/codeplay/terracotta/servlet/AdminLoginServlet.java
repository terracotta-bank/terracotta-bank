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

import com.joshcummings.codeplay.terracotta.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is vulnerable to Brute Force attacks because
 * it contains a trojan, or a backdoor login.
 *
 * @author Josh Cummings
 */
public class AdminLoginServlet extends HttpServlet {
	private static final User SUPER_USER =
			new User("-1", "system", "backoffice", "System User", "system@terracottabank.com");

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if ( SUPER_USER.getPassword().equals(req.getParameter("password")) ) {
			req.getSession().setAttribute("authenticatedUser", SUPER_USER);
			resp.sendRedirect(req.getContextPath() + "/siteStatistics");
		} else {
			resp.setStatus(401);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher("/adminLogin.html").forward(req, resp);
	}
}
