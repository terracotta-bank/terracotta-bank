package com.joshcummings.codeplay.enumerationtester;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.apache.http.client.methods.RequestBuilder.post;

@SpringBootApplication
public class EnumerationTesterApplication implements CommandLineRunner {

	private static HttpSupport http;
	private static Map<String, String> responses = new ConcurrentHashMap<>();
	private static String control;

	private static void test(String username) {
		System.out.printf("Attempting [%s]...", Color.YELLOW + username + Color.RESET);
		String content = http.postForContent(
				post("/forgotPassword").addParameter("forgotPasswordAccount", username));
		content = content.replaceAll(username, "");

		if (responses.isEmpty()) {
			System.out.printf("%nResponse (with username removed): [%s]", content);
			control = content;
		} else if ( !responses.containsKey(content)) {
			System.out.printf(Color.RED + "potential match found" + Color.RESET);
		}
		System.out.printf("%n");
		responses.put(content, username);
	}

	private static Stream<String> readAllLines(String filename) {
		InputStream is = EnumerationTesterApplication.class.getClassLoader().getResourceAsStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return br.lines();
	}

	private static Stream<String> firstNames() {
		return Stream.of("josh");
	}

	private static Stream<String> lastNames() {
		return readAllLines("last-names.csv");
	}

	@Override
	public void run(String... args) throws Exception {
		http = new HttpSupport(args.length == 1 ? args[0] : "localhost:8080", false);

		// our control value
		test(UUID.randomUUID().toString());

		Set<String> usernamesToTest = new HashSet<>();
		firstNames().forEach((firstName) ->
				lastNames().forEach((lastName) -> {
					usernamesToTest.add(firstName.toLowerCase());
					usernamesToTest.add(firstName.toLowerCase().substring(0, 1) + lastName.toLowerCase());
					usernamesToTest.add(firstName.toLowerCase() + "." + lastName.toLowerCase());
				}));

		System.out.printf("Will test [%d] usernames%n", usernamesToTest.size());
		usernamesToTest.stream().forEach(EnumerationTesterApplication::test);
		responses.remove(control);
		System.out.printf("These appear to be legit usernames: %s%n",
				Color.RED_BOLD_BRIGHT + String.valueOf(responses.values()) + Color.RESET);
	}

	public static void main(String[] args) {
		SpringApplication.run(EnumerationTesterApplication.class, args);
	}
}
