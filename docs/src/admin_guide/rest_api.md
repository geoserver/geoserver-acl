# REST API Usage

This page provides information about using the GeoServer ACL REST API for programmatic management of rules and automated integration.

## API Overview

The GeoServer ACL REST API provides comprehensive endpoints for managing:

- Data access rules
- Administrative access rules
- Authorization decisions
- System configuration
- Health and status information

The API follows REST principles and uses JSON for data exchange.

## Base URLs

Depending on your deployment:

- Standalone: `http://localhost:8080/acl/api/`
- Embedded: `http://localhost:8080/geoserver/acl/api/`
- Docker: Depends on your Docker configuration (e.g., `http://localhost:8181/acl/api/`)

## Authentication

The API requires authentication. The exact method depends on your configuration:

### Basic Authentication

```bash
curl -u username:password http://localhost:8080/acl/api/rules
```

### Token-Based Authentication

```bash
curl -H "Authorization: Bearer your_token_here" http://localhost:8080/acl/api/rules
```

## API Documentation

### OpenAPI/Swagger

The API is documented using OpenAPI. You can access the Swagger UI at:

```
http://localhost:8080/acl/swagger-ui.html
```

And the OpenAPI specification at:

```
http://localhost:8080/acl/api-docs
```

## Rules Management API

### List Rules

Get all data access rules:

```bash
curl -u admin:geoserver http://localhost:8080/acl/api/rules
```

### Get Rule by ID

Get a specific rule by its ID:

```bash
curl -u admin:geoserver http://localhost:8080/acl/api/rules/123
```

### Create Rule

Create a new rule:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/rules \
  -H 'Content-Type: application/json' \
  -d '{
    "priority": 100,
    "access": "ALLOW",
    "username": "user1",
    "workspace": "workspace1",
    "layer": "layer1",
    "service": "WMS",
    "request": "GetMap"
  }'
```

### Update Rule

Update an existing rule:

```bash
curl -u admin:geoserver -X PUT \
  http://localhost:8080/acl/api/rules/123 \
  -H 'Content-Type: application/json' \
  -d '{
    "priority": 100,
    "access": "ALLOW",
    "username": "user1",
    "workspace": "workspace1",
    "layer": "layer1",
    "service": "WMS",
    "request": "GetMap"
  }'
```

### Delete Rule

Delete a rule:

```bash
curl -u admin:geoserver -X DELETE \
  http://localhost:8080/acl/api/rules/123
```

### Find Rules

Search for rules with specific criteria:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/rules/search \
  -H 'Content-Type: application/json' \
  -d '{
    "workspace": "workspace1",
    "layer": "layer1"
  }'
```

### Count Rules

Count rules matching specific criteria:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/rules/count \
  -H 'Content-Type: application/json' \
  -d '{
    "workspace": "workspace1"
  }'
```

### Bulk Import/Export

Export all rules:

```bash
curl -u admin:geoserver http://localhost:8080/acl/api/rules > rules_backup.json
```

Import rules:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/rules/bulk \
  -H 'Content-Type: application/json' \
  -d @rules_backup.json
```

## Admin Rules Management API

### List Admin Rules

Get all admin rules:

```bash
curl -u admin:geoserver http://localhost:8080/acl/api/adminrules
```

### Get Admin Rule by ID

Get a specific admin rule by its ID:

```bash
curl -u admin:geoserver http://localhost:8080/acl/api/adminrules/123
```

### Create Admin Rule

Create a new admin rule:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/adminrules \
  -H 'Content-Type: application/json' \
  -d '{
    "priority": 100,
    "username": "workspace_admin",
    "workspace": "workspace1",
    "access": "ADMIN"
  }'
```

### Update Admin Rule

Update an existing admin rule:

```bash
curl -u admin:geoserver -X PUT \
  http://localhost:8080/acl/api/adminrules/123 \
  -H 'Content-Type: application/json' \
  -d '{
    "priority": 100,
    "username": "workspace_admin",
    "workspace": "workspace1",
    "access": "ADMIN"
  }'
```

### Delete Admin Rule

Delete an admin rule:

```bash
curl -u admin:geoserver -X DELETE \
  http://localhost:8080/acl/api/adminrules/123
```

### Find Admin Rules

Search for admin rules with specific criteria:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/adminrules/search \
  -H 'Content-Type: application/json' \
  -d '{
    "workspace": "workspace1"
  }'
```

### Count Admin Rules

Count admin rules matching specific criteria:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/adminrules/count \
  -H 'Content-Type: application/json' \
  -d '{
    "workspace": "workspace1"
  }'
```

### Bulk Import/Export

Export all admin rules:

```bash
curl -u admin:geoserver http://localhost:8080/acl/api/adminrules > adminrules_backup.json
```

Import admin rules:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/adminrules/bulk \
  -H 'Content-Type: application/json' \
  -d @adminrules_backup.json
```

