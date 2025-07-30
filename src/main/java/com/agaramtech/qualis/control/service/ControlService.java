package com.agaramtech.qualis.control.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.UserRoleScreenControl;
import com.agaramtech.qualis.credential.model.UsersRoleScreen;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds methods to perform CRUD operation
 * through its DAO layer.
* @author ATE234
* @version 9.0.0.1
 *@since 04-04-2025
 */
public interface ControlService {
	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified site.
	 * @param objUserInfo object is used for fetched the list of active records based on site
	 * @param nuserrolescreencode [int]  is used to fectched the  specified userrolescreen record.
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getScreenRights(Integer nuserrolescreencode, UserInfo objUserInfo) throws Exception;
	 /**
		 *  This method is used to retrieve list of all active screenrights for the
		 * specified site.
		 * @param nuserrolecode [int] is used to fetch the list of records based on nuserrolecode.
		 * @param userInfo object is used for fetched the list of active records based on site
		 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	      * @throws Exception that are thrown from this DAO layer
		 */
	ResponseEntity<Object> getScreenRightsByUserRoleCode(Integer nuserrolecode, UserInfo userInfo) throws Exception;
	 /**
     * This method is used to retrieve list of all active qualisformname for the
	 * specified site.
	 * @param nuserrolecode [int] is used to fetch the list of records based on nuserrolecode.
	 * @param userInfo object is used for fetched the list of active records based on site.
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getAvailableScreen(Integer nuserrolecode, UserInfo userInfo) throws Exception;
	/**
	 * This method is used to add a new entry to usersrolescreen  table and userrolescreencontrol.
	 * Need to check for duplicate entry of nformcode  for the specified site and specified userrole
	 * before saving into database and after complete insert of usersrolescreen then  insert userrolescreencontrol  based on  usersrolescreen records default add nneedrights as 4 . 
	 * lstacAvailableScreenArrayCollection [List]  holding details of screenrights to be added in usersrolescreen table
	 * @param inputMap  [Map] map object with "screenrights" as key for which the list is to be fetched
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> createScreenRights(Map<String, Object> inputMap) throws Exception;
	/**
	 * This method is used to retrieve list of all active controlrights  for the
	 * specified site.
	 * @param nuserrolesceencode [int] primary key of userrolescreen object for which the list is to be fetched
	 * @param userInfo object is used for fetched the list of active records based on site.
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getControlRights(UserInfo userInfo, Integer nuserrolesceencode) throws Exception;
	/**
	 * This method is used to add control rights . 
	 * This method is used to to update  nneedrights 4 is not control rights for specified screen and 3 is controlrights for specified screen.
	 * @param userInfo object is used for fetched the list of active records based on site. 
	 * @param userroleController object is used for fetched the list of active records based on nuserrolescreencode.  
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in list as string. 
	 * @param nflag [int] is used to refer  auto save control rights  or manual save .
	 * @param nneedrights [int] is used passing value.if passing 3 is enable controlrights and 4 is disable controlrights  .
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> createControlRights(UserInfo userInfo, UserRoleScreenControl userroleController, List<UsersRoleScreen> lstusersrolescreen, int nflag, int nneedrights) throws Exception;

	/**
	 * This method is used to add Esignature rights . 
	 * This method is used to  update  nneedesign in sitecontrolmaster table. 4 is not Esignature rights for specified screen and 3 is Esignature rights for specified screen.   
	 * @param userInfo object is used for fetched the list of active records based on site. 
	 * @param controlMaster object is used for fetched the list of active records based on ncontrolcode.  
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in list as string. 
	 * @param nflag [int] is used to refer  auto save control rights  or manual save .
	 * @param nneedesign [int] is used passing value.if passing 3 is enable esign rights and 4 is disable esign rights .
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> createEsign(UserInfo userInfo,UserRoleScreenControl userRoleScreenControl, List<UsersRoleScreen> lstusersrolescreen, int nflag, int nneedesign) throws Exception;

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 *  specified nuserrolescreencode.
	 * @param objUserInfo object is used for fetched the list of active records based on site.   
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in list as string.
	 * @param nuserrolecode [int] is used to fetch the list of records based on nuserrolecode. 
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getScreenRightsSelectAll(List<UsersRoleScreen> lstusersrolescreen, int nuserrolecode, UserInfo objUserInfo) throws Exception;
	/**
	 * This method is used to delete entry in usersrolescreen table.
	 * @param userInfo object is used for fetched the list of active records based on site.   
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in list as string.
	 * @param nuserrolecode [int] is used to fetch the list of records based on nuserrolecode. 
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> deleteScreenRights(List<UsersRoleScreen> lstusersrolescreen, UserInfo userInfo,
			Integer nuserrolecode) throws Exception;
	/**
	 * This method is used to retrieve list of all active screenrights for specified UsersRoleScreen.
	 * @param objUserInfo object is used for fetched the list of active records based on site.
	 * @param lstusersrolescreen this list contain list of UsersRoleScreen.fetech record from lstusersrolescreen. 
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getSingleSelectScreenRights(List<UsersRoleScreen> lstusersrolescreen, UserInfo objUserInfo, Integer nuserrolecode) throws Exception;
	/**
	 * This method is used to retrive list of all active userrole for specified site.
	 * @param nuserrolecode [int] fetch record from userrole without specified nuserrolecode . 
	 * @param userInfo object is used for fetched the list of active records based on site.
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getUserRole(Integer nuserrolecode, UserInfo userInfo) throws Exception;
	/**
	 * This method is used to copy screen from specified nuserrolecode. 
	 * this userrolecode record deleted in usersrolescreen and userrolescreencontrol  then  this nuserrolecode records  added in this  tables  usersrolescreen and userrolescreencontrol.
	 * @param inputMap  [Map] map object with "userinfo,userrolecode,nuserrolecode" as key for which the list is to be fetched
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> CopyScreenRights(Map<String, Object> inputMap) throws Exception;
	/**
	 *  This method is used to retrieve list of all active screenrights for the
	 * specified nuserrolecode.
	 * @param nuserrolecode [int] fetch record based on nuserrolecode  . 
	 * @param userInfo object is used for fetched the list of active records based on site.
	 * @return a response entity which holds the list of screenrights with respect to site and also have the HTTP response code 
	  * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getCopyUserRoleCode(Integer nuserrolecode, UserInfo userInfo) throws Exception;
	
	ResponseEntity<Object> getSearchScreenRights(String nuserrolescreencode, UserInfo objUserInfo) throws Exception;
}
