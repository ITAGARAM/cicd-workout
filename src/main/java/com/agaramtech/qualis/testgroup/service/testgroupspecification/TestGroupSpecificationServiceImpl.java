package com.agaramtech.qualis.testgroup.service.testgroupspecification;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class TestGroupSpecificationServiceImpl implements TestGroupSpecificationService {

	private TestGroupSpecificationDAO testGroupSpecificationDAO;

	public TestGroupSpecificationServiceImpl(TestGroupSpecificationDAO testGroupSpecificationDAO) {
		this.testGroupSpecificationDAO = testGroupSpecificationDAO;
	}

	@Override
	public ResponseEntity<Object> getSpecificationHistory(final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception {
		return testGroupSpecificationDAO.getSpecificationHistory(userInfo, objSpecification);
	}

	public ResponseEntity<Object> specReportGenerate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupSpecificationDAO.specReportGenerate(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> retireSpec(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return testGroupSpecificationDAO.retireSpec(inputMap, userInfo);
	}

}
