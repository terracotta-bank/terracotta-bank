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
package com.joshcummings.codeplay.terracotta.network;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class IpAddressResolver {
	private final boolean proxied;

	public IpAddressResolver(boolean proxied) {
		this.proxied = proxied;
	}

	public String ipAddress(HttpServletRequest request) {
		if ( this.proxied ) {
			String xForwardedFor = request.getHeader("x-forwarded-for");
			if ( StringUtils.hasText(xForwardedFor) ) {
				return xForwardedFor.split(",")[0];
			}
		}

		return request.getRemoteAddr();
	}
}
