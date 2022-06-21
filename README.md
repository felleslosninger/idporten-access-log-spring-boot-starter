# idporten-access-log-spring-boot-starter

[![Maven build status](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml/badge.svg)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml)
[![Latest Stable Version](https://img.shields.io/github/v/release/felleslosninger/idporten-access-log-spring-boot-starter?display_name=tag)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)


This spring boot starter is a library for Tomcat access log (v9) for Spring Boot project as JSON.
See src/main/resources/logback-access.yaml for the syntax of logging.

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

### Configuration
The library is configured through the application.yml file.
```yaml
accesslog-application-name: my-application-name
```
This property must be on root since only can import properties in logback-access.xml, no spring properties.

To load the lib you must add e.g.
```
@Import({ AccessLogsConfiguration.class }) 
```
In your SpringBootApplication class.