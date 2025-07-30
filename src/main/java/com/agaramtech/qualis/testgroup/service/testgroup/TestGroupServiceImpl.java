package com.agaramtech.qualis.testgroup.service.testgroup;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.service.section.SectionDAO;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
/**
 * This class holds methods to perform CRUD operation on 'testgroup' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class TestGroupServiceImpl implements TestGroupService {


	private final TestGroupDAO testGroupDAO;
	
	public TestGroupServiceImpl(TestGroupDAO testGroupDAO) {
		this.testGroupDAO = testGroupDAO;
	}
	
	@Override
	public ResponseEntity<Object> getTestGroup(final UserInfo objUserInfo) throws Exception {
		return testGroupDAO.getTestGroup(objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getProductCategory(final UserInfo objUserInfo, final SampleType objSampleType)
			throws Exception {
		return testGroupDAO.getProductCategory(objUserInfo, objSampleType);
	}

	@Override
	public ResponseEntity<Object> getProduct(final UserInfo objUserInfo, final Map<String, Object> categoryMap,
			final SampleType objSampleType) throws Exception {
		return testGroupDAO.getProduct(objUserInfo, categoryMap, objSampleType);
	}

	@Override
	public ResponseEntity<Object> getTreeVersionTemplate(final UserInfo objUserInfo, final SampleType objSampleType,
			final Map<String, Object> categoryMap, Map<String, Object> productMap, Map<String, Object> projectMap) throws Exception {
		return testGroupDAO.getTreeVersionTemplate(objUserInfo, objSampleType, categoryMap, productMap, projectMap);
	}

	@Override
	public ResponseEntity<Object> getTestGroupSpecification(final UserInfo objUserInfo,
			final TreeTemplateManipulation objTree) throws Exception {
		return testGroupDAO.getTestGroupSpecification(objUserInfo, objTree, false, null);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> getAddSpecification(final UserInfo objUserInfo,final int ntreeversiontempcode,final int nprojectmastercode) throws Exception {
		return testGroupDAO.getAddSpecification(objUserInfo,ntreeversiontempcode,nprojectmastercode);
	}

	@Override
	public ResponseEntity<Object> getActiveSpecificationById(UserInfo objUserInfo,
			TestGroupSpecification objTestGroupSpec, int ntreeversiontempcode) throws Exception {
		return testGroupDAO.getActiveSpecificationById(objUserInfo, objTestGroupSpec,ntreeversiontempcode);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createSpecification(final UserInfo objUserInfo,
			final TestGroupSpecification objTestGroupSpec, final TreeTemplateManipulation objTree) throws Exception {
		return testGroupDAO.createSpecification(objUserInfo, objTestGroupSpec, objTree);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateSpecification(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec,
			TreeTemplateManipulation objTree) throws Exception {
		return testGroupDAO.updateSpecification(objUserInfo, objTestGroupSpec, objTree);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteSpecification(Map<String,Object>inputMap, UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec, TreeTemplateManipulation objTree,
			int ntreeversiontempcode) throws Exception {
		return testGroupDAO.deleteSpecification(inputMap,objUserInfo, objTestGroupSpec,objTree, ntreeversiontempcode);
	}

	// (isQualisLite) added by Gowtham - ALPD-5329 - In test group specification record getting Auto Approved when configuration for test group approval not done
	@Override
	@Transactional
	public ResponseEntity<Object> completeSpecification(UserInfo objUserInfo,final TestGroupSpecification objTestGroupSpec,
			TreeTemplateManipulation objTree,int ntreeversiontempcode,int isQualisLite,final List<TestGroupTest> listTestGroupTest) throws Exception {
		return testGroupDAO.completeSpecification(objUserInfo, objTestGroupSpec,objTree, ntreeversiontempcode, isQualisLite,listTestGroupTest);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> approveSpecification(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec,
			 TreeTemplateManipulation objTree,int ntreeversiontempcode) throws Exception {
		ResponseEntity<Object> validation = testGroupDAO.approveValidateSpecification(objUserInfo, objTestGroupSpec);
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(validation.getBody())) {
			return testGroupDAO.approveSpecification(objUserInfo, objTestGroupSpec,objTree, ntreeversiontempcode);
		} else {
			return validation;
		}
	}

	@Override
	public ResponseEntity<Object> getTemplateMasterDetails(final UserInfo objUserInfo, final int ncategorycode,
			final SampleType objSampleType, final int ntreeversiontempcode,
			final TreeTemplateManipulation objTreeTemplateManipulation,final int nprojectmastercode) throws Exception {
		return testGroupDAO.getTemplateMasterDetails(objUserInfo, ncategorycode, objSampleType, ntreeversiontempcode,
				objTreeTemplateManipulation,nprojectmastercode);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createTree(UserInfo objUserInfo, SampleType objSampleType, int ncategorycode,
			int nproductcode, final int ntreeversiontempcode, final int nprojectMasterCode, final List<TreeTemplateManipulation> listTree)
			throws Exception {
		
		return testGroupDAO.createTree(objUserInfo, objSampleType, ncategorycode, nproductcode, ntreeversiontempcode,
				nprojectMasterCode, listTree);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteTree(Map<String, Object> inputMap,UserInfo objUserInfo,  TreeTemplateManipulation objTree,int ntreeversiontempcode ) throws Exception {
		return testGroupDAO.deleteTree(inputMap,objUserInfo,objTree, ntreeversiontempcode);
	}

	@Override
	public ResponseEntity<Object> getTreeById(UserInfo objUserInfo, TreeTemplateManipulation objTree, int ntreeversiontempcode) throws Exception {
		return testGroupDAO.getTreeById(objUserInfo, objTree,ntreeversiontempcode);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateTree(UserInfo objUserInfo, TreeTemplateManipulation objTree, String sNode)
			throws Exception {
		return testGroupDAO.updateTree(objUserInfo, objTree, sNode);
	}

	@Override
	public ResponseEntity<Object> filterTestGroup(final UserInfo objUserInfo, final int nSampleTypeCode,
			final int nproductCatCode, final int nproductCode, final int nTreeVersionTempCode, final int nprojectCode) throws Exception {
		return testGroupDAO.filterTestGroup(objUserInfo, nSampleTypeCode, nproductCatCode, nproductCode,
				nTreeVersionTempCode, nprojectCode);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> copySpecification(Map<String, Object> inputMap,final int ntreeversiontempcode) throws Exception {
		Map<String, Object> seqnoMap = testGroupDAO.getcopySpecificationSeqno(inputMap);
		String rtnFromseqoMap = (String) seqnoMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(rtnFromseqoMap)) {
			inputMap.putAll(seqnoMap);
			return testGroupDAO.copySpecification(inputMap,ntreeversiontempcode);
		} else {
			return new ResponseEntity<>(rtnFromseqoMap, HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getTestGroupSampleType(TestGroupSpecification objTestGroupSpec, UserInfo objUserInfo)
			throws Exception {
		return testGroupDAO.getTestGroupSampleType(objTestGroupSpec, objUserInfo);
	}

	public ResponseEntity<Object> validateTestGroupComplete(final int nallottedSpecCode, final UserInfo userInfo)
			throws Exception {
		return testGroupDAO.validateTestGroupComplete(nallottedSpecCode, userInfo);
	}
	@Override
	public ResponseEntity<Object> getSpecDetailsForCopy(final Map<String, Object> inputMap,final UserInfo objUserInfo)
			throws Exception {
		return testGroupDAO.getSpecDetailsForCopy(inputMap, objUserInfo);
	}
}
