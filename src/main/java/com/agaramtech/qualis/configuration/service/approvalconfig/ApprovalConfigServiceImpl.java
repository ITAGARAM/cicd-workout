package com.agaramtech.qualis.configuration.service.approvalconfig;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'unit' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ApprovalConfigServiceImpl implements ApprovalConfigService {

	private final ApprovalConfigDAO approvalConfigDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param approvalConfigDAO ApprovalConfigDAO Interface
	 */
	public ApprovalConfigServiceImpl(ApprovalConfigDAO approvalConfigDAO) {
		this.approvalConfigDAO = approvalConfigDAO;
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigFilter(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception {
		return approvalConfigDAO.getApprovalConfigFilter(objmap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception {
		return approvalConfigDAO.getApprovalConfigVersion(objmap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigRoleDetails(int napprovalConfigRoleCode, int napprovalsubtypecode,
			UserInfo userInfo) throws Exception {
		return new ResponseEntity<Object>(
				approvalConfigDAO.getApprovalConfigRoleDetails(napprovalConfigRoleCode, napprovalsubtypecode, userInfo),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUserRoleApprovalConfig(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {
		return approvalConfigDAO.getUserRoleApprovalConfig(objMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAvailableComboData(int napprovalsubtypecode, UserInfo userInfo) throws Exception {
		return approvalConfigDAO.getAvailableComboData(napprovalsubtypecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createApprovalConfig(Map<String, Object> objMap) throws Exception {
		objMap.putAll(approvalConfigDAO.getApprovalConfigSequenceNumbers(objMap, false));
		return approvalConfigDAO.createApprovalConfig(objMap);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigEditData(Map<String, Object> objmap) throws Exception {
		return approvalConfigDAO.getApprovalConfigEditData(objmap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateApprovalConfigVersion(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {
		objMap.putAll(approvalConfigDAO.getApprovalConfigSequenceNumbers(objMap, true));
		return approvalConfigDAO.updateApprovalConfigVersion(objMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception {
		return approvalConfigDAO.deleteApprovalConfigVersion(objmap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception {
		String validation = approvalConfigDAO.preValidateApprovalConfigVersion(objmap);
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equalsIgnoreCase(validation)) {
			return approvalConfigDAO.approveApprovalConfigVersion(objmap, userInfo);
		} else {
			return new ResponseEntity<Object>(validation, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> copyApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception {
		return approvalConfigDAO.copyApprovalConfigVersion(objmap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getCopyRegType(final int napprovalconfigversioncode, final int napprovalsubtypecode,
			final UserInfo userInfo) throws Exception {
		return approvalConfigDAO.getCopyRegType(napprovalconfigversioncode, napprovalsubtypecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getCopyRegSubType(int nregTypeCode, UserInfo userInfo) throws Exception {
		return approvalConfigDAO.getCopyRegSubType(nregTypeCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> setDefaultFilterStatus(Map<String, Object> objmap) throws Exception {
		return approvalConfigDAO.setDefaultFilterStatus(objmap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> setDefaultDecisionStatus(Map<String, Object> objmap) throws Exception {
		return approvalConfigDAO.setDefaultDecisionStatus(objmap);
	}

	@Override
	public ResponseEntity<Object> getDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return approvalConfigDAO.getDesignTemplateMapping(inputMap, userInfo);
	}

}
