package com.example.monolith.role.internal;

import com.example.monolith.permission.PermissionCreated;
import com.example.monolith.permission.PermissionDeleted;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 권한 모듈의 이벤트를 소비한다. {@link ApplicationModuleListener}는 발행 트랜잭션이 커밋된 뒤
 * 별도 트랜잭션에서 비동기로 실행되며, 실패하면 Event Publication Registry에 미완료로 남는다.
 */
@Component
public class PermissionEventListener {

	private final RoleService roles;

	public PermissionEventListener(RoleService roles) {
		this.roles = roles;
	}

	@ApplicationModuleListener
	public void on(PermissionCreated event) {
		roles.registerKnownPermission(event.code());
	}

	@ApplicationModuleListener
	public void on(PermissionDeleted event) {
		roles.removePermissionEverywhere(event.code());
	}

}
