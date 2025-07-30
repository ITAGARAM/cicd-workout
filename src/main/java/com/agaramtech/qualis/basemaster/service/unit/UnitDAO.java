package com.agaramtech.qualis.basemaster.service.unit;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'unit' table
 */
public interface UnitDAO {
	
	/**
	 * This interface declaration is used to get all the available units with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of unit records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active unit object based
	 * on the specified nunitCode.
	 * @param nunitCode [int] primary key of unit object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Unit getActiveUnitById(final int nunitCode,UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to add a new entry to unit  table.
	 * @param objUnit [Unit] object holding details to be added in unit table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createUnit(Unit objUnit,UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in unit  table.
	 * @param objUnit [Unit] object holding details to be updated in unit table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateUnit(Unit objUnit,UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete an entry in unit  table.
	 * @param objUnit [Unit] object holding detail to be deleted from unit table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteUnit(Unit objUnit,UserInfo userInfo) throws Exception;
}
