package com.agaramtech.qualis.scheduler.service;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign.DynamicPreRegDesignDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;



public class SchedulerConfigurationServiceImpl implements SchedulerConfigurationService {

	private final CommonFunction commonFunction;
	private final DynamicPreRegDesignDAO dynamicPreRegDesignDAO;
	private final SchedulerConfigurationDAO schedulerConfigurationDAO;

	public SchedulerConfigurationServiceImpl(CommonFunction commonFunction,
			DynamicPreRegDesignDAO dynamicPreRegDesignDAO, SchedulerConfigurationDAO schedulerConfigurationDAO) {
		super();
		this.commonFunction = commonFunction;
		this.dynamicPreRegDesignDAO = dynamicPreRegDesignDAO;
		this.schedulerConfigurationDAO = schedulerConfigurationDAO;
	}



	@Override
	public ResponseEntity<Object> getSchedulerConfiguration(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfiguration(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSampleType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationType(int nSampleType, Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getRegistrationType(nSampleType, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubType(int nregTypeCode, Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getRegistrationSubType(nregTypeCode, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(int nregTypeCode, int nregSubTypeCode, Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getApprovalConfigVersion(nregTypeCode, nregSubTypeCode, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(int nregTypeCode, int nregSubTypeCode, int napproveConfigVersionCode, Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getApproveConfigVersionRegTemplate(nregTypeCode, nregSubTypeCode, napproveConfigVersionCode, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigByFilterSubmit(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigByFilterSubmit(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> createSchedulerConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final short ntransactionstatus=schedulerConfigurationDAO.getActiveApprovalConfigId((int)inputMap.get("napproveconfversioncode"));

		if(ntransactionstatus==Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

			return schedulerConfigurationDAO.createSchedulerConfig(inputMap, userInfo);

		}
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDCONFIGVERSION", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigSubSample(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigSubSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigTest(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigTest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigParameter(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigParameter(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getComponentBySpec(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getComponentBySpec(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestfromTestPackage(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestfromTestPackage(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestfromDB(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestfromDB(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestfromSection(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestfromSection(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestBasedTestSection(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestBasedTestSection(inputMap);
	}

	@Override
	public ResponseEntity<Object> createSubSample(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.createSubSample(inputMap);
	}

	@Override
	public ResponseEntity<Object> updateSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.updateSchedulerConfigSubSample(inputMap);
	}

	@Override
	public ResponseEntity<Object> getEditSchedulerSubSampleComboService(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getEditSchedulerSubSampleComboService(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.deleteSchedulerConfigSubSample(inputMap);
	}

	@Override
	public ResponseEntity<Object> getMoreTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getMoreTest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getMoreTestSection(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getMoreTestSection(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getMoreTestPackage(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getMoreTestPackage(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> createTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.createTest(inputMap,userInfo);
	}


	@Override
	public ResponseEntity<Object> deleteSchedulerConfigTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.deleteSchedulerConfigTest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditSchedulerConfigDetails(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		Map<String, Object> map = (Map<String, Object>) schedulerConfigurationDAO.getEditSchedulerConfigDetails(inputMap, userInfo)
				.getBody();
		// map.putAll(inputMap);
		map.putAll((Map<? extends String, ? extends Object>) dynamicPreRegDesignDAO
				.getComboValuesForEdit(map, inputMap, userInfo).getBody());


		return new ResponseEntity<>(map, HttpStatus.OK);
	}


	@Override
	public ResponseEntity<Object> updateSchedulerConfig(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.updateSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> getSiteByUser(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getSiteByUser(inputMap);
	}

	@Override
	public ResponseEntity<Object> approveSchedulerConfig(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.approveSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> getSchedulerMaster(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getSchedulerMaster(inputMap);
	}

	@Override
	public ResponseEntity<Object> deleteSchedulerConfig(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.deleteSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> updateActiveStatusSchedulerConfig(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.updateActiveStatusSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> getSchedulerMasteDetails(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getSchedulerMasteDetails(inputMap);
	}
}
