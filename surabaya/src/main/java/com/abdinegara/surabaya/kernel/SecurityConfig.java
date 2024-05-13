package com.abdinegara.surabaya.kernel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	protected void configure(final HttpSecurity http) throws Exception {
//		http.csrf().disable().authorizeHttpRequests().antMatchers("**").permitAll();
		// Disable CSRF (cross site request forgery)
		http.csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler);
		http.cors();
		// No session will be created or used by spring security
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// Entry points
		http.authorizeRequests()//
				.antMatchers("/api/v1/user/login").permitAll()//
				.antMatchers("/api/v1/user/register").permitAll()
				.antMatchers("/h2-console/**/**").permitAll()
				.antMatchers("/api-docs").permitAll()
				.antMatchers("/v2/api-docs").permitAll()
				.antMatchers("/swagger-ui.html").permitAll()
				.antMatchers("/swagger-resources/**").permitAll()
				.antMatchers("/configuration/**").permitAll()
				.antMatchers("/webjars/**").permitAll()
				.antMatchers("/api/v1/user/forgot-password").permitAll()
				.antMatchers("/api/v1/user/forgot-change-password").permitAll()
				// Disallow everything else..
				.antMatchers("/api/v1/**").authenticated();


		// Apply JWT
		http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider));

		// Optional, if you want to test the API from a browser
		http.httpBasic();

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}
