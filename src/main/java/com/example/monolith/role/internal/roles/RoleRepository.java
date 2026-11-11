package com.example.monolith.role.internal.roles;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

	List<Role> findByPermissionCodesContaining(String permissionCode);

}
