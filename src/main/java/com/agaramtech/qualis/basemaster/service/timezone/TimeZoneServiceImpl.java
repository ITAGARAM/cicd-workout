package com.agaramtech.qualis.basemaster.service.timezone;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class TimeZoneServiceImpl  implements TimeZoneService
{
	private final TimeZoneDAO timeZoneDAO;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param timeZoneDAO TimeZoneServiceImpl Interface
	 */
	public TimeZoneServiceImpl(TimeZoneDAO timeZoneDAO) {
		this.timeZoneDAO = timeZoneDAO;
		
	}
	
	@Override
	public ResponseEntity<Object> getTimeZone() throws Exception{
		return timeZoneDAO.getTimeZone();
	}
	@Override
	public ResponseEntity<Object> getLocalTimeByZone(UserInfo userInfo) throws Exception {
		return timeZoneDAO.getLocalTimeByZone(userInfo);
	}
}
