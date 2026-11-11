package com.example.monolith.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Set;

import com.example.monolith.permission.PermissionCreated;
import com.example.monolith.permission.PermissionDeleted;
import com.example.monolith.role.internal.KnownPermissionRepository;
import com.example.monolith.role.internal.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

/**
 * 권한 모듈의 빈 없이(STANDALONE) 역할 모듈만 기동해 이벤트 소비를 검증한다.
 */
@ApplicationModuleTest
class RoleModuleTests {

	@Autowired
	RoleService roles;

	@Autowired
	KnownPermissionRepository knownPermissions;

	@Test
	void consumesPermissionCreatedIntoLocalReplica(Scenario scenario) {
		scenario.publish(new PermissionCreated("order:read", "주문 조회"))
				.andWaitForStateChange(() -> knownPermissions.existsById("order:read"), Boolean::booleanValue)
				.andVerify(exists -> assertThat(exists).isTrue());
	}

	@Test
	void createsRoleOnlyWithKnownPermissions(Scenario scenario) {
		roles.registerKnownPermission("report:read");

		scenario.stimulate(() -> roles.create("VIEWER", Set.of("report:read")))
				.andWaitForEventOfType(RoleCreated.class)
				.matching(event -> event.name().equals("VIEWER"))
				.toArriveAndVerify(event -> assertThat(event.permissionCodes()).containsExactly("report:read"));

		assertThatIllegalArgumentException()
				.isThrownBy(() -> roles.create("GHOST", Set.of("unknown:permission")))
				.withMessageContaining("unknown:permission");
	}

	@Test
	void revokesDeletedPermissionFromRoles(Scenario scenario) {
		roles.registerKnownPermission("audit:read");
		roles.registerKnownPermission("audit:write");
		roles.create("AUDITOR", Set.of("audit:read", "audit:write"));

		scenario.publish(new PermissionDeleted("audit:write"))
				.andWaitForEventOfType(RolePermissionsChanged.class)
				.matching(event -> event.name().equals("AUDITOR"))
				.toArriveAndVerify(event -> assertThat(event.permissionCodes()).containsExactly("audit:read"));
	}

}
