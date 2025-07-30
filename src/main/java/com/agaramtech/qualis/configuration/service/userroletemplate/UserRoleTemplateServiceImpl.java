package com.agaramtech.qualis.configuration.service.userroletemplate;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class UserRoleTemplateServiceImpl implements UserRoleTemplateService {

	private UserRoleTemplateDAO userRoleTemplateDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param userRoleTemplateDAO UserRoleTemplateDAO Interface
	 * @param commonFunction      CommonFunction holding common utility functions
	 */
	public UserRoleTemplateServiceImpl(UserRoleTemplateDAO userRoleTemplateDAO) {
		this.userRoleTemplateDAO = userRoleTemplateDAO;
	}

	public ResponseEntity<Object> getApprovalSubType(final int nmoduletypecode, final UserInfo userInfo)
			throws Exception {
		return userRoleTemplateDAO.getApprovalSubType(nmoduletypecode, userInfo);
	}

	public ResponseEntity<Object> getApprovalRegSubType(final int nflag, final int nregtypecode, final int isregneed,
			final int napprovalsubtypecode, final int ntemplatecode, final UserInfo userInfo) throws Exception {
		return userRoleTemplateDAO.getApprovalRegSubType(nflag, nregtypecode, isregneed, napprovalsubtypecode,
				ntemplatecode, userInfo);
	}

	public ResponseEntity<Object> getUserRoleforTree(final UserInfo userInfo, final Integer nregtypecode,
			final Integer nregsubtypecode, final Integer napprovalsubtypecode) throws Exception {
		return userRoleTemplateDAO.getUserRoleforTree(userInfo, nregtypecode, nregsubtypecode, napprovalsubtypecode);
	}

	public ResponseEntity<Object> getUserroletemplate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return userRoleTemplateDAO.getUserroletemplate(inputMap, userInfo);
	}

	public ResponseEntity<Object> getUserroletemplatebyId(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception {
		return userRoleTemplateDAO.getUserroletemplatebyId(ntreeversiontempcode, userInfo);
	}

	public ResponseEntity<Object> getTreetemplate(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception {
		return userRoleTemplateDAO.getTreetemplate(ntreeversiontempcode, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> createUserRoleTemplatemaster(Map<String, Object> inputMap) throws Exception {
		inputMap.putAll(userRoleTemplateDAO.fnSequencenumberUserRoleTemplateUpdate(inputMap, true));
		return userRoleTemplateDAO.createUserRoleTemplatemaster(inputMap);
	}

	@Transactional
	public ResponseEntity<Object> updateEditUserRoleTemplatemaster(Map<String, Object> inputMap) throws Exception {
		boolean bflag = false;
		String strStatus = userRoleTemplateDAO.fnCheckUserName(inputMap);
		if (strStatus.equals("Failure")) {
			bflag = true;
		}
		inputMap.putAll(userRoleTemplateDAO.fnSequencenumberUserRoleTemplateUpdate(inputMap, false));
		return userRoleTemplateDAO.updateEditUserRoleTemplatemaster(inputMap, bflag);
	}

	@Transactional
	public ResponseEntity<Object> deleteUserroleTemplatemaster(final int napprovalsubtypecode,
			final int ntreeversiontempcode, final int ntreetemplatecode, final int isregneed, final int nregtypecode,
			final int nregsubtypecode, final UserInfo userInfo) throws Exception {
		return userRoleTemplateDAO.deleteUserroleTemplatemaster(napprovalsubtypecode, ntreeversiontempcode,
				ntreetemplatecode, isregneed, nregtypecode, nregsubtypecode, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> approveUserroleTemplatemaster(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return userRoleTemplateDAO.approveUserroleTemplatemaster(inputMap, userInfo);
	}

}
