package com.contactManager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class MyConfig {
	
	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
//	An {@link AuthenticationProvider} implementation that retrieves user details from a
//	 * {@link UserDetailsService}.
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}
	
//	configure method.... (Important)
	
	protected void configure (AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}
	
	@Bean
	protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(request -> request
										   .requestMatchers("/admin/**")
										   .hasRole("ADMIN"));
		http.authorizeHttpRequests(request -> request
											.requestMatchers("/user/**")
											.hasAnyRole("USER","ADMIN"));
		
		http.authorizeHttpRequests(request -> request
											.requestMatchers("/**")
											.permitAll());
		
		http.formLogin(f -> f.loginPage("/login")
							.loginProcessingUrl("/himani")
							.defaultSuccessUrl("/user/index"));
		
		
		
		http.csrf(csrf -> csrf.disable());
		
		return http.build();
	}
}

//   several methods that we can use to configure the behaviour of the form login

//loginPage() - to give url of custom login page

//loginProcessingUrl() - the url to submit the username and password to

//defaultSuccessUrl() - the landing page after a successful login.

//failureUrl() - the landing page after an unsuccessful login.