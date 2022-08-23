# idporten-access-log-spring-boot-starter

[![Maven build status](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml/badge.svg)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml)
[![Latest Stable Version](https://img.shields.io/github/v/release/felleslosninger/idporten-access-log-spring-boot-starter?display_name=tag)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)


This spring boot starter is a library for Tomcat access log (version 9.0.X Tomcat) for Spring Boot project as JSON. Not tested on Tomcat 10 (will most likely fail on Tomcat 10).
See [src/main/resources/logback-access.yaml for the syntax of logging].

## Requirements

To build and run the application you need:

* JDK 17
* Maven

## Build library

Build with Maven:
```
mvn clean install
```


## Usage
The library can be imported through maven with:
```xml
<dependency>
    <groupId>no.idporten.logging</groupId>
    <artifactId>idporten-access-log-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

Also needs dependency:
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-access</artifactId>
</dependency>
```
And you probably already have included these:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
</dependency>
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>${logstash.logback.version}</version>
</dependency>

```

### Configuration
The library is configured through the application.yml file.
```yaml
accesslog-application-name: my-application-name
```
This property must be on root since only can import properties in logback-access.xml, no spring properties.


## Troubleshooting
If you can not see any access logging in IntelliJ, then try Maven->reload project.
