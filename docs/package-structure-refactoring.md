# 모듈 내부 패키지 구조 리팩터링

모듈이 커져도 가독성과 경계를 유지할 수 있도록, `permission`·`role`·`user` 세 모듈의 내부 패키지 구조를
Spring Modulith 권장 방식에 맞춰 정리했다. 외부 동작(API, 이벤트 흐름)은 바뀌지 않았다.

## 1. 배경: Spring Modulith가 강제하는 것과 강제하지 않는 것

Spring Modulith는 모듈 **내부** 패키지 구조를 규정하지 않는다. `ApplicationModules.verify()`가 검사하는 규칙은 세 가지다.

1. 모듈 간 순환 의존이 없을 것
2. 다른 모듈의 internal 타입을 참조하지 않을 것
3. (선택) `@ApplicationModule(allowedDependencies = …)`에 선언한 대상에만 의존할 것

모듈 기본 패키지 아래의 하위 패키지는 **깊이와 관계없이 모두 internal**로 취급된다. 따라서 `internal` 아래를
얼마나 잘게 나누든 모듈 경계는 그대로 유지된다. 단, 하위 패키지로 나누면 같은 모듈 안에서 쓰기 위해 타입을
`public`으로 열어야 하므로, 이때부터 경계는 컴파일러가 아니라 `verify()`가 지킨다.

## 2. 권장 단계 (단순한 것에서 정교한 것으로)

공식 문서는 "단순하게 시작해 필요할 때 더 정교한 수단으로 옮겨 가라"는 입장이다.

| 단계 | 방식 | 적용 시점 |
|---|---|---|
| 1 | 평평한 기본 패키지 + package-private | 클래스가 적을 때. 컴파일러가 캡슐화를 보장한다. |
| 2 | `internal` 하위 패키지 | 파일이 늘어날 때. 경계는 `verify()`가 지킨다. |
| 3 | `@NamedInterface` | 공개 API를 용도별로 나누고 싶을 때(예: `events`, `spi`). |
| 4 | 중첩 모듈 (1.3+) | 모듈 안에 독자적 경계가 필요한 하위 영역이 생겼을 때. 하위 패키지에 `@ApplicationModule`을 붙인다. |
| 5 | 최상위 모듈로 분리 | 중첩 모듈로도 버거울 때. 모듈 경계 설정이 잘못되었다는 신호로 본다. |

`@ApplicationModule(type = Type.OPEN)`은 레거시 이관용이다. 완전히 모듈화된 애플리케이션에서 사용하면
"최적이 아닌 모듈화의 신호"라고 문서에 명시되어 있으므로 신규 코드에는 쓰지 않는다.

이번 리팩터링은 **2단계 → 3단계**로의 이동이며, `internal` 안은 레이어가 아니라 **기능 단위**로 나눴다.
기능 단위로 나눠 두면 4단계(중첩 모듈)로 승격할 때 패키지를 그대로 옮길 수 있다.

## 3. 변경 전후 구조

### 변경 전

```
permission/   PermissionCreated, PermissionDeleted            ← 기본 패키지 = 공개 API
  internal/   Permission, PermissionRepository, PermissionService, PermissionController
role/         RoleCreated, RolePermissionsChanged
  internal/   Role, RoleRepository, RoleService, RoleController,
              KnownPermission, KnownPermissionRepository, PermissionEventListener
user/         UserCreated
  internal/   User, UserRepository, UserService, UserController,
              RoleSnapshot, RoleSnapshotRepository, RoleEventListener
```

### 변경 후

```
permission/
├─ package-info.java          @ApplicationModule(allowedDependencies = {})
├─ events/                    @NamedInterface("events")
│  └─ PermissionCreated, PermissionDeleted
└─ internal/                  Permission, PermissionRepository, PermissionService, PermissionController

role/
├─ package-info.java          @ApplicationModule(allowedDependencies = "permission :: events")
├─ events/                    @NamedInterface("events")
│  └─ RoleCreated, RolePermissionsChanged
└─ internal/
   ├─ roles/                  Role, RoleRepository, RoleService, RoleController, PermissionEventListener
   └─ permissionview/         KnownPermissions, KnownPermission, KnownPermissionRepository

user/
├─ package-info.java          @ApplicationModule(allowedDependencies = "role :: events")
├─ events/                    @NamedInterface("events")
│  └─ UserCreated
└─ internal/
   ├─ users/                  User, UserRepository, UserService, UserController
   └─ roleview/               RoleSnapshots, RoleSnapshot, RoleSnapshotRepository, RoleEventListener
```

`permission`은 기능이 하나뿐이라 `internal`을 더 나누지 않았다. 필요 이상으로 쪼개지 않는 것도 원칙이다.

## 4. 변경 내용

### 4.1 공개 이벤트를 `events` named interface로 분리

