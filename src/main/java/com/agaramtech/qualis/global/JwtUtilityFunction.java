package com.agaramtech.qualis.global;

import java.util.Base64;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtilityFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtilityFunction.class);

	// for production
	 private static final SecretKey secretKey = Jwts.SIG.HS512.key().build();
	
	// for development
//	private static final SecretKey secretKey = new SecretKeySpec(Base64.getDecoder()
//			.decode("7bAtN8BLcPbAd4pqvgwrx0ffIekTe97i1tGiyTbsYYmPCb7QPotOp+O+IYC4WQjUAj7P6vepekrISXDBQZXcrg=="),0,
//			Base64.getDecoder().decode("7bAtN8BLcPbAd4pqvgwrx0ffIekTe97i1tGiyTbsYYmPCb7QPotOp+O+IYC4WQjUAj7P6vepekrISXDBQZXcrg==").length,
//		    "HmacSHA256");

	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;

	public JwtUtilityFunction(JdbcTemplate jdbcTemplate, JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction) {
		super();
		this.jdbcTemplate = jdbcTemplate;
		this.jdbcTemplateUtilityFunction = jdbcTemplateUtilityFunction;
	}

	public boolean isTokenExpired(final String token) {
		try {
			Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
			return false;
		} catch (ExpiredJwtException e) {
			String ssessionid = e.getClaims().get("ssessionid", String.class);
			if (ssessionid != null) {
				try {
					ssessionid = (String) jdbcTemplateUtilityFunction.queryForObject(
							"select ssessionid from sessiondetails where ssessionid='" + ssessionid + "' LIMIT 1",
							String.class, jdbcTemplate);
				} catch (Exception e1) {
					LOGGER.error("JWT parsing error: ", e1.getMessage());
				}
			}
			return ssessionid != null ? true : false;
		} catch (Exception e) {
			LOGGER.error("JWT parsing error: ", e.getMessage());
			return false;
		}
	}

	public String generateToken(final String ssessionid, final int expiry) {
		if(expiry > 0) {
			return Jwts.builder().subject("userInfo").claim("ssessionid", ssessionid).claim("expiry", expiry)
					.issuedAt(new Date(System.currentTimeMillis()))
					.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * expiry)).signWith(secretKey).compact();			
		} else {
			return Jwts.builder().subject("userInfo").claim("ssessionid", ssessionid).claim("expiry", expiry)
					.issuedAt(new Date(System.currentTimeMillis())).signWith(secretKey).compact();	
		}
	}
	
	public String generateRefreshToken(final String token) {
		try {
			Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
			return null;
		} catch (ExpiredJwtException e) {
			final String ssessionid = e.getClaims().get("ssessionid", String.class);
			final int expiry = (int) e.getClaims().get("expiry", Integer.class);
			return generateToken(ssessionid, expiry);
		} catch (Exception e) {
			LOGGER.error("JWT parsing error: ", e.getMessage());
			return null;
		}
	}

	public boolean isTokenValid(final String token) {
		try {
			Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String extractSessionId(final String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("ssessionid",
					String.class);
		} catch (ExpiredJwtException e) {
			return e.getClaims().get("ssessionid", String.class);
		} catch (Exception e) {
			LOGGER.error("Failed to extract ssessionid: {}", e.toString());
			return null;
		}
	}
	
	public int extractExpiryTime(final String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("expiry",
					Integer.class);
		} catch (ExpiredJwtException e) {
			return e.getClaims().get("expiry", Integer.class);
		} catch (Exception e) {
			LOGGER.error("Failed to extract expiry: {}", e.toString());
			return 0;
		}
	}

}
