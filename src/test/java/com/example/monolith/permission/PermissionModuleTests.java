package com.example.monolith.permission;

import com.example.monolith.permission.internal.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

@ApplicationModuleTest
class PermissionModuleTests {

	@Autowired
	PermissionService permissions;

	@Test
	void publishesPermissionCreatedOnCreate(Scenario scenario) {
		scenario.stimulate(() -> permissions.create("user:read", "사용자 조회"))
				.andWaitForEventOfType(PermissionCreated.class)
				.matching(event -> event.code().equals("user:read"))
				.toArrive();
	}

	@Test
	void publishesPermissionDeletedOnDelete(Scenario scenario) {
		permissions.create("user:delete", "사용자 삭제");

		scenario.stimulate(() -> permissions.delete("user:delete"))
				.andWaitForEventOfType(PermissionDeleted.class)
				.matching(event -> event.code().equals("user:delete"))
				.toArrive();
	}

}
