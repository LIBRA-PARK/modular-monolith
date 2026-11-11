package com.example.monolith.role.internal.roles;

import java.util.List;
import java.util.Set;

import com.example.monolith.role.events.RoleCreated;
import com.example.monolith.role.events.RolePermissionsChanged;
import com.example.monolith.role.internal.permissionview.KnownPermissions;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

	private final RoleRepository roles;

	private final KnownPermissions knownPermissions;

	private final ApplicationEventPublisher events;

	public RoleService(RoleRepository roles, KnownPermissions knownPermissions, ApplicationEventPublisher events) {
		this.roles = roles;
		this.knownPermissions = knownPermissions;
		this.events = events;
	}

	public Role create(String name, Set<String> permissionCodes) {
		if (roles.existsById(name)) {
			throw new IllegalArgumentException("이미 존재하는 역할입니다: " + name);
		}
		knownPermissions.requireAll(permissionCodes);
		var role = roles.save(new Role(name, permissionCodes));
		events.publishEvent(new RoleCreated(name, role.getPermissionCodes()));
		return role;
	}

	public Role grantPermissions(String name, Set<String> permissionCodes) {
		var role = getRole(name);
		knownPermissions.requireAll(permissionCodes);
		role.grant(permissionCodes);
		events.publishEvent(new RolePermissionsChanged(name, role.getPermissionCodes()));
		return role;
	}

	/**
	 * 삭제된 권한을 복제본과 모든 역할에서 제거하고, 영향받은 역할마다 변경 이벤트를 발행한다.
	 */
	public void removePermissionEverywhere(String permissionCode) {
		knownPermissions.remove(permissionCode);
		for (var role : roles.findByPermissionCodesContaining(permissionCode)) {
			role.revoke(permissionCode);
			events.publishEvent(new RolePermissionsChanged(role.getName(), role.getPermissionCodes()));
		}
	}

	@Transactional(readOnly = true)
	public List<Role> findAll() {
		return roles.findAll();
	}

	private Role getRole(String name) {
		return roles.findById(name)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다: " + name));
	}

}
