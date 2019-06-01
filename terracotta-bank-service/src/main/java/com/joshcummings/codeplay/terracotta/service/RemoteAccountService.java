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

import com.joshcummings.codeplay.terracotta.model.RemoteAccount;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

/**
 * This class makes Terracotta Bank vulnerable to SQL injection
 * attacks because it concatenates queries instead of using
 * bind variables.
 *
 * @author Josh Cummings
 */
@Service
public class RemoteAccountService extends ServiceSupport {
	public RemoteAccount findById(String id) {
		Set<RemoteAccount> accounts = runQuery("SELECT * FROM accounts WHERE id = " + id, (rs) ->
			new RemoteAccount(rs.getString(1), new BigDecimal(rs.getString(2)),
					rs.getLong(3), rs.getString(4)));
		return accounts.size() > 0 ? accounts.iterator().next() : null;
	}
	
	public RemoteAccount findByAccountNumber(Integer accountNumber) {
		Set<RemoteAccount> accounts = runQuery("SELECT * FROM accounts WHERE number = " + accountNumber, (rs) ->
			new RemoteAccount(rs.getString(1), new BigDecimal(rs.getString(2)),
				rs.getLong(3), rs.getString(4)));
		return accounts.size() > 0 ? accounts.iterator().next() : null;		
	}

	public RemoteAccount makeDeposit(Integer accountNumber, BigDecimal amount) {
		RemoteAccount account = findByAccountNumber(accountNumber);
		return makeDeposit(account, amount);
	}

	public RemoteAccount makeDeposit(RemoteAccount account, BigDecimal amount) {
		runUpdate("UPDATE accounts SET amount = " + account.getAmount().add(amount).toString() + " WHERE id = " + account.getId());
		return findById(account.getId());
	}
}
