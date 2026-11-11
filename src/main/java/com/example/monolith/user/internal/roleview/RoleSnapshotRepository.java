package com.example.monolith.user.internal.roleview;

import org.springframework.data.jpa.repository.JpaRepository;

interface RoleSnapshotRepository extends JpaRepository<RoleSnapshot, String> {
}
