package com.agaramtech.qualis.testgroup.service.testgroup;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;

public interface TestGroupDAO {
	
	public ResponseEntity<Object> getTestGroup(UserInfo objUserInfo) throws Exception;
	public ResponseEntity<Object> getProductCategory(final UserInfo objUserInfo, final SampleType objSampleType) throws Exception;
	public ResponseEntity<Object> getProduct(final UserInfo objUserInfo, final Map<String, Object> categoryMap, 
			final SampleType objSampleType) throws Exception;
	public ResponseEntity<Object> getTreeVersionTemplate(final UserInfo objUserInfo, final SampleType objSampleType, 
			final Map<String, Object> categoryMap, Map<String, Object> productMap,  Map<String, Object> projectMap) throws Exception;
	//public Map<String, Object> fnTestGroupTestgetSequence(Map<String, Object> objMap) throws Exception;
	//public Map<String, Object> fnTestGroupTestUpdateSequence(Map<String, Object> objMap) throws Exception;
	public ResponseEntity<Object> getTestGroupSpecification(final UserInfo objUserInfo, final TreeTemplateManipulation objTree, 
			final boolean isEditAction, final TestGroupSpecification objTestGroupSpec) throws Exception;
	public ResponseEntity<Object> getAddSpecification(UserInfo objUserInfo,int ntreeversiontempcode,int nprojectmastercode)
			throws Exception;
	public ResponseEntity<Object> getActiveSpecificationById(final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec,final int ntreeversiontempcode) throws Exception;
	public ResponseEntity<Object> createSpecification(final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec, 
			final TreeTemplateManipulation objTree) throws Exception;
	public ResponseEntity<Object> updateSpecification(final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec, 
			final TreeTemplateManipulation objTree) throws Exception;
	public ResponseEntity<Object> deleteSpecification(final Map<String,Object>inputMap,final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec,final TreeTemplateManipulation objTree,
			final int ntreeversiontempcode) throws Exception;
	// (isQualisLite) added by Gowtham - ALPD-5329 - In test group specification record getting Auto Approved when configuration for test group approval not done
	public ResponseEntity<Object> completeSpecification(final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec,TreeTemplateManipulation objTree, 
			final int ntreeversiontempcode, int isQualisLite,final List<TestGroupTest> listTestGroupTest) throws Exception;
	
	public ResponseEntity<Object> approveValidateSpecification(final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec) throws Exception;
	
	public ResponseEntity<Object> approveSpecification(final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec,final TreeTemplateManipulation objTree, 
			final int ntreeversiontempcode) throws Exception;
	
	public ResponseEntity<Object> getTemplateMasterDetails(final UserInfo objUserInfo, final int ncategorycode,
			final SampleType objSampleType, final int ntreeversiontempcode, final TreeTemplateManipulation objTreeTemplateManipulation,final int nprojectmastercode) throws Exception;
	
	public Map<String, Object> updateCreateTreeSeqNo(final List<TreeTemplateManipulation> listTree) throws Exception;
	
	public ResponseEntity<Object> createTree(final UserInfo objUserInfo, final SampleType objSampleType, final int ncategorycode, 
			final int nproductcode, final int ntreeversiontempcode, final int nprojectMasterCode,final List<TreeTemplateManipulation> listTree
			) throws Exception;
	
	public ResponseEntity<Object> deleteTree(final Map<String, Object> inputMap,final UserInfo objUserInfo,final TreeTemplateManipulation objTree, final int ntreeversiontempcode) throws Exception;
	
	public ResponseEntity<Object> getTreeById(final UserInfo objUserInfo,final TreeTemplateManipulation objTree, final int ntreeversiontempcode) throws Exception;
	
	public ResponseEntity<Object> updateTree(final UserInfo objUserInfo, final TreeTemplateManipulation objTree, String sNode) throws Exception;
	
	public ResponseEntity<Object> filterTestGroup(final UserInfo objUserInfo, final int nSampleTypeCode, final int nproductCatCode,
			final int nproductCode, final int nTreeVersionTempCode, final int nprojectCode) throws Exception;
	
	Map<String, Object> getcopySpecificationSeqno(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> copySpecification(Map<String, Object> inputMap,final int ntreeversiontempcode) throws Exception;
	
	public ResponseEntity<Object> getTestGroupSampleType(TestGroupSpecification objTestGroupSpec, UserInfo objUserInfo) throws Exception;
	
	public List<ApprovalRoleActionDetail> getApprovalConfigAction(final TestGroupSpecification ts, UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> validateTestGroupComplete(final int nallottedSpecCode, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getSpecDetailsForCopy(final Map<String, Object> inputMap,final UserInfo objUserInfo) throws Exception;

}
