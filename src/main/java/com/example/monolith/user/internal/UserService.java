package com.example.monolith.user.internal;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.example.monolith.user.UserCreated;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

	private final UserRepository users;

	private final RoleSnapshotRepository roleSnapshots;

	private final ApplicationEventPublisher events;

	public UserService(UserRepository users, RoleSnapshotRepository roleSnapshots,
			ApplicationEventPublisher events) {
		this.users = users;
		this.roleSnapshots = roleSnapshots;
		this.events = events;
	}

	/**
	 * 사용자를 생성하면서 이미 만들어진 역할을 지정한다. 역할 존재 여부는 로컬 스냅샷으로 검증한다.
	 */
	public User create(String username, Set<String> roleNames) {
		if (users.existsById(username)) {
			throw new IllegalArgumentException("이미 존재하는 사용자입니다: " + username);
		}
		requireKnownRoles(roleNames);
		var user = users.save(new User(username, roleNames));
		events.publishEvent(new UserCreated(username, user.getRoleNames()));
		return user;
	}

	public User assignRoles(String username, Set<String> roleNames) {
		var user = getUser(username);
		requireKnownRoles(roleNames);
		user.assign(roleNames);
		return user;
	}

	/**
	 * 사용자 → 역할 → 권한 경로로만 유효 권한을 계산한다.
	 */
	@Transactional(readOnly = true)
	public Set<String> findEffectivePermissions(String username) {
		var permissions = new TreeSet<String>();
		roleSnapshots.findAllById(getUser(username).getRoleNames())
				.forEach(snapshot -> permissions.addAll(snapshot.getPermissionCodes()));
		return permissions;
	}

	@Transactional(readOnly = true)
	public User findByUsername(String username) {
		return getUser(username);
	}

	@Transactional(readOnly = true)
	public List<User> findAll() {
		return users.findAll();
	}

	public void syncRole(String roleName, Set<String> permissionCodes) {
		roleSnapshots.findById(roleName).ifPresentOrElse(
				snapshot -> snapshot.replacePermissions(permissionCodes),
				() -> roleSnapshots.save(new RoleSnapshot(roleName, permissionCodes)));
	}

	private User getUser(String username) {
		return users.findById(username)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + username));
	}

	private void requireKnownRoles(Set<String> roleNames) {
		var unknown = new TreeSet<>(roleNames);
		roleSnapshots.findAllById(roleNames).forEach(snapshot -> unknown.remove(snapshot.getRoleName()));
		if (!unknown.isEmpty()) {
			throw new IllegalArgumentException("존재하지 않는 역할입니다: " + unknown);
		}
	}

}
