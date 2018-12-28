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
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.testng.Assert.*;

public class RequestLogFilterTests {
	private User user = new User("1", "test", "test", "Test", "test@test");
	private Clock fixed = Clock.fixed(Instant.now(), ZoneId.systemDefault());

	private RequestLogFilter filter = new RequestLogFilter(this.fixed);

	@Test
	public void computeRequestAttributesWhenLoggedInThenIncludesUserId() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.getSession().setAttribute("authenticatedUser", this.user);

		Map<String, Object> attributes =
				this.filter.computeRequestAttributes(
						request, response, this.fixed.instant());

		assertEquals("1", attributes.get("userId"));
	}

	@Test
	public void computeRequestAttributesWhenUnauthenticatedThenIncludesBasicAttributes() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
		request.setSession(new MockHttpSession());
		MockHttpServletResponse response = new MockHttpServletResponse();

		Instant oneSecondAgo = this.fixed.instant().minusSeconds(1L);

		Map<String, Object> attributes =
				this.filter.computeRequestAttributes(
						request, response, oneSecondAgo);

		assertNotNull(attributes.get("requestId"));
		assertNotNull(attributes.get("sessionId"));
		assertNull(attributes.get("userId"));
		assertEquals(request.getRequestURI(), attributes.get("action"));
		assertEquals(response.getStatus(), attributes.get("result"));
		assertEquals(1000L, attributes.get("duration"));
	}
}
