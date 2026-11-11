package com.example.monolith.user.internal.roleview;

import com.example.monolith.role.events.RoleCreated;
import com.example.monolith.role.events.RolePermissionsChanged;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 역할 모듈의 이벤트를 소비해 역할 스냅샷을 최신 상태로 유지한다.
 */
@Component
class RoleEventListener {

	private final RoleSnapshots snapshots;

	RoleEventListener(RoleSnapshots snapshots) {
		this.snapshots = snapshots;
	}

	@ApplicationModuleListener
	void on(RoleCreated event) {
		snapshots.sync(event.name(), event.permissionCodes());
	}

	@ApplicationModuleListener
	void on(RolePermissionsChanged event) {
		snapshots.sync(event.name(), event.permissionCodes());
	}

}
