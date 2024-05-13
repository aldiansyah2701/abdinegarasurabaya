package com.abdinegara.surabaya.service;

import java.security.SecureRandom;
import java.util.*;

import com.abdinegara.surabaya.entity.*;
import com.abdinegara.surabaya.message.*;
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

import com.abdinegara.surabaya.entity.Role.ROLE;
import com.abdinegara.surabaya.kernel.JwtTokenProvider;
import com.abdinegara.surabaya.message.RequestRegisterUser.TYPE;
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

	@Autowired
	private SoalService soalService;
	
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
		user.setAdminEmaill(data.getAdmin() != null ? data.getAdmin().getEmail():null);
		user.setAdminName(data.getAdmin() != null ? data.getAdmin().getName():null);
		user.setAdminPhone(data.getAdmin() != null ? data.getAdmin().getHandphone():null);

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

	public ResponseEntity<Object> getAllAdmin() {

		List<ModelAdminAndRoles> datas = roleRepository.getAllAdminAndRoles();

		return new ResponseEntity<>(datas, HttpStatus.OK);
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
	public ResponseEntity<Object> deleteAdmin(String uuid) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Delete data successfully");
		Optional<User> dataUser = userRepository.findById(uuid);
		if (dataUser.isPresent()) {
			User user = dataUser.get();
			if("SISWA".equals(user.getUserType())){
				Siswa siswa = siswaRepository.findByUserUuid(uuid);
				siswa.setUserUuid(null);

				siswaRepository.save(siswa);
			}
			roleRepository.deleteByUser(user);
			userRepository.deleteById(uuid);



			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		response.setMessage("Data not found");
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

	@Transactional(readOnly = false)
	public ResponseEntity<Object> updateForgotPasswordUser(RequestUpdateForgotPassword data) {
		BaseResponse response = new BaseResponse();
		User user = userRepository.findByName(data.getUsername());

		if (user != null) {
			String validOtp = chekOtp(data.getOtp(), user);
			if(validOtp.equals("success")){
				user.setActive(true);
				user.setPassword(passwordEncoder.encode(data.getPassword()));
				EncryptDecryptModal decryptEncrypt = new EncryptDecryptModal();
				user.setPasswordData(decryptEncrypt.setSensitiveData(data.getPassword()));
				user.setOtpStatus(User.STATUS.USED.toString());
				user = userRepository.save(user);

				response.setMessage(BaseResponse.SUCCESS);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				response.setMessage(validOtp);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}


		}
		response.setMessage(BaseResponse.NOT_FOUND);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

	String chekOtp(String otp, User user){
		Date currentDate = new Date();
		if(currentDate.before(user.getOtpExpired())){
			if(user.getOtpStatus().equals(User.STATUS.NEW.toString())){
				if(user.getOtp().equals(otp)){
					return "success";
				} else {
					return "OTP not match";
				}
			} else {
				return "OTP have been used";
			}
		} else {
			return "OTP expired";
		}
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> reqeustForgotPasswordUser(RequestForgotPassword data) {
		BaseResponse response = new BaseResponse();
		User user = userRepository.findByName(data.getUsername());

		if (user != null) {

			if(user.getUserType().equals("ADMIN")){
				if(user.getAdminEmaill() == null){
					response.setMessage("Email not found, Please contact administrator");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
					Map<String, Object> model = new HashMap<>();
					model.put("fullName",user.getName());
					model.put("body","OTP Forgot password");
					String otp = generateOTP();
					user.setOtp(otp);
					model.put("otpCode",otp);

					soalService.sendEmail("otp", user.getAdminEmaill(),"OTP Forgot password",model, null);

			} else {
				Siswa siswa = siswaRepository.findByUserUuid(user.getUuid());
				if(siswa.getEmail() == null){
					response.setMessage("Email not found, Please contact administrator");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

				Map<String, Object> model = new HashMap<>();
				model.put("fullName",user.getName());
				model.put("body","OTP Forgot password");
				String otp = generateOTP();
				user.setOtp(otp);
				model.put("otpCode",otp);

				soalService.sendEmail("otp", siswa.getEmail(),"OTP Forgot password",model, null);

			}

			user.setOtpStatus(User.STATUS.NEW.toString());
			user.setOtpExpired(datePlusMinute(5));
			userRepository.save(user);
			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.setMessage(BaseResponse.NOT_FOUND);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

	private String generateOTP() {
		SecureRandom secureRandom = new SecureRandom();
		// Generate a random six-digit number
		int min = 100000; // Minimum six-digit number
		int max = 999999; // Maximum six-digit number
		int randomNum = secureRandom.nextInt(max - min + 1) + min;
		return String.format("%06d", randomNum);
	}

	public Date datePlusMinute(int minutesToAdd) {
		Date currentDate = new Date();

		// Create a Calendar instance and set it to the current date
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);

		// Add minutes to the date
		calendar.add(Calendar.MINUTE, minutesToAdd);

		// Get the updated date
		Date updatedDate = calendar.getTime();

		return updatedDate;
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> changePasswordUser(RequestChangePassword data) {
		BaseResponse response = new BaseResponse();
		User user = userRepository.findByName(data.getUsername());

		if (user != null) {
			EncryptDecryptModal decryptEncrypt = new EncryptDecryptModal();
			String passwordData = user.getPasswordData();
			String decryptPass = decryptEncrypt.getSensitiveData(passwordData);

			if(decryptPass.equals(data.getCurrentPassword())){
				user.setActive(true);
				user.setPassword(passwordEncoder.encode(data.getNewPassword()));

				user.setPasswordData(decryptEncrypt.setSensitiveData(data.getNewPassword()));
				user = userRepository.save(user);
			} else {
				response.setMessage("Wrong current password");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.setMessage(BaseResponse.NOT_FOUND);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

}
