package com.agaramtech.qualis.testmanagement.service.testprice;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPriceVersion;

public interface TestPriceVersionDAO {

	public ResponseEntity<Object> getTestPriceVersion(final Integer npriceVersionCode, final UserInfo userInfo)
			throws Exception;

	public TestPriceVersion getActiveTestPriceVersionById(final int npriceVersionCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> copyTestPriceVersion(final TestPriceVersion testPriceVersion, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> approveTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception;

}