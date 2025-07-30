package com.agaramtech.qualis.basemaster.service.timezone;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TimeZone;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;


/**
 * This class is used to perform CRUD Operation on "timezone" table by 
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class TimeZoneDAOImpl implements TimeZoneDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeZoneDAOImpl.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;

	
	@Override
	public ResponseEntity<Object> getTimeZone() throws Exception
	{
		final String strQuery	= "select * from timezone where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new TimeZone()), HttpStatus.OK);
		
	}

	
	@Override
	public ResponseEntity<Object> getLocalTimeByZone(UserInfo userInfo) throws Exception {
		String date = "";
		if (userInfo.getIsutcenabled()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			date = dateUtilityFunction.convertUTCToSiteTime(Instant.now(), userInfo);
		}
		else {
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());  
			final LocalDateTime now = LocalDateTime.now(); 
			date = dtf.format(now); 
		      
		}
		LOGGER.info("date:"+date);
		return new ResponseEntity<Object>(date,HttpStatus.OK);
	}
}