package com.agaramtech.qualis.basemaster.service.timezone;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface TimeZoneDAO {

		public ResponseEntity<Object> getTimeZone() throws Exception;

		public ResponseEntity<Object> getLocalTimeByZone(UserInfo userInfo)throws Exception; 
}
