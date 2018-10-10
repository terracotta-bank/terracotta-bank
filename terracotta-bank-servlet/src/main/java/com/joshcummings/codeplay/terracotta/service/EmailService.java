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
package com.joshcummings.codeplay.terracotta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is vulnerable to information leakage because
 * it places credentials inside source code.
 *
 * @author Josh Cummings
 */
@Service
public class EmailService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String from = "no-reply-terracotta-bank@mailinator.com";
	private String host = "in-v3.mailjet.com";
	private Properties properties = System.getProperties();

	{
		properties.setProperty("mail.smtp.host", host);
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.port", "587");
	}

	private String apiKey;
	private String apiSecret;

	public EmailService(@Value("${mailjet.api.key}") String apiKey,
						@Value("${mailjet.api.secret}") String apiSecret) {
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}

	public void sendMessage(String to, String subject, String content) {
		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(apiKey, apiSecret);
			}
		});

		MimeMessage message = new MimeMessage(session);

		try {
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setContent(content, "text/html; charset=utf-8");
			Transport.send(message);
		} catch (MessagingException mex) {
			this.logger.error("Failed to send message", mex);
		}
	}
}