- 각 모듈의 이벤트 `record`를 `<module>.events` 패키지로 옮기고 `package-info.java`에
  `@NamedInterface("events")`를 선언했다.
- 모듈 기본 패키지에는 `package-info.java`만 남는다. 무엇이 공개 API인지 패키지 이름만 보고 알 수 있다.

### 4.2 `allowedDependencies`를 named interface 단위로 변경

| 모듈 | 변경 전 | 변경 후 |
|---|---|---|
| `role` | `"permission"` | `"permission :: events"` |
| `user` | `"role"` | `"role :: events"` |

**실측 결과**: 이벤트를 `events`로 옮긴 뒤 선언을 바꾸지 않으면 `verify()`가 다음과 같이 실패한다.
모듈 이름만 적으면 기본 패키지(unnamed interface)만 허용되기 때문이다.

```
Module 'role' depends on named interface(s) 'permission :: events' via
  ...role.internal.roles.PermissionEventListener -> ...permission.events.PermissionCreated.
  Allowed targets: permission.
```

named interface 단위로 선언하면 "다른 모듈의 이벤트만 참조한다"는 설계 의도가 선언 자체에 드러나고,
나중에 해당 모듈이 `spi` 같은 다른 공개 API를 추가하더라도 의도치 않게 의존이 넓어지지 않는다.

### 4.3 `internal`을 기능 단위로 분리하고 하위 패키지 간 의존을 단방향으로 정리

`verify()`는 모듈 **내부** 하위 패키지 간 순환은 검사하지 않는다. 그대로 나누기만 하면
`RoleService → KnownPermissionRepository`, `PermissionEventListener → RoleService`처럼 양방향 참조가 생기므로,
읽기 모델 쪽 패키지가 자기 데이터만 다루는 컴포넌트를 공개하도록 바꿨다.

| 모듈 | 의존 방향 | 읽기 모델 컴포넌트 | 비공개로 전환 |
|---|---|---|---|
| `role` | `roles → permissionview` | `KnownPermissions` (`register`, `remove`, `contains`, `requireAll`) | `KnownPermissionRepository` |
| `user` | `users → roleview` | `RoleSnapshots` (`sync`, `contains`, `requireAll`, `permissionsOf`) | `RoleSnapshotRepository`, `RoleEventListener` |

- `role`: 권한 삭제 시 역할 매핑까지 정리해야 하므로 `PermissionEventListener`는 `roles`에 둔다.
  `permissionview`는 `roles`를 전혀 모른다.
- `user`: `roleview`는 이벤트 수신부터 조회까지 자기 완결적이다. `RoleEventListener`까지 package-private으로
  감췄고, `UserService`는 `RoleSnapshots`만 사용한다. 이 구조 덕분에 `roleview`는 그대로 중첩 모듈로 승격할 수 있다.
- 기존 `RoleService.registerKnownPermission`, `UserService.syncRole`은 각 읽기 모델 컴포넌트로 이동했다.

## 5. 검증

- `./gradlew test`: 11개 테스트 모두 통과 (`ModularityTests`의 `verify()` 포함)
- 생성된 모듈 다이어그램(`build/spring-modulith-docs/components.puml`)의 모듈 간 관계는 여전히 `listens to`뿐이다.
- `./gradlew bootRun` 후 HTTP 시나리오 재확인: 권한 생성 → 역할 생성 → 역할을 지정한 사용자 생성 →
  권한 삭제 시 사용자 유효 권한에서 제거, 없는 역할 지정 시 400.

## 6. 다음 단계 (필요해질 때)

- **중첩 모듈로 승격**: `user.internal.roleview`가 더 커지면 별도 패키지(예: `user.roleview`)로 옮기고
  `@ApplicationModule`을 선언한다. 중첩 모듈은 부모 모듈이나 같은 부모 아래의 형제 중첩 모듈에서만 접근할 수 있다.
- **모듈 내부 레이어 규칙 강제**: jMolecules ArchUnit을 추가하면 `verify()`가 DDD·아키텍처 규칙을 함께 검사한다.

  ```java
  var hexagonal = JMoleculesArchitectureRules.ensureHexagonal(VerificationDepth.STRICT);
  ApplicationModules.of(MonolithApplication.class)
      .verify(VerificationOptions.defaults().withAdditionalVerifications(hexagonal));
  ```
- **내부 하위 패키지 순환 검사**: 현재는 코드 리뷰로 단방향을 지킨다. 자동화가 필요하면 ArchUnit의
  `slices().matching("..internal.(*)..").should().beFreeOfCycles()` 규칙을 추가한다.

## 참고

- [Spring Modulith – Fundamentals](https://docs.spring.io/spring-modulith/reference/fundamentals.html)
- [Spring Modulith – Verification](https://docs.spring.io/spring-modulith/reference/verification.html)
- [jMolecules ArchUnit](https://github.com/xmolecules/jmolecules-integrations/tree/main/jmolecules-archunit)
