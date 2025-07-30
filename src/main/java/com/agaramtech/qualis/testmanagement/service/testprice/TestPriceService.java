package com.agaramtech.qualis.testmanagement.service.testprice;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPriceDetail;

public interface TestPriceService {

	public ResponseEntity<Object> getTestPrice(final int npriceVersionCode, final Integer ntestPriceCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getPriceUnmappedTest(final int npriceVersionCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTestPrice(final List<TestPriceDetail> testPriceList,
			final int npriceVersionCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateTestPrice(final List<TestPriceDetail> testPriceList,
			final int npriceVersionCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteTestPrice(final TestPriceDetail testPrice, final int npriceVersionCode,
			final UserInfo userInfo) throws Exception;
}
