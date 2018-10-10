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

import com.joshcummings.codeplay.terracotta.model.Message;
import com.joshcummings.codeplay.terracotta.service.MessageService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * This class is not protected as it should be in that it is a
 * page for employees, but does not ensure that the user has
 * employee privileges to show it.
 *
 * @author Josh Cummings
 */
//@WebServlet("/showMessages")
public class MessagesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private MessageService messageService;

	public MessagesServlet(MessageService messageService) {
		this.messageService = messageService;
	}

	protected void doGet(
						HttpServletRequest request,
						HttpServletResponse response) throws ServletException, IOException {

		if ( request.getAttribute("authenticatedUser") == null )
		{
			String relay = request.getRequestURL().toString();
			response.sendRedirect(request.getContextPath() + "/employee.jsp?relay=" + relay);
		}
		else
		{
			Set<Message> messages = this.messageService.findAll();
			request.setAttribute("messages", messages);
			request.getRequestDispatcher("/WEB-INF/messages.jsp").forward(request, response);
		}
	}
}
