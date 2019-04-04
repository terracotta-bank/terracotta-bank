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
package com.joshcummings.codeplay.terracotta.model;

import java.io.Serializable;

/**
 * This class isn't safe to serialize and also may
 * leak the user's password into the String pool.
 *
 * @author Josh Cummings
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String id;
	private final String username;
	private final String password;
	private final String name;
	private final String email;
	private boolean isAdmin;
	
	public User(String id, String username, String password, String name, String email) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.name = name;
		this.email = email;
	}

	public User(String id, String username, String password, String name, String email, boolean isAdmin) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.name = name;
		this.email = email;
		this.isAdmin = isAdmin;
	}


	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public boolean isAdmin() {
		return isAdmin;
	}
}
