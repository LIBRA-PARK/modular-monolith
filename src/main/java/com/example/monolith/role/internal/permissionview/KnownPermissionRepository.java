package com.example.monolith.role.internal.permissionview;

import org.springframework.data.jpa.repository.JpaRepository;

interface KnownPermissionRepository extends JpaRepository<KnownPermission, String> {
}
