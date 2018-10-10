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
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;
import com.joshcummings.codeplay.terracotta.service.passwords.Evaluation;
import com.joshcummings.codeplay.terracotta.service.passwords.PasswordEntropyEvaluator;
import com.joshcummings.codeplay.terracotta.service.passwords.WeakPasswordEntropyEvaluator;

/**
 * This servlet makes Terracotta vulnerable to Cross-site Scripting attacks
 * because it doesn't validate the {@code verifyPassword} parameter before persisting
 * and it needlessly reflects the client-provided parameters back to the screen.
 *
 * It has an authorization flaw in that it doesn't require the user to
 * prove her authority to change her password by performing the same authentication steps
 * as when logging in.
 *
 * It is vulnerable to CSRF because it naively overrides {@link this#doGet} to invoke
 * {@link this#doPost}
 *
 * @author Josh Cummings
 */
public class ChangePasswordServlet extends HttpServlet {
	private final UserService userService;
	private final PasswordEntropyEvaluator evaluator = new WeakPasswordEntropyEvaluator();

	public ChangePasswordServlet(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User loggedInUser = (User) request.getAttribute("authenticatedUser");

		if (loggedInUser != null) {
			changePassword(request, response, loggedInUser);
			return;
		}

		response.setStatus(401);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	private boolean changePassword(HttpServletRequest request, HttpServletResponse response, User loggedInUser)
			throws ServletException, IOException {

		String password = request.getParameter("changePassword");
		String verifyPassword = request.getParameter("verifyChangePassword");

		if ( password.equals(verifyPassword) ) {
			Evaluation evaluation =
					this.evaluator.evaluate(password, loggedInUser);

			if ( evaluation.isSuccess() ) {
				User user = new User(loggedInUser.getId(),
						loggedInUser.getUsername(),
						password,
						loggedInUser.getName(),
						loggedInUser.getEmail());
				this.userService.updateUserPassword(user);
				return true;
			} else {
				sendError(request, response,
						"Your password (" + password + ") isn't strong enough: <br/>" +
								evaluation.getDetails().stream().collect(Collectors.joining("<br/>")));
				return false;
			}
		} else {
			sendError(request, response,
					"Your password (" + password + ") is not equal to your verification password (" +
							verifyPassword + ")");
			return false;
		}
	}

	private void sendError(HttpServletRequest request, HttpServletResponse response, String error)
			throws ServletException, IOException {

		response.setStatus(400);
		request.setAttribute("message", error);
		request.getRequestDispatcher("/WEB-INF/json/error.jsp").forward(request, response);
	}
}