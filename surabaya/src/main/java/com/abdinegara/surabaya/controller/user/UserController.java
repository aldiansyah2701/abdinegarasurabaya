package com.abdinegara.surabaya.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abdinegara.surabaya.message.RequestLogin;
import com.abdinegara.surabaya.message.RequestRegisterUser;
import com.abdinegara.surabaya.message.RequestUpdateUser;
import com.abdinegara.surabaya.message.RequestRegisterUser.TYPE;
import com.abdinegara.surabaya.service.UserService;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> loginUser(@RequestBody RequestLogin request) {
		return userService.loginUser(request.getUsername(), request.getPassword());
	}

	@PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> registerUser(@RequestBody RequestRegisterUser request) {
		return userService.registerUser(request);
	}
	
	@PutMapping(path = "/update-user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERVISOR')")
	public ResponseEntity<Object> updateUser(@RequestBody RequestRegisterUser request) {
		return userService.updateUser(request);
	}
	
	@PutMapping(path = "/update-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERVISOR')")
	public ResponseEntity<Object> updatePassword(@RequestBody RequestUpdateUser request) {
		return userService.updatePasswordUser(request);
	}

	@GetMapping(value = "/all")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERVISOR')")
	public ResponseEntity<Object> getAllUser() {
		return userService.getAllUser();
	}
	
	@GetMapping(value = "/get-user/{name}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERVISOR')")
	public ResponseEntity<Object> getUser(@PathVariable("name") String name) {
		return userService.getUser(name);
	}
	
	@GetMapping(value = "/get-all-user/{type}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERVISOR')")
	public ResponseEntity<Object> getAllUserByType(@PathVariable("type") TYPE type) {
		return userService.getAllUserByType(type);
	}

	@DeleteMapping(value = "/delete-user/{name}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERVISOR')")
	public ResponseEntity<Object> deleteUser(@PathVariable("name") String name) {
		return userService.deleteUser(name);
	}
}
