package com.abdinegara.surabaya.message;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestRegisterUser {

	private String username;
	
	private String password;
	
	private List<String> roles;
	
	private TYPE type;
	
	private RequestRegisterSiswa siswa;

	private RequestRegisterAdmin admin;
	
	
	public enum TYPE{
		ADMIN, SISWA
	}
	
}
