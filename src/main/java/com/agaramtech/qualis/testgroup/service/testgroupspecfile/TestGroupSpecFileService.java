package com.agaramtech.qualis.testgroup.service.testgroupspecfile;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecFile;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;

public interface TestGroupSpecFileService {

	public ResponseEntity<Object> getSpecificationFile(final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception;

	public ResponseEntity<Object> createSpecificationFile(final UserInfo userInfo,
			final MultipartHttpServletRequest request) throws Exception;

	public ResponseEntity<Object> updateSpecificationFile(final UserInfo userInfo,
			final MultipartHttpServletRequest request) throws Exception;

	public ResponseEntity<Object> deleteSpecificationFile(final UserInfo userInfo,
			final TestGroupSpecFile objTestGroupSpecFile, final TestGroupSpecification objSpecification,
			final int ntreeversiontempcode) throws Exception;

	public ResponseEntity<Object> getActiveSpecFileById(final UserInfo userInfo, final TestGroupSpecFile objSpecFile,
			final int ntreeversiontempcode) throws Exception;

	public ResponseEntity<Object> viewTestGroupSpecFile(final TestGroupSpecFile objSpecFile, final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception;

}
