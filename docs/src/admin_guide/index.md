# Administrator Guide

This guide is intended for system administrators who are responsible for setting up, configuring, and maintaining GeoServer ACL. It covers installation, configuration, rule management, and integration with other systems.

## What is GeoServer ACL?

GeoServer ACL is an advanced authorization system for GeoServer that provides fine-grained access control to geospatial resources. As an administrator, you'll define rules that determine who can access which resources and with what limitations.

## Components

GeoServer ACL consists of two main components:

1. **GeoServer ACL Service**: A standalone Spring Boot application that manages and stores access rules, providing a REST API for integration
2. **GeoServer ACL Plugin**: A plugin for GeoServer that connects to the ACL Service to enforce access rules

These components can be deployed in various configurations:

- **Standalone**: The ACL Service runs independently, and one or more GeoServer instances connect to it
- **Embedded**: For simpler deployments, the ACL Service can be embedded within GeoServer
- **Cloud Native**: The ACL Service can be deployed as part of a GeoServer Cloud infrastructure

## Administrative Capabilities

As an administrator, GeoServer ACL allows you to:

- **Define Data Access Rules**: Control who can access which resources
- **Set Administrative Permissions**: Define workspace administrators
- **Apply Spatial Restrictions**: Limit access to specific geographic areas
- **Filter Attributes**: Control which attributes users can see or edit
- **Integrate with Authentication**: Work with any authentication system used by GeoServer
- **Automate via REST API**: Programmatically manage rules through the REST API

## Administration Workflow

The typical administration workflow includes:

1. **Installation and Configuration**: Set up the ACL Service and GeoServer Plugin
2. **Authentication Integration**: Connect to your authentication system
3. **Rule Definition**: Create and prioritize access rules
4. **Testing**: Verify that rules work as expected
5. **Monitoring**: Monitor access and adjust rules as needed

