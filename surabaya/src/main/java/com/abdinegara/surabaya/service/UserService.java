package com.abdinegara.surabaya.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abdinegara.surabaya.entity.ModelUserAndRoles;
import com.abdinegara.surabaya.entity.Role;
import com.abdinegara.surabaya.entity.Role.ROLE;
import com.abdinegara.surabaya.entity.Siswa;
import com.abdinegara.surabaya.entity.User;
import com.abdinegara.surabaya.kernel.JwtTokenProvider;
import com.abdinegara.surabaya.message.BaseResponse;
import com.abdinegara.surabaya.message.RequestRegisterUser;
import com.abdinegara.surabaya.message.RequestRegisterUser.TYPE;
import com.abdinegara.surabaya.message.RequestUpdateUser;
import com.abdinegara.surabaya.message.ResponseCreateToken;
import com.abdinegara.surabaya.message.ResponseGetAllUsers;
import com.abdinegara.surabaya.repository.RoleRepository;
import com.abdinegara.surabaya.repository.SiswaRepository;
import com.abdinegara.surabaya.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private SiswaRepository siswaRepository;
	
	public ResponseEntity<Object> loginUser(String userName, String password) {
		BaseResponse response = new BaseResponse();
		try {
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName, password);
			Authentication authenticate = authenticationManager.authenticate(authentication); //HOW THIS WORKS?
			// ternyata proses aunthenticate call loadUserByUsername, untuk mendapatkan data dari database
			
			ResponseCreateToken responseJwt = jwtTokenProvider.createToken(userName);
			responseJwt.setMessage(BaseResponse.SUCCESS);
			log.info("Login success");

			return new ResponseEntity<>(responseJwt, HttpStatus.OK);
		} catch (AuthenticationException e) {
			response.setMessage(BaseResponse.FAILED);
			log.info("Login failed");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> registerUser(RequestRegisterUser data) {
		BaseResponse response = new BaseResponse();
		User findByName = userRepository.findByName(data.getUsername());
		if (findByName != null) {
			response.setMessage(BaseResponse.ALREADY_EXIST);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		User user = new User();
		user.setName(data.getUsername());
		user.setPassword(passwordEncoder.encode(data.getPassword()));
		EncryptDecryptModal decryptEncrypt = new EncryptDecryptModal();
		user.setPasswordData(decryptEncrypt.setSensitiveData(data.getPassword()));
		user.setCreatedDate(new Date());
		user.setActive(true);
		user.setUserType(data.getType().toString());
		user = userRepository.save(user);

		for (String roleName : data.getRoles()) {
			Role role = new Role();
			role.setName(ROLE.valueOf(roleName).toString());
			role.setCreatedDate(new Date());
			role.setUser(user);
			role = roleRepository.save(role);
		}
		
		if(TYPE.SISWA.equals(data.getType())) {
			Siswa siswa = new Siswa();
			siswa.setCreatedDate(new Date());
			siswa.setEmail(data.getSiswa().getEmail());
			siswa.setHandphone(data.getSiswa().getHandphone());
			siswa.setName(data.getSiswa().getName());
			siswa.setTitle(data.getSiswa().getTitle());
			siswa.setUserUuid(user.getUuid());
			siswaRepository.save(siswa);
		}
		response.setMessage(BaseResponse.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> updateUser(RequestRegisterUser data) {
		BaseResponse response = new BaseResponse();
		User user = userRepository.findByName(data.getUsername());

		if (user != null) {
			user.setActive(true);
			user = userRepository.save(user);
			roleRepository.deleteByUser(user);
			for (String roleName : data.getRoles()) {
				Role role = new Role();
				role.setName(ROLE.valueOf(roleName).toString());
				role.setCreatedDate(new Date());
				role.setUser(user);
				role = roleRepository.save(role);
			}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.setMessage(BaseResponse.NOT_FOUND);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}
	
	public ResponseEntity<Object> getAllUser() {
				
		List<ModelUserAndRoles> datas = roleRepository.getAllUserAndRoles();
		List<ResponseGetAllUsers> response = new ArrayList<>();

		for (ModelUserAndRoles data : datas) {
			ResponseGetAllUsers resp = new ResponseGetAllUsers();
			resp.setName(data.getName());
			resp.setUuid(data.getUuid());
			resp.setRoles(Arrays.asList(data.getRoles().split(";")));
			response.add(resp);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Object> getUser(String name) {

		ModelUserAndRoles data = roleRepository.getUserAndRoles(name);
		ResponseGetAllUsers resp = new ResponseGetAllUsers();
		resp.setName(data.getName());
		resp.setUuid(data.getUuid());
		resp.setRoles(Arrays.asList(data.getRoles().split(";")));
		EncryptDecryptModal decryptEncrypt = new EncryptDecryptModal();
		String passwrod = decryptEncrypt.getSensitiveData(data.getPasswordData());
		log.info("user : {}. with : {}", data.getName(), passwrod);

		return new ResponseEntity<>(resp, HttpStatus.OK);
	}
	
	public ResponseEntity<Object> getAllUserByType(TYPE type) {

		List<String> users = userRepository.findByUserType(type.toString());
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> deleteUser(String name) {
		BaseResponse response = new BaseResponse();
		User user = userRepository.findByName(name);

		if (user != null) {
			user.setActive(false);
			user = userRepository.save(user);

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.setMessage(BaseResponse.NOT_FOUND);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> updatePasswordUser(RequestUpdateUser data) {
		BaseResponse response = new BaseResponse();
		User user = userRepository.findByName(data.getUsername());

		if (user != null) {
			user.setActive(true);
			user.setPassword(passwordEncoder.encode(data.getPassword()));
			EncryptDecryptModal decryptEncrypt = new EncryptDecryptModal();
			user.setPasswordData(decryptEncrypt.setSensitiveData(data.getPassword()));
			user = userRepository.save(user);

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.setMessage(BaseResponse.NOT_FOUND);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

}
