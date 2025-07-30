package com.agaramtech.qualis.registration.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;

public interface RegistrationService {

	public ResponseEntity<Object> getRegistration(UserInfo userInfo, String currentUIDate) throws Exception;

	public ResponseEntity<Object> getTreeByProduct(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestGroupSpecification(int ntreetemplatemanipulationcode, int npreregno, int ntestGroupSpecRequired)
			throws Exception;
	
	public ResponseEntity<Object> getComponentBySpec(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getTestfromDB(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromTestPackage(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getRegTypeBySampleType(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getRegSubTypeByRegType(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getRegistrationByFilterSubmit(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> getRegistrationSubSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getRegistrationTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationParameter(String ntransactionTestCode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getMoreTestPackage(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMoreTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;


	public ResponseEntity<Object> insertRegistration(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> insertRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception;

	public ResponseEntity<Object> acceptRegistration(Map<String, Object> inputMap) throws Exception;
	
	
	////////////////////////////////////
	
	
	public ResponseEntity<Object> getRegistrationTemplate(int nregtypecode, int nregsubtypecode) throws Exception;

	
	
	public ResponseEntity<Object> getTestBasesdTestPackage(Map<String, Object> inputMap) throws Exception;

	
	
	public ResponseEntity<Object> createTest(UserInfo userInfo, List<String> listSample, List<TestGroupTest> listTest,
			int nregtypecode, int nregsubtypecode, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getEditRegistrationDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateRegistration(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception;

	public ResponseEntity<Object> cancelTest(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> cancelSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createSubSample(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> createSubSampleWithFile(MultipartHttpServletRequest objmap) throws Exception;

	public ResponseEntity<Object> getEditRegistrationSubSampleDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateRegistrationSubSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateRegistrationSubSampleWithFile(MultipartHttpServletRequest inputMap)
			throws Exception;

	public ResponseEntity<Object> cancelSubSample(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> quarantineRegistration(Map<String, Object> objmap) throws Exception;

	
	public List<TransactionStatus> getFilterStatus(int nregtypecode, int nregsubtypecode, UserInfo userinfo)
			throws Exception;

	public ResponseEntity<Object> schedulerinsertRegistration(Map<String, Object> inputMap) throws Exception;

	
	public ResponseEntity<Object> getSampleBasedOnExternalOrder(Map<String, Object> inputMap, UserInfo userInfo);

	public Map<String, Object> viewRegistrationFile(final Registration objFile, final UserInfo objUserInfo,
			Map<String, Object> inputMap) throws Exception;

	public Map<String, Object> viewRegistrationSubSampleFile(final RegistrationSample objFile,
			final UserInfo objUserInfo, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> importRegistrationData(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getExternalOrderForMapping(Map<String, Object> inputMap, UserInfo userInfo);

	public ResponseEntity<Object> orderMapping(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> importRegistrationSample(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getExternalOrderAttachment(String nexternalordercode, String npreregno,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getOutsourceDetails(String npreregno, String ntransactionsamplecode,
			UserInfo userInfo) throws Exception;

	public Map<String, Object> viewExternalOrderAttachment(final Map<String, Object> objExternalOrderAttachmentFile,
			int ncontrolCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestfromSection(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestSectionBasesdTestPackage(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestBasedTestSection(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getMoreTestSection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getAdhocTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createAdhocTest(UserInfo userInfo, List<String> listSample, List<TestGroupTest> listTest,
			int nregtypecode, int nregsubtypecode, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getSampleBasedOnPortalOrder(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> copySample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	//Added by Dhanushya RI for JIRA ID:ALPD-4912,ALPD-4913,ALPD-4914  Filter save detail --Start

	public ResponseEntity<Object> createFavoriteFilterName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public List<FilterName> getFavoriteFilterName(UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getRegistrationFilter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

//End
   //Added by Dhanushya RI for JIRA ID:ALPD-5511 edit test method insert --Start
   /**
	 * This interface declaration is used to get the over all methods with respect to test
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of method with respect to test and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTestMethod(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to update entry in method table.
	 * @param inputMap [Map] object holding details to be updated in registrationtest table
	 * @return response entity object holding response status and data of updated method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateTestMethod(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	//End

}
