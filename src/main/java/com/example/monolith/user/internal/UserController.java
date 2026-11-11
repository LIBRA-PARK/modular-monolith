package com.example.monolith.user.internal;

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
@RequestMapping("/api/users")
class UserController {

	private final UserService users;

	UserController(UserService users) {
		this.users = users;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	UserResponse create(@RequestBody CreateUserRequest request) {
		var user = users.create(request.username(), nullSafe(request.roleNames()));
		return toResponse(user);
	}

	@PostMapping("/{username}/roles")
	UserResponse assignRoles(@PathVariable String username, @RequestBody AssignRolesRequest request) {
		return toResponse(users.assignRoles(username, nullSafe(request.roleNames())));
	}

	@GetMapping("/{username}")
	UserResponse find(@PathVariable String username) {
		return toResponse(users.findByUsername(username));
	}

	@GetMapping
	List<UserResponse> findAll() {
		return users.findAll().stream().map(this::toResponse).toList();
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(user.getUsername(), user.getRoleNames(),
				users.findEffectivePermissions(user.getUsername()));
	}

	private static Set<String> nullSafe(Set<String> names) {
		return names == null ? Set.of() : names;
	}

	record CreateUserRequest(String username, Set<String> roleNames) {
	}

	record AssignRolesRequest(Set<String> roleNames) {
	}

	record UserResponse(String username, Set<String> roleNames, Set<String> effectivePermissions) {
	}

}
