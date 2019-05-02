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

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.Check;
import com.joshcummings.codeplay.terracotta.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Set;

/**
 * This class makes Terracotta Bank vulnerable to SQL injection
 * attacks because it concatenates queries instead of using
 * bind variables.
 *
 * @author Josh Cummings
 */
@Service
public class AccountService extends ServiceSupport {
	public Account findById(String id) {
		Set<Account> accounts = runQuery("SELECT * FROM accounts WHERE id = " + id, (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
					rs.getLong(3), rs.getString(4)));
		return accounts.size() > 0 ? accounts.iterator().next() : null;
	}
	
	public Set<Account> findByUsername(String username) {
		Set<Account> accounts = runQuery("SELECT accounts.* FROM accounts, users WHERE users.username = '" + username + "' AND accounts.owner_id = users.id", (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
					rs.getLong(3), rs.getString(4)));
		return accounts;
	}
	
	public Account findByAccountNumber(Integer accountNumber) {
		Set<Account> accounts = runQuery("SELECT * FROM accounts WHERE number = " + accountNumber, (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
				rs.getLong(3), rs.getString(4)));
		return accounts.size() > 0 ? accounts.iterator().next() : null;		
	}
	
	public Set<Account> findAll() {
		return runQuery("SELECT * FROM accounts", (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
				rs.getLong(3), rs.getString(4)));
	}
	
	public Account findDefaultAccountForUser(User user) {
		Set<Account> accounts = runQuery("SELECT * FROM accounts WHERE owner_id = '" + user.getId() + "'", (rs) ->
			new Account(rs.getString(1), new BigDecimal(rs.getString(2)),
				rs.getLong(3), rs.getString(4)));
		
		return accounts.size() > 0 ? accounts.iterator().next() : null;		
	}

	public Integer count() {
		return super.count("accounts");
	}
	
	public void addAccount(Account account) {
		runUpdate("INSERT INTO accounts (id, amount, number, owner_id)"
				+ " VALUES ('" + account.getId() + "','" + account.getAmount() + 
				"','" + account.getNumber() + "','" + account.getOwnerId() + "')");
	}

	public Account makeDeposit(Account account, Check check) {
		return makeDeposit(account, check.getAmount());
	}
	public Account makeDeposit(Integer accountNumber, BigDecimal amount) {
		Account account = findByAccountNumber(accountNumber);
		return makeDeposit(account, amount);
	}

	public Account makeDeposit(Account account, BigDecimal amount) {
		runUpdate("UPDATE accounts SET amount = " + account.getAmount().add(amount).toString() + " WHERE id = " + account.getId());
		return findById(account.getId());
	}
	
	public Account transferMoney(Account from, Account to, BigDecimal amount) {
		runUpdate("UPDATE accounts SET amount = " + from.getAmount().subtract(amount).toString() + " WHERE id = " + from.getId());
		runUpdate("UPDATE accounts SET amount = " + to.getAmount().add(amount).toString() + " WHERE id = " + to.getId());
		return findById(from.getId());
	}
}
