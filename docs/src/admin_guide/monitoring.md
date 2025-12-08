# Monitoring

This page explains how to monitor the GeoServer ACL service and plugin to ensure optimal performance, troubleshoot issues, and maintain security.

## Monitoring Overview

Monitoring GeoServer ACL involves tracking several aspects:

1. **System Health**: Ensuring all components are operational
2. **Performance Metrics**: Tracking response times and throughput
3. **Access Logs**: Recording authorization decisions and rule evaluations
4. **Error Tracking**: Identifying and diagnosing issues
5. **Security Events**: Monitoring for suspicious activities

## Built-in Monitoring Tools

GeoServer ACL includes built-in monitoring capabilities through Spring Boot Actuator. For enhanced security and separation of concerns, actuator endpoints are exposed on a dedicated management port (typically `8081`) and do not inherit the `/acl` service context path.

### Health Endpoints

The health endpoint provides information on system status:

```
http://localhost:8081/actuator/health
```

Response example:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 96612954112,
        "threshold": 10485760
      }
    }
  }
}
```

### Metrics Endpoint

The metrics endpoint provides detailed performance metrics:

```
http://localhost:8081/actuator/metrics
```

To view specific metrics:

```
http://localhost:8081/actuator/metrics/http.server.requests
```

Response example:

```json
{
  "name": "http.server.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 2385
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 133.607534
    },
    {
      "statistic": "MAX",
      "value": 0.631959
    }
  ],
  "availableTags": [
    {
      "tag": "uri",
      "values": [
        "/acl/api/rules",
        "/acl/api/authorization",
        "/acl/api/adminrules"
      ]
    },
    {
      "tag": "status",
      "values": [
        "200",
        "404",
        "500"
      ]
    }
  ]
}
```

### Configuring Actuator

To enable additional actuator endpoints, configure `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers,httptrace
  endpoint:
    health:
      show-details: always
```

## Logging Configuration

Proper logging is essential for monitoring and troubleshooting.

### Log Levels

Configure log levels in `application.yml`:

```yaml
logging:
  level:
    root: INFO
    org.geoserver.acl: INFO
    org.geoserver.acl.authorization: DEBUG
    org.springframework: WARN
```

Common log levels:
- **ERROR**: Only error events
- **WARN**: Warning and error events
- **INFO**: Informational events plus warnings and errors
- **DEBUG**: Detailed information for debugging
- **TRACE**: Most detailed information

### Log Format

Configure the log format to include relevant information:

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Log Files

Configure log file outputs:

```yaml
logging:
  file:
    name: logs/acl.log
    max-size: 10MB
    max-history: 7
```

## Monitoring Performance

### Performance Metrics

1. **Rule Evaluation Times**: How long it takes to evaluate rules
2. **Database Query Performance**: Response times for database operations
3. **API Response Times**: Overall API endpoint performance
4. **Cache Hit Rates**: Effectiveness of caching
5. **Memory Usage**: JVM heap and non-heap memory
6. **Thread Pool Utilization**: Thread usage and contention

### Tracking Performance Metrics

Using Actuator metrics:

```
http://localhost:8081/actuator/metrics/acl.rule.evaluation
http://localhost:8081/actuator/metrics/http.server.requests
```

### Performance Optimization

If monitoring reveals performance issues:

1. **Increase Cache Size**: Configure larger caches for frequently accessed rules
2. **Optimize Database**: Add indexes for common query patterns
3. **Tune JVM**: Allocate appropriate memory and GC settings
4. **Optimize Rules**: Reduce the number of rules or improve rule organization
5. **Horizontal Scaling**: Deploy multiple instances behind a load balancer

## Integration with External Monitoring Systems

### Prometheus Integration

Spring Boot applications can expose Prometheus metrics:

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: prometheus
```

Access Prometheus metrics at:

```
http://localhost:8081/actuator/prometheus
```

### Grafana Dashboards

Create Grafana dashboards to visualize metrics from Prometheus:

1. Add Prometheus as a data source in Grafana
2. Import or create dashboards for:
   - System health
   - Request rates and latencies
   - Error rates
   - Memory usage
   - Database connection pool

### ELK Stack Integration

For log analysis:

1. Configure Filebeat to collect logs
2. Send logs to Logstash for processing
3. Store in Elasticsearch
4. Visualize with Kibana

## Alerting

Set up alerts to notify administrators of potential issues:

### Health Checks

Monitor the health endpoint and trigger alerts when status is not "UP":

```bash
curl -s http://localhost:8081/actuator/health | grep -q '"status":"UP"' || send_alert
```

### Performance Alerts

Set thresholds for important metrics:

- Rule evaluation time > 100ms
- API response time > 500ms
- Error rate > 1%
- Memory usage > 80%

### Log-based Alerts

Monitor logs for critical events:

- Authentication failures
- Authorization failures with unusual patterns
- Database connection issues
- Unexpected exceptions

## Security Monitoring

### Authentication Monitoring

Track authentication attempts:

- Failed login attempts
- Unusual login patterns
- Repeated authentication failures from the same source

### Authorization Monitoring

Monitor authorization decisions:

- Track denied access patterns
- Look for unusual rule evaluation patterns
- Monitor rule changes

### Admin Actions Monitoring

Track administrative actions:

- Rule additions, modifications, and deletions
- Admin rule changes
- Configuration changes

## Audit Logging

Enable comprehensive audit logging for security and compliance:

```yaml
geoserver:
  acl:
    audit:
      enabled: true
      log-level: INFO
      include-request-details: true
```

## Troubleshooting Common Issues

### High Rule Evaluation Times

If rule evaluation is slow:

1. **Check Rule Count**: Too many rules can slow down evaluation
2. **Review Rule Organization**: Ensure high-priority rules match frequently
3. **Examine Database Performance**: Slow database queries might be the cause
4. **Enable Query Logging**: Add SQL logging to identify slow queries

### Memory Issues

For memory-related problems:

1. **Check Heap Usage**: Monitor JVM heap usage
2. **Optimize Cache Settings**: Adjust cache sizes
3. **Enable GC Logging**: Add detailed garbage collection logging
4. **Tune JVM Settings**: Adjust memory allocation and GC parameters

### Connection Pool Exhaustion

If database connections are being exhausted:

1. **Increase Pool Size**: Configure a larger connection pool
2. **Check Connection Leaks**: Ensure connections are being closed
3. **Reduce Connection Holding Time**: Optimize query execution
4. **Add Connection Timeout**: Set appropriate timeouts

## Monitoring Best Practices

1. **Establish Baselines**: Measure normal performance to detect anomalies
2. **Monitor Trends**: Track metrics over time to identify gradual degradation
3. **Comprehensive Coverage**: Monitor all components (service, database, plugin)
4. **Correlate Metrics**: Connect performance metrics with user experience
5. **Regular Review**: Periodically review monitoring data for improvements
6. **Test Alerting**: Ensure alert mechanisms work as expected
7. **Document Procedures**: Create runbooks for common issues

## Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/)
- [Grafana Documentation](https://grafana.com/docs/grafana/latest/)