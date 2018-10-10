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

/**
 * This class makes Terracotta Bank vulnerable to persisted Cross-Site Scripting
 * attacks because it does not validate and encode the email contents before
 * persisting into the database.
 *
 * @author Josh Cummings
 */
//@WebServlet("/contactus")
public class ContactUsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Long nextMessageId = 2L;

	private MessageService messageService;

	public ContactUsServlet(MessageService messageService) {
		this.messageService = messageService;
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = request.getParameter("contactName");
		String email = request.getParameter("contactEmail");
		String subject = request.getParameter("contactSubject");
		String message = request.getParameter("contactMessage");
		Message m = new Message(String.valueOf(nextMessageId++), name, email, subject, message);
		this.messageService.addMessage(m);
	}

}
