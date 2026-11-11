# modular-monolith

Spring Boot와 Spring Modulith 기반 프로젝트입니다. Gradle Toolchain이 JDK 25를 자동 설치하며,
별도 DB 설정이 없으면 H2 인메모리 DB를 사용합니다.

## 모듈 구조

`MonolithApplication`에 `@Modulithic`을 선언하고 Spring Modulith의 기본 모듈 탐지 규칙을 사용합니다.

### 업무 모듈: Permission ← Role ← User

| 모듈 | 허용 의존 | 공개 이벤트 (`events`) | 소비 이벤트 | 내부 기능 패키지 |
|---|---|---|---|---|
| `permission` | 없음 | `PermissionCreated`, `PermissionDeleted` | - | `internal` |
| `role` | `permission :: events` | `RoleCreated`, `RolePermissionsChanged` | 권한 이벤트 | `internal.roles`, `internal.permissionview` |
| `user` | `role :: events` | `UserCreated` | 역할 이벤트 | `internal.users`, `internal.roleview` |

- 모듈 간에는 서로의 빈을 호출하지 않고, 각 모듈이 `events` named interface로 공개한 **이벤트 타입만** 참조합니다.
- `internal` 아래는 기능 단위로 나누며, 하위 패키지 간 의존은 단방향(`roles → permissionview`, `users → roleview`)으로 유지합니다.
  구조와 근거는 [모듈 내부 패키지 구조 리팩터링](docs/package-structure-refactoring.md)을 참고하세요.
- `@ApplicationModuleListener`(커밋 후 비동기, 별도 트랜잭션)로 이벤트를 소비해 로컬 읽기 모델을 갱신합니다.
  발행 내역은 Event Publication Registry(JPA)에 기록됩니다.
- 사용자 생성 시 지정한 역할은 `user` 모듈의 역할 스냅샷으로 검증하고, 유효 권한도 스냅샷으로 계산합니다.
- `user`는 `permission`에 의존할 수 없으므로 권한을 사용자에게 직접 매핑하는 경로가 없습니다.
- 읽기 모델은 최종 일관성을 가집니다. 역할 생성 직후 아주 짧은 시간 동안은 사용자 생성 시 역할이 아직
  보이지 않을 수 있습니다.

```sh
curl -H 'Content-Type: application/json' -d '{"code":"user:read","description":"조회"}' localhost:8080/api/permissions
curl -H 'Content-Type: application/json' -d '{"name":"ADMIN","permissionCodes":["user:read"]}' localhost:8080/api/roles
curl -H 'Content-Type: application/json' -d '{"username":"alice","roleNames":["ADMIN"]}' localhost:8080/api/users
curl localhost:8080/api/users/alice   # effectivePermissions: ["user:read"]
```

앱 실행 후 브라우저에서 `http://localhost:8080`을 열면 버튼으로 같은 흐름을 확인할 수 있는 데모 페이지가 열립니다.

- `com.example.monolith` 바로 아래의 하위 패키지를 업무 모듈로 인식합니다.
- 모듈 기본 패키지와 `@NamedInterface`를 선언한 패키지(예: `events`)만 외부에 공개되며, 나머지 하위 패키지는 내부 구현입니다.
- 모듈 간 순환 의존과 다른 모듈의 내부 구현 참조를 금지합니다.
- 의존 대상을 제한할 때는 모듈의 `package-info.java`에
  `@ApplicationModule(allowedDependencies = {"허용할 모듈 이름"})`을 선언합니다.

## 실행과 검증

```sh
./gradlew bootRun
./gradlew build
./gradlew test --tests com.example.monolith.ModularityTests
```

`ModularityTests`는 모듈 경계를 검사하며, 모듈이 추가되면 같은 테스트로 검증합니다.
검증에 성공하면 `build/spring-modulith-docs/`에 전체·개별 모듈의 PlantUML 다이어그램,
모듈별 AsciiDoc Canvas와 이를 모은 `all-docs.adoc`을 자동 생성합니다.
문서를 강제로 다시 생성하려면 다음 명령을 실행합니다.

```sh
./gradlew test --tests com.example.monolith.ModularityTests --rerun-tasks
```

`MonolithApplicationTests`는 H2와 JPA를 포함한 전체 애플리케이션 컨텍스트의 기동을 확인합니다.
개별 업무 모듈의 통합 테스트는 해당 모듈 패키지에 두고 `@ApplicationModuleTest`를 사용합니다.

규칙 참고: [Spring Modulith 모듈 구조 검증](https://docs.spring.io/spring-modulith/reference/verification.html)
