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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class LockoutDecisionManager {
	private LoadingCache<String, AtomicInteger> failedAttemptsByUsername;
	private LoadingCache<String, AtomicInteger> failedAttemptsByIpAddress;

	private int maxConsecutiveUsernameFailures;
	private int maxConsecutiveIpAddressFailures;

	public LockoutDecisionManager() {
		this(5, 30000, 25, 180000);
	}

	public LockoutDecisionManager(
			int maxConsecutiveUsernameFailures,
			int failedUsernameSoftLockout,
			int maxConsecutiveIpAddressFailures,
			int failedIpAddressSoftLockout) {

		this.maxConsecutiveUsernameFailures = maxConsecutiveUsernameFailures;
		this.maxConsecutiveIpAddressFailures = maxConsecutiveIpAddressFailures;

		this.failedAttemptsByUsername = CacheBuilder.newBuilder()
				.expireAfterWrite(Duration.ofMillis(failedUsernameSoftLockout))
				.build(defaultToZero());

		this.failedAttemptsByIpAddress = CacheBuilder.newBuilder()
				.expireAfterWrite(Duration.ofMillis(failedIpAddressSoftLockout))
				.build(defaultToZero());

	}

	public boolean tooManyFailedAttempts(String username, String ipAddress) {
		return
				this.failedAttemptsByUsername.getUnchecked(username).intValue() >=
						this.maxConsecutiveUsernameFailures ||
				this.failedAttemptsByIpAddress.getUnchecked(ipAddress).intValue() >=
						this.maxConsecutiveIpAddressFailures;
	}

	public void failedLogin(String username, String ipAddress) {
		this.failedAttemptsByUsername.getUnchecked(username).incrementAndGet();
		this.failedAttemptsByIpAddress.getUnchecked(ipAddress).incrementAndGet();
	}

	public void successfulLogin(String username, String ipAddress) {
		this.failedAttemptsByUsername.invalidate(username);
		this.failedAttemptsByIpAddress.invalidate(ipAddress);
	}

	private CacheLoader<String, AtomicInteger> defaultToZero() {
		return new CacheLoader<String, AtomicInteger>() {
			@Override
			public AtomicInteger load(String key) {
				return new AtomicInteger(0);
			}
		};
	}
}
