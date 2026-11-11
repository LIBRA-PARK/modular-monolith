package com.example.monolith.role;

import java.util.Set;

/**
 * 역할이 생성되었음을 알리는 이벤트. 역할에 매핑된 권한 코드를 함께 전달해
 * 소비 측이 권한 모듈에 의존하지 않고도 유효 권한을 계산할 수 있게 한다.
 */
public record RoleCreated(String name, Set<String> permissionCodes) {
}
