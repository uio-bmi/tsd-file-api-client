# TSD-File-API-client
[![CodeFactor](https://www.codefactor.io/repository/github/uio-bmi/tsd-file-api-client/badge)](https://www.codefactor.io/repository/github/uio-bmi/tsd-file-api-client)
[![Download](https://img.shields.io/badge/GitHub%20Packages-Download-GREEN)](https://maven.pkg.github.com/uio-bmi/tsd-file-api-client/no.uio.ifi.tsd-file-api-client/2.0.0/tsd-file-api-client-2.0.0.jar)
## Protocol

More info on the protocol: https://data.tsd.usit.no/api/tsd-api-integration.html

## Maven Installation
To include this library to your Maven project add following to the `pom.xml`:

```xml

...

    <dependencies>
        <dependency>
            <groupId>no.uio.ifi</groupId>
            <artifactId>tsd-file-api-client</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>

...

    <repositories>
        <repository>
            <id>github</id>
            <name>uio-bmi-tsd-file-api-client</name>
            <url>https://maven.pkg.github.com/uio-bmi/tsd-file-api-client</url>
        </repository>
    </repositories>

...

```
