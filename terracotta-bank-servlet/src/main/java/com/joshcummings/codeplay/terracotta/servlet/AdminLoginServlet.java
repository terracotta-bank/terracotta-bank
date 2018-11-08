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
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

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

	private final String username;

	private final String hash;

	public AdminLoginServlet(String username, String hash) {
		Assert.notNull(username, "username is required");
		Assert.notNull(hash, "password hash is required");

		this.username = username;
		this.hash = hash;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String username = req.getParameter("username");
		String password = req.getParameter("password");

		if ( BCrypt.checkpw(password, this.hash) && this.username.equals(username) ) {
			User user = new User("-1", username, null, "System User", "system@terracottabank.com");
			req.getSession().setAttribute("authenticatedUser", user);
			resp.sendRedirect(req.getContextPath() + "/siteStatistics");
		} else {
			resp.setStatus(401);
		}
	}
}
