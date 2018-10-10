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

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.testng.ITestContext;

import java.net.InetSocketAddress;

public class ProxySupport {
	protected static HttpProxyServer proxy;
	
	public void start(ITestContext ctx) {
		start(ctx.getName());
	}
	
	public void start(String type) {
		proxy = DefaultHttpProxyServer.bootstrap()
		        .withPort(8081)
		        .withServerResolver((host, port) -> {
						if ( host.equals(TestConstants.host) ||
								host.equals(TestConstants.evilHost)) {
							return new InetSocketAddress("docker".equals(type) ? "192.168.99.100" : "localhost", 8080);
						}
						return new InetSocketAddress(host, port);
					})
		        .start();
	}
	
	public void stop() {
		proxy.stop();
	}
	
	public static void main(String[] args) {
		new ProxySupport().start("tomcat");
	}
}
