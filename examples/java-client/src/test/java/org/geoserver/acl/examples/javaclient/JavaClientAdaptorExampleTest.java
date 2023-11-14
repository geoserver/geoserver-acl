/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.examples.javaclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.client.AclClient;
import org.geoserver.acl.client.AclClientAdaptor;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Demonstrates how to use {@link AclClientAdaptor} to use the domain API directly
 * instead of the raw OpenAPI to manipulate rules and perform authorization requests.
 */
@Testcontainers(disabledWithoutDocker = true)
class JavaClientAdaptorExampleTest {

	private static final String dockerImageName = "geoservercloud/geoserver-acl:2.0-SNAPSHOT";

	// container port the application runs on with the 'dev' profile
	private static final int DEV_PORT = 9000;

	@Container
	static GenericContainer<?> aclServer = new GenericContainer<>(dockerImageName)
			// enabling the spring dev profile uses an embedded database and port 9000
			.withEnv("SPRING_PROFILES_ACTIVE", "dev").withExposedPorts(9000);

	/**
	 * {@link AclClient} provides the raw API clients through
	 * {@link AclClient#getAdminRulesApi()}, {@link AclClient#getRulesApi()}, and
	 * {@link AclClient#getAuthorizationApi()}
	 */
	private AclClient client;

	/**
	 * {@link AclClientAdaptor} provides domain repositories and the authorization
	 * implementation using the {@link AclClient} API client through
	 * {@link AclClientAdaptor#getAdminRuleRepository()},
	 * {@link AclClientAdaptor#getRuleRepository()}, and
	 * {@link AclClientAdaptor#getAuthorizationService()}
	 */
	AclClientAdaptor adaptor;

	@BeforeEach
	void beforeEach() {
		assertTrue(aclServer.isRunning());

		final Integer mappedPort = aclServer.getMappedPort(DEV_PORT);
		final String apiUrl = String.format("http://localhost:%d/acl/api", mappedPort);
		final String username = "admin";
		final String password = "s3cr3t";

		client = new AclClient()//
				.setBasePath(apiUrl)//
				.setUsername(username)//
				.setPassword(password);

		adaptor = new AclClientAdaptor(client);
	}

	@Test
	void testRulesAndAuthorization() {
		List<Rule> rules = createSampleRules();
		assertThat(rules).isNotEmpty();
		
		// Get the AuthorizationService that will defer requests to the remote ACL service
		AuthorizationService authService = adaptor.getAuthorizationService();
		
		// a user with ROLE_USER has access to layers in the users_ws workspace
		AccessRequest request = AccessRequest.builder()//
				.user("john")
				.roles("ROLE_AUTHENTICATED", "ROLE_USER")//
				.workspace("users_ws")//
				.layer("layer")//
				.build();
		
		AccessInfo accessInfo = authService.getAccessInfo(request);
		assertThat(accessInfo.getGrant()).isEqualTo(GrantType.ALLOW);
		
		// but does not have access to layers in the editors_ws workspace
		request = request.withWorkspace("editors_ws");
		
		accessInfo = authService.getAccessInfo(request);
		assertThat(accessInfo.getGrant()).isEqualTo(GrantType.DENY);
	}

	public List<Rule> createSampleRules() {
		// you can use a RuleAdminService using the repository implementation provided
		// by the API client adaptor to defer service calls to the remote service
		RuleAdminService service = new RuleAdminServiceImpl(adaptor.getRuleRepository());

		//prepare rules to insert
		Rule r1 = Rule.allow().withPriority(1L).withRolename("ROLE_USER").withWorkspace("users_ws");
		Rule r2 = Rule.allow().withPriority(2L).withRolename("ROLE_EDITOR").withWorkspace("editors_ws");

		//insert the rules, response comes with assigned id
		r1 = service.insert(r1);
		r2 = service.insert(r2);
		return List.of(r1, r2);
	}
}
