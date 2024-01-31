package com.abdinegara.surabaya.message;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseResponse {
	
	public static final String SUCCESS = "Success";
	public static final String FAILED = "Failed";
	public static final String ALREADY_EXIST = "Already Exist";
	public static final String NOT_FOUND = "Not Found";
	
	private String message;
	private Object data;

}
