/**
 * 역할(Role) 모듈. 권한 모듈의 이벤트만 소비하며, 권한 모듈의 빈을 직접 호출하지 않는다.
 * 기본 패키지의 이벤트 타입만 외부에 공개한다.
 */
@ApplicationModule(displayName = "Role", allowedDependencies = "permission")
package com.example.monolith.role;

import org.springframework.modulith.ApplicationModule;
