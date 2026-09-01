DOCKER_REPO="geoservercloud/geoserver-acl"

VERSION?=$(shell git describe --tags --exact-match 2>/dev/null || ./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)

#default target
build: install build-image test-examples

.PHONY: help
help:
	@echo "GeoServer ACL Makefile targets:"
	@echo ""
	@echo "  build          - Build, test, and install all modules, build Docker image, and test examples (default)"
	@echo "  install        - Build, test, and install all modules"
	@echo "  package        - Build all modules without tests"
	@echo "  test           - Run unit and integration tests"
	@echo "  test-examples  - Run the java and python client examples"
	@echo "  test-java-example - Install testcontainer module and run the java client example"
	@echo "  test-python-example - Run the python client example against the docker image (run 'make build-image' first)"
	@echo "  test-python-client - Run python client smoke tests against a local dev-profile app (run 'make package' first)"
	@echo "  dist-python-client - Build the python client sdist and wheel into target/dist (run 'make package' first)"
	@echo ""
	@echo "Code quality:"
	@echo "  lint           - Verify code formatting and pom.xml file ordering"
	@echo "  format         - Apply code formatting and sort pom.xml files"
	@echo ""
	@echo "Docker:"
	@echo "  build-image    - Build Docker image (run 'make package' first if code changed)"
	@echo "  push-image     - Push Docker image to Docker Hub"
	@echo ""
	@echo "Other:"
	@echo "  deploy         - Package and deploy artifacts (requires MAVEN_SETTINGS)"
	@echo "  show-version   - Display current project version"
	@echo "  docs           - Generate documentation (see docs/Makefile)"
	@echo ""

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

test-examples: test-java-example test-python-example

test-java-example:
	./mvnw -Drevision=$(VERSION) install -pl :gs-acl-webapi-v1-client-adapter -am -ntp -nsu -DskipTests
	./mvnw -Drevision=$(VERSION) install -DskipTests -ntp -nsu -pl :gs-acl-testcontainer
	./mvnw -Drevision=$(VERSION) install -ntp -nsu -T4 -f examples/

# Requires the docker image: run `make build-image` first if anything changed
test-python-example: dist-python-client
	ACL_IMAGE=$(DOCKER_REPO):$(VERSION) examples/python-client/run-example.sh

# Requires the app jar and the generated client: run `make package` first if anything changed
test-python-client:
	src/infrastructure/web-api/v1/clients/python/run-tests.sh

# Requires the generated client: run `make package` first if anything changed
dist-python-client:
	src/infrastructure/web-api/v1/clients/python/build-dist.sh

# Make sure `make package` was run before if anything changed since the last build
# Consecutive COPY commands in Dockerfile fail on github runners
# Added "DOCKER_BUILDKIT=1" as a temporary fix
# more discussion on the same issue:
# https://github.com/moby/moby/issues/37965
# https://github.community/t/attempting-to-build-docker-image-with-copy-from-on-actions/16715
# https://stackoverflow.com/questions/51115856/docker-failed-to-export-image-failed-to-create-image-failed-to-get-layer
build-image:
	DOCKER_BUILDKIT=1 docker build -t $(DOCKER_REPO):$(VERSION) src/infrastructure/app-main/

push-image:
	docker push $(DOCKER_REPO):$(VERSION)

deploy:
	./mvnw -Drevision=$(VERSION) clean package -ntp -T1C -fae -Dspotless.skip -U -DskipTests
	./mvnw -Drevision=$(VERSION) deploy -s $$MAVEN_SETTINGS -ntp -T1 -fae -Dspotless.skip -DskipTests \
	  -DdeployAtEnd=true -DretryFailedDeploymentCount=3 \
	  -Dmaven.wagon.rto=120000 -Dmaven.wagon.http.retryHandler.count=3

show-version:
	@echo ${VERSION}

.PHONY: docs
docs:
	(cd docs/ && make build)
