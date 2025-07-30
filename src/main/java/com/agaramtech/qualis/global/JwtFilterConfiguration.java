package com.agaramtech.qualis.global;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Class created by gowtham on 18 July, ALPDJ21-27 - JWT
@Component
public class JwtFilterConfiguration extends OncePerRequestFilter {

	private final JwtUtilityFunction jwtUtilityFunction;

	public JwtFilterConfiguration(JwtUtilityFunction jwtUtilityFunction) {
		super();
		this.jwtUtilityFunction = jwtUtilityFunction;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer")) {
			token = token.substring(7);
			if(!token.equals("undefined")) {
				boolean flag = jwtUtilityFunction.isTokenExpired(token);
				final String sessionId = jwtUtilityFunction.extractSessionId(token);
				if ((sessionId != null && SecurityContextHolder.getContext().getAuthentication() == null) || flag) {
					if (jwtUtilityFunction.isTokenValid(token)) {
						UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(sessionId,
								null, null);
						authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authtoken);
					}
				}				
			}
			filterChain.doFilter(request, response);
		}
	}
}
