# Plugin Development
This page explains how to develop and extend the GeoServer ACL plugin for GeoServer. It covers the plugin architecture, extension points, and development workflow.

## Plugin Architecture

The GeoServer ACL plugin integrates with GeoServer's authorization system to enforce access control rules. The plugin consists of several components:

### Core Components

1. **ACLResourceAccessManager**: Implements GeoServer's ResourceAccessManager interface to enforce access control
2. **ACLDispatcherCallback**: Intercepts OGC service requests for authorization
3. **AccessRequestBuilder**: Converts GeoServer requests to ACL AccessRequest objects
4. **ACL Configuration**: Manages plugin configuration and connection to the ACL service

### Extension Points

The plugin provides several extension points for customization:

1. **AccessRequestUserResolver**: Resolves user information for access requests
2. **CatalogSecurityFilterBuilder**: Builds CatalogFilters for filtering catalog resources
3. **ContainerLimitResolver**: Resolves container-based limitations
4. **Custom Rule Types**: Support for extended rule types

## Development Environment Setup

### Prerequisites

- Java 17
- Maven 3.8+
- GeoServer 2.19+ source code
- IDE (IntelliJ IDEA or Eclipse)

### Setting Up the Development Environment

1. Clone the GeoServer ACL repository:
   ```bash
   git clone https://github.com/geoserver/geoserver-acl.git
   cd geoserver-acl
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Set up GeoServer for development:
   ```bash
   # Clone GeoServer repository
   git clone https://github.com/geoserver/geoserver.git
   cd geoserver
   
   # Build GeoServer
   mvn clean install -DskipTests
   ```

4. Set up the plugin module for development:
   ```bash
   cd geoserver-acl/src/plugin
   mvn eclipse:eclipse  # For Eclipse
   # or
   mvn idea:idea        # For IntelliJ IDEA
   ```

## Plugin Structure

The plugin is organized into several modules:

```
src/plugin/
├── accessmanager/     # Core access management components
├── client/            # Client for communicating with the ACL service
├── config/            # Configuration components
├── plugin/            # Plugin assembly and Spring integration
└── web/               # Web UI components
```

### accessmanager Module

This module contains the core access management implementation:

- **ACLResourceAccessManager**: Implements ResourceAccessManager for access control
- **AccessRequestBuilder**: Builds AccessRequest objects from GeoServer requests
- **CatalogSecurityFilterBuilder**: Creates catalog filters based on access rules

### client Module

This module provides the client for communicating with the ACL service:

- **AclClient**: Low-level client for the ACL API
- **AclClientAdaptor**: High-level client for easier integration

### config Module

This module manages plugin configuration:

- **AccessManagerConfig**: Configuration properties
- **AccessManagerConfigProvider**: Provider for configuration

### plugin Module

This module assembles the plugin and provides Spring integration:

- **Spring Configuration**: Spring context configuration
- **Auto-configuration**: Automatic plugin setup

### web Module

This module provides the web UI components:

- **ACL UI Panel**: Web UI for managing rules
- **ACL Admin Page**: Admin page for configuration

## Extending the Plugin

### Creating a Custom User Resolver

The default user resolver extracts user information from GeoServer's Authentication object. You can create a custom implementation:

```java
import org.geoserver.acl.plugin.accessmanager.AccessRequestUserResolver;
import org.geoserver.security.impl.GeoServerUser;
import org.springframework.security.core.Authentication;

public class CustomUserResolver implements AccessRequestUserResolver {
    @Override
    public String resolveUserName(Authentication authentication) {
        // Your custom logic to extract username
        return authentication.getName();
    }

    @Override
    public Set<String> resolveRoles(Authentication authentication) {
        // Your custom logic to extract roles
        Set<String> roles = new HashSet<>();
        authentication.getAuthorities().forEach(a -> roles.add(a.getAuthority()));
        return roles;
    }
}
```

Then register your custom resolver in the Spring context:

```xml
<bean id="aclAccessRequestUserResolver" 
      class="com.example.CustomUserResolver"/>
```

### Creating a Custom Catalog Filter Builder

You can extend the catalog filtering behavior:

```java
import org.geoserver.acl.plugin.accessmanager.CatalogSecurityFilterBuilder;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.WorkspaceInfo;

public class CustomCatalogFilterBuilder implements CatalogSecurityFilterBuilder {
    @Override
    public CatalogFilter createFilter(Catalog catalog, AccessInfo accessInfo) {
        // Create a custom catalog filter
        return new CatalogFilter() {
            @Override
            public boolean canAccess(WorkspaceInfo workspace) {
                // Your custom logic
                return true;
            }

            @Override
            public boolean canAccess(LayerInfo layer) {
                // Your custom logic
                return true;
            }
        };
    }
}
```

Then register your custom filter builder:

```xml
<bean id="aclCatalogFilterBuilder" 
      class="com.example.CustomCatalogFilterBuilder"/>
