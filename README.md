# idporten-access-log-spring-boot-starter

[![Maven build status](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/pull-request.yml/badge.svg)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/actions/workflows/pull-request.yml)
[![Latest Stable Version](https://img.shields.io/github/v/release/felleslosninger/idporten-access-log-spring-boot-starter?display_name=tag)](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)


This project provides Spring Boot starter modules configuring Tomcat access logs in JSON (version 10.0.x and 11.0.x).
See [idporten-access-log-common/src/main/resources/logback-access.xml](/idporten-access-log-common/src/main/resources/logback-access.xml) for the syntax of logging.

## Provided artifacts
From stable version ``3.0.0`` the following artifacts are provided:

| Package Coordinates                                             | Description             |
|-----------------------------------------------------------------|-------------------------|
| `no.idporten.logging.idporten-access-log-spring-boot-3-starter` | Spring Boot 3.4.x/3.5.x |
| `no.idporten.logging.idporten-access-log-spring-boot-4-starter` | Spring Boot 4.0.x/4.1.x |
## Requirements

To build and run the application you need **Maven** and the Java version below.

![Java](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27java.version%27%5D&label=Java&color=orange)

Dependency versions are read live from each starter's `pom.xml` (via shields.io, from the `main` branch):

**Spring Boot 3 starter**  
![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-3-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27spring-boot.version%27%5D&label=Spring+Boot&color=blue) ![logback](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-3-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27logback.version%27%5D&label=logback&color=blue) ![logback-access](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-3-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27logback-access.version%27%5D&label=logback-access&color=blue) ![tomcat](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-3-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27tomcat.version%27%5D&label=tomcat&color=blue)

**Spring Boot 4 starter**  
![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-4-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27spring-boot.version%27%5D&label=Spring+Boot&color=blue) ![logback](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-4-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27logback.version%27%5D&label=logback&color=blue) ![logback-access](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-4-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27logback-access.version%27%5D&label=logback-access&color=blue) ![tomcat](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffelleslosninger%2Fidporten-access-log-spring-boot-starter%2Fmain%2Fidporten-access-log-spring-boot-4-starter%2Fpom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27tomcat.version%27%5D&label=tomcat&color=blue)

## Compatibility and upgrading
Each starter pins a `logback-core`/`logback-classic` version that matches its bundled `logback-access`
(logback-access performs a version check at startup and logs a warning on a mismatch; depending on the
`logback-core` version on the classpath it reads `For logback-core, expected version X but found Y` or
`Depender [logback-access-common] was expecting version X for dependency [logback-core] but found version Y`).

To upgrade safely:
* Prefer bumping the starter version — it brings a known-compatible Logback + logback-access set.
* If you override Logback yourself, keep `logback-core` and `logback-classic` on the version expected by your
  `logback-access` version, and verify startup logs are free of the version-mismatch warning above.

A safety-net test (`LogbackVersionCompatibilityTest`) runs in both starters and fails the build on a mismatch,
so mismatches are caught before release. It compares the `logback-core` version expected by `logback-access` with
the one actually on the classpath (independent of the warning's wording) and also fails on any version-related
warning the valve records at startup.

## Build library

Build with Maven:
```
mvn clean install
```


## Usage
The library can be imported through Maven with (see latest version under [releases](https://github.com/felleslosninger/idporten-access-log-spring-boot-starter/releases)):

### Spring Boot 3/4:
```xml
<dependency>
    <groupId>no.idporten.logging</groupId>
    <!-- Spring Boot 3.x -->
    <artifactId>idporten-access-log-spring-boot-3-starter</artifactId>
    <version>3.x.x</version>
    <!-- or Spring Boot 4.x -->
    <artifactId>idporten-access-log-spring-boot-4-starter</artifactId>
    <version>3.x.x</version>
</dependency>
```

And you probably already have included these:
```xml
<dependencies>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <!-- Spring Boot 3.x -->
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- or Spring Boot 4.x -->
        <artifactId>spring-boot-starter-webmvc</artifactId>
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
