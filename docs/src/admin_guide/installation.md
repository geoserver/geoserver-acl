# Installation Guide

This guide details how to deploy GeoServer ACL.

## Deployment Options

1.  **Docker Compose (Recommended)**: The simplest way to run the ACL service alongside a database.
2.  **GeoServer Cloud**: ACL support is built-in; no separate installation required.
3.  **Manual Installation**: Run the JAR file directly on your infrastructure.

---

## Method 1: Docker Compose (Recommended)

You do not need to build the application. We provide official Docker images.

### 1. Create a Compose File
Create a file named `docker-compose.yml` with the following content:

```yaml
volumes:
  acl_data:

services:
  acldb:
    image: imresamu/postgis:15-3.4
    environment:
      - POSTGRES_DB=acl
      - POSTGRES_USER=acl
      - POSTGRES_PASSWORD=acls3cr3t
    volumes:
      - acl_data:/var/lib/postgresql/data
    restart: always
    ports:
      - 6432:5432
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U acl"]
      interval: 5s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 2G

  acl:
    image: geoservercloud/geoserver-acl:latest
    environment:
      - PG_HOST=acldb
      - PG_PORT=5432
      - PG_DB=acl
      - PG_SCHEMA=acl
      - PG_USER=acl
      - PG_PASSWORD=acls3cr3t
    ports:
      - 8080:8080
    depends_on:
      acldb:
        condition: service_healthy
        required: true
```

### 2. Start the Services
Run the following command in the directory where you created the file:

```bash
docker compose up -d
```

### 3. Connect GeoServer
Once the ACL service is running (accessible at `http://localhost:8080/acl`), configure your GeoServer instance to use it via the ACL Plugin.

---

## Method 2: GeoServer Cloud

If you are using [GeoServer Cloud](https://geoserver.org/geoserver-cloud/), ACL support is integrated out-of-the-box.

To enable it, simply activate the `acl` Spring Boot profile for your services. No additional plugin installation is required.

Refer to the [GeoServer Cloud Documentation](https://geoserver.org/geoserver-cloud/) for detailed configuration instructions.

---

## Method 3: Manual Installation

If you cannot use Docker, you can run the service manually.

### Prerequisites
*   **Java**: JRE 17 or higher.
*   **Database**: PostgreSQL 15+ with PostGIS extension.

### 1. Prepare the Database
Create a PostgreSQL database and enable PostGIS:

```sql
CREATE USER acl WITH PASSWORD 'acl_password';
CREATE DATABASE acldb OWNER acl;
\c acldb
CREATE EXTENSION postgis;
```

### 2. Download Artifacts
Download the latest release artifacts from the [GitHub Releases Page](https://github.com/geoserver/geoserver-acl/releases). You will need:

*   **ACL Service**: `geoserver-acl-app-<version>.jar`
*   **GeoServer Plugin**: `geoserver-acl-plugin-<version>.zip`

### 3. Run the ACL Service
Create an `application.yml` file to configure the database connection:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/acldb
    username: acl
    password: acl_password
```

Run the service:

```bash
java -jar geoserver-acl-app-<version>.jar --spring.config.location=file:./application.yml
```

### 4. Install the GeoServer Plugin
1.  Stop your traditional GeoServer instance.
2.  Unzip the `geoserver-acl-plugin-<version>.zip` into `WEB-INF/lib`.
3.  Restart GeoServer.
4.  Navigate to **Security > GeoServer ACL** in the GeoServer Web UI.
5.  Configure the **ACL Service URL** (e.g., `http://localhost:8080/acl`) and test the connection.

---

## Production Considerations

For a production environment, ensure you address the following:

*   **Security**: Do not use default passwords (`acl`/`acl`). Use a dedicated secret management system.
*   **High Availability**: Run multiple instances of the ACL service behind a load balancer.
*   **Database**: Use a managed PostgreSQL service (e.g., AWS RDS, Azure Database) with automated backups.
*   **Network**: Restrict access to the ACL service API (port 8080/8180) to only trusted networks and the GeoServer instances.
