package com.abdinegara.surabaya.message;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseCreateToken extends BaseResponse {
	
	private String username;
	private String uuid;
	private List<String> roles;
	private String jwtToken;

}
