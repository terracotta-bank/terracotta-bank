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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;

public class SiteStatisticsFunctionalTest extends AbstractEmbeddedTomcatSeleniumTest {

	@Test
	public void testAdminLoginForPrivilegeEscalation() throws Exception {

		String jsessionIdCookie = http.session();

		// attempt to register the system user

		try (CloseableHttpResponse response =
					 http.post(post("/register")
							 .addParameter("registerUsername", "system")
							 .addParameter("registerPassword", "password")
							 .addParameter("registerEmail", "system@password.com")
							 .addParameter("registerName", "Backdoor Registration"))) {
		}

		// can I get to the backoffice pages now?

		try (CloseableHttpResponse response =
					 http.getForEntity(get("/siteStatistics")
							 .addHeader("Cookie", jsessionIdCookie))) {

			Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		}
	}
}
