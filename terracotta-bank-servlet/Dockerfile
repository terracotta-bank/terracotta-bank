FROM anapsix/alpine-java:8_server-jre_unlimited

# Not secure! In a production environment, use haveged
RUN sed -i 's/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/urandom/g' /opt/jdk/jre/lib/security/java.security

COPY src/main/webapp /app/terracotta-bank/src/main/webapp
COPY target/terracotta-bank.jar /app/terracotta-bank/target/
COPY target/classes /app/terracotta-bank/target/classes/

WORKDIR /app/terracotta-bank

EXPOSE 8080

CMD ["java", "-jar", "target/terracotta-bank.jar"]