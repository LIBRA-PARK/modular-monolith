package com.example.monolith.role;

import java.util.Set;

/**
 * 역할의 권한 매핑이 바뀌었음을 알리는 이벤트. 변경 후의 전체 권한 코드를 담는다.
 */
public record RolePermissionsChanged(String name, Set<String> permissionCodes) {
}
