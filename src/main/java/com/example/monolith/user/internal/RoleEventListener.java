package com.example.monolith.user.internal;

import com.example.monolith.role.RoleCreated;
import com.example.monolith.role.RolePermissionsChanged;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 역할 모듈의 이벤트를 소비해 역할 스냅샷을 최신 상태로 유지한다.
 */
@Component
public class RoleEventListener {

	private final UserService users;

	public RoleEventListener(UserService users) {
		this.users = users;
	}

	@ApplicationModuleListener
	public void on(RoleCreated event) {
		users.syncRole(event.name(), event.permissionCodes());
	}

	@ApplicationModuleListener
	public void on(RolePermissionsChanged event) {
		users.syncRole(event.name(), event.permissionCodes());
	}

}
