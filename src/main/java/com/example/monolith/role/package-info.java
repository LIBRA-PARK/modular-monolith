/**
 * 역할(Role) 모듈. 권한 모듈의 이벤트만 소비하며, 권한 모듈의 빈을 직접 호출하지 않는다.
 * events 패키지(named interface "events")만 외부에 공개한다.
 */
@ApplicationModule(displayName = "Role", allowedDependencies = "permission :: events")
package com.example.monolith.role;

import org.springframework.modulith.ApplicationModule;
