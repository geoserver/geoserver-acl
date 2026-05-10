FROM eclipse-temurin:25-jre AS builder
ARG JAR_FILE=target/gs-acl-app-*-bin.jar

WORKDIR /builder

COPY ${JAR_FILE} application.jar

RUN java -Djarmode=layertools -jar application.jar extract

##########
FROM eclipse-temurin:25-jre

LABEL maintainer="GeoServer PSC <geoserver-users@lists.sourceforge.net>"

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/app/bin
ENV DEFAULT_JAVA_TOOL_OPTIONS="\
--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.text=ALL-UNNAMED \
--add-opens=java.desktop/java.awt.font=ALL-UNNAMED \
--add-opens=java.desktop/sun.awt.image=ALL-UNNAMED \
--add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED \
-Djava.awt.headless=true \
--enable-native-access=ALL-UNNAMED \
-XX:+UseCompressedOops \
-XX:+UseCompactObjectHeaders \
-XX:MaxRAMPercentage=80 \
-XX:+IgnoreUnrecognizedVMOptions \
-XX:UseSVE=0"

ENV JAVA_TOOL_OPTIONS="$DEFAULT_JAVA_TOOL_OPTIONS"

ENV JAVA_OPTS=""
ENV AOT_MODE="auto"

ENV MANAGEMENT_SERVER_PORT=8081
EXPOSE 8080
EXPOSE 8081

# application.yml loads this optional config file
RUN mkdir /etc/geoserver && chmod 0777 /etc/geoserver
COPY src/main/resources/values.yml /etc/geoserver/acl-service.yml

COPY --from=builder /builder/dependencies/ ./
COPY --from=builder /builder/snapshot-dependencies/ ./
COPY --from=builder /builder/spring-boot-loader/ ./
COPY --from=builder /builder/application/ ./

HEALTHCHECK \
--interval=10s \
--timeout=5s \
--start-period=3s \
--retries=5 \
CMD curl -f -s -o /dev/null localhost:$MANAGEMENT_SERVER_PORT/actuator/health || exit 1

ARG APP_ARGS=""

# AOT training run for faster startup (each arch builds natively, no QEMU)
RUN java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh \
    org.springframework.boot.loader.launch.JarLauncher --spring.profiles.active=dev

ENTRYPOINT [ "/bin/bash", "-c", "exec java -XX:AOTMode=$AOT_MODE -XX:AOTCache=app.aot $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher \"${@}\"", "--" ]

CMD ["${APP_ARGS}"]
