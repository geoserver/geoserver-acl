# User Guide

This guide is intended for end users of GeoServer who are accessing resources protected by GeoServer ACL. If you're an administrator looking to configure GeoServer ACL, please see the [Administrator Guide](../admin_guide/index.md).

## What is GeoServer ACL?

GeoServer ACL is an authorization system that controls access to GeoServer resources. As a user, you don't interact with GeoServer ACL directly - instead, it works behind the scenes to determine what you can and cannot access when you use GeoServer services.

## How Access Control Affects You

Depending on how your administrator has configured GeoServer ACL, you may experience the following:

- **Filtered Workspaces and Layers**: You might only see certain workspaces and layers in GeoServer's capabilities documents
- **Spatial Restrictions**: Your view of geographic data might be limited to specific areas
- **Attribute Filtering**: Certain attributes or properties of the data might be hidden
- **Service Limitations**: You might be able to view data (WMS) but not download it (WFS), or vice versa
- **Workspace Administration**: If you're a workspace administrator, you'll only be able to manage resources within your authorized workspaces

## Common Scenarios

### Viewing Maps (WMS)

When you make a WMS GetCapabilities request, GeoServer ACL filters the response to only include layers you have access to. When you request a map with GetMap, you may receive:

- A full map if you have full access
- A map clipped to your allowed geographic area
- A limited view of the data based on your attribute permissions
- An access denied error if you don't have any access to the layer

### Downloading Data (WFS)

When working with WFS:

- GetCapabilities will only show features you can access
- GetFeature requests will filter out features outside your allowed area
- Attributes may be filtered based on your permissions
- Insert, Update, and Delete operations may be restricted

### Other Services

GeoServer ACL can restrict access to any OGC service provided by GeoServer, including:

- Web Coverage Service (WCS)
- Web Processing Service (WPS)
- Web Map Tile Service (WMTS)
- Catalog Services for the Web (CSW)

## Using GeoServer with Access Restrictions

If you're using a GIS client like QGIS, ArcGIS, or a web mapping application, you'll generally authenticate through your client application. The client will pass your credentials to GeoServer, and GeoServer ACL will apply the appropriate restrictions.

Your experience will be seamless - you'll simply see and access the resources you're authorized to use. If you encounter "Access Denied" errors or missing data, it's likely due to authorization restrictions.

