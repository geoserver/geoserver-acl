DOCKER_REPO="geoservercloud/geoserver-acl"

#default target
build: install build-image test-examples

#build, test, and install all modules, including the API server and GeoServer plugin
install:
	./mvnw clean install

# build, test, and install, the geoserver plugin and its dependencies
plugin:
	./mvnw clean install -pl :gs-acl-client-plugin --also-make -ntp

plugin-test-gs-dev:
	./mvnw -P gs_dev verify -pl :gs-acl-client-plugin -am -ntp

plugin-test-gs-stable:
	./mvnw -P gs_stable verify -pl :gs-acl-client-plugin -am -ntp

lint:
	./mvnw sortpom:verify fmt:check -ntp

format:
	./mvnw sortpom:sort fmt:format -ntp

package:
	./mvnw clean package -DskipTests -U -ntp -T4

test:
	./mvnw verify -ntp -T4

test-examples:
	./mvnw install -DskipTests -ntp -pl :gs-acl-testcontainer
	./mvnw verify -ntp -T4 -f examples/

# Make sure `make package` was run before if anything changed since the last build
# Consecutive COPY commands in Dockerfile fail on github runners
# Added "DOCKER_BUILDKIT=1" as a temporary fix
# more discussion on the same issue:
# https://github.com/moby/moby/issues/37965
# https://github.community/t/attempting-to-build-docker-image-with-copy-from-on-actions/16715
# https://stackoverflow.com/questions/51115856/docker-failed-to-export-image-failed-to-create-image-failed-to-get-layer
build-image:
	@VERSION=`./mvnw help:evaluate -q -DforceStdout -Dexpression=project.version` && \
	DOCKER_BUILDKIT=1 docker build -t $(DOCKER_REPO):$${VERSION} src/artifacts/api/

push-image:
	@VERSION=`./mvnw help:evaluate -q -DforceStdout -Dexpression=project.version` && \
	docker push $(DOCKER_REPO):$${VERSION}

deploy:
	./mvnw clean package deploy \
		-s $$MAVEN_SETTINGS \
		-pl :gs-acl-client-plugin -pl :gs-acl-api-client-spring6 -pl :gs-acl-testcontainer \
		--also-make -ntp -T2 -fae -Dfmt.skip -U -DskipTests
