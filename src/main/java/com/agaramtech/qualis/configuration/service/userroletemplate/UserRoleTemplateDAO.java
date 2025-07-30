package com.agaramtech.qualis.configuration.service.userroletemplate;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;


public interface UserRoleTemplateDAO {

	public ResponseEntity<Object> getApprovalSubType(int nmoduletypecode, final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getApprovalRegSubType(final int nflag, int nregtypecode, final int isregneed,	final int napprovalsubtypecode, final int ntemplatecode,
			final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getUserRoleforTree(final UserInfo userInfo,final Integer nregtypecode,final Integer nregsubtypecode,final Integer napprovalsubtypecode) throws Exception;
	public ResponseEntity<Object> getUserroletemplate(Map<String, Object> inputMap, final UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> getUserroletemplatebyId(final int ntreeversiontempcode, final UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> getTreetemplate(final int ntreeversiontempcode, final UserInfo userInfo)throws Exception;
	public Map<String, Object> fnSequencenumberUserRoleTemplateUpdate(Map<String, Object> inputMap, final boolean bflag)throws Exception;
	public ResponseEntity<Object> createUserRoleTemplatemaster(Map<String, Object> inputMap) throws Exception;
	public String fnCheckUserName(Map<String, Object> inputMap) throws Exception;
	public ResponseEntity<Object> updateEditUserRoleTemplatemaster(Map<String, Object> inputMap, boolean bflag)throws Exception;
//	public boolean prevalidateuserroletemplate(final int isregneed, final int nregtypecode, final int nregsubtypecode)throws Exception;
	public ResponseEntity<Object> deleteUserroleTemplatemaster(final int napprovalsubtypecode, final int ntreeversiontempcode,final int ntreetemplatecode,
			final int isregneed, final int nregtypecode, final int nregsubtypecode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> approveUserroleTemplatemaster(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
}
