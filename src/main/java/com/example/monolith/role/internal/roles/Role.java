package com.example.monolith.role.internal.roles;

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

@Entity
@Table(name = "roles")
public class Role {

	@Id
	private String name;

	// 권한 엔티티를 참조하지 않고 권한 코드(값)만 보관해 모듈 간 결합을 끊는다.
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_name"))
	@Column(name = "permission_code")
	private Set<String> permissionCodes = new HashSet<>();

	protected Role() {
	}

	public Role(String name, Set<String> permissionCodes) {
		this.name = name;
		this.permissionCodes.addAll(permissionCodes);
	}

	public void grant(Set<String> codes) {
		permissionCodes.addAll(codes);
	}

	public void revoke(String code) {
		permissionCodes.remove(code);
	}

	public String getName() {
		return name;
	}

	public Set<String> getPermissionCodes() {
		return Set.copyOf(permissionCodes);
	}

}
