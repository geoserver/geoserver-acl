DOCKER_REPO="geoservercloud/geoserver-acl"

VERSION?=$(shell git describe --tags --exact-match 2>/dev/null || ./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)

#default target
build: install build-image test-examples

#build, test, and install all modules
install:
	./mvnw -Drevision=$(VERSION) clean install

lint:
	./mvnw -Drevision=$(VERSION) sortpom:verify spotless:check -ntp

format:
	./mvnw -Drevision=$(VERSION) sortpom:sort spotless:apply -ntp

package:
	./mvnw -Drevision=$(VERSION) clean package -DskipTests -U -ntp -T4

test:
	./mvnw -Drevision=$(VERSION) verify -ntp -T4

test-examples:
	./mvnw -Drevision=$(VERSION) install -DskipTests -ntp -pl :gs-acl-testcontainer
	./mvnw -Drevision=$(VERSION) verify -ntp -T4 -f examples/

# Make sure `make package` was run before if anything changed since the last build
# Consecutive COPY commands in Dockerfile fail on github runners
# Added "DOCKER_BUILDKIT=1" as a temporary fix
# more discussion on the same issue:
# https://github.com/moby/moby/issues/37965
# https://github.community/t/attempting-to-build-docker-image-with-copy-from-on-actions/16715
# https://stackoverflow.com/questions/51115856/docker-failed-to-export-image-failed-to-create-image-failed-to-get-layer
build-image:
	DOCKER_BUILDKIT=1 docker build -t $(DOCKER_REPO):$(VERSION) src/artifacts/api/

push-image:
	docker push $(DOCKER_REPO):$(VERSION)

deploy:
	./mvnw -Drevision=$(VERSION) clean package -ntp -T1C -fae -Dspotless.skip -U -DskipTests
	./mvnw -Drevision=$(VERSION) deploy -s $$MAVEN_SETTINGS -ntp -T1 -fae -Dspotless.skip -DskipTests

show-version:
	@echo ${VERSION}
