package com.joshcummings.codeplay.terracotta.testng;

import com.joshcummings.codeplay.terracotta.Mainer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class TomcatSupport {
	private ConfigurableApplicationContext context;

	public ApplicationContext startContainer() {
		context = SpringApplication.run(Mainer.class);
		return context;
	}
	
	public void stopContainer() {
		context.close();
	}

	public ApplicationContext getContext() {
		return context;
	}
}
