package com.example.monolith.user.events;

import java.util.Set;

/**
 * 사용자가 생성되었음을 알리는 이벤트.
 */
public record UserCreated(String username, Set<String> roleNames) {
}
