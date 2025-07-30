package com.agaramtech.qualis.basemaster.service.period;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface PeriodDAO {

	public ResponseEntity<Object> getPeriodByControl(Integer ncontrolCode, final UserInfo userInfo) throws Exception;

}
