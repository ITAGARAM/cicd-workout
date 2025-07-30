package com.agaramtech.qualis.credential.service.usersrolescreenhide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.UserRoleScreenControlHide;
import com.agaramtech.qualis.credential.model.UsersRoleScreenHide;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class UsersRoleScreenHideServiceImpl implements UsersRoleScreenHideService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UsersRoleScreenHideServiceImpl.class);

	
	private final UsersRoleScreenHideDAO usersRoleScreenHideDAO;
	
	public UsersRoleScreenHideServiceImpl(UsersRoleScreenHideDAO usersRoleScreenHideDAO, CommonFunction commonFunction) {
		this.usersRoleScreenHideDAO = usersRoleScreenHideDAO;
	}
	

	public ResponseEntity<Object> getUserScreenhide(final Integer nuserrolescreencode,final UserInfo objUserInfo) throws Exception {
		return usersRoleScreenHideDAO.getUserScreenhide(nuserrolescreencode,objUserInfo);
	}

	public ResponseEntity<Object> getUserScreenhidecomboo(final Integer nuserrolecode,final UserInfo objUserInfo) throws Exception {
		return usersRoleScreenHideDAO.getUserScreenhidecomboo(nuserrolecode,  objUserInfo);
	}

	public ResponseEntity<Object> getAvailableUserScreenhide(final Integer nusercode,final Integer nuserrolecode,final UserInfo userInfo) throws Exception{
		return usersRoleScreenHideDAO.getAvailableUserScreenhide(nusercode,nuserrolecode,userInfo);
	}	

	@Transactional
	public ResponseEntity<Object> createUserScreenhide(final Map<String, Object> inputMap) throws Exception{
		Map<String,Object> seqnoMap = new HashMap<String, Object>();
		try {
			seqnoMap=usersRoleScreenHideDAO.getUserScreenhideSeqno(inputMap);
			String rtnFromseqoMap=(String) seqnoMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(rtnFromseqoMap)) {
				inputMap.putAll(seqnoMap);
				return usersRoleScreenHideDAO.createUserScreenhide(inputMap);
			}else {
				return new ResponseEntity<Object>(rtnFromseqoMap,HttpStatus.EXPECTATION_FAILED) ;
			}
		}catch(Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
		}

	@Transactional
	public ResponseEntity<Object> deleteUserScreenhide(final List<UsersRoleScreenHide> lstusersrolescreen, final UserInfo userInfo, final Integer nuserrolecode, final Integer nusercode) throws Exception{
		return usersRoleScreenHideDAO.deleteUserScreenhide(lstusersrolescreen, userInfo,nuserrolecode,nusercode);
	}

	public ResponseEntity<Object> getSingleSelectUserScreenhide(final List<UsersRoleScreenHide> lstusersrolescreen, final UserInfo objUserInfo, final String userRoleScreenCode,final Map<String, Object> inputMap) throws Exception {
		return usersRoleScreenHideDAO.getSingleSelectUserScreenhide(lstusersrolescreen,objUserInfo, userRoleScreenCode,inputMap);
	}

	@Transactional
	public ResponseEntity<Object> updateScreenHideControlRights(final UserInfo userInfo,final  UserRoleScreenControlHide userroleController,final List<UsersRoleScreenHide> lstusersrolescreen,final int nflag,final  Integer nneedrights,final Map<String, Object> inputMap) throws Exception{
			return usersRoleScreenHideDAO.updateScreenHideControlRights(userInfo,userroleController,lstusersrolescreen,nflag,  nneedrights,inputMap);
	}

	@Transactional
	public ResponseEntity<Object> updateListControlRights(final int nneedrights,final int nhidescreencode,final Map<String, Object> inputMap) throws Exception{
		return usersRoleScreenHideDAO.updateListControlRights( nneedrights, nhidescreencode,inputMap);
	}

	public ResponseEntity<Object> getSearchScreenHide(final String nuserrolescreencode,final UserInfo objUserInfo,final Integer nusercode) throws Exception {
		return usersRoleScreenHideDAO.getSearchScreenHide(nuserrolescreencode,objUserInfo,nusercode);
	}
}