```

### Creating a Custom Container Limit Resolver

You can customize how container limits are resolved:

```java
import org.geoserver.acl.plugin.accessmanager.ContainerLimitResolver;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;

public class CustomContainerLimitResolver implements ContainerLimitResolver {
    @Override
    public AccessInfo resolveContainerLimits(LayerGroupInfo container, 
                                            List<LayerInfo> layers,
                                            List<AccessInfo> accessInfos) {
        // Your custom logic
        return mergeAccessInfos(accessInfos);
    }
    
    private AccessInfo mergeAccessInfos(List<AccessInfo> accessInfos) {
        // Implement your merging logic
        // ...
    }
}
```

Then register your custom resolver:

```xml
<bean id="aclContainerLimitResolver" 
      class="com.example.CustomContainerLimitResolver"/>
```

## Plugin Configuration

### Configuration Properties

The plugin supports several configuration properties:

- **ACL Services URL**: URL of the ACL service
- **Instance Name**: Instance identifier
- **Cache Configuration**: Cache settings
- **Connection Settings**: Connection parameters

### GeoServer Configuration

Configure the plugin in GeoServer:

1. Log in to the GeoServer Admin interface
2. Navigate to "Security" → "GeoServer ACL"
3. Configure the plugin settings
4. Click "Save"

### Programmatic Configuration

You can also configure the plugin programmatically:

```java
AccessManagerConfig config = new AccessManagerConfig();
config.setServicesUrl("http://localhost:8080/acl");
config.setInstanceName("default");
config.setCacheEnabled(true);
config.setCacheSize(1000);
config.setCacheExpiryMinutes(30);

// Apply the configuration
AccessManagerConfigProvider configProvider = applicationContext.getBean(AccessManagerConfigProvider.class);
configProvider.setConfig(config);
```

## Integration Testing

To test your plugin extensions, create integration tests:

```java
import org.geoserver.acl.plugin.accessmanager.ACLResourceAccessManager;
import org.geoserver.data.test.MockData;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.junit.Test;

public class CustomUserResolverTest extends GeoServerSystemTestSupport {

    @Test
    public void testUserResolution() {
        // Set up authentication
        login("admin", "geoserver", "ROLE_ADMINISTRATOR");
        
        // Get the access manager
        ACLResourceAccessManager accessManager = applicationContext.getBean(ACLResourceAccessManager.class);
        
        // Test with a layer
        LayerInfo layer = getCatalog().getLayerByName(MockData.BASIC_POLYGONS.getLocalPart());
        AccessLimits limits = accessManager.getAccessLimits(user, layer);
        
        // Assert expected behavior
        assertNotNull(limits);
        // Additional assertions
    }
}
```

## Common Customization Scenarios

### Custom Authentication Integration

To integrate with a custom authentication system:

1. Create a custom AccessRequestUserResolver
2. Implement logic to extract user information from your authentication system
3. Register the resolver in the Spring context

### Custom Rule Evaluation

To customize rule evaluation logic:

1. Create a custom AuthorizationService implementation
2. Override the rule evaluation process
3. Register your service in the Spring context

### Custom UI for Rule Management

To create a custom UI for rule management:

1. Extend the ACL UI panel
2. Create your custom UI components
3. Register them in the GeoServer web application context

## Deploying Plugin Extensions

To deploy your plugin extensions:

1. Create a JAR containing your extensions
2. Add required dependencies
3. Create a Spring context file with your bean definitions
4. Place the JAR in GeoServer's WEB-INF/lib directory
5. Restart GeoServer

## Troubleshooting Plugin Development

### Common Issues

#### ClassNotFoundException

If you encounter ClassNotFoundException:

1. Check that all required dependencies are included
2. Verify JAR packaging includes the classes
3. Check for conflicts with existing GeoServer libraries

#### Spring Bean Autowiring Issues

If beans aren't being autowired correctly:

1. Verify Spring context configuration
2. Check component scanning settings
3. Ensure bean names don't conflict

#### Authentication Problems

If authentication doesn't work as expected:

1. Check AccessRequestUserResolver implementation
2. Verify authentication objects contain expected information
3. Enable debug logging for authentication components

### Debugging Tips

1. Enable Debug Logging:
   ```xml
   <logger name="org.geoserver.acl" level="DEBUG"/>
   ```

2. Use GeoServer's Development Mode:
   ```
   -DGEOSERVER_CSRF_DISABLED=true -DGEOSERVER_CONSOLE_DISABLED=true
   ```

3. Use Remote Debugging:
   ```
   JAVA_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
   ```

## Best Practices

1. **Follow GeoServer Conventions**: Maintain compatibility with GeoServer's design
2. **Use Dependency Injection**: Use Spring for managing components
3. **Write Tests**: Create comprehensive integration tests
4. **Handle Errors Gracefully**: Provide meaningful error messages
5. **Document Extensions**: Document your extensions thoroughly
6. **Consider Performance**: Be mindful of performance impacts
7. **Maintain Backward Compatibility**: Ensure compatibility with existing configurations
