package com.abdinegara.surabaya.message;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseGetAllUsers {

	private String name;
	
	private List<String> roles;
	
	private String uuid;
}
