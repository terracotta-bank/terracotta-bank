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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class makes Terracotta Bank vulnerable to CSRF
 * attacks due to naively mapping {@code doGet} to {@code doPost}.
 *
 * @author Josh Cummings
 */
//@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;


	@Override
	protected void doGet(
						HttpServletRequest req,
						HttpServletResponse resp) throws ServletException, IOException {

		doPost(req, resp);
	}

	protected void doPost(
						HttpServletRequest request,
						HttpServletResponse response) throws ServletException, IOException {

		request.getSession().invalidate();
		response.sendRedirect(request.getContextPath());
	}

}
