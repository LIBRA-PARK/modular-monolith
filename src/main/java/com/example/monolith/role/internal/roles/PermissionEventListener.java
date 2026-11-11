package com.example.monolith.role.internal.roles;

import com.example.monolith.permission.events.PermissionCreated;
import com.example.monolith.permission.events.PermissionDeleted;
import com.example.monolith.role.internal.permissionview.KnownPermissions;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 권한 모듈의 이벤트를 소비한다. {@link ApplicationModuleListener}는 발행 트랜잭션이 커밋된 뒤
 * 별도 트랜잭션에서 비동기로 실행되며, 실패하면 Event Publication Registry에 미완료로 남는다.
 * 권한 삭제는 역할 매핑까지 정리해야 하므로 roles 패키지에 둔다(roles → permissionview 단방향).
 */
@Component
public class PermissionEventListener {

	private final KnownPermissions knownPermissions;

	private final RoleService roles;

	public PermissionEventListener(KnownPermissions knownPermissions, RoleService roles) {
		this.knownPermissions = knownPermissions;
		this.roles = roles;
	}

	@ApplicationModuleListener
	public void on(PermissionCreated event) {
		knownPermissions.register(event.code());
	}

	@ApplicationModuleListener
	public void on(PermissionDeleted event) {
		roles.removePermissionEverywhere(event.code());
	}

}
