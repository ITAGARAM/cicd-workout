package com.agaramtech.qualis.testmanagement.service.testprice;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPriceDetail;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class TestPriceServiceImpl implements TestPriceService {

	private final TestPriceDAO testPriceDAO;

	@Override
	public ResponseEntity<Object> getTestPrice(final int npriceVersionCode, final Integer ntestPriceCode,
			final UserInfo userInfo) throws Exception {
		return testPriceDAO.getTestPrice(npriceVersionCode, ntestPriceCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getPriceUnmappedTest(final int npriceVersionCode, final UserInfo userInfo)
			throws Exception {
		return testPriceDAO.getPriceUnmappedTest(npriceVersionCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTestPrice(final List<TestPriceDetail> testPriceList,
			final int npriceVersionCode, final UserInfo userInfo) throws Exception {
		return testPriceDAO.createTestPrice(testPriceList, npriceVersionCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTestPrice(final List<TestPriceDetail> testPriceList,
			final int npriceVersionCode, final UserInfo userInfo) throws Exception {
		return testPriceDAO.updateTestPrice(testPriceList, npriceVersionCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestPrice(final TestPriceDetail testPrice, final int npriceVersionCode,
			final UserInfo userInfo) throws Exception {
		return testPriceDAO.deleteTestPrice(testPrice, npriceVersionCode, userInfo);
	}

}
