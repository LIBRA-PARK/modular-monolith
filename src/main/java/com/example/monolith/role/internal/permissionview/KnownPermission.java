package com.example.monolith.role.internal.permissionview;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 권한 모듈의 이벤트로 채워지는 로컬 복제본. 역할에 권한을 매핑할 때
 * 권한 모듈을 동기 호출하지 않고 이 테이블로 존재 여부를 검증한다.
 */
@Entity
@Table(name = "role_known_permissions")
public class KnownPermission {

	@Id
	private String code;

	protected KnownPermission() {
	}

	public KnownPermission(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
