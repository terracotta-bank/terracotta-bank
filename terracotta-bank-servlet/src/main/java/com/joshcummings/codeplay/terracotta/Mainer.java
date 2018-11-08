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

import com.joshcummings.codeplay.terracotta.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.UUID;

/**
 * @author Josh Cummings
 */
@SpringBootApplication
public class Mainer extends SpringBootServletInitializer
		implements ApplicationListener<ContextRefreshedEvent> {

	private final Logger log = LoggerFactory.getLogger(Mainer.class);

	@Autowired
	UserService userService;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Mainer.class);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		String password = UUID.randomUUID().toString();

		int updated =
				this.userService.maybeChangeAdminPassword(password);

		if ( updated > 0 ) {
			log.warn("", password);
			log.warn("Default password was 'admin', set to {}", password);
			log.warn("", password);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Mainer.class, args);
	}
}
