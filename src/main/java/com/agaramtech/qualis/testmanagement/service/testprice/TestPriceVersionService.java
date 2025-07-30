package com.agaramtech.qualis.testmanagement.service.testprice;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPriceVersion;

public interface TestPriceVersionService {

	public ResponseEntity<Object> getTestPriceVersion(Integer npriceVersionCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveTestPriceVersionById(final int npriceVersionCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTestPriceVersion(TestPriceVersion testPriceVersion, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateTestPriceVersion(TestPriceVersion testPriceVersion, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteTestPriceVersion(TestPriceVersion testPriceVersion, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> copyTestPriceVersion(final TestPriceVersion testPriceVersion, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> approveTestPriceVersion(TestPriceVersion testPriceVersion, UserInfo userInfo)
			throws Exception;

}
