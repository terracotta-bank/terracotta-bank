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
package com.joshcummings.codeplay.terracotta.app;

import com.fasterxml.jackson.databind.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.joshcummings.codeplay.terracotta.model.Client;
import com.joshcummings.codeplay.terracotta.service.ClientService;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * This filter makes the application vulnernable to
 * XXE and Insecure Deserialization both of which can be leveraged for
 * Denial of Service, Remote Code Execution, and Information Disclosure.
 *
 * It's vulnerable to these vectors, first because of allowing data to be
 * presented in these formats by the client, and second because of
 * weakly-configured parsers.
 *
 * @author Josh Cummings
 */
public class ContentParsingFilter implements Filter {
	private ClientService clientService;

	public ContentParsingFilter(ClientService clientService) {
		this.clientService = clientService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(wrapRequest((HttpServletRequest) request, (HttpServletResponse) response), response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException { }

	@Override
	public void destroy() {}

	private HttpServletRequest wrapRequest(HttpServletRequest request, HttpServletResponse response) {
		String contentType = Optional.ofNullable(request.getContentType()).orElse("");
		if (contentType.contains("xml")) {
			Map<String, Object> parameters = xmlDeserialize(request);
			return new HttpServletRequestParameterWrapper(request, parameters);
		} else if (contentType.contains("json")) {
			Map<String, Object> parameters = jsonDeserialize(request);
			return new HttpServletRequestParameterWrapper(request, parameters);
		} else if (contentType.contains("octet-stream")) {
			Map<String, Object> parameters = javaDeserialize(request);
			return new HttpServletRequestParameterWrapper(request, parameters);
		} else if (contentType.equals("application/jwt")) {
			Map<String, Object> parameters = jwtDeserialize(request, response);
			return new HttpServletRequestParameterWrapper(request, parameters);
		}
		return request;
	}

	// xml deserialization

	private Map<String, Object> xmlDeserialize(HttpServletRequest request) {
		try {
			InputStream body = request.getInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setXIncludeAware(true);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Element root = builder.parse(new InputSource(body)).getDocumentElement();
			return unmarshal(root);
		} catch ( Exception e ) {
			throw new IllegalArgumentException(e);
		}
	}

	private Map<String, Object> unmarshal(Element doc) {
		return unmarshal(doc, new LinkedHashMap<>());
	}

	private Map<String, Object> unmarshal(Node node, Map<String, Object> map) {
		NodeList children = node.getChildNodes();
		for ( int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String name = child.getNodeName();
			if (child instanceof Element && child.hasChildNodes() &&
					( child.getChildNodes().getLength() > 1 ||
							!"#text".equals(child.getChildNodes().item(0).getNodeName()))) {
				map.put(name, unmarshal(child, new LinkedHashMap<>()));
			} else {
				map.put(name, child.getTextContent());
			}
		}

		return map;
	}

	// json deserialization

	private Map<String, Object> jsonDeserialize(HttpServletRequest request) {
		try {
			InputStream body = request.getInputStream();
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enableDefaultTyping();
			return objectMapper.readValue(body, HashMap.class);
		} catch ( Exception e ) {
			throw new IllegalArgumentException(e);
		}
	}

	// java deserialization

	private Map<String, Object> javaDeserialize(HttpServletRequest request) {
		try {
			ObjectInputStream body = new ObjectInputStream(request.getInputStream());
			return (Map<String, Object>) body.readObject();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	// jwt deserialization

	private static Cache<String, String> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofSeconds(120)).build();

	private Map<String, Object> jwtDeserialize(HttpServletRequest request,
											   HttpServletResponse response) {
		try {
			return jwtDeserialize(request);
		} catch (Exception e) {
			response.setStatus(401);
			return Collections.emptyMap();
		}
	}

	private Map<String, Object> jwtDeserialize(HttpServletRequest request) throws Exception {
		String encodedJwt = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
		JWT jwt = JWTParser.parse(encodedJwt);
		Client client = this.clientService.findByClientId((String) jwt.getJWTClaimsSet().getClaim("clientId"));
		if (client == null) {
			throw new IllegalArgumentException("Could not find client");
		}

		// note that this construction could be done at startup time - inlined for convenience in demoing
		NimbusJwtDecoderJwkSupport support =
				new NimbusJwtDecoderJwkSupport(client.getKeySetUri().toString(), JwsAlgorithms.RS256);
		DelegatingOAuth2TokenValidator<Jwt> jwtValidator =
				new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(),
						j -> {
							String jti = j.getId();
							Instant iat = j.getIssuedAt();
							Instant now = Instant.now();
							if (iat == null || iat.isAfter(now) || iat.isBefore(now.minusSeconds(120))) {
								return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
							}
							if (cache.asMap().putIfAbsent(jti, jti) != null) {
								return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
							}
							return OAuth2TokenValidatorResult.success();
						});
		support.setJwtValidator(jwtValidator);

		return support.decode(encodedJwt).getClaims();
	}

	private static class HttpServletRequestParameterWrapper
		extends HttpServletRequestWrapper {
		private final Map<String, Object> parameters;

		public HttpServletRequestParameterWrapper
				(HttpServletRequest request, Map<String, Object> parameters) {
			super(request);
			this.parameters = parameters;
		}

		@Override
		public String getParameter(String name) {
			return Optional.ofNullable(this.parameters.get(name))
					.map(Object::toString)
					.orElse(super.getParameter(name));
		}
	}
}
