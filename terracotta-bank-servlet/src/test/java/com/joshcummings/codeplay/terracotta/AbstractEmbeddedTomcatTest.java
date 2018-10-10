package com.joshcummings.codeplay.terracotta;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.joshcummings.codeplay.terracotta.testng.DockerSupport;
import com.joshcummings.codeplay.terracotta.testng.HttpSupport;
import com.joshcummings.codeplay.terracotta.testng.ProxySupport;
import com.joshcummings.codeplay.terracotta.testng.SeleniumSupport;
import com.joshcummings.codeplay.terracotta.testng.TestConstants;
import com.joshcummings.codeplay.terracotta.testng.TomcatSupport;
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

import org.springframework.context.ApplicationContext;

import static org.apache.http.client.methods.RequestBuilder.post;

public class AbstractEmbeddedTomcatTest {
	protected TomcatSupport tomcat = new TomcatSupport();
	protected DockerSupport docker = new DockerSupport();
	protected HttpSupport http = new HttpSupport();

	protected ApplicationContext context;

	@BeforeTest(alwaysRun=true)
	public void start(ITestContext ctx) throws Exception {
		if ( "docker".equals(ctx.getName()) ) {
			docker().startContainer();
		} else {
			context = tomcat.startContainer();
		}
	}

	@AfterTest(alwaysRun=true)
	public void stop(ITestContext ctx) throws Exception {
		if ( "docker".equals(ctx.getName()) ) {
			docker().stopContainer();
		} else {
			tomcat.stopContainer();
		}
	}

	protected DockerSupport docker() {
		return docker == null ? ( docker = new DockerSupport() ) : docker;
	}

	protected String login(String username, String password) {
		return http.postForContent(post("/login")
				.addParameter("username", username)
				.addParameter("password", password));
	}

	protected void logout() {
		http.postForContent(post("/logout"));
	}
}
