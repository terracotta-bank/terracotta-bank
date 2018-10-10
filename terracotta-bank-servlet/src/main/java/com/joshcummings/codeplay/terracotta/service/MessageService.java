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

import java.sql.SQLException;
import java.util.Set;

import com.joshcummings.codeplay.terracotta.model.Message;
import org.springframework.stereotype.Service;

/**
 * This class makes Terracotta Bank vulnerable to SQL injection
 * attacks because it concatenates queries instead of using
 * bind variables.
 *
 * @author Josh Cummings
 */
@Service
public class MessageService extends ServiceSupport {
	public Set<Message> findAll() {
		return runQuery("SELECT * FROM messages", (rs) -> {
			try {
				return new Message(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
			} catch ( SQLException e ) {
				throw new IllegalStateException(e);
			}
		});
	}
	
	public void addMessage(Message message) {
		runUpdate("INSERT INTO messages (id, name, email, subject, message) VALUES ('" +
			message.getId() + "','" + message.getName() + "','" + message.getEmail() + "','" +
			message.getSubject() + "','" + message.getMessage() + "')");
	}
}
