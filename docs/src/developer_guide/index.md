# Developer Guide

This guide is intended for developers who want to integrate with, extend, or contribute to GeoServer ACL. It covers the architecture, API, and development workflow.

## Architecture Overview

GeoServer ACL follows a modular architecture with a clean separation of concerns:

1. **Domain Layer**: Contains the core business logic and domain model
2. **Application Layer**: Implements use cases and coordinates the domain objects
3. **Integration Layer**: Connects to external systems and provides persistence
4. **API Layer**: Exposes functionality through REST endpoints
5. **Plugin Layer**: Integrates with GeoServer

For a detailed architecture description, see the [Architecture](architecture.md) section.

## Core Components

The main components of GeoServer ACL are:

- **Rule Management**: Core domain logic for managing data access rules
- **Admin Rule Management**: Domain logic for managing administrative permissions
- **Authorization Service**: Logic for evaluating access requests against rules
- **REST API**: Interface for programmatic access to rules and authorization
- **GeoServer Plugin**: Integration with GeoServer for enforcing access control

## Integration Options

Developers can integrate with GeoServer ACL in several ways:

1. **REST API**: Direct HTTP calls to the GeoServer ACL service
2. **Java Client**: Use the provided Java client library
3. **JavaScript Client**: Use the generated JavaScript client
4. **Python Client**: Use the generated Python client
5. **Custom Client**: Generate a client for your language using the OpenAPI specification

## Development Setup

To set up a development environment for GeoServer ACL:

1. Java 17 JDK is required for building
2. Clone the repository: `git clone https://github.com/geoserver/geoserver-acl.git`
3. Build with Maven: `./mvnw clean install`

For more detailed instructions, see [Building from Source](building.md).

## API Integration

The GeoServer ACL API follows OpenAPI 3.0 standards:

- **API Documentation**: Available at `/api-docs` when the service is running
- **Swagger UI**: Interactive documentation at `/swagger-ui.html`
- **Generated Clients**: Available in Java, JavaScript, and Python

For detailed API usage, see [API Integration](api_integration.md).

## Contributing

Contributions to GeoServer ACL are welcome! The project follows standard open source practices:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

For detailed contributing guidelines, see [Contributing](contributing.md).
