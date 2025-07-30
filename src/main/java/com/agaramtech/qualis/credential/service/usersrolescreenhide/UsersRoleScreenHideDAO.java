package com.agaramtech.qualis.credential.service.usersrolescreenhide;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.credential.model.UserRoleScreenControlHide;
import com.agaramtech.qualis.credential.model.UsersRoleScreenHide;
import com.agaramtech.qualis.global.UserInfo;

public interface UsersRoleScreenHideDAO {
	public ResponseEntity<Object> getUserScreenhide(final Integer nuserrolescreencode, final UserInfo objUserInfo) throws Exception;
	
	public Map<String, Object> getUserScreenhideControlRights(final UserInfo userInfo, final Integer nuserrolescreencode,final Integer nusercode) throws Exception;
	
	public ResponseEntity<Object> getUserScreenhidecomboo(final Integer nuserrolecode,final  UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> getAvailableUserScreenhide(final Integer nusercode,final Integer nuserrolecode, final UserInfo userInfo) throws Exception;
	
	public Map<String, Object> getUserScreenhideSeqno(final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> createUserScreenhide(final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> deleteUserScreenhide(final List<UsersRoleScreenHide> lstusersrolescreen, final UserInfo userInfo,final Integer nuserrolecode, final Integer nusercode) throws Exception;
	
	public ResponseEntity<Object> getSingleSelectUserScreenhide(final List<UsersRoleScreenHide> lstusersrolescreen, final UserInfo objUserInfo, final String userRoleScreenCode,final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> updateScreenHideControlRights(final UserInfo userInfo, final UserRoleScreenControlHide userroleController, final List<UsersRoleScreenHide> lstusersrolescreen, final int nflag, final Integer nneedrights,final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> updateListControlRights(final int nneedrights,final int nhidescreencode,final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getSearchScreenHide(final String nuserrolescreencode, final UserInfo objUserInfo,final Integer nusercode) throws Exception;
	//For Inserting Record while we insert in Screen Rights Table
	public void deleteUserScreenhideScreenRights(final String suserrolescreencode, final Integer nuserrolecode) throws Exception; 
	
	public void createUserScreenhideScreenRights(final Map<String, Object> inputMap) throws Exception ; 
}

