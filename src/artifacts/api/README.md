# GeoServer ACL OpenAPI Service

This is a (spring-boot) application for the GeoServer Access Control List REST API.

The OpenAPI 3 specification is available at [acl-api.yaml](../../openapi/acl-api.yaml).

With the application running at [http://localhost:8080](http://localhost:8080):

- The root URL will redirect to [http://localhost:8080/acl](http://localhost:8080/acl)
- The base URL [http://localhost:8080/acl](http://localhost:8080/acl) redirects to the HTML API Swagger UI at [http://localhost:8080/acl/openapi/swagger-ui/index.html](http://localhost:8080/acl/openapi/swagger-ui/index.html)
- The OpenAPI spec is exposed in JSON format at [http://localhost:8080/acl/openapi](http://localhost:8080/acl/openapi)


## Build

```
mvn clean install
```

will create a single-jar executable at `target/gs-acl-service-<version>-bin.jar`.

## Run


Run in development mode with an in-memory H2 database, either with

	mvn spring-boot:run -Dspring-boot.run.profiles=dev

or

	java -jar target/gs-acl-service-1.0.0-bin.jar --spring.profiles.active=dev


## Dependency graph

```mermaid
flowchart LR
	subgraph external-dependencies
		spring-context
		spring-data-jpa
		hibernate-spatial
		spring-boot-starter-web
		springdoc-openapi-ui
		h2
		postgresql
	end
	subgraph domain
		rule-management
		adminrule-management --> rule-management
		user-management
	end
	subgraph openapi-codegen
		openapi-server --> openapi-model
	end
	direction TB
	subgraph spring-integration
		domain-spring-integration --> rule-management & adminrule-management & user-management & spring-context
	end
	subgraph openapi-integration
		api-model-mapper --> openapi-model & rule-management & adminrule-management & user-management
		api-impl --> api-model-mapper & openapi-server & domain-spring-integration
	end
	subgraph persistence-jpa
		jpa-persistence --> spring-data-jpa & hibernate-spatial
		jpa-integration --> jpa-persistence
	end
	rest-app --> api-impl & jpa-integration & spring-boot-starter-web & springdoc-openapi-ui & h2 & postgresql
```
