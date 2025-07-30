package com.agaramtech.qualis.testgroup.service.testgroupspecification;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;

public interface TestGroupSpecificationDAO {
	public ResponseEntity<Object> getSpecificationHistory(final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception;

	public ResponseEntity<Object> specReportGenerate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> retireSpec(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
}
