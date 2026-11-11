/**
 * 권한(Permission) 모듈. 다른 업무 모듈에 의존하지 않는 최하위 모듈이다.
 * 기본 패키지의 이벤트 타입만 외부에 공개한다.
 */
@ApplicationModule(displayName = "Permission", allowedDependencies = {})
package com.example.monolith.permission;

import org.springframework.modulith.ApplicationModule;
