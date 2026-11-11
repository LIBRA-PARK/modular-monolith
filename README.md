# modular-monolith

Spring Boot와 Spring Modulith 기반 프로젝트입니다. Gradle Toolchain이 JDK 25를 자동 설치하며,
별도 DB 설정이 없으면 H2 인메모리 DB를 사용합니다.

## 모듈 구조

`MonolithApplication`에 `@Modulithic`을 선언하고 Spring Modulith의 기본 모듈 탐지 규칙을 사용합니다.
현재는 초기 설정 단계로 업무 모듈이 없습니다.

- `com.example.monolith` 바로 아래의 하위 패키지를 업무 모듈로 인식합니다.
- 모듈 기본 패키지에는 외부에 공개할 API를, 그 아래 하위 패키지에는 내부 구현을 둡니다.
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
현재는 업무 모듈이 없으므로 개별 모듈 문서는 생성되지 않습니다.
문서를 강제로 다시 생성하려면 다음 명령을 실행합니다.

```sh
./gradlew test --tests com.example.monolith.ModularityTests --rerun-tasks
```

`MonolithApplicationTests`는 H2와 JPA를 포함한 전체 애플리케이션 컨텍스트의 기동을 확인합니다.
개별 업무 모듈의 통합 테스트는 해당 모듈 패키지에 두고 `@ApplicationModuleTest`를 사용합니다.

규칙 참고: [Spring Modulith 모듈 구조 검증](https://docs.spring.io/spring-modulith/reference/verification.html)