## Authorization API

The Authorization API can be used to test access permissions based on specific request properties.

### Check Access

Check if a specific request would be authorized:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/authorization \
  -H 'Content-Type: application/json' \
  -d '{
    "user": "user1",
    "roles": ["ROLE_USER"],
    "workspace": "workspace1",
    "layer": "layer1",
    "service": "WMS",
    "request": "GetMap"
  }'
```

### Get Access Summary

Get a summary of accessible workspaces and layers:

```bash
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/authorization/summary \
  -H 'Content-Type: application/json' \
  -d '{
    "user": "user1",
    "roles": ["ROLE_USER"]
  }'
```

## Health and Monitoring API

### Health Check

Check the health of the ACL service:

```bash
curl http://localhost:8081/acl/actuator/health
```

### Metrics

Get system metrics:

```bash
curl http://localhost:8081/acl/actuator/metrics
```

## Implementing API Clients

GeoServer ACL provides generated API clients for several languages. You can also create your own clients using the OpenAPI specification.

### Java Client

Maven dependency:

```xml
<dependency>
  <groupId>org.geoserver.acl</groupId>
  <artifactId>geoserver-acl-api-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

Example usage:

```java
import org.geoserver.acl.client.AclClient;
import org.geoserver.acl.client.AclClientAdaptor;

// Create a client
AclClient client = new AclClient("http://localhost:8080/acl", "admin", "geoserver");
AclClientAdaptor adaptor = new AclClientAdaptor(client);

// Get rules
List<Rule> rules = adaptor.getRules();
```

### JavaScript Client

Installation:

```bash
npm install geoserver-acl-client
```

Example usage:

```javascript
import { AclApi } from 'geoserver-acl-client';

// Create a client
const api = new AclApi({
  basePath: 'http://localhost:8080/acl',
  username: 'admin',
  password: 'geoserver'
});

// Get rules
api.getRules().then(rules => {
  console.log(rules);
});
```

### Python Client

Installation:

```bash
pip install geoserver-acl-client
```

Example usage:

```python
from geoserver_acl_client import AclApi

# Create a client
api = AclApi(
  base_url='http://localhost:8080/acl',
  username='admin',
  password='geoserver'
)

# Get rules
rules = api.get_rules()
print(rules)
```

## Common API Tasks

### Bulk Rule Management

For large-scale rule management, use bulk operations:

```bash
# Export rules
curl -u admin:geoserver http://localhost:8080/acl/api/rules > rules.json

# Modify the JSON file (e.g., with a script)
# ...

# Import modified rules
curl -u admin:geoserver -X POST \
  http://localhost:8080/acl/api/rules/bulk \
  -H 'Content-Type: application/json' \
  -d @rules.json
```

### Integration with Automation Scripts

Example bash script to create a set of rules:

```bash
#!/bin/bash

# Base URL
BASE_URL="http://localhost:8080/acl/api"
AUTH="admin:geoserver"

# Create a rule
create_rule() {
  local username=$1
  local workspace=$2
  local layer=$3
  local priority=$4
  
  curl -u $AUTH -X POST \
    $BASE_URL/rules \
    -H 'Content-Type: application/json' \
    -d '{
      "priority": '"$priority"',
      "access": "ALLOW",
      "username": "'"$username"'",
      "workspace": "'"$workspace"'",
      "layer": "'"$layer"'"
    }'
}

# Create rules
create_rule "user1" "workspace1" "layer1" 100
create_rule "user1" "workspace1" "layer2" 101
create_rule "user2" "workspace2" "layer1" 200
```

## Error Handling

The API returns standard HTTP status codes:

- 200 OK: Successful operation
- 201 Created: Resource created
- 400 Bad Request: Invalid input
- 401 Unauthorized: Authentication failed
- 403 Forbidden: Permission denied
- 404 Not Found: Resource not found
- 409 Conflict: Resource conflict (e.g., duplicate rule)
- 500 Server Error: Internal server error

Error responses include a JSON body with details:

```json
{
  "error": "Bad Request",
  "message": "Invalid rule format",
  "timestamp": "2023-09-20T10:15:30Z",
  "path": "/acl/api/rules"
}
```

## API Versioning

The API uses versioning to maintain backward compatibility:

- V1 API: `/acl/api/v1/...` (if specified)
- Default (latest) API: `/acl/api/...`

## Performance Considerations

- Use pagination for large result sets
- Implement caching for frequent queries
- Consider bulk operations for mass updates
- Monitor API usage and response times

## Security Best Practices

- Use HTTPS for all API communication
- Implement API rate limiting
- Audit API access
- Use role-based access control for API endpoints
- Regularly rotate API credentials
- Avoid storing credentials in scripts
