workspace {
    name "GeoServer ACL"
    description "GeoServer Access Control List - Advanced authorization system for GeoServer"

    !identifiers hierarchical

    model {
        user = person "GeoServer User" "End user accessing GeoServer resources through OWS services"
        administrator = person "GeoServer Administrator" "Configures and manages GeoServer and ACL rules"
        developer = person "System Integrator" "Integrates with GeoServer ACL through API"
        
        gisClient = softwareSystem "GIS Client" "Desktop or web application that accesses GeoServer services"
        authProvider = softwareSystem "Authentication Provider" "External authentication system (e.g., OAuth2, LDAP)"

        aclSystem = softwareSystem "GeoServer ACL" "Advanced authorization system for GeoServer" {
            aclService = container "ACL Service" "Manages access rules and provides authorization decisions"
            aclDatabase = container "ACL Database" "Stores access rules and administrative rules"
            aclApi = container "ACL REST API" "REST API for managing rules and authorization checks"
        }
        
        geoserver = softwareSystem "GeoServer" "OGC-compliant server for sharing geospatial data" {
            geoserverCore = container "GeoServer Core" "Core GeoServer functionality"
            geoserverData = container "GeoServer Data Directory" "Configuration and data files"
            geoserverWebUI = container "GeoServer Admin UI" "Web interface for GeoServer administration"
            aclPlugin = container "ACL Plugin" "Integrates GeoServer with ACL Service"
        }
        
        # People relationships
        user -> gisClient "Uses"
        administrator -> geoserver "Configures"
        administrator -> aclSystem "Manages rules in"
        developer -> aclSystem "Integrates with"
        
        # System relationships
        gisClient -> geoserver "Makes OWS requests to"
        geoserver -> authProvider "Authenticates users with"
        geoserver -> aclSystem "Authorizes requests through"
        
        # Container relationships
        aclSystem.aclApi -> aclSystem.aclService "Forwards requests to"
        aclSystem.aclService -> aclSystem.aclDatabase "Reads from and writes to"
        
        geoserver.geoserverWebUI -> geoserver.geoserverCore "Administers"
        geoserver.geoserverCore -> geoserver.geoserverData "Reads from and writes to"
        geoserver.geoserverCore -> geoserver.aclPlugin "Uses for authorization"
        geoserver.aclPlugin -> aclSystem.aclApi "Makes API calls to"
    }

    views {
        systemContext aclSystem "SystemContext" {
            include *
            autoLayout
            title "System Context diagram for GeoServer ACL"
            description "Shows how GeoServer ACL fits in the broader system landscape"
        }
        
        container aclSystem "ACLContainers" {
            include *
            autoLayout
            title "Container diagram for GeoServer ACL"
            description "Shows the containers that make up the GeoServer ACL system"
        }
        
        container geoserver "GeoServerContainers" {
            include *
            autoLayout
            title "Container diagram for GeoServer with ACL Plugin"
            description "Shows the containers that make up GeoServer with the ACL Plugin"
        }

        theme default
    }
}

