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
import com.joshcummings.codeplay.terracotta.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class makes Terracotta Bank vulnerable to Open Redirect
 * and CRLF attacks because it doesn't validate and encode the
 * {@code relay} parameter before adding it to the url.
 *
 * @author Josh Cummings
 */
//@WebServlet("/employeeLogin")
public class EmployeeLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private UserService userService;

	public EmployeeLoginServlet(UserService userService) {
		this.userService = userService;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		User user = this.userService.findByUsernameAndPassword(username, password);
		request.getSession().setAttribute("authenticatedUser", user);
		
		String relay = request.getParameter("relay");
		if ( relay == null || relay.isEmpty() ) {
			response.sendRedirect(request.getContextPath() + "/employee.jsp");
		} else {
			response.sendRedirect(relay);
		}
	}

}
