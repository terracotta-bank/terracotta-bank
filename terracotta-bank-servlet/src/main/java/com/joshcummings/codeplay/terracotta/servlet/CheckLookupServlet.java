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

import com.joshcummings.codeplay.terracotta.service.CheckService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class makes Terracotta vulnerable Cross-site Scripting attacks
 * because it does not validate and encode the {@code checkNumber}
 * before it writes it to the response.
 *
 * It is vulnerable to CSRF in part because it naively maps
 * {@code doGet} to {@code doPost}.
 *
 * @author Josh Cummings
 */
//@WebServlet("/checkLookup")
public class CheckLookupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private CheckService checkService;

	public CheckLookupServlet(CheckService checkService) {
		this.checkService = checkService;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String checkNumber = request.getParameter("checkLookupNumber");

		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			this.checkService.findCheckImage(checkNumber, baos);
			response.setContentType("image/jpg");
			response.getOutputStream().write(baos.toByteArray());
			response.flushBuffer();
		}
		catch ( IllegalArgumentException e )
		{
			response.setStatus(400);
			request.setAttribute("message", checkNumber + " is invalid");
			request.getRequestDispatcher("/WEB-INF/json/error.jsp").forward(request, response);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
