DOCKER_REPO="geoservercloud/geoserver-acl"

lint:
	./mvnw sortpom:verify fmt:check -ntp

format:
	./mvnw sortpom:sort fmt:format -ntp

package:
	./mvnw clean package -DskipTests -U -ntp -T4

test:
	./mvnw verify -ntp -T4

# Make sure `make package` was run before if anything changed since the last build
build-image:
	@VERSION=`./mvnw help:evaluate -q -DforceStdout -Dexpression=project.version` && \
	docker build -t $(DOCKER_REPO):$${VERSION} src/artifacts/api/

push-image:
	@VERSION=`./mvnw help:evaluate -q -DforceStdout -Dexpression=project.version` && \
	docker push $(DOCKER_REPO):$${VERSION}

