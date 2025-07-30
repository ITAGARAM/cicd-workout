package com.agaramtech.qualis.credential.service.controlmaster;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'controlmaster' table
 */
public interface ControlMasterDAO {

	/**
	 * This interface declaration is used to get all the available controlmaster
	 * data with respect to site and nformcode
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched and nformcode [int] primary key of qualisforms object
	 *                 for which the list is to be fetched
	 * @return a response entity which holds the list of controlmaster records with
	 *         respect to site and nformcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<ControlMaster>> getUploadControlsByFormCode(final UserInfo userInfo) throws Exception;
}
