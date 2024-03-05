# Management endpoints

[Spring boot actuator](https://docs.spring.io/spring-boot/docs/current/actuator-api/htmlsingle/) exposes
a number of management endpoints.

The `acl-service` instance is configured by default to expose the application itself on port `8080` and the management
API on port `8081`, and are under the same `/acl` context path as the application's API.

So, if the application API is located at [http://localhost:8080/acl/api](http://localhost:8080/acl/api), the
Spring Boot Actuator API is at [http://localhost:8081/acl/actuator](http://localhost:8081/acl/actuator).

The management port can be changed with the standard `management.server.port` configuration property,
including the spring-boot style environment variable alternative `MANAGEMENT_SERVER_PORT`.

Note however, the environment variable is preferred as it'll be catch up by the health-check configured in the Docker image:

```bash
CMD curl -f -s -o /dev/null localhost:$MANAGEMENT_SERVER_PORT/acl/actuator/health || exit 1
```

The `/actuator/health` endpoints, including the liveness and readiness probes `/actuator/health/liveness` and `/actuator/health/readiness`
are accessible without authentication.

All other management endpoints require authentication with a `ROLE_ADMIN` user, and will result in a basic auth challenge if accessed anonymously.