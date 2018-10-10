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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.joshcummings.codeplay.terracotta.model.Transaction;
import com.joshcummings.codeplay.terracotta.model.User;

import org.springframework.stereotype.Service;

/**
 * This service makes Terracotta vulnerable to DoS and Transaction
 * Hijacking
 *
 * It is vulnerable to DoS because there is neither an expiry nor
 * a cap on the {@code transactions} cache.
 *
 * It is vulnerable to Transaction Hijacking because the key is guessable.
 *
 * It is also vulnerable to hijacking because the incrementer is not atomic
 * and in the even of a lost update, two people could get the same
 * key.
 *
 * @author Josh Cummings
 */
@Service
public class TransactionService {
	private final Map<String, Transaction> transactions = new HashMap<>();
	private int keyCounter = 0;

	public Transaction beginTransaction(User user, String action) {
		String key = String.valueOf(++this.keyCounter);
		Transaction transaction = new Transaction(key, user, action);
		this.transactions.put(key, transaction);
		return transaction;
	}

	public Transaction retrieveTransaction(String key) {
		return this.transactions.get(key);
	}

	public void endTransaction(String key) {
		this.transactions.remove(key);
	}

	public Collection<Transaction> retrieveTransactionsForUser(User user) {
		Collection<Transaction> userTransactions = new ArrayList<>();
		for ( Map.Entry<String, Transaction> entry : this.transactions.entrySet() ) {
			User transactionUser = this.transactions.get(entry.getKey()).getUser();
			if ( transactionUser.equals(user.getId()) ) {
				userTransactions.add(entry.getValue());
			}
		}
		return userTransactions;
	}

	public void endAllTransactionsForUser(User user) {
		for ( Map.Entry<String, Transaction> entry : this.transactions.entrySet() ) {
			User transactionUser = this.transactions.get(entry.getKey()).getUser();
			if ( transactionUser.equals(user.getId()) ) {
				this.transactions.remove(entry.getKey());
			}
		}
	}
}
