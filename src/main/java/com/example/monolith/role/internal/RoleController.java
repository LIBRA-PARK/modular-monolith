package com.example.monolith.role.internal;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
class RoleController {

	private final RoleService roles;

	RoleController(RoleService roles) {
		this.roles = roles;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	RoleResponse create(@RequestBody CreateRoleRequest request) {
		return RoleResponse.from(roles.create(request.name(), nullSafe(request.permissionCodes())));
	}

	@PostMapping("/{name}/permissions")
	RoleResponse grant(@PathVariable String name, @RequestBody GrantPermissionsRequest request) {
		return RoleResponse.from(roles.grantPermissions(name, nullSafe(request.permissionCodes())));
	}

	@GetMapping
	List<RoleResponse> findAll() {
		return roles.findAll().stream().map(RoleResponse::from).toList();
	}

	private static Set<String> nullSafe(Set<String> codes) {
		return codes == null ? Set.of() : codes;
	}

	record CreateRoleRequest(String name, Set<String> permissionCodes) {
	}

	record GrantPermissionsRequest(Set<String> permissionCodes) {
	}

	record RoleResponse(String name, Set<String> permissionCodes) {

		static RoleResponse from(Role role) {
			return new RoleResponse(role.getName(), role.getPermissionCodes());
		}

	}

}
