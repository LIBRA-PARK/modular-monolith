package com.example.monolith.user.internal.users;

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
@Table(name = "users")
public class User {

	@Id
	private String username;

	// 사용자는 역할 이름만 가진다. 권한은 역할을 통해서만 계산된다.
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "username"))
	@Column(name = "role_name")
	private Set<String> roleNames = new HashSet<>();

	protected User() {
	}

	public User(String username, Set<String> roleNames) {
		this.username = username;
		this.roleNames.addAll(roleNames);
	}

	public void assign(Set<String> names) {
		roleNames.addAll(names);
	}

	public String getUsername() {
		return username;
	}

	public Set<String> getRoleNames() {
		return Set.copyOf(roleNames);
	}

}
