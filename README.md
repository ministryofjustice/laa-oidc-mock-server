# LAA OIDC Mock Server

A Spring Boot-based OpenID Connect (OIDC) mock server for testing and development purposes. This server simulates an OIDC provider, making it easier to develop and test OIDC client applications without depending on a production identity provider.

## Prerequisites

- Java 21 or higher
- Gradle 8.x or higher

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/ministryofjustice/laa-oidc-mock-server.git
cd laa-oidc-mock-server
```

### Build the Project

Using the Gradle wrapper:

```bash
./gradlew clean build
```

### Running the server via docker

#### Using docker-compose within this repository

For a fast way to run the server, you can use the docker-compose.yml file in this repository:
```bash
docker-compose up -d
```

#### Creating a docker image

First build the jar file:
```bash
./gradlew clean build
```

Build the docker image:

```bash
docker build . -t laa-oidc-mock-server:latest
```

Then include the service in your docker-compose.yml file for the application you wish to use
the OIDC mock server for:

```yaml
services:
  laa-mock-oidc-service:
    image: laa-oidc-mock-server:latest
    ports:
      - "9000:9000"
```

Then run the docker-compose file:
```shell
docker-compose up -d
```

### Run the Application (Old)

```bash
./gradlew bootRun
```

The server will start on the default port (usually 8080, check application.yml for specific configuration).

## Features

- OpenID Connect mock implementation
- Customizable ID Token generation
- User info endpoint simulation
- Spring Security integration

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── uk/
│   │       └── gov/
│   │           └── mockserver/
│   │               ├── IdTokenCustomizerConfig.java
│   │               └── LaaOidcMockServerApplication.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── uk/
            └── gov/
                └── mockserver/
                    └── LaaOidcMockServerApplicationTests.java
```

## Configuration

The application can be configured through the `application.yml` file located in `src/main/resources/`.

## Development

This project uses:
- Spring Boot 3.5.4
- Java 21
- Gradle as the build tool
