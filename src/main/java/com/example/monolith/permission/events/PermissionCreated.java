package com.example.monolith.permission.events;

/**
 * 권한이 생성되었음을 알리는 이벤트.
 */
public record PermissionCreated(String code, String description) {
}
