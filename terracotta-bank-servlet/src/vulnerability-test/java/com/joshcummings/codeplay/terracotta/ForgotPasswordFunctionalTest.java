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
package com.joshcummings.codeplay.terracotta;

import java.util.Collection;

import com.joshcummings.codeplay.terracotta.model.Transaction;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.TransactionService;
import com.joshcummings.codeplay.terracotta.service.UserService;
import com.joshcummings.codeplay.terracotta.testng.HttpSupport;
import com.joshcummings.codeplay.terracotta.testng.TomcatSupport;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;

public class ForgotPasswordFunctionalTest {
	TomcatSupport tomcat = new TomcatSupport();
	HttpSupport http = new HttpSupport();

	@BeforeMethod(alwaysRun = true)
	public void createUser() {
		this.tomcat.startContainer();
		UserService userService = this.tomcat.getContext().getBean(UserService.class);
		User user = new User("0", "user", "P@ssw0rd!", "User", "user@username");
		userService.addUser(user);
	}

	@AfterMethod(alwaysRun = true)
	public void removeUser() {
		this.tomcat.stopContainer();
	}

	@Test(groups = "passwordupdate")
	public void testForgotPasswordForEnumeration() {
		String validAccount = http.postForContent(post("/forgotPassword")
			.addParameter("forgotPasswordAccount", "user"));
		String invalidAccount = http.postForContent(post("/forgotPassword")
			.addParameter("forgotPasswordAccount", "invaliduser"));

		Assert.assertEquals(validAccount, invalidAccount);
	}

	@Test(groups = "passwordupdate")
	public void testForgotPasswordDoesNotRevealPassword() {
		String validAccount = http.postForContent(post("/forgotPassword")
				.addParameter("forgotPasswordAccount", "user"));

		Assert.assertFalse(validAccount.contains("P@ssw0rd!"));
	}

	@Test(groups = "passwordupdate")
	public void testForgotPasswordCannotBePerformedWithGet() {
		int status = http.getForStatus(get("/forgotPassword")
			.addParameter("forgotPasswordAccount", "user"));

		Assert.assertEquals(status, 405);
	}

	@Test(groups = "passwordupdate")
	public void testForgotPasswordUsesTransactionalKeys() {
		TransactionService transactionService = this.tomcat.getContext().getBean(TransactionService.class);
		UserService userService = this.tomcat.getContext().getBean(UserService.class);
		User user = userService.findByUsername("user");
		Collection<Transaction> transactions = transactionService.retrieveTransactionsForUser(user);

		Assert.assertTrue(transactions.isEmpty());

		String content = http.postForContent(post("/forgotPassword")
					  		.addParameter("forgotPasswordAccount", "user"));

		transactions = transactionService.retrieveTransactionsForUser(user);
		Assert.assertTrue(transactions.size() == 1);
		Assert.assertEquals("change_password", transactions.iterator().next().getAction());
		Assert.assertFalse(content.contains(transactions.iterator().next().getKey()));
	}
}