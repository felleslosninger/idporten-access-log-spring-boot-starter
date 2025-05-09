# idporten-access-log-spring-boot-starter

[![Maven build status](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml/badge.svg)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/call-maventests.yml)
[![Latest Stable Version](https://img.shields.io/github/v/release/felleslosninger/idporten-access-log-spring-boot-starter?display_name=tag)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)


This spring boot starter is a library for Tomcat access log (version 10.0.X Tomcat) for Spring Boot project as JSON.
See [idporten-access-log-spring-boot-3-starter/src/main/resources/logback-access.xml](/idporten-access-log-spring-boot-3-starter/src/main/resources/logback-access.xml) for the syntax of logging.

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
The library can be imported through Maven with (see latest version under [releases](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)):

### Spring Boot 3:
```xml
<dependency>
    <groupId>no.idporten.logging</groupId>
    <artifactId>idporten-access-log-spring-boot-3-starter</artifactId>
    <version>2.8.0</version>
</dependency>
```

And you probably already have included these:
```xml
<dependencies>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
    </dependency>
    
</dependencies>
```


### Configuration
The library is configured through the application.yml/yaml or the active spring profile yaml-file.
```yaml
spring:
  application:
    name: my-application-name
    environment: current-running-environment
```

The library uses the standard tomcat accesslog property for enabling or disabling logging:
```yaml
server:
  tomcat:
    accesslog:
      enabled: true # default is true if not set
```
Only include this property if you need to disable access logging, since it is always default enabled.

Use your own logback-access.xml file or configure debug-logging:
```yaml
digdir:
  access:
    logging:
      debug-level: request # [request|response],  default config if not set or null
      config-file: my-logback.xml # will override debug setting
      filtering:
        static-resources: true # filters out static resources. default is true
        paths: /config.json, /.well-known # comma-separated list of paths to filter out. Matches paths using .startsWith(). Default is empty. 
```
USE EITHER `debug-level` OR `config-file`, not both.
Valid values for debug-level are: 
* `request`: logging attribute `fullRequest` in addition to normal logging.
* `response`: logging attributes `fullRequest` and `fullResponse` in addition to normal logging.
* Use default config if not set or null.

NB: `debug-level` mode will log very much, use only temporary on servers with not to high load to avoid exhausting central logging-system.

## Troubleshooting
If you can not see any access logging in IntelliJ, then try Maven->reload project.
