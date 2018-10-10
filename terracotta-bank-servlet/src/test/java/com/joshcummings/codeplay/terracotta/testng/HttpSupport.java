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
package com.joshcummings.codeplay.terracotta.testng;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;

import static org.apache.http.client.methods.RequestBuilder.get;

public class HttpSupport {
	protected CloseableHttpClient httpclient = HttpClients.custom()
			.setRedirectStrategy(new LaxRedirectStrategy())
			.build();

	protected HttpHost proxy = new HttpHost("localhost", 8081, "http");
	protected RequestConfig config;
	
	protected final String host;

	public HttpSupport() {
		this(null);
	}

	public HttpSupport(String host) {
		this.host = host == null ? "localhost:8080" : host;

		if ( this.host.startsWith("localhost") ) {
			config = RequestConfig.custom().build();
		} else {
			config = RequestConfig.custom().setProxy(proxy).build();
		}
	}

	public CloseableHttpResponse post(String path, BasicNameValuePair... body) throws IOException {
		return post(RequestBuilder.post(path).addParameters(body));
	}

	public CloseableHttpResponse post(RequestBuilder post) throws IOException {
		try ( CloseableHttpResponse csrf = getForEntity("/csrf.jsp") ) {
			String token = csrf.getStatusLine().getStatusCode() == 200 ?
					new String(IOUtils.toByteArray(csrf.getEntity().getContent())) : null;

			post.setUri("http://" + host + post.getUri());
			post.setConfig(config);
			post.addParameter("csrfToken", token);

			CloseableHttpResponse response = httpclient.execute(post.build());
			return response;
		}
	}

	public String postForContent(RequestBuilder post) {
		try ( CloseableHttpResponse response = post(post) ) {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} catch ( IOException e ) {
			throw new IllegalStateException(e);
		}
	}

	public int postForStatus(RequestBuilder post) {
		try ( CloseableHttpResponse response = post(post) ) {
			return response.getStatusLine().getStatusCode();
		} catch ( IOException e ) {
			throw new IllegalStateException(e);
		}
	}

	public CloseableHttpResponse getForEntity(String path) throws IOException {
		return getForEntity(RequestBuilder.get(path));
	}

	public CloseableHttpResponse getForEntity(RequestBuilder get) throws IOException {
		get.setUri("http://" + host + get.getUri());
		get.setConfig(config);
		return httpclient.execute(get.build());
	}

	public int getForStatus(RequestBuilder get) {
		try ( CloseableHttpResponse response = getForEntity(get) ) {
			return response.getStatusLine().getStatusCode();
		} catch ( IOException e ) {
			throw new IllegalStateException(e);
		}
	}

	public String session() throws IOException {
		try (CloseableHttpResponse response = getForEntity(get("/"))) {

			Header[] cookies = response.getHeaders("Set-Cookie");

			for ( Header cookie : cookies ) {
				if ( cookie.getValue().startsWith("JSESSIONID") ) {
					return cookie.getValue();
				}
			}

		}

		return null;
	}

	public String login(String username, String password) {
		return postForContent(RequestBuilder.post("/login")
				.addParameter("username", username)
				.addParameter("password", password));
	}

	public String logout() {
		return postForContent(RequestBuilder.post("/logout"));
	}
}
