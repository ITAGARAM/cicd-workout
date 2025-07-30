package com.agaramtech.qualis.registration.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign.DynamicPreRegDesignDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RegistrationServiceImpl implements RegistrationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationServiceImpl.class);

	private final RegistrationDAO registrationDAO;
	private final CommonFunction commonFunction;
	private final DynamicPreRegDesignDAO dynamicPreRegDesignDAO;
	

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param registrationDAO RegistrationDAO Interface
	 */
	public RegistrationServiceImpl(RegistrationDAO registrationDAO, DynamicPreRegDesignDAO dynamicPreRegDesignDAO,
			CommonFunction commonFunction) {
		this.registrationDAO = registrationDAO;
		this.commonFunction = commonFunction;
		this.dynamicPreRegDesignDAO = dynamicPreRegDesignDAO;
	}
	
	
	@Override
	public ResponseEntity<Object> getRegistration(UserInfo userInfo, String currentUIDate) throws Exception {
		return registrationDAO.getRegistration(userInfo, currentUIDate);
	}
	
	public ResponseEntity<Object> getTreeByProduct(Map<String, Object> inputMap) throws Exception {
	
		//ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the approval config retired
		final ObjectMapper objMapper = new ObjectMapper();		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {});
		
		if(inputMap.containsKey("napproveconfversioncode")) 
		{
			final short ntransactionstatus = registrationDAO.getActiveApprovalConfigId((int)inputMap.get("napproveconfversioncode"), userInfo);
			
			if(ntransactionstatus == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
	
				return registrationDAO.getTreeByProduct(inputMap);			
			}
			else {			
				
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDCONFIGVERSION", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}		
		}
		else {
			return registrationDAO.getTreeByProduct(inputMap);
		}
	
	}
	
	@Override
	public ResponseEntity<Object> getTestGroupSpecification(int ntreetemplatemanipulationcode, int npreregno, int ntestGroupSpecRequired)
			throws Exception {
		return new ResponseEntity<>(registrationDAO.getTestGroupSpecification(ntreetemplatemanipulationcode, npreregno, ntestGroupSpecRequired),
				HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getComponentBySpec(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getComponentBySpec(inputMap);
	}
	
	@Override
	public ResponseEntity<Object> getTestfromDB(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getTestfromDB(inputMap);
	}
	
	@Override
	public ResponseEntity<Object> getTestfromTestPackage(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getTestfromTestPackage(inputMap);
	}
	
	@Override
	public ResponseEntity<Object> getRegTypeBySampleType(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getRegTypeBySampleType(inputMap);
	}
	
	@Override
	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getRegTemplateTypeByRegSubType(inputMap);
	}
	
	@Override
	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return registrationDAO.getApprovalConfigBasedTemplateDesign(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getRegSubTypeByRegType(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getRegSubTypeByRegType(inputMap);
	}

	@Override
	public ResponseEntity<Object> getRegistrationByFilterSubmit(Map<String, Object> objmap) throws Exception {
		return registrationDAO.getRegistrationByFilterSubmit(objmap);
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.getRegistrationSubSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.getRegistrationTest(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationParameter(String ntransactionTestCode, UserInfo userInfo)
			throws Exception {
		return registrationDAO.getRegistrationParameter(ntransactionTestCode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getMoreTestPackage(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return registrationDAO.getMoreTestPackage(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getMoreTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return registrationDAO.getMoreTest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> insertRegistration(Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();			
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		
		//ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the approval config retired
		//start
		final short ntransactionstatus = registrationDAO.getActiveApprovalConfigId((int)inputMap.get("napproveconfversioncode"),userInfo);
				
		if(ntransactionstatus==Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
		{		
			return registrationDAO.preRegisterSample(inputMap);
		
		}
		else {
			
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDCONFIGVERSION", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	//end
	}

	@Override
	public ResponseEntity<Object> insertRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		
		//ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the approval config retired
	//start
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {});
		final Object regData = objMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {});	
		
	    final	Map<String, Object> regMap = (Map<String, Object>)regData;
		
		final short ntransactionstatus = registrationDAO.getActiveApprovalConfigId((int)regMap.get("napproveconfversioncode"), userInfo);
		
		if(ntransactionstatus == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {		
			return registrationDAO.preRegisterSampleWithFile(inputMap);		
		}
		else {		
								
			return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDCONFIGVERSION", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
		}
	//end
	}
	
	@Override
	public ResponseEntity<Object> acceptRegistration(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		//LOGGER.info("Date1:"+ new Date());
		
		Map<String, Object> objmap = registrationDAO.insertSeqNoRegistration(inputMap);
		
		//LOGGER.info("Date2:"+ new Date());
		LOGGER.info("AcceptRegistration:" + objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
		
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(registrationDAO.acceptRegistration(inputMap), HttpStatus.OK);
		} else {

			if (objmap.containsKey("NeedConfirmAlert") && (Boolean) objmap.get("NeedConfirmAlert") == true) {
				// inputMap.putAll(objmap);
				return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
			} else {
				objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
				return new ResponseEntity<>(objmap1, HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	@Override
	public ResponseEntity<Object> getRegistrationTemplate(int nregtypecode, int nregsubtypecode) throws Exception {
		// TODO Auto-generated method stub
		return registrationDAO.getRegistrationTemplate(nregtypecode, nregsubtypecode);
	}
	

//	@Override
//	public ResponseEntity<Object> getTestfromDB(Map<String, Object> inputMap) throws Exception {
//		Map<String, Object> objmap =  (Map<String, Object>) registrationDAO.getTestfromTestPackage(inputMap).getBody();
//		if ((objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())
//				.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()))) {
//			return new ResponseEntity<>(registrationDAO.getTestfromDB(inputMap), HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
//		}
//	}
//	
	@Override
	public ResponseEntity<Object> getTestBasesdTestPackage(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getTestBasesdTestPackage(inputMap);
	}		

	@Override
	public ResponseEntity<Object> createTest(UserInfo userInfo, List<String> listSample, List<TestGroupTest> listTest,
			int nregtypecode, int nregsubtypecode, Map<String, Object> inputMap) throws Exception {

		Map<String, Object> objmap = registrationDAO.getCreateTestSequenceNo(userInfo, listSample, listTest,
				nregtypecode, nregsubtypecode, inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			return registrationDAO.createTest(userInfo, listSample, listTest, nregtypecode, nregsubtypecode, inputMap);
		} else {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getEditRegistrationDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		Map<String, Object> map = (Map<String, Object>) registrationDAO.getEditRegistrationDetails(inputMap, userInfo)
				.getBody();
		// map.putAll(inputMap);
		map.putAll((Map<? extends String, ? extends Object>) dynamicPreRegDesignDAO
				.getComboValuesForEdit(map, inputMap, userInfo).getBody());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateRegistration(final Map<String, Object> inputMap) throws Exception {
		return registrationDAO.updateRegistration(inputMap);
	}

	@Override
	public ResponseEntity<Object> updateRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		return registrationDAO.updateRegistrationWithFile(inputMap);
	}

	public ResponseEntity<Object> cancelTest(Map<String, Object> inputMap) throws Exception {
		// try {
		final Map<String, Object> objMap = registrationDAO.seqNoTestSampleInsert(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objMap);
			final Map<String, Object> objmap1 = (Map<String, Object>) registrationDAO.cancelTestSamples(inputMap);
			
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(objmap1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				return new ResponseEntity<>(objmap1, HttpStatus.OK);
			} else {
				inputMap.putAll(objmap1);
				return new ResponseEntity<>(objmap1, HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			inputMap.putAll(objMap);
			return new ResponseEntity<>(objMap, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> cancelSample(Map<String, Object> inputMap) throws Exception {
		
		final Map<String, Object> objmap = registrationDAO.seqNoSampleInsert(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(registrationDAO.rejectSample(inputMap), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createSubSample(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = registrationDAO.validateSeqnoSubSampleNo(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			objmap1 = registrationDAO.createSubSample(inputMap);
			return new ResponseEntity<>(objmap1, HttpStatus.OK);

		} else {
			if (objmap.containsKey("NeedConfirmAlert") && (Boolean) objmap.get("NeedConfirmAlert") == true) {
				return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
			} else {
				objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
				return new ResponseEntity<>(objmap1, HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> createSubSampleWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		//return registrationDAO.createSubSampleWithFile(inputMap);
		
		final ObjectMapper objectMapper = new ObjectMapper();
		
		final UserInfo userInfo = objectMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {});
		final Object reg = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {});
		final String uploadStatus = registrationDAO.createSubSampleWithFile(inputMap);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
			return createSubSample((Map<String, Object>) reg);
		} 
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getEditRegistrationSubSampleDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> map = (Map<String, Object>) registrationDAO
				.getEditRegistrationSubSampleDetails(inputMap, userInfo).getBody();
		map.putAll((Map<? extends String, ? extends Object>) dynamicPreRegDesignDAO
				.getComboValuesForEdit(map, inputMap, userInfo).getBody());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateRegistrationSubSample(final Map<String, Object> inputMap) throws Exception {
		return registrationDAO.updateRegistrationSubSample(inputMap);
	}

	@Override
	public ResponseEntity<Object> updateRegistrationSubSampleWithFile(MultipartHttpServletRequest inputMap)
			throws Exception {
		return registrationDAO.updateRegistrationSubSampleWithFile(inputMap);
	}

	@Override
	public ResponseEntity<Object> cancelSubSample(Map<String, Object> inputMap) throws Exception {
		
		final Map<String, Object> objmap = registrationDAO.seqNoSubSampleInsert(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(registrationDAO.cancelSubSample(inputMap), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> quarantineRegistration(Map<String, Object> inputMap) throws Exception {
		// try {
		final Map<String, Object> objMap = registrationDAO.seqNoQuarentineSampleInsert(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objMap);
			return new ResponseEntity<>(registrationDAO.quarantineSamples(inputMap), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(objMap, HttpStatus.EXPECTATION_FAILED);
		}

	}
	

	public List<TransactionStatus> getFilterStatus(int nregtypecode, int nregsubtypecode, UserInfo userinfo)
			throws Exception {
		return registrationDAO.getFilterStatus(nregtypecode, nregsubtypecode, userinfo);
	}

	@Override
	public ResponseEntity<Object> schedulerinsertRegistration(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = registrationDAO.validateAndInsertSeqNoRegistrationData(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			objmap1 = registrationDAO.schedulerInsertRegistration(inputMap);
			return new ResponseEntity<>(objmap1, HttpStatus.OK);
		} else {
			objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			return new ResponseEntity<>(objmap1, HttpStatus.EXPECTATION_FAILED);
		}
	}


	@Override
	public ResponseEntity<Object> getSampleBasedOnExternalOrder(Map<String, Object> inputMap, UserInfo userInfo) {
		return registrationDAO.getSampleBasedOnExternalOrder(inputMap, userInfo);
	}

	@Override
	public Map<String, Object> viewRegistrationFile(final Registration objFile, UserInfo objUserInfo,
			Map<String, Object> inputMap) throws Exception {
		return registrationDAO.viewRegistrationFile(objFile, objUserInfo, inputMap);
	}

	@Override
	public Map<String, Object> viewRegistrationSubSampleFile(final RegistrationSample objFile, UserInfo objUserInfo,
			Map<String, Object> inputMap) throws Exception {
		return registrationDAO.viewRegistrationSubSampleFile(objFile, objUserInfo, inputMap);
	}

	@Override
	public ResponseEntity<Object> importRegistrationData(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return registrationDAO.importRegistrationData(request, userInfo);
	}

	@Override
	public ResponseEntity<Object> importRegistrationSample(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		
		//ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the approval config retired
		//start
		final ObjectMapper objMapper = new ObjectMapper();
		
		final Map<String, Object> objMap1 = objMapper.readValue(request.getParameter("Map"), Map.class);
		
		final short ntransactionstatus=registrationDAO.getActiveApprovalConfigId((int)objMap1.get("napproveconfversioncode"), userInfo);
				
		if(ntransactionstatus==Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {		
			return registrationDAO.importRegistrationSample(request, userInfo);
		}
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDCONFIGVERSION", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	
	}

	@Override
	public ResponseEntity<Object> getExternalOrderAttachment(String nexternalordercode, String npreregno,
			UserInfo userInfo) throws Exception {
		return registrationDAO.getExternalOrderAttachment(nexternalordercode, npreregno, userInfo);
	}

	@Override
	public ResponseEntity<Object> getOutsourceDetails(String npreregno, String ntransactionsamplecode,
			UserInfo userInfo) throws Exception {
		return registrationDAO.getOutsourceDetails(npreregno, ntransactionsamplecode, userInfo);
	}

	@Override
	public Map<String, Object> viewExternalOrderAttachment(final Map<String, Object> objExternalOrderAttachmentFile,
			int ncontrolCode, UserInfo userInfo) throws Exception {
		return registrationDAO.viewExternalOrderAttachment(objExternalOrderAttachmentFile, ncontrolCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getExternalOrderForMapping(Map<String, Object> inputMap, UserInfo userInfo) {
		return registrationDAO.getExternalOrderForMapping(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> orderMapping(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return registrationDAO.orderMapping(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestfromSection(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getTestfromSection(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestSectionBasesdTestPackage(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getTestSectionBasesdTestPackage(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestBasedTestSection(Map<String, Object> inputMap) throws Exception {
		return registrationDAO.getTestBasedTestSection(inputMap);
	}

	@Override
	public ResponseEntity<Object> getMoreTestSection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return registrationDAO.getMoreTestSection(inputMap, userInfo);
	}
	@Override
	public ResponseEntity<Object> getAdhocTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return registrationDAO.getAdhocTest(inputMap, userInfo);
	}
	@Override
	public ResponseEntity<Object> createAdhocTest(UserInfo userInfo, List<String> listSample, List<TestGroupTest> listTest,
			int nregtypecode, int nregsubtypecode, Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objmap = registrationDAO.getCreateTestGroupTestSeqNo(userInfo, listSample, listTest,
				nregtypecode, nregsubtypecode, inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			return registrationDAO.createTest(userInfo, listSample, listTest, nregtypecode, nregsubtypecode, inputMap);
		} else {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getSampleBasedOnPortalOrder(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.getSampleBasedOnPortalOrder(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> copySample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.copySample(inputMap, userInfo);
	}
	
	//Added by Dhanushya RI for JIRA ID:ALPD-4912,ALPD-4913,ALPD-4914  Filter save detail --Start
	@Override
	public ResponseEntity<Object> createFavoriteFilterName(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.createFavoriteFilterName(inputMap, userInfo);
	}
	
	@Override
	public List<FilterName> getFavoriteFilterName(UserInfo userInfo)
			throws Exception {
		return registrationDAO.getFavoriteFilterName(userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationFilter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.getRegistrationFilter(inputMap, userInfo);
	}
	
	//Added by Dhanushya RI for JIRA ID:ALPD-5511 edit test method insert --Start
    /**
	 * This interface declaration is used to get the over all methods with respect to test
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of method with respect to test and also have the HTTP response code 
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getTestMethod(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.getTestMethod(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to update entry in method table.
	 * @param inputMap [Map] object holding details to be updated in unit table
	 * @return response entity object holding response status and data of updated method object
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> updateTestMethod(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAO.updateTestMethod(inputMap, userInfo);
	}
	//End

	
}
