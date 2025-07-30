package com.agaramtech.qualis.joballocation.service.TestWiseMyJobs;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.agaramtech.qualis.global.UserInfo;


@Service
public class TestWiseMyJobsServiceImpl implements TestWiseMyJobsService {

	private final TestWiseMyJobsDAO testwisemyjobsDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 *
	 * @param TestWiseMyJobsDAO testwisemyjobsDAO
	 */
	public TestWiseMyJobsServiceImpl(TestWiseMyJobsDAO testwisemyjobsDAO) {
		super();
		this.testwisemyjobsDAO = testwisemyjobsDAO;
	}

	@Override
	public ResponseEntity<Object> getTestWiseMyJobs(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getTestWiseMyJobs(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getSampleType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getRegistrationType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTypeBySampleType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getRegistrationTypeBySampleType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getRegistrationsubType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationsubTypeByRegType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getRegistrationsubTypeByRegType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getFilterStatus(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFilterStatusByApproveVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getFilterStatusByApproveVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSection(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return testwisemyjobsDAO.getSection(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSectionByApproveVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getSectionByApproveVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getApprovalConfigVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return testwisemyjobsDAO.getApprovalConfigVersionByRegSubType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestCombo(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return testwisemyjobsDAO.getTestCombo(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestComboBySection(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getTestComboBySection(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return testwisemyjobsDAO.getDesignTemplateByApprovalConfigVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getMyTestWiseJobsDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.getMyTestWiseJobsDetails(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> CreateAcceptTest(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testwisemyjobsDAO.CreateAcceptTest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		return testwisemyjobsDAO.createFilterName(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getmyjobsFilterDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return testwisemyjobsDAO.getmyjobsFilterDetails(inputMap, userInfo);
	}

}
