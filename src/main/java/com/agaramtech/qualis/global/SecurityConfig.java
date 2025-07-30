package com.agaramtech.qualis.global;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Class created by gowtham on 18 July, ALPDJ21-27 - JWT
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtFilterConfiguration jwtFilter;
	private final JdbcTemplate jdbcTemplate;

	public SecurityConfig(JwtFilterConfiguration jwtFilter, JdbcTemplate jdbcTemplate) {
		this.jwtFilter = jwtFilter;
		this.jdbcTemplate = jdbcTemplate;
	}

	private List<ApiEndPoint> getApiEndPoints() {
		return jdbcTemplate.query("select sapiendpoint from apiendpoint where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), new ApiEndPoint());
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		List<ApiEndPoint> dynamicUrls = getApiEndPoints();
		return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> {
			auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
			for (ApiEndPoint apiendpoint : dynamicUrls) {
				auth.requestMatchers(apiendpoint.getSapiendpoint()).permitAll();
			}
			auth.anyRequest().authenticated();
		}).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
