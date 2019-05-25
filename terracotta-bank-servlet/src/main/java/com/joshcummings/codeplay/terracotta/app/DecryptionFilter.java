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

import org.springframework.util.StreamUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class tries to offer decryption, but fails in
 * that only a simple substitution cipher is employed.
 * Further, it employs no way to verify that the
 * data associated with the ciphertext hasn't been
 * altered in transit.
 *
 * @author Josh Cummings
 */
public class DecryptionFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String version = request.getHeader("X-Encryption");
		if (version == null) {
			chain.doFilter(request, response);
			return;
		}

		try {
			chain.doFilter(wrap(version, request), response);
		} catch (Exception e) {
			response.setStatus(401);
		}
	}

	private HttpServletRequest wrap(String version, HttpServletRequest request) throws Exception {
		String encoded = StreamUtils.copyToString(request.getInputStream(), UTF_8);
		byte[] ciphertext = Base64.getDecoder().decode(encoded.getBytes(UTF_8));
		ByteBuffer buffer = ByteBuffer.allocate(ciphertext.length);
		for (byte b : ciphertext) {
			buffer.put((byte)(((b + 128) + 13) % 256 - 128));
		}
		return new DecryptedWrapper(request, new ByteArrayInputStream(buffer.array()));
	}

	@Override
	public void destroy() {

	}

	private static final class DecryptedWrapper extends HttpServletRequestWrapper {
		private ServletInputStream inputStream;

		public DecryptedWrapper(HttpServletRequest request, InputStream is) {
			super(request);
			this.inputStream = new ServletInputStream() {
				@Override
				public boolean isFinished() {
					return available() < 0;
				}

				@Override
				public boolean isReady() {
					return available() > 0;
				}

				@Override
				public int available() {
					try {
						return is.available();
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}

				@Override
				public int read() throws IOException {
					return is.read();
				}

				@Override
				public void setReadListener(ReadListener listener) {
					throw new UnsupportedOperationException("unsupported");
				}
			};
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return this.inputStream;
		}
	}

	private static SecretKey secretKey() {
		byte[] bytes = Base64.getDecoder().decode("fYJE4bObiVAbhseUTXaRkg==".getBytes(UTF_8));
		return new SecretKeySpec(bytes, "AES");
	}

	private static RSAPublicKey publicKey() {
		try {
			byte[] bytes = Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtWO9vMYnCtL55JKSAkPLVZ8EzAcpSoNSM42UBdcaoZUks8SYQMYrshQmrcYB6RNcqglJX9EWCeD14y6nt5cTEsW6UAabZD/7Qj1tyJm50KA3UFwDov3n4xwtph5EAbLxw/DiFt6rN3kXwDiuzjuWg9ShmoxeE3LTTLVy/B+WP5YfeXoSOrGHTj/hpexDG5pYUIFPoDb79LzzBbghpQ3Pvwg1lkKAnL1OYLkv66V24DIBv/LeqGTGT95TpTdRpQpp2RvhopzntP88EyGJf3mRXq9TQ5isHypbvKuimBwE2Ww3Un9vu+HBn8p3n4P3TDcOxOVeAGtALUdflGaHJbNhIQIDAQAB");
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
		} catch ( Exception e ) {
			throw new IllegalArgumentException(e);
		}
	}

	private static RSAPrivateKey privateKey() {
		try {
			byte[] bytes = Base64.getDecoder().decode("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC1Y728xicK0vnkkpICQ8tVnwTMBylKg1IzjZQF1xqhlSSzxJhAxiuyFCatxgHpE1yqCUlf0RYJ4PXjLqe3lxMSxbpQBptkP/tCPW3ImbnQoDdQXAOi/efjHC2mHkQBsvHD8OIW3qs3eRfAOK7OO5aD1KGajF4TctNMtXL8H5Y/lh95ehI6sYdOP+Gl7EMbmlhQgU+gNvv0vPMFuCGlDc+/CDWWQoCcvU5guS/rpXbgMgG/8t6oZMZP3lOlN1GlCmnZG+GinOe0/zwTIYl/eZFer1NDmKwfKlu8q6KYHATZbDdSf2+74cGfynefg/dMNw7E5V4Aa0AtR1+UZocls2EhAgMBAAECggEAXWiYq97K+jr9LuT/xaTN0DDMkpjZfaK0sRGmeX91GmKofN6vnSOwGstfw7sk/rbW0EVqAKq2k63CUhSTj+p/ivpB4LYWTYDZTho+L8BiPPpUodBQmx3vzTeUlmgdk1ZoRAQHGcnfF/kG7xkBg/iRoR/dfK3uQEuwXl9OcGF/yQwRn83w6DFz//Q6NToUio4iAa2MyialSVdMrK49vjwwcMuP7JYp9kT4qhhJW7+AHoWAkcEJ71UjWvXZWARCEOKzw9G9kQ6kV/4OrBMaWvJbPrWPNz9eEcAZX15mdGuqYKlWFJlnua+4J5BZ7gREHhv85HzOJnal0xTFO+YOeA4GlQKBgQDxGX+duIB01NJLma0zIK6iRjeyDmkayy8GQK49e0OkVD4OWe59MlZ6t6SpWy+4vz3Kncjg2rg8UsQsSLi/SEBiLHYjyYEFsjneO/GkhLTdDy18vxZQzAmsUCpu5/1Bgl4oniIIgKT/ZEK++mNbEqHzcA3dvXggN+ZYHqjVmNyjnwKBgQDAmY4N4H+UZteTaYl/839CGzDzqJw43Ww6+bRsWRunj2G0TObRNFsrJHrhNa3G8Vu/eL8y5BDIVwpzHDf7NPewcDAYtxoY5N+ytIQRBSPLKEbkbkOtFFG3ZnAmt+EOqSOIoBXa7lNTlvAnSHR3R/iep20r5vKjHrE2TWZKqxvDPwKBgFMlMP5qZ1pjHpbVy4YrSi5KOuDb2WFVGsV0PuKTBNPB/ZijaPyiBHLTrCR/fuiegyfB3Em3A/xBqsd+2L+WoiV5IdDbp/QX7571WzMaVOk7V7uChqachuV2y/ttY8hYtjIZvrDh9ITixaXo4aTBuzohtZZ3xdqOjJBtGlp3GfgPAoGAN7XzJkZzAO1CnExhzpYTkH6nCUQTdEtwPBrwuoqH+k76NxorhgY7/N2/gZdyXeKA5IC75a+cvyaWSje2Zb6riujYsL1+GgdSQbH/paCz+tb6sqbGgrEm9gL2m+yCeCgqtCGCUOKxTbOSYfqOXdZ+rv3FEXbrZo3BOvDmjuCx1icCgYEAs4N8Y3R0tKmHRzUTUq9GVi1S8YOoPfOqL7bxXEfH2fgYt0zB8cJCjSzdpQ66+bY8gXSx9L32Js8eRgfpmxXtKdftCcdAO6fJfSTdSz/bZ9HrJkHquC/9lPTOVe8uSdBy9bkk2JF2Nk91azPCjyIdeWO4VMk5pzRe05z9O33U0SE=");
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
		} catch ( Exception e ) {
			throw new IllegalArgumentException(e);
		}
	}
}
