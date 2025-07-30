package com.agaramtech.qualis.testmanagement.service.grade;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;


public interface GradeService {

	public ResponseEntity<Object> getGrade(final UserInfo objUserInfo) throws Exception;
}
