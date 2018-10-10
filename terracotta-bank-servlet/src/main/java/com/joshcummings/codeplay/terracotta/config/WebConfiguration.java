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
package com.joshcummings.codeplay.terracotta.config;

import com.joshcummings.codeplay.terracotta.app.UserFilter;
import com.joshcummings.codeplay.terracotta.metrics.RequestClassificationFilter;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.CheckService;
import com.joshcummings.codeplay.terracotta.service.EmailService;
import com.joshcummings.codeplay.terracotta.service.MessageService;
import com.joshcummings.codeplay.terracotta.service.UserService;
import com.joshcummings.codeplay.terracotta.servlet.AccountServlet;
import com.joshcummings.codeplay.terracotta.servlet.AdminLoginServlet;
import com.joshcummings.codeplay.terracotta.servlet.ChangePasswordServlet;
import com.joshcummings.codeplay.terracotta.servlet.CheckLookupServlet;
import com.joshcummings.codeplay.terracotta.servlet.ContactUsServlet;
import com.joshcummings.codeplay.terracotta.servlet.EmployeeLoginServlet;
import com.joshcummings.codeplay.terracotta.servlet.ForgotPasswordServlet;
import com.joshcummings.codeplay.terracotta.servlet.LoginServlet;
import com.joshcummings.codeplay.terracotta.servlet.LogoutServlet;
import com.joshcummings.codeplay.terracotta.servlet.MakeDepositServlet;
import com.joshcummings.codeplay.terracotta.servlet.MessagesServlet;
import com.joshcummings.codeplay.terracotta.servlet.RegisterServlet;
import com.joshcummings.codeplay.terracotta.servlet.SendResponseServlet;
import com.joshcummings.codeplay.terracotta.servlet.SiteStatisticsServlet;
import com.joshcummings.codeplay.terracotta.servlet.TransferMoneyServlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.annotation.MultipartConfig;
import java.util.Arrays;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
	@Bean
	public Filter userFilter(AccountService accountService, UserService userService) {
		return new UserFilter(accountService, userService);
	}

	@Bean
	public FilterRegistrationBean requestClassificationFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.setFilter(new RequestClassificationFilter());
		bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR);
		return bean;
	}

	@Bean
	public ServletRegistrationBean accountsServlet(AccountService accountService) {
		return this.servlet(new AccountServlet(accountService), "/showAccounts");
	}

	@Bean
	public ServletRegistrationBean adminLoginServlet(AccountService accountService, UserService userService) {
		return this.servlet(new AdminLoginServlet(), "/adminLogin");
	}

	@Bean
	public ServletRegistrationBean changePasswordServlet(UserService userService) {
		return this.servlet(new ChangePasswordServlet(userService), "/changePassword");
	}

	@Bean
	public ServletRegistrationBean checkLookupServlet(CheckService checkService) {
		return this.servlet(new CheckLookupServlet(checkService), "/checkLookup");
	}

	@Bean
	public ServletRegistrationBean contactUsServlet(MessageService messageService) {
		return this.servlet(new ContactUsServlet(messageService), "/contactus");
	}

	@Bean
	public ServletRegistrationBean employeeLoginServlet(UserService userService) {
		return this.servlet(new EmployeeLoginServlet(userService), "/employeeLogin");
	}

	@Bean
	public ServletRegistrationBean forgotPasswordServlet(UserService userService) {
		return this.servlet(new ForgotPasswordServlet(userService), "/forgotPassword");
	}

	@Bean
	public ServletRegistrationBean loginServlet(AccountService accountService, UserService userService)  {
		return this.servlet(new LoginServlet(accountService, userService), "/login");
	}

	@Bean
	public ServletRegistrationBean logoutServlet() {
		return this.servlet(new LogoutServlet(), "/logout");
	}

	@Bean
	public ServletRegistrationBean makeDepositServlet(AccountService accountService, CheckService checkService) {
		ServletRegistrationBean bean = this.servlet(
				new MakeDepositServlet(accountService, checkService), "/makeDeposit");

		MultipartConfigElement element =
				new MultipartConfigElement(MakeDepositServlet.class.getAnnotation(MultipartConfig.class));

		bean.setMultipartConfig(element);

		return bean;
	}

	@Bean
	public ServletRegistrationBean messagesServlet(MessageService messageService) {
		return this.servlet(new MessagesServlet(messageService), "/showMessages");
	}

	@Bean
	public ServletRegistrationBean registerServlet(AccountService accountService, UserService userService) {
		return this.servlet(new RegisterServlet(accountService, userService), "/register");
	}

	@Bean
	public ServletRegistrationBean sendResponseServlet(EmailService emailService) {
		return this.servlet(new SendResponseServlet(emailService), "/sendResponse");
	}

	@Bean
	public ServletRegistrationBean siteStatisticsServlet(AccountService accountService, UserService userService) {
		return this.servlet(new SiteStatisticsServlet(accountService, userService), "/siteStatistics");
	}

	@Bean
	public ServletRegistrationBean transferMoneyServlet(AccountService accountService) {
		return this.servlet(new TransferMoneyServlet(accountService), "/transferMoney");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController( "/" ).setViewName( "forward:/index.jsp" );
		registry.setOrder( Ordered.HIGHEST_PRECEDENCE );
	}

	private ServletRegistrationBean servlet(Servlet servlet, String urlMapping) {
		ServletRegistrationBean bean = new ServletRegistrationBean();
		bean.setServlet(servlet);
		bean.setUrlMappings(Arrays.asList(urlMapping));
		return bean;
	}
}
