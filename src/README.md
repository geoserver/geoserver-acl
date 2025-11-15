# GeoServer ACL - Source Code Architecture

> For user documentation, features, and deployment instructions, see the [main README](../README.md).

This document describes the architecture and code organization of the GeoServer ACL service.

## Architectural Vision

The codebase is structured around **Clean Architecture**, strictly separating the project into three core layers. This design enforces the **Dependency Rule**: all dependencies must point inwards, from outer layers to inner layers.

This means our core business logic (`Domain`) knows nothing about the application's use cases, and neither of them knows anything about technical details like databases or web frameworks (`Infrastructure`).

```
+---------------------------------------------------------------------+
|                              Infrastructure                         |
| (Adapters: Web API, Persistence, Messaging, Spring Config, etc.)    |
+---------------------------------------------------------------------+
                                   |
                                   v
+---------------------------------------------------------------------+
|                               Application                           |
| (Use Cases: AuthorizationService)                                   |
+---------------------------------------------------------------------+
                                   |
                                   v
+---------------------------------------------------------------------+
|                                 Domain                              |
| (Entities: Rule, AdminRule. Ports: RuleRepository)                  |
+---------------------------------------------------------------------+
```

This separation allows us to test business logic without frameworks, swap technical implementations (e.g., JPA for persistence technology), and evolve the core logic independently of the delivery mechanism.

## Module Breakdown

The project is organized into three top-level directories, each representing an architectural layer.

### The Domain Layer (`domain/`)

This layer contains the heart of the application: the business objects and rules. It has **zero dependencies** on any other layer or framework.

-   **`domain/access-criteria/`**: A framework for building query specifications to filter rules.
-   **`domain/data-rules/`**: Defines the `Rule` entity and its repository port (`RuleRepository`), which govern access to GeoServer data.
-   **`domain/workspaceadmin-rules/`**: Defines the `AdminRule` entity and its repository port (`AdminRuleRepository`), which govern workspace administration rights.

### The Application Layer (`application/`)

This layer orchestrates the domain objects to perform specific application tasks or use cases. It depends only on the `domain` layer.

-   **`application/authorization-api/`**: Defines the primary application port, the `AuthorizationService` interface, and its required data structures (`AccessRequest`, `AccessInfo`).
-   **`application/authorization-impl/`**: A concrete implementation of `AuthorizationService` that contains the core authorization logic. It has a dependency on GeoTools for spatial operations.

### The Infrastructure Layer (`infrastructure/`)

This layer contains all the technical details: frameworks, database access, web controllers, and any other code that communicates with the outside world. It is organized into a series of **adapters** that implement the ports defined by the inner layers.

-   **`infrastructure/app-main/`**: The **Composition Root**. This is the main Spring Boot application (`gs-acl-app`) that wires together and runs the entire service.
-   **`infrastructure/spring-config/`**: General Spring `@Configuration` for wiring domain and application services with their infrastructure implementations.
-   **`infrastructure/persistence/jpa-adapter/`**: A **Secondary Adapter** that implements the `RuleRepository` and `AdminRuleRepository` ports from the domain layer using Spring Data JPA.
-   **`infrastructure/web-api/`**: A **Primary Adapter** containing all components for the REST API.
    -   `web-api/spec/`: The OpenAPI 3.0 specification file ([geoserver-acl-web-api-spec-v1.yaml](./infrastructure/web-api/spec/src/main/resources/geoserver-acl-web-api-spec-v1.yaml)).
    -   `web-api/v1/generated-model/`: API DTO models generated from the OpenAPI spec.
    -   `web-api/v1/spring/model-mapping-adapter/`: Maps between the API DTOs and the internal domain/application models.
    -   `web-api/v1/spring/server-adapter/`: The Spring REST controllers that implement the generated API interfaces.
    -   `web-api/v1/spring/client-adapter/`: A client adapter that implements the `AuthorizationService` port over HTTP, for remote use.
    -   `web-api/v1/spring/e2e-tests/`: End-to-end tests for the web API.
    -   `web-api/v1/clients/`: Generated clients in multiple languages (Java, Python, TypeScript).
-   **`infrastructure/caching/spring-cache-adapter/`**: A **Secondary Adapter** for caching authorization results using Spring Cache.
-   **`infrastructure/messaging/spring-cloud-bus-adapter/`**: A **Secondary Adapter** for distributed cache invalidation using Spring Cloud Bus.
-   **`infrastructure/springboot-jndi/`**: Provides a lightweight, servlet-container-independent JNDI implementation configurable through Spring Boot externalized properties for datasource configuration.
-   **`infrastructure/testing/testcontainer/`**: Contains support for Testcontainers used in integration tests.

## Design Philosophy

-   **Ports and Adapters (Hexagonal Architecture)**: Inner layers define interfaces (**Ports**), and the outer `infrastructure` layer provides concrete implementations (**Adapters**). This makes the core logic independent of technical details.
-   **Immutability**: All domain objects are immutable value objects (using Lombok's `@Value` and `@With`). This ensures thread safety and predictable state. To modify an object, you create a new copy with the desired changes using its `.with*()` methods.
-   **Repository Pattern**: The domain layer defines repository interfaces for data access. The `jpa-adapter` in the infrastructure layer provides the JPA-based implementation.
-   **Domain Events**: Services can publish events (e.g., `RuleEvent`) which are used by infrastructure adapters for tasks like distributed cache invalidation.

## Domain Concepts

-   **Data Rules (`data-rules`)**: Priority-ordered rules controlling access to GeoServer resources (workspaces, layers). They match on user, role, IP, service, and more, granting `ALLOW`, `DENY`, or `LIMIT` access.
-   **Workspace Admin Rules (`workspaceadmin-rules`)**: Simpler, priority-ordered rules that grant `ADMIN` or `USER` rights for a given workspace.

## Developer Onboarding

### Code Tour

-   **Core Business Logic**: `domain/data-rules/` and `application/authorization-impl/`
-   **Database Implementation**: `infrastructure/persistence/jpa-adapter/`
-   **REST API Implementation**: `infrastructure/web-api/v1/spring/server-adapter/`
-   **Service Wiring**: `infrastructure/spring-config/` and `infrastructure/app-main/`

### Running the Service

The main entry point is `org.geoserver.acl.app.AccesControlListApplication` in the `infrastructure/app-main` module.

### Tech Stack

-   Java 17+
-   Spring Boot 3.5
-   `geolatte-geom` for spatial operations
-   Lombok for reducing boilerplate
-   MapStruct for type-safe mapping
-   QueryDSL for type-safe database queries
-   OpenAPI for API contract generation

### Testing Strategy

-   **Unit Tests**: For domain and application logic, using in-memory repositories. No Spring or database context required.
-   **Integration Tests**: For infrastructure adapters, verifying component interactions with a real database (H2) and Spring context.
-   **End-to-End Tests**: For the web API, testing the full system via HTTP requests and verifying the API contract. Found in `infrastructure/web-api/v1/spring/e2e-tests/`.
