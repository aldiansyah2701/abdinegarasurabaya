package com.abdinegara.surabaya.kernel;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.abdinegara.surabaya.filter.HeaderFilter;


public class JwtTokenFilterConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	private JwtTokenProvider jwtTokenProvider;

	  public JwtTokenFilterConfigurer(JwtTokenProvider jwtTokenProvider) {
	    this.jwtTokenProvider = jwtTokenProvider;
	  }
	  
	  @Override
	  public void configure(HttpSecurity http) throws Exception {
		  HeaderFilter customFilter = new HeaderFilter(jwtTokenProvider);
		//untuk add filter token sebelum call controller
	    http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
	  }
}
