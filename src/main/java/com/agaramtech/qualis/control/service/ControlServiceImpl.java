package com.agaramtech.qualis.control.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.UserRoleScreenControl;
import com.agaramtech.qualis.credential.model.UsersRoleScreen;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.restcontroller.TestPackageController;

/**
 * This class holds methods to perform CRUD operation through its DAO layer.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 04-04-2025
 */

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ControlServiceImpl implements ControlService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestPackageController.class);

	private final ControlDAO objControlDAO;
	//private final CommonFunction commonFunction;	


	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param screenrightsDAO        screenrightsDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public ControlServiceImpl(ControlDAO objControlDAO) {
		this.objControlDAO = objControlDAO;
		
	}

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified site.
	 * 
	 * @param objUserInfo         object is used for fetched the list of active
	 *                            records based on site
	 * @param nuserrolescreencode [int] is used to fectched the specified
	 *                            userrolescreen record.
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getScreenRights(Integer nuserrolescreencode, UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return objControlDAO.getScreenRights(nuserrolescreencode, objUserInfo);
	}

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified site.
	 * 
	 * @param nuserrolecode [int] is used to fetch the list of records based on
	 *                      nuserrolecode.
	 * @param userInfo      object is used for fetched the list of active records
	 *                      based on site
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getScreenRightsByUserRoleCode(Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		return objControlDAO.getScreenRightsByUserRoleCode(nuserrolecode, userInfo);// siteCode);
	}

	/**
	 * This method is used to retrieve list of all active qualisformname for the
	 * specified site.
	 * 
	 * @param nuserrolecode [int] is used to fetch the list of records based on
	 *                      nuserrolecode.
	 * @param userInfo      object is used for fetched the list of active records
	 *                      based on site.
	 * @return response entity object holding response status and list of all active
	 *         qualisformname
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getAvailableScreen(Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		return objControlDAO.getAvailableScreen(nuserrolecode, userInfo);// siteCode);
	}

	/**
	 * This method is used to add a new entry to usersrolescreen table and
	 * userrolescreencontrol. Need to check for duplicate entry of nformcode for the
	 * specified site and specified userrole before saving into database and after
	 * complete insert of usersrolescreen then insert userrolescreencontrol based on
	 * usersrolescreen records default add nneedrights as 4 .
	 * lstacAvailableScreenArrayCollection [List] holding details of screenrights to
	 * be added in usersrolescreen table
	 * 
	 * @param inputMap [Map] map object with "screenrights" as key for which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         screenrights object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> createScreenRights(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> seqnoMap = new HashMap<String, Object>();

		seqnoMap = objControlDAO.getControlRightsSeqno(inputMap);
		String rtnFromseqoMap = (String) seqnoMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(rtnFromseqoMap)) {
			inputMap.putAll(seqnoMap);
			return objControlDAO.createScreenRights(inputMap);
		} else {
			return new ResponseEntity<Object>(rtnFromseqoMap, HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to retrieve list of all active controlrights for the
	 * specified site.
	 * 
	 * @param nuserrolescreencode [int] primary key of userrolescreen object for
	 *                            which the list is to be fetched
	 * @param userInfo            object is used for fetched the list of active
	 *                            records based on site.
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getControlRights(UserInfo userInfo, Integer nuserrolescreencode) throws Exception {

		return objControlDAO.getControlRights(userInfo, nuserrolescreencode);
	}

	/**
	 * This method is used to add control rights . This method is used to to update
	 * nneedrights 4 is not control rights for specified screen and 3 is
	 * controlrights for specified screen.
	 * 
	 * @param userInfo           object is used for fetched the list of active
	 *                           records based on site.
	 * @param userroleController object is used for fetched the list of active
	 *                           records based on nuserrolescreencode.
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in
	 *                           list as string.
	 * @param nflag              [int] is used to refer auto save control rights or
	 *                           manual save .
	 * @param nneedrights        [int] is used passing value.if passing 3 is enable
	 *                           controlrights and 4 is disable controlrights .
	 * @return response entity object holding response status and data of added
	 *         controlrights object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> createControlRights(UserInfo userInfo, UserRoleScreenControl userroleController,
			List<UsersRoleScreen> lstusersrolescreen, int nflag, int nneedrights) throws Exception {

		return objControlDAO.createControlRights(userInfo, userroleController, lstusersrolescreen, nflag, nneedrights);
	}

	/**
	 * This method is used to add Esignature rights . This method is used to update
	 * nneedesign in sitecontrolmaster table. 4 is not Esignature rights for
	 * specified screen and 3 is Esignature rights for specified screen.
	 * 
	 * @param userInfo           object is used for fetched the list of active
	 *                           records based on site.
	 * @param controlMaster      object is used for fetched the list of active
	 *                           records based on ncontrolcode.
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in
	 *                           list as string.
	 * @param nflag              [int] is used to refer auto save control rights or
	 *                           manual save .
	 * @param nneedesign         [int] is used passing value.if passing 3 is enable
	 *                           esign rights and 4 is disable esign rights .
	 * @return response entity object holding response status and data of added
	 *         controlrights object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> createEsign(UserInfo userInfo, UserRoleScreenControl userRoleScreenControl,
			List<UsersRoleScreen> lstusersrolescreen, int nflag, int nneedesign) throws Exception {
		// TODO Auto-generated method stub
		return objControlDAO.createEsign(userInfo, userRoleScreenControl, lstusersrolescreen, nflag, nneedesign);
	}

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified nuserrolescreencode.
	 * 
	 * @param objUserInfo        object is used for fetched the list of active
	 *                           records based on site.
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in
	 *                           list as string.
	 * @param nuserrolecode      [int] is used to fetch the list of records based on
	 *                           nuserrolecode.
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getScreenRightsSelectAll(List<UsersRoleScreen> lstusersrolescreen, int nuserrolecode,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return objControlDAO.getScreenRightsSelectAll(lstusersrolescreen, nuserrolecode, objUserInfo);
	}

	/**
	 * This method is used to delete entry in usersrolescreen table.
	 * 
	 * @param userInfo           object is used for fetched the list of active
	 *                           records based on site.
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in
	 *                           list as string.
	 * @param nuserrolecode      [int] is used to fetch the list of records based on
	 *                           nuserrolecode.
	 * @return response entity object holding response status and data of deleted
	 *         screenrights object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> deleteScreenRights(List<UsersRoleScreen> lstusersrolescreen, UserInfo userInfo,
			Integer nuserrolecode) throws Exception {

		return objControlDAO.deleteScreenRights(lstusersrolescreen, userInfo, nuserrolecode);
	}

	/**
	 * This method is used to retrieve list of all active screenrights for specified
	 * UsersRoleScreen.
	 * 
	 * @param objUserInfo        object is used for fetched the list of active
	 *                           records based on site.
	 * @param lstusersrolescreen this list contain list of UsersRoleScreen.fetech
	 *                           record from lstusersrolescreen.
	 * @return response entity object holding response status and screenrights
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSingleSelectScreenRights(List<UsersRoleScreen> lstusersrolescreen,
			UserInfo objUserInfo, Integer nuserrolecode) throws Exception {
		// TODO Auto-generated method stub
		return objControlDAO.getSingleSelectScreenRights(lstusersrolescreen, objUserInfo, nuserrolecode);
	}

	/**
	 * This method is used to retrive list of all active userrole for specified
	 * site.
	 * 
	 * @param nuserrolecode [int] fetch record from userrole without specified
	 *                      nuserrolecode .
	 * @param userInfo      object is used for fetched the list of active records
	 *                      based on site.
	 * @return response entity object holding response status and userrole object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUserRole(Integer nuserrolecode, UserInfo userInfo) throws Exception {

		return objControlDAO.getUserRole(nuserrolecode, userInfo);
	}

	/**
	 * This method is used to copy screen from specified nuserrolecode. this
	 * userrolecode record deleted in usersrolescreen and userrolescreencontrol then
	 * this nuserrolecode records added in this tables usersrolescreen and
	 * userrolescreencontrol.
	 * 
	 * @param inputMap [Map] map object with "userinfo,userrolecode,nuserrolecode"
	 *                 as key for which the list is to be fetched
	 * @return response entity object holding response status and screenrights
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> CopyScreenRights(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> seqnoMap = new HashMap<String, Object>();
		try {
			seqnoMap = objControlDAO.getControlRightsSeqno(inputMap);
			String rtnFromseqoMap = (String) seqnoMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(rtnFromseqoMap)) {
				inputMap.putAll(seqnoMap);
				return objControlDAO.CopyScreenRights(inputMap);
			} else {
				return new ResponseEntity<Object>(rtnFromseqoMap, HttpStatus.EXPECTATION_FAILED);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;

	}

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified nuserrolecode.
	 * 
	 * @param nuserrolecode [int] fetch record based on nuserrolecode .
	 * @param userInfo      object is used for fetched the list of active records
	 *                      based on site.
	 * @return response entity object holding response status and copyscreenrights
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getCopyUserRoleCode(Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		return objControlDAO.getCopyUserRoleCode(nuserrolecode, userInfo);// siteCode);
	}

	@Override
	public ResponseEntity<Object> getSearchScreenRights(String nuserrolescreencode, UserInfo objUserInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return objControlDAO.getSearchScreenRights(nuserrolescreencode, objUserInfo);
	}

}
