package com.abdinegara.surabaya.kernel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.abdinegara.surabaya.message.BaseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	// exception handle for authentication
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		System.out.println(authException.getMessage());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		BaseResponse baseResponse = new BaseResponse();
		baseResponse.setMessage(authException.getMessage());

		String jsonResponse = convertObjectToJson(baseResponse);
		log.info("Unauthorized {}", jsonResponse);
		response.getWriter().write(jsonResponse);
	}

	@ExceptionHandler(value = { AccessDeniedException.class })
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AccessDeniedException accessDeniedException) throws IOException {
		httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
		BaseResponse baseResponse = new BaseResponse();
		baseResponse.setMessage(accessDeniedException.getMessage());
		String jsonResponse = convertObjectToJson(baseResponse);
		log.info("Forbidden {}", jsonResponse);
		httpServletResponse.getWriter().write(jsonResponse);
	}

	public String convertObjectToJson(Object object) throws JsonProcessingException {
		if (object == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}

}
