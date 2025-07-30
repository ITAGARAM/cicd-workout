package com.agaramtech.qualis.basemaster.service.period;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface PeriodService {

	public ResponseEntity<Object> getPeriodByControl(final Integer ncontrolCode, final UserInfo userInfo) throws Exception;

}
