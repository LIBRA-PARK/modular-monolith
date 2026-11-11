/**
 * 사용자(User) 모듈. 역할 모듈의 이벤트만 소비한다. 권한 모듈에는 의존할 수 없으므로
 * 사용자에게 권한을 직접 매핑하는 경로가 구조적으로 존재하지 않는다.
 */
@ApplicationModule(displayName = "User", allowedDependencies = "role :: events")
package com.example.monolith.user;

import org.springframework.modulith.ApplicationModule;
