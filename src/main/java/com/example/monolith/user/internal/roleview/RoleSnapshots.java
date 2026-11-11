package com.example.monolith.user.internal.roleview;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 역할 모듈 이벤트로 채워지는 역할 스냅샷을 다룬다. 사용자 기능(users)이 이 컴포넌트만 사용하며,
 * 이 패키지는 users에 의존하지 않는다(단방향). 이벤트 수신부터 조회까지 자기 완결적이므로
 * 더 커지면 중첩 모듈(@ApplicationModule)로 승격하기 쉽다.
 */
@Component
@Transactional
public class RoleSnapshots {

	private final RoleSnapshotRepository repository;

	RoleSnapshots(RoleSnapshotRepository repository) {
		this.repository = repository;
	}

	public void sync(String roleName, Set<String> permissionCodes) {
		repository.findById(roleName).ifPresentOrElse(
				snapshot -> snapshot.replacePermissions(permissionCodes),
				() -> repository.save(new RoleSnapshot(roleName, permissionCodes)));
	}

	@Transactional(readOnly = true)
	public boolean contains(String roleName) {
		return repository.existsById(roleName);
	}

	@Transactional(readOnly = true)
	public void requireAll(Set<String> roleNames) {
		var unknown = new TreeSet<>(roleNames);
		repository.findAllById(roleNames).forEach(snapshot -> unknown.remove(snapshot.getRoleName()));
		if (!unknown.isEmpty()) {
			throw new IllegalArgumentException("존재하지 않는 역할입니다: " + unknown);
		}
	}

	/**
	 * 주어진 역할들에 매핑된 권한 코드의 합집합을 반환한다.
	 */
	@Transactional(readOnly = true)
	public Set<String> permissionsOf(Set<String> roleNames) {
		var permissions = new TreeSet<String>();
		repository.findAllById(roleNames).forEach(snapshot -> permissions.addAll(snapshot.getPermissionCodes()));
		return permissions;
	}

}
