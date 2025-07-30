package com.agaramtech.qualis.configuration.service.approvalconfig;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface ApprovalConfigDAO {

	public ResponseEntity<Object> getApprovalConfigFilter(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception;

	public Map<String, Object> getApprovalConfigRoleDetails(int napprovalConfigRoleCode, int napprovalsubtypecode,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUserRoleApprovalConfig(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAvailableComboData(int napprovalsubtypecode, UserInfo userInfo) throws Exception;

	public Map<String, Object> getApprovalConfigSequenceNumbers(Map<String, Object> objMap, boolean editFlag)
			throws Exception;

	public ResponseEntity<Object> createApprovalConfig(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> getApprovalConfigEditData(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> updateApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception;

	public String preValidateApprovalConfigVersion(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> approveApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getCopyRegType(final int napprovalconfigversioncode, final int napprovalsubtypecode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCopyRegSubType(int nregTypeCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> copyApprovalConfigVersion(Map<String, Object> objmap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> setDefaultFilterStatus(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> setDefaultDecisionStatus(Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> getDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
