FROM eclipse-temurin:17-jre as builder
ARG JAR_FILE=target/gs-acl-service-*-bin.jar

COPY ${JAR_FILE} application.jar

RUN java -Djarmode=layertools -jar application.jar extract

##########
FROM eclipse-temurin:17-jre

LABEL maintainer="GeoServer PSC <geoserver-users@lists.sourceforge.net>"

WORKDIR /opt/app/bin
ENV DEF_JAVA_TOOL_OPTS="\
--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.text=ALL-UNNAMED \
--add-opens=java.desktop/java.awt.font=ALL-UNNAMED \
--add-opens=java.desktop/sun.awt.image=ALL-UNNAMED \
--add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED \
-Djava.awt.headless=true"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80 -XshowSettings:system"

ENV JAVA_TOOL_OPTS="$DEF_JAVA_TOOL_OPTS"
ENV MANAGEMENT_SERVER_PORT=8081
EXPOSE 8080
EXPOSE 8081

# application.yml loads this optional config file
RUN mkdir /etc/geoserver && chmod 0777 /etc/geoserver
COPY src/main/resources/values.yml /etc/geoserver/acl-service.yml

COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./

HEALTHCHECK \
--interval=10s \
--timeout=5s \
--start-period=3s \
--retries=5 \
CMD curl -f -s -o /dev/null localhost:$MANAGEMENT_SERVER_PORT/actuator/health || exit 1

ARG APP_ARGS=""

ENTRYPOINT [ "/bin/bash", "-c", "exec java $JAVA_OPTS $JAVA_TOOL_OPTS org.springframework.boot.loader.JarLauncher \"${@}\"", "--" ]

CMD ["${APP_ARGS}"]
