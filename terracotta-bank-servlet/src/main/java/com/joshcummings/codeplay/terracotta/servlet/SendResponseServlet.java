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

import com.joshcummings.codeplay.terracotta.service.EmailService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class makes Terracotta Bank an unwitting participant in
 * sending malicious scripts to recipients by not validating and
 * encoding {@code sendResponseContent}.
 *
 * Further, in conjunction with other Cross-site Scripting
 * vulnerabilities, this servlet could be used to exfiltrate
 * sensitive data from the site.
 *
 * @author Josh Cummings
 */
//@WebServlet("/sendResponse")
public class SendResponseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private EmailService emailService;

	public SendResponseServlet(EmailService emailService) {
		this.emailService = emailService;
	}

	protected void doPost(
						HttpServletRequest request,
						HttpServletResponse response) throws ServletException, IOException {

		String to = request.getParameter("sendResponseTo");
		String subject = "In Response To Your Inquiry";
		String content = request.getParameter("sendResponseContent");
		this.emailService.sendMessage(to, subject, content);
	}
}
