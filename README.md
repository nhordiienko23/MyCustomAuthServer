# MyCustomAuthServer

A standalone Spring Boot application implementing a custom OAuth2 Authorization Server and OpenID Connect provider.

The server provides user registration and authentication, issues OAuth2 access and refresh tokens, and can be used as a centralized Identity Provider for other Spring Boot applications, such as the Team Management System.


## 🚀 Key Features


* **OAuth2 Authorization Server::** Implements the OAuth2 Authorization Code and Refresh Token grant types and supports registration of client applications.
* **OpenID Connect:** Enables OIDC with openid, profile, and email scopes and provides the standard discovery and UserInfo endpoints.
* **JWT Token Issuance:** Generates and signs JWT tokens using an RSA key pair and exposes the public key through the JWK Set endpoint.
* **Custom ID Token Claims:** Adds the authenticated user's email as a custom claim to the ID token.
* **User Registration and Authentication:** Provides user registration and login using Spring Security, custom UserDetails, UserDetailsService, and BCrypt password hashing.
* **Login Tracking:** Listens for successful authentication events and updates the user's lastLoginAt timestamp in PostgreSQL.
* **PostgreSQL Persistence:** Stores user data in PostgreSQL using Spring Data JPA and Hibernate.
* **Dockerized Environment:** Provides Docker Compose configuration for running the application together with PostgreSQL.
* **Integration Testing:** Integration tests use Spring Boot Test, MockMvc, Spring Security Test, and PostgreSQL to verify authentication, registration, security configuration, OAuth2/OIDC endpoints, and authentication event handling.


## 🛠 Tech Stack

* **Core:** Java 21, Spring Boot 3
* **Security:** Spring Security, Spring Authorization Server
* **Data Access:** Spring Data JPA, Hibernate
* **Database:** PostgreSQL
* **Templating:** Thymeleaf (for login and registration views)
* **Build Tool:** Maven
* **Infrastructure:** Docker & Docker Compose
* **Testing:** JUnit 5, Spring Boot Test, Testcontainers


## 🏗 Project Architecture


* **`AuthorizationServerConfig`:** The core configuration class. It sets up the OAuth2 endpoints, configures the `SecurityFilterChain` for the authorization server, registers client applications, and manages the RSA key pairs for signing JWTs.
* **`TokenCustomizer`:** Intercepts the JWT creation process to inject custom claims (like `email`) into the `id_token`.
* **Controllers & Views:** Handles the user-facing `/login` and `/register` endpoints using Thymeleaf templates.
* **Services & Repositories:** Manages user data persistence using Spring Data JPA.
* **Infrastructure:**
    * `docker-compose.yml`: Defines the `my_auth_server_db` PostgreSQL instance and the application container.
    * `application.properties`: Configures server ports, database connections, and session handling.


## ⚙️ Configuration & Setup

By default, the server is configured to run on port `9000` and connect to a PostgreSQL database on port `5444`.


### application.properties Highlights

```properties
spring.application.name=MyCustomAuthServer
server.port=9000

spring.datasource.url=jdbc:postgresql://localhost:5444/my_auth_server_db
spring.datasource.username=auth_user
spring.datasource.password=auth_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.servlet.session.cookie.name=AUTH_SESSION
spring.main.allow-bean-definition-overriding=true
```


### Registered Clients
The server is pre-configured with a default client application in AuthorizationServerConfig. If you need to connect a different application, you must update or add a RegisteredClient bean.

**Default Client Configuration:**

Client ID: `my-client-id`

Client Secret: `my-client-secret`

Redirect URI: `http://localhost:8080/login/oauth2/code/my-custom-auth` (This matches the Team Management System's expected callback).

Allowed Scopes: `openid`, `profile`, `email`

Grant Types: `authorization_code`, `refresh_token`


## 🚀 Quick Start

### Prerequisites

* **Java 21** (if running locally without Docker)
* **Maven** 
* **Docker & Docker Compose** (for containerized execution)


### Installation & Run

1. **Clone the repository:**
   
```bash
git clone [https://github.com/nhordiienko23/MyCustomAuthServer.git](https://github.com/nhordiienko23/MyCustomAuthServer.git)
cd MyCustomAuthServer
```

2. **Build the project:**
   
```bash
docker-compose up --build
```
The server will start and be available at `http://localhost:9000`.


## 🔗 Integration Guide (How to use it as an Identity Provider)
To use `MyCustomAuthServer` to protect another Spring Boot application (like a resource server or a client application), you need to configure the client application to point to this server.

In your client application's configuration (e.g., `application.yml`), set the OAuth2 provider details to match this server:

```bash
spring:
  security:
    oauth2:
      client:
        registration:
          my-custom-auth:
            client-name: My Custom Auth
            client-id: my-client-id
            client-secret: my-client-secret
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope:
              - openid
              - profile
              - email
        provider:
          my-custom-auth:
            authorization-uri: http://localhost:9000/oauth2/authorize
            token-uri: http://localhost:9000/oauth2/token
            user-info-uri: http://localhost:9000/userinfo
            jwk-set-uri: http://localhost:9000/oauth2/jwks
            user-name-attribute: sub
```
