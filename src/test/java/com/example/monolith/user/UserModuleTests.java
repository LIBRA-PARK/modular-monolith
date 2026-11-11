package com.example.monolith.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Set;

import com.example.monolith.role.events.RoleCreated;
import com.example.monolith.role.events.RolePermissionsChanged;
import com.example.monolith.user.events.UserCreated;
import com.example.monolith.user.internal.roleview.RoleSnapshots;
import com.example.monolith.user.internal.users.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

/**
 * 역할·권한 모듈의 빈 없이 사용자 모듈만 기동해, 역할 이벤트만으로 동작하는지 검증한다.
 */
@ApplicationModuleTest
class UserModuleTests {

	@Autowired
	UserService users;

	@Autowired
	RoleSnapshots roleSnapshots;

	@Test
	void createsUserWithExistingRoleAndResolvesPermissionsThroughRole(Scenario scenario) {
		scenario.publish(new RoleCreated("MANAGER", Set.of("user:read", "user:write")))
				.andWaitForStateChange(() -> roleSnapshots.contains("MANAGER"), Boolean::booleanValue)
				.andVerify(exists -> assertThat(exists).isTrue());

		users.create("alice", Set.of("MANAGER"));

		assertThat(users.findEffectivePermissions("alice")).containsExactly("user:read", "user:write");
	}

	@Test
	void rejectsUserWithUnknownRole() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> users.create("bob", Set.of("NOT_EXISTS")))
				.withMessageContaining("NOT_EXISTS");
	}

	@Test
	void publishesUserCreated(Scenario scenario) {
		roleSnapshots.sync("STAFF", Set.of("board:read"));

		scenario.stimulate(() -> users.create("carol", Set.of("STAFF")))
				.andWaitForEventOfType(UserCreated.class)
				.matching(event -> event.username().equals("carol"))
				.toArriveAndVerify(event -> assertThat(event.roleNames()).containsExactly("STAFF"));
	}

	@Test
	void reflectsRolePermissionChangesInEffectivePermissions(Scenario scenario) {
		roleSnapshots.sync("EDITOR", Set.of("post:read"));
		users.create("dave", Set.of("EDITOR"));

		scenario.publish(new RolePermissionsChanged("EDITOR", Set.of("post:read", "post:write")))
				.andWaitForStateChange(() -> users.findEffectivePermissions("dave"), perms -> perms.size() == 2)
				.andVerify(perms -> assertThat(perms).containsExactly("post:read", "post:write"));
	}

}
