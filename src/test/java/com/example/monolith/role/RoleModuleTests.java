package com.example.monolith.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Set;

import com.example.monolith.permission.events.PermissionCreated;
import com.example.monolith.permission.events.PermissionDeleted;
import com.example.monolith.role.events.RoleCreated;
import com.example.monolith.role.events.RolePermissionsChanged;
import com.example.monolith.role.internal.permissionview.KnownPermissions;
import com.example.monolith.role.internal.roles.RoleService;
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
	KnownPermissions knownPermissions;

	@Test
	void consumesPermissionCreatedIntoLocalReplica(Scenario scenario) {
		scenario.publish(new PermissionCreated("order:read", "주문 조회"))
				.andWaitForStateChange(() -> knownPermissions.contains("order:read"), Boolean::booleanValue)
				.andVerify(exists -> assertThat(exists).isTrue());
	}

	@Test
	void createsRoleOnlyWithKnownPermissions(Scenario scenario) {
		knownPermissions.register("report:read");

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
		knownPermissions.register("audit:read");
		knownPermissions.register("audit:write");
		roles.create("AUDITOR", Set.of("audit:read", "audit:write"));

		scenario.publish(new PermissionDeleted("audit:write"))
				.andWaitForEventOfType(RolePermissionsChanged.class)
				.matching(event -> event.name().equals("AUDITOR"))
				.toArriveAndVerify(event -> assertThat(event.permissionCodes()).containsExactly("audit:read"));
	}

}
