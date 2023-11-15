# idporten-access-log-spring-boot-starter

[![Maven build status](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml/badge.svg)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml)
[![Latest Stable Version](https://img.shields.io/github/v/release/felleslosninger/idporten-access-log-spring-boot-starter?display_name=tag)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)


This spring boot starter is a library for Tomcat access log (version 9.0.X/10.0.X Tomcat) for Spring Boot project as JSON.
See [idporten-access-log-spring-boot-3-starter/src/main/resources/logback-access.xml](/idporten-access-log-spring-boot-3-starter/src/main/resources/logback-access.xml) for the syntax of logging.

## Requirements

To build and run the application you need:

* JDK 11
* Maven

## Build library

Build with Maven:
```
mvn clean install
```


## Usage
The library can be imported through maven with (see latest version under [releases](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)):
```xml
<dependency>
    <groupId>no.idporten.logging</groupId>
    <artifactId>idporten-access-log-spring-boot-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```
Or, for Spring Boot 3:
```xml
<dependency>
    <groupId>no.idporten.logging</groupId>
    <artifactId>idporten-access-log-spring-boot-3-starter</artifactId>
    <version>2.0.2</version>
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
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-instrumentation-api</artifactId>
</dependency>
```

### Configuration
The library is configured through the application.yml/yaml or the active spring profile yaml-file.
```yaml
spring:
  application:
    name: my-application-name
    environment: current-running-environment
```

If you want to disable the Tomcat access logging completely, e.g. for local development, you can set the following property:
```yaml
tomcat:
  accesslog: disabled
```
Set this property to 'enabled' or remove completely to enable Tomcat access logging.

Use your own logback-access.xml file or configure debug-logging:
```yaml
digdir:
  access:
    logging:
      debug-level: request
      config-file: my-logback.xml # will override debug setting
```
USE EITHER `debug-level` OR `config-file`, not both.
Valid values for debug-level are: 
* `request`: logging attribute `fullRequest` in addition to normal logging.
* `response`: logging attributes `fullRequest` and `fullResponse` in addition to normal logging.
* Use default config if not set or null.

NB: `debug-level` mode will log very much, use only temporary on servers with not to high load to avoid exhausting central logging-system.

## Troubleshooting
If you can not see any access logging in IntelliJ, then try Maven->reload project.
