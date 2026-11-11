package com.example.monolith.permission.internal;

import java.util.List;

import com.example.monolith.permission.events.PermissionCreated;
import com.example.monolith.permission.events.PermissionDeleted;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionService {

	private final PermissionRepository permissions;

	private final ApplicationEventPublisher events;

	public PermissionService(PermissionRepository permissions, ApplicationEventPublisher events) {
		this.permissions = permissions;
		this.events = events;
	}

	public Permission create(String code, String description) {
		if (permissions.existsById(code)) {
			throw new IllegalArgumentException("이미 존재하는 권한입니다: " + code);
		}
		var permission = permissions.save(new Permission(code, description));
		// 같은 트랜잭션에서 발행하므로 Event Publication Registry에 함께 기록된다.
		events.publishEvent(new PermissionCreated(code, description));
		return permission;
	}

	public void delete(String code) {
		if (!permissions.existsById(code)) {
			throw new IllegalArgumentException("존재하지 않는 권한입니다: " + code);
		}
		permissions.deleteById(code);
		events.publishEvent(new PermissionDeleted(code));
	}

	@Transactional(readOnly = true)
	public List<Permission> findAll() {
		return permissions.findAll();
	}

}
