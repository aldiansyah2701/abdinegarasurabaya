package com.abdinegara.surabaya.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.abdinegara.surabaya.kernel.JwtTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeaderFilter extends OncePerRequestFilter {

	private JwtTokenProvider jwtTokenProvider;

	public HeaderFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = jwtTokenProvider.resolveToken(request);
		try {
			if (token != null && jwtTokenProvider.validateToken(token)) {
				UsernamePasswordAuthenticationToken auth = jwtTokenProvider.getAuthentication(token, request);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (SignatureException ex) {
			logger.error("Invalid JWT Signature");
			log.info("error auth {}", "Invalid JWT Signature");
		} catch (MalformedJwtException ex) {
			logger.error("Invalid JWT token");
			log.info("error auth {}", "Invalid JWT token");
		} catch (UnsupportedJwtException ex) {
			logger.error("Unsupported JWT exception");
			log.info("error auth {}", "Unsupported JWT exception");
		} catch (IllegalArgumentException ex) {
			logger.error("Jwt claims string is empty");
			log.info("error auth {}", "Jwt claims string is empty");

//			SecurityContextHolder.clearContext();
//			response.sendError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
//			return;
		} catch ( ExpiredJwtException ex) {
			request.setAttribute("exception", "JWT expired");
		} catch (Exception ex) {
			// this is very important, since it guarantees the user is not authenticated at all
			SecurityContextHolder.clearContext();
			response.sendError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
			log.info("error auth {}", ex.getMessage());

			return;
		}

		filterChain.doFilter(request, response);

	}

}
