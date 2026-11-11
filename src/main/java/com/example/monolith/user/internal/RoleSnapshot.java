package com.example.monolith.user.internal;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * 역할 모듈의 이벤트로 채워지는 읽기 모델. 사용자 생성 시 지정한 역할의 존재 여부를 검증하고,
 * 역할 모듈을 호출하지 않고 사용자의 유효 권한을 계산하는 데 쓴다.
 */
@Entity
@Table(name = "user_role_snapshots")
public class RoleSnapshot {

	@Id
	private String roleName;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_role_snapshot_permissions", joinColumns = @JoinColumn(name = "role_name"))
	@Column(name = "permission_code")
	private Set<String> permissionCodes = new HashSet<>();

	protected RoleSnapshot() {
	}

	public RoleSnapshot(String roleName, Set<String> permissionCodes) {
		this.roleName = roleName;
		this.permissionCodes.addAll(permissionCodes);
	}

	public void replacePermissions(Set<String> codes) {
		permissionCodes.clear();
		permissionCodes.addAll(codes);
	}

	public String getRoleName() {
		return roleName;
	}

	public Set<String> getPermissionCodes() {
		return Set.copyOf(permissionCodes);
	}

}
