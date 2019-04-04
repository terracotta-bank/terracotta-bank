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
package com.joshcummings.codeplay.terracotta.app;

import com.joshcummings.codeplay.terracotta.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This filter makes users indirectly vulnerable to Session Hijacking
 * since it logs the session id to the request logs.
 *
 * @author Josh Cummings
 */
public class RequestLogFilter implements Filter {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Clock clock;

	public RequestLogFilter() {
		this(Clock.systemUTC());
	}

	public RequestLogFilter(Clock clock) {
		this.clock = clock;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException { }

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		Instant start = Instant.now(this.clock);
		try {
			chain.doFilter(req, resp);
		} finally {
			if (req instanceof HttpServletRequest) {
				Map<String, Object> attributes = computeRequestAttributes
						((HttpServletRequest) req, (HttpServletResponse) resp, start);
				this.log.info(attributes.toString());
			}
		}
	}

	Map<String, Object> computeRequestAttributes
			(HttpServletRequest request, HttpServletResponse response, Instant start) {
		Map<String, Object> attributes = new LinkedHashMap<>();

		attributes.put("requestId", UUID.randomUUID().toString());
		HttpSession session = request.getSession(false);
		if (session != null) {
			computeSessionIdForLogs(session, attributes);
			User user = (User) session.getAttribute("authenticatedUser");
			if (user != null) {
				attributes.put("userId", user.getId());
			}
		}

		String action = request.getRequestURI() +
			Optional.ofNullable(request.getQueryString())
				.map(query -> "?" + URLDecoder.decode(query)).orElse("");

		attributes.put("action", action);
		attributes.put("result", response.getStatus());
		attributes.put("duration", Duration.between(start, Instant.now(this.clock)).toMillis());
		return attributes;
	}

	void computeSessionIdForLogs(HttpSession session, Map<String, Object> attributes) {
		String hashed = Base64.getEncoder().encodeToString(
				session.getId().getBytes());
		attributes.put("sessionId", hashed);
	}

	@Override
	public void destroy() { }
}
