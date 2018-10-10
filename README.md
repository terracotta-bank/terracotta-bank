# Terracotta Bank

Terracotta Bank is an intentionally-vulernable web application, useful for practicing detection, exploitation, and mitigation of common web application security vulnerabilities.

Terrcotta Bank binds locally to port 8080 by default, and while it is running, the machine on which it is running is vulnerable in the same way that this application is.

## Getting Started

Terracotta Bank is a fully-functional Spring Boot web application that lacks most common security mechanisms and makes numerous classic security mistakes.
 
To run Terracotta Bank, simply clone the repo and then run:

```bash
./gradlew bootRun
```

And browse to `localhost:8080` to begin looking for vulnerabilities.

## Usage

The intent of this application is to support engineers in practicing and learning about secure coding, ethical hacking, and detection and triage techniques. There are several ways to engage with this application to achieve this.

### Blind Pentesting

Because Terracotta Bank is a real web application, it can be effective to point popular pentesting tools like Burpsuite, ZAP, and sqlmap at it to discover vulnerabilities.

For example, try pointing [http://sqlmap.org/](sqlmap) at the `/login` endpoint to find more than one way to log in.

### Unit Tests

Terracotta Bank is equipped with dozens of intentionally failing unit tests. These are useful for praticing mitigation. As mitigation steps are added, the unit tests begin to pass, helping engineers evaluate the strength of their mitigations.

### Lessons

For a more guided approach, see the `/lessons` folder, which has written guides explaining the nature of a given vulnerability along with some hints for how to exploit and mitigate it in the application.
 
### Branches

For each lesson, there is a git branch with individual commits which progressively make the application more secure against the vulnerability described. Follow the commit messages to gain an understanding of good mitigations and why they are beneficial.

