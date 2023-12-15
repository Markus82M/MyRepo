package com.example.kyn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;

@SpringBootApplication
public class WeatherApplication {

	private static final Logger log = LoggerFactory.getLogger(WeatherApplication.class);
	private final Environment env;

	public WeatherApplication(Environment env) {
		this.env = env;
	}

	@PostConstruct
	public void initApplication() {
		Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
		if (activeProfiles.contains("dev") && activeProfiles.contains("prod")) {
			log.error("You have misconfigured your application! It should not run " +
					"with both the 'dev' and 'prod' profiles at the same time.");
		}
		if (activeProfiles.contains("dev") && activeProfiles.contains("cloud")) {
			log.error("You have misconfigured your application! It should not " +
					"run with both the 'dev' and 'cloud' profiles at the same time.");
		}
	}

	public static void main(String[] args) {

		SpringApplication.run(WeatherApplication.class, args);

		Runtime runtime = Runtime.getRuntime();

		final NumberFormat format = NumberFormat.getInstance();

		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		final long mb = 1024 * 1024;
		final String mbStr = " MB";

		log.info("\n========================== Memory Info =========================="
				+ "\nFree memory: \t\t" + format.format(freeMemory / mb) + mbStr
				+ "\nAllocated memory: \t" + format.format(allocatedMemory / mb) + mbStr
				+ "\nMax memory: \t\t" + format.format(maxMemory / mb) + mbStr
				+ "\nTotal free memory: \t " + format.format((freeMemory + (maxMemory - allocatedMemory)) / mb) + mbStr
				+ "\n=================================================================\n"
		);
	}

}
