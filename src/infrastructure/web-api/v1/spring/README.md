
# GeoServer ACL OpenAPI 3.0.3 interface specification

GeoServer ACL API is defined in the [geoserver-acl-web-api-spec-v1.yaml](../spec/src/main/resources/geoserver-acl-web-api-spec-v1.yaml) [OpenAPI](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md) 3
specification document.

Here's a graphical representation of the API and its object model:

![image](schemas.webp)

The [openapi-generator](https://github.com/OpenAPITools/openapi-generator) can be used to
create service stubs, ready to use client libraries, and documentation in a number of
programming languages, frameworks, and formats.

We're using the [openapi-generator-maven-plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin)
to generate a Java [object model](model/README.md) library, shared
between the [java-client](java-client/README.md) and the [spring-server](server-api/README.md) stub.

Please refer to the [openapi-generator](https://github.com/OpenAPITools/openapi-generator) documentation
to create client libraties in the programming language of your choice.
