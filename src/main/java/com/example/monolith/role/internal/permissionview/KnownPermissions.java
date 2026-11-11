package com.example.monolith.role.internal.permissionview;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 권한 모듈 이벤트로 채워지는 권한 복제본을 다룬다. 역할 기능(roles)이 이 컴포넌트만 사용하며,
 * 이 패키지는 roles에 의존하지 않는다(단방향).
 */
@Component
@Transactional
public class KnownPermissions {

	private final KnownPermissionRepository repository;

	KnownPermissions(KnownPermissionRepository repository) {
		this.repository = repository;
	}

	public void register(String code) {
		repository.save(new KnownPermission(code));
	}

	public void remove(String code) {
		repository.deleteById(code);
	}

	@Transactional(readOnly = true)
	public boolean contains(String code) {
		return repository.existsById(code);
	}

	@Transactional(readOnly = true)
	public void requireAll(Set<String> codes) {
		var unknown = new TreeSet<>(codes);
		repository.findAllById(codes).forEach(known -> unknown.remove(known.getCode()));
		if (!unknown.isEmpty()) {
			throw new IllegalArgumentException("존재하지 않는 권한입니다: " + unknown);
		}
	}

}
