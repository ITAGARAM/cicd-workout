package com.agaramtech.qualis.submitter.service.region;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Region;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'region' table
 * 
 * @author ATE234
 * @version 10.0.0.2 ALPD-5645 Master screen -> Region
 */
public interface RegionService {
	/**
	 * This interface declaration is used to get the over all Region with respect to
	 * site
	 * 
	 * @param userInfo [UserInfo]
	 * @param inputMap 
	 * @return a response entity which holds the list of Region with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active Region object based on
	 * the specified nregioncode.
	 * 
	 * @param nregioncode [int] primary key of Region object
	 * @return response entity object holding response status and data of Region
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveRegionById(final int nregioncode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to Region table.
	 * 
	 * @param objRegion [Region] object holding details to be added in Region table
	 * @return response entity object holding response status and data of added
	 *         Region object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createRegion(Region objRegion, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in region table.
	 * 
	 * @param objRegion [region] object holding details to be updated in region
	 *                  table
	 * @return response entity object holding response status and data of updated
	 *         region object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateRegion(Region objRegion, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in region table.
	 * 
	 * @param objRegion [region] object holding detail to be deleted in region table
	 * @return response entity object holding response status and data of deleted
	 *         region object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteRegion(Region objRegion, UserInfo userInfo) throws Exception;
}
