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

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.joshcummings.codeplay.terracotta.testng.HttpSupport;
import com.joshcummings.codeplay.terracotta.testng.ProxySupport;
import com.joshcummings.codeplay.terracotta.testng.SeleniumSupport;
import com.joshcummings.codeplay.terracotta.testng.TestConstants;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

public class AbstractEmbeddedTomcatSeleniumTest extends AbstractEmbeddedTomcatTest {
	static WebDriver driver;
	
	protected SeleniumSupport selenium = new SeleniumSupport();
	protected ProxySupport proxy = new ProxySupport();
	protected HttpSupport honest = new HttpSupport(TestConstants.host);
	
	@BeforeTest(alwaysRun=true)
	public void startSelenium() {
		driver = selenium.start();
	}
	
	@BeforeTest(alwaysRun=true)
	public void startProxy(ITestContext ctx) {
		proxy.start(ctx);
	}
	
	@BeforeTest(groups="filesystem")
	public void startClamav(ITestContext ctx) throws Exception {
		if ( "docker".equals(ctx.getName()) ) {
			docker().startClamav();
		}
	}

	@AfterTest(alwaysRun=true)
	public void shutdownSelenium() {
		selenium.stop(driver);
	}
	
	@AfterTest(alwaysRun=true)
	public void shutdownProxy() {
		proxy.stop();
	}
	
	@AfterTest(groups="filesystem")
	public void stopClamav(ITestContext ctx) throws Exception {
		if ( "docker".equals(ctx.getName()) ) {
			docker().stopClamav();
		}
	}

	protected void goToPage(String page) {
		driver.get("http://" + TestConstants.host + page);
	}
	
	protected String login(String username, String password) {
		super.login(username, password);
		goToPage("/");
		driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("login")).submit();
        FluentWait<WebDriver> wait = new WebDriverWait(driver, 2).pollingEvery(100, TimeUnit.MILLISECONDS);
        wait.until((Function<WebDriver, Boolean>)driver -> driver.findElement(By.id("service")) != null);
        return driver.getPageSource();
	}
	
	protected void employeeLogin(String username, String password) {
		goToPage("/employee.jsp");
		driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("login")).submit();
        FluentWait<WebDriver> wait = new WebDriverWait(driver, 2).pollingEvery(100, TimeUnit.MILLISECONDS);
        wait.until(driver -> driver.findElement(By.id("service")) != null);
	}
	
	protected void logout() {
		super.logout();
		goToPage("/logout");
	}
	
	protected String getTextThenDismiss(Alert alert) {
		String text = alert.getText();
		alert.dismiss();
		return text;
	}
	
	protected void ignoreErrors(Runnable r) {
		try { 
			r.run();
		} catch ( Exception e ) {
			// eat
		}
	}
	
	protected Alert switchToAlertEventually(WebDriver driver, long timeoutInMilliseconds) throws NoAlertPresentException {
		long now = System.currentTimeMillis();
		try {
			return driver.switchTo().alert();
		} catch ( NoAlertPresentException e ) {
			if ( timeoutInMilliseconds <= 0 ) {
				throw e;
			}
		}
		
		try {
			Thread.sleep(100);
			return switchToAlertEventually(driver, timeoutInMilliseconds - ( System.currentTimeMillis() - now ));
		} catch ( InterruptedException e ) {
			Thread.currentThread().interrupt();
			throw new NoAlertPresentException(e);
		}
	}
	
	protected WebElement findElementEventually(WebDriver driver, By by, long timeoutInMilliseconds) throws NoSuchElementException {
		long now = System.currentTimeMillis();
		try {
			return driver.findElement(by);
		} catch ( NoSuchElementException e ) {
			if ( timeoutInMilliseconds <= 0 ) {
				throw e;
			}
		}
		
		try {
			Thread.sleep(100);
			return findElementEventually(driver, by, timeoutInMilliseconds - ( System.currentTimeMillis() - now ));
		} catch ( InterruptedException e ) {
			Thread.currentThread().interrupt();
			throw new NoSuchElementException(e.getMessage());
		}
	}
}
