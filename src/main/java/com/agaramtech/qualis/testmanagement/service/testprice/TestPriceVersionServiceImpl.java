package com.agaramtech.qualis.testmanagement.service.testprice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPriceVersion;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class TestPriceVersionServiceImpl implements TestPriceVersionService {

	private final TestPriceVersionDAO testPriceVersionDAO;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getTestPriceVersion(final Integer npriceVersionCode, final UserInfo userInfo)
			throws Exception {
		return testPriceVersionDAO.getTestPriceVersion(npriceVersionCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveTestPriceVersionById(final int npriceVersionCode, final UserInfo userInfo)
			throws Exception {
		final TestPriceVersion testPriceVersion = (TestPriceVersion) testPriceVersionDAO
				.getActiveTestPriceVersionById(npriceVersionCode, userInfo);
		if (testPriceVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(testPriceVersion, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		return testPriceVersionDAO.createTestPriceVersion(testPriceVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		return testPriceVersionDAO.updateTestPriceVersion(testPriceVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		return testPriceVersionDAO.deleteTestPriceVersion(testPriceVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> copyTestPriceVersion(final TestPriceVersion testPriceVersion, final UserInfo userInfo)
			throws Exception {
		return testPriceVersionDAO.copyTestPriceVersion(testPriceVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		return testPriceVersionDAO.approveTestPriceVersion(testPriceVersion, userInfo);
	}

}
