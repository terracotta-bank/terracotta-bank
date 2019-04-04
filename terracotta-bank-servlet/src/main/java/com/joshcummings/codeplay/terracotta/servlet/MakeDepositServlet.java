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
package com.joshcummings.codeplay.terracotta.servlet;

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.Check;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.CheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * This class makes Terracotta Bank vulnerable to Cross-site Scripting
 * as well as Malicious File Upload due to it not validating various
 * input fields.
 *
 * It is vulnerable to Cross-site Scripting because it doesn't encode
 * the {@code depositAccountNumber}, {@code depositAmount}, and
 * {@code depositCheckNumber} fields.
 *
 * It is vulnerable to Malicious File Upload because it doesn't
 * validate and encode the {@code depositCheckNumber} field nor
 * validate the contents of the {@code depositCheckImage} input stream.
 *
 * @author Josh Cummings
 */
//@WebServlet("/makeDeposit")
@MultipartConfig
public class MakeDepositServlet extends HttpServlet {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final long serialVersionUID = 1L;

	private static final Pattern IS_DOLLAR = Pattern.compile("([0-9]+)*\\.([0-9]{0,2})");

	private static final String CHECK_IMAGE_LOCATION = "images/checks";

	static {
		new File(CHECK_IMAGE_LOCATION).mkdirs();
	}
	
	private Integer nextCheckNumber = 1;

	private AccountService accountService;
	private CheckService checkService;

	public MakeDepositServlet(AccountService accountService, CheckService checkService) {
		this.accountService = accountService;
		this.checkService = checkService;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<String> errors = new ArrayList<>();

		Optional<Integer> accountNumber = tryParse(request.getParameter("depositAccountNumber"), Integer::parseInt, errors);
		Optional<BigDecimal> amount = tryParse(request.getParameter("depositAmount"),
				param -> {
					if (IS_DOLLAR.matcher(param).matches()) {
						return new BigDecimal(param);
					} else {
						return null;
					}
				}, errors);
		Optional<String> checkNumber = tryParse(request.getParameter("depositCheckNumber"), String::new, errors);

		Part image = request.getPart("depositCheckImage");

		if ( errors.isEmpty() )
		{
			Account account = this.accountService.findByAccountNumber(accountNumber.get());
			if ( image.getSubmittedFileName().endsWith(".zip") ) {
				this.checkService.updateCheckImagesBulk(checkNumber.get(), image.getInputStream());
			} else {
				this.checkService.updateCheckImage(checkNumber.get(), image.getInputStream());
			}

			Check check = new Check(String.valueOf(nextCheckNumber++), checkNumber.get(), amount.get(), account.getId());
			this.checkService.addCheck(check);
			
			account = this.accountService.makeDeposit(account, check);
			request.setAttribute("account", account);
			request.getRequestDispatcher("/WEB-INF/json/account.jsp").forward(request, response);
		}
		else
		{
			response.setStatus(400);
			request.setAttribute("message", errors.stream().findFirst().get());
			request.getRequestDispatcher("/WEB-INF/json/error.jsp").forward(request, response);
		}
	}

	private <E> Optional<E> tryParse(String possibleInteger, Function<String, E> parser, List<String> errors) {
		try {
			return Optional.of(parser.apply(possibleInteger));
		} catch ( Exception e ) {
			errors.add(possibleInteger + " is invalid");
			return Optional.empty();
		}
	}
}
