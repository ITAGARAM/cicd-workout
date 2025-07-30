package com.agaramtech.qualis.registration.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;

public interface RegistrationDAO {

	public ResponseEntity<Object> getRegistration(UserInfo userInfo, String currentUIDate) throws Exception;

	//ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the approval config retired
	public short getActiveApprovalConfigId(final int ntransactionstatus, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getTreeByProduct(Map<String, Object> inputMap) throws Exception;

	public List<TestGroupSpecification> getTestGroupSpecification(int ntreetemplatemanipulationcode, int npreregno, int ntestGroupSpecRequired)
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

	public Map<String, Object> insertRegistration(Map<String, Object> inputMap) throws Exception;

	// ALPD-2046
	public ResponseEntity<Object> preRegisterSample(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> preRegisterSampleWithFile(MultipartHttpServletRequest inputMap) throws Exception;

	public Map<String, Object> insertSeqNoRegistration(Map<String, Object> inputMap) throws Exception;

	public Map<String, Object> acceptRegistration(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getRegistrationTemplate(int nregtypecode, int nregsubtypecode) throws Exception;
	
	public ResponseEntity<Object> getTestBasesdTestPackage(Map<String, Object> inputMap) throws Exception;
		
	public Map<String, Object> getCreateTestSequenceNo(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> createTest(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getEditRegistrationDetails(final Map<String, Object> inputMap,
	final UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> updateRegistration(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception;
	
	public Map<String, Object> seqNoTestSampleInsert(Map<String, Object> inputMap) throws Exception ;
	
	public Map<String, Object> cancelTestSamples(Map<String, Object> inputMap) throws Exception;
	
	Map<String, Object> seqNoSampleInsert(Map<String, Object> inputMap) throws Exception;

	public Map<String, Object> rejectSample(Map<String, Object> inputMap) throws Exception ;

	public Map<String, Object> validateSeqnoSubSampleNo(Map<String, Object> inputMap) throws Exception;
	
	public Map<String, Object> createSubSample(Map<String, Object> inputMap) throws Exception;
	
	public String createSubSampleWithFile(MultipartHttpServletRequest objmap) throws Exception;
	
	public ResponseEntity<Object> getEditRegistrationSubSampleDetails(final Map<String, Object> inputMap,
	final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateRegistrationSubSample(final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> updateRegistrationSubSampleWithFile(MultipartHttpServletRequest inputMap)
	throws Exception;	
	
	public Map<String, Object> seqNoSubSampleInsert(Map<String, Object> inputMap) throws Exception;
	
	public Map<String, Object> cancelSubSample(Map<String, Object> inputMap) throws Exception;

	public Map<String, Object> seqNoQuarentineSampleInsert(Map<String, Object> uiMap) throws Exception;
	
	public Map<String, Object> quarantineSamples(Map<String, Object> inputMap) throws Exception;
	
	public List<TransactionStatus> getFilterStatus(int nregtypecode, int nregsubtypecode, UserInfo userinfo)
	throws Exception;

	public Map<String, Object> validateAndInsertSeqNoRegistrationData(Map<String, Object> inputMap) throws Exception;
	
	public Map<String, Object> schedulerInsertRegistration(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getSampleBasedOnExternalOrder(Map<String, Object> inputMap, UserInfo userInfo);
	
	public Map<String, Object> viewRegistrationFile(Registration objFile, UserInfo objUserInfo,
				Map<String, Object> inputMap) throws Exception ;

	public Map<String, Object> viewRegistrationSubSampleFile(RegistrationSample objFile, UserInfo objUserInfo,
				Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> importRegistrationData(MultipartHttpServletRequest request, UserInfo userInfo)
				throws Exception;
	
	public ResponseEntity<Object> importRegistrationSample(MultipartHttpServletRequest request, UserInfo userInfo)
				throws Exception;

	public ResponseEntity<Object> getExternalOrderAttachment(String nexternalordercode, String npreregno,
				UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> getOutsourceDetails(String npreregno, String ntransactionsamplecode,
				UserInfo userInfo) throws Exception;

	public Map<String, Object> viewExternalOrderAttachment(Map<String, Object> objExternalOrderAttachmentFile,
	int ncontrolCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getExternalOrderForMapping(Map<String, Object> inputMap, UserInfo userInfo);
	
	public ResponseEntity<Object> orderMapping(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestfromSection(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getTestSectionBasesdTestPackage(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestBasedTestSection(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getMoreTestSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAdhocTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> getCreateTestGroupTestSeqNo(UserInfo userInfo, List<String> listSample,
			List<TestGroupTest> listTest, int nregtypecode, int nregsubtypecode, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getSampleBasedOnPortalOrder(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> copySample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createFavoriteFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public List<FilterName> getFavoriteFilterName(UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getRegistrationFilter(final Map<String, Object> uiMap,final UserInfo userInfo) throws Exception ;
	
	public ResponseEntity<Object> getTestMethod(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateTestMethod(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception ;
	
	
//	public Map<String, Object> validateAndInsertSeqNoRegistrationDataList(Map<String, Object> inputMap)
//	throws Exception;//

//	public ResponseEntity<Object> getRegistrationSubSampleAfterTestAdd(Map<String, Object> inputMap, UserInfo userInfo)
//			throws Exception;
	
//	public Map<String, Object> insertRegistrationList(Map<String, Object> inputMap) throws Exception;
//	
//	public List<SampleType> getSampleType(UserInfo userinfo) throws Exception;
//
//	public List<RegistrationType> getRegistrationType(int nsampletypecode, UserInfo userinfo) throws Exception;
//
//	public List<RegistrationSubType> getRegistrationSubType(int nregtypecode, UserInfo userinfo) throws Exception;
//
//	Map<String, Object> seqNoSampleInsert(Map<String, Object> inputMap) throws Exception;
//	
//	ResponseEntity<Object> getRegistrationSubSampleTemplate(int ndesigntemplatemappingcode) throws Exception;
//
//	Map<String, Object> getTemplateDesign(int ndesigntemplatemappingcode, int nformcode) throws Exception;
//
//	public Map<String, Object> validateUniqueConstraint(List<Map<String, Object>> masterUniqueValidation,
//			Map<String, Object> map, UserInfo userInfo, String string, Class<?> class1, String string2, boolean b)
//			throws Exception;
//
//	List<ApprovalConfigAutoapproval> getApprovalConfigVersion(int nregtypecode, int nregsubtypecode, UserInfo userinfo)
//			throws Exception;
//
//	ResponseEntity<Object> getApproveConfigVersionRegTemplate(int nregtypecode, int nregsubtypecode,
//			int napprovalconfigversioncode) throws Exception;
//	
//	Map<String, Object> getRegistrationSampleAfterSubsampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
//			throws Exception;	
//
	

//	public Map<String, Object> getDynamicRegistration (Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
//
//	public HttpEntity<Object> getRegistrationTestAudit(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
//
//	public List<Map<String, Object>> testAuditGet(Map<String, Object> rtnMap, UserInfo uI) throws Exception;

}
