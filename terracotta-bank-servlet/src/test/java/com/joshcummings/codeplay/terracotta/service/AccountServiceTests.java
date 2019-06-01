/*
 * Copyright 2015-2019 Josh Cummings
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

import com.joshcummings.codeplay.terracotta.Mainer;
import com.joshcummings.codeplay.terracotta.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AccountService}
 */
public class AccountServiceTests {
	private Integer accountNumber = 0;
	private Account account = new Account(String.valueOf(accountNumber), new BigDecimal("25.00"), 0L, "0");

	@Autowired
	AccountService accountService;

	@BeforeClass
	public void setup() {
		ApplicationContext context = SpringApplication.run(Mainer.class);
		this.accountService = context.getBean(AccountService.class);
		this.accountService.addAccount(this.account);
	}

	/**
	 * NOTE: here in the data-tls branch, this test requires that the remote SSL service,
	 * terracotta-bank-service, be started up
	 */
	@Test
	public void makeDepositWhenKnownAccountThenPasses() {
		Account before = this.accountService.findByAccountNumber(this.accountNumber);
		BigDecimal amount = new BigDecimal("50");
		Account after = this.accountService.makeDeposit(this.accountNumber, amount);
		assertEquals(amount.add(before.getAmount()), after.getAmount());
	}
}
