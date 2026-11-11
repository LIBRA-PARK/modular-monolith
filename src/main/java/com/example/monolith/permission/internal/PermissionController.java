package com.example.monolith.permission.internal;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
class PermissionController {

	private final PermissionService permissions;

	PermissionController(PermissionService permissions) {
		this.permissions = permissions;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	PermissionResponse create(@RequestBody CreatePermissionRequest request) {
		return PermissionResponse.from(permissions.create(request.code(), request.description()));
	}

	@GetMapping
	List<PermissionResponse> findAll() {
		return permissions.findAll().stream().map(PermissionResponse::from).toList();
	}

	@DeleteMapping("/{code}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable String code) {
		permissions.delete(code);
	}

	record CreatePermissionRequest(String code, String description) {
	}

	record PermissionResponse(String code, String description) {

		static PermissionResponse from(Permission permission) {
			return new PermissionResponse(permission.getCode(), permission.getDescription());
		}

	}

}
