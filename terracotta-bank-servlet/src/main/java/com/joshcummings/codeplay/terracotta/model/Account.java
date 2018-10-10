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
import java.math.BigDecimal;

/**
 * @author Josh Cummings
 */
public class Account implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String id;
	private final BigDecimal amount;
	private final Long number;
	private final String ownerId;
	
	public Account(String id, BigDecimal amount, Long number, String ownerId) {
		this.id = id;
		this.amount = amount;
		this.number = number;
		this.ownerId = ownerId;
	}

	public String getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Long getNumber() {
		return number;
	}

	public String getOwnerId() {
		return ownerId;
	}
}
