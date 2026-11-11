package com.example.monolith.role.internal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnownPermissionRepository extends JpaRepository<KnownPermission, String> {
}
