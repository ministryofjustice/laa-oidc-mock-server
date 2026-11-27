[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-oidc-mock-server/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-oidc-mock-server)

# LAA OIDC Mock Server

A Spring Boot-based OpenID Connect (OIDC) mock server for testing and development purposes. This
server simulates an OIDC provider, making it easier to develop and test OIDC client applications
without depending on a production identity provider.

## Prerequisites

- JDK 25
- Docker with Docker Compose, if running the server in a container

Use the included Gradle wrapper; no separate Gradle installation is required.

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

The server listens on port 9000 by default.

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

The application can be configured through the `application.yml` file located in
`src/main/resources/`.

### Users

The server signs in the users listed under `mock.users`. Out of the box there is one:

```yaml
mock:
  users:
    - username: "provider.user@provider.com"
      password: "password"
```

Add an entry per user you need. Only `username` and `password` are required; each user may also set
`email`, `name`, `given-name`, `family-name`, `firm-code`, `firm-name` and `laa-accounts`:

```yaml
mock:
  users:
    - username: "provider.user@provider.com"
      password: "password"
    - username: "user.two@example.com"
      password: "another-password"
      firm-code: "9999"
      firm-name: "OTHER FIRM"
      laa-accounts: "7,8"
```

The same two users as environment variables, for a deployed server:

```
MOCK_USERS_0_USERNAME=provider.user@provider.com
MOCK_USERS_0_PASSWORD=password
MOCK_USERS_1_USERNAME=user.two@example.com
MOCK_USERS_1_PASSWORD=another-password
MOCK_USERS_1_FIRM_CODE=9999
MOCK_USERS_1_FIRM_NAME=OTHER FIRM
```

`email` defaults to the username, which usually matters: a client that looks its own user up by
email address will otherwise resolve every sign-in to the same person. Anything else you omit falls
back to the values below, so the defaults are unchanged for a single-user setup.

The whole list can also be passed in `SPRING_APPLICATION_JSON`.

### Environment variables

- `LAA_ACCOUNTS` (comma separated) controls the values emitted in the `LAA_ACCOUNTS` ID token
  claim.  
  Defaults to `1,123,0P322F`.
- `FIRM_CODE` controls the values emitted in the `FIRM_CODE` ID token claim.  
  Defaults to `1234`.
- `FIRM_NAME` controls the values emitted in the `FIRM_NAME` ID token claim.  
  Defaults to `TEST FIRM NAME`.

## Development

This project uses:

- Spring Boot 4
- Java 25 (Amazon Corretto on Alpine in the shared LAA container image)
- Gradle as the build tool

## ECR

The repo also builds and publishes an ECR image. This can be accessed by having role
access to the ECR repo. GitHub repositories can be added to the ECR configuration to grant access
on a per repository basis to allow pulling the OIDC mock server image in your GitHub actions.
Configuration can be found
[here](https://github.com/ministryofjustice/cloud-platform-environments/tree/main/namespaces/live.cloud-platform.service.justice.gov.uk/laa-oidc-mock-server-dev/resources/ecr.tf).

By raising and merging a PR to this repo, your GitHub repository will be granted the required
GitHub action variables to pull the OIDC mock server image from ECR:

- `LAA_MOCK_OIDC_SERVER_ECR_REPOSITORY`
- `LAA_MOCK_OIDC_SERVER_ECR_REGION`
- `LAA_MOCK_OIDC_SERVER_ECR_ROLE_TO_ASSUME`

