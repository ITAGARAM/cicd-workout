package com.agaramtech.qualis.configuration.service.userroletemplate;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface UserRoleTemplateService {

	public ResponseEntity<Object> getApprovalSubType(final int nmoduletypecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovalRegSubType(final int nflag, int nregtypecode, final int isregneed,
			final int napprovalsubtypecode, final int ntemplatecode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUserRoleforTree(final UserInfo userInfo, final Integer nregtypecode,
			final Integer nregsubtypecode, final Integer napprovalsubtypecode) throws Exception;

	public ResponseEntity<Object> getUserroletemplate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getUserroletemplatebyId(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTreetemplate(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createUserRoleTemplatemaster(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateEditUserRoleTemplatemaster(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteUserroleTemplatemaster(final int napprovalsubtypecode,
			final int ntreeversiontempcode, final int ntreetemplatecode, final int isregneed, final int nregtypecode,
			final int nregsubtypecode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> approveUserroleTemplatemaster(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;
}
