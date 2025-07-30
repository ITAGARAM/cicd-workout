package com.agaramtech.qualis.testgroup.service.testgroupspecfile;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecFile;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class TestGroupSpecFileServiceImpl implements TestGroupSpecFileService {

	private TestGroupSpecFileDAO testGroupSpecFileDAO;

	public TestGroupSpecFileServiceImpl(TestGroupSpecFileDAO testGroupSpecFileDAO) {
		this.testGroupSpecFileDAO = testGroupSpecFileDAO;
	}

	@Override
	public ResponseEntity<Object> getSpecificationFile(UserInfo userInfo, TestGroupSpecification objSpecification)
			throws Exception {
		return testGroupSpecFileDAO.getSpecificationFile(userInfo, objSpecification);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSpecificationFile(final UserInfo userInfo,
			final MultipartHttpServletRequest request) throws Exception {
		return testGroupSpecFileDAO.createSpecificationFile(userInfo, request);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSpecificationFile(final UserInfo userInfo,
			final MultipartHttpServletRequest request) throws Exception {
		return testGroupSpecFileDAO.updateSpecificationFile(userInfo, request);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSpecificationFile(final UserInfo userInfo,
			final TestGroupSpecFile objTestGroupSpecFile, final TestGroupSpecification objSpecification,
			final int ntreeversiontempcode) throws Exception {
		return testGroupSpecFileDAO.deleteSpecificationFile(userInfo, objTestGroupSpecFile, objSpecification,
				ntreeversiontempcode);
	}

	@Override
	public ResponseEntity<Object> getActiveSpecFileById(final UserInfo userInfo, final TestGroupSpecFile objSpecFile,
			final int ntreeversiontempcode) throws Exception {
		return testGroupSpecFileDAO.getActiveSpecFileById(userInfo, objSpecFile, ntreeversiontempcode);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> viewTestGroupSpecFile(TestGroupSpecFile objSpecFile, UserInfo userInfo,
			TestGroupSpecification objSpecification) throws Exception {
		return testGroupSpecFileDAO.viewTestGroupSpecFile(objSpecFile, userInfo, objSpecification);
	}

}
