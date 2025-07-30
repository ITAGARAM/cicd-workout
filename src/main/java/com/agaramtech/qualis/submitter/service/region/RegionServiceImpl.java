package com.agaramtech.qualis.submitter.service.region;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Region;

/**
 * This class holds methods to perform CRUD operation on 'region' table through its DAO layer.
 * @author ATE234
 * @version 10.0.0.2
 * ALPD-5645 Master screen -> Region
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class RegionServiceImpl implements RegionService
{

	private RegionDAO regionDAO;
	private final CommonFunction commonFunction;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param unitDAO UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public RegionServiceImpl(RegionDAO regionDAO, CommonFunction commonFunction) {
		this.regionDAO = regionDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This method is used to retrieve list of all active Region for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active Region
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception {	
		return regionDAO.getRegion(userInfo);
	}

	/**
	 * This method is used to retrieve active Region object based on the specified nregioncode.
	 * @param nregioncode [int] primary key of Region object
	 * @return response entity  object holding response status and data of Region object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveRegionById(final int nregioncode, final UserInfo userInfo)throws Exception {
		final Region objRegion = regionDAO.getActiveRegionById(nregioncode,userInfo);
		if (objRegion == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objRegion, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to Region table. 
	 * @param objRegion [Region] object holding details to be added in region table
	 * @return inserted region object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> createRegion(Region objRegion, UserInfo userInfo)throws Exception {
		return regionDAO.createRegion(objRegion, userInfo);
	}

	/**
	 * This method is used to update entry in region  table.
	 * @param objRegion [region] object holding details to be updated in region table
	 * @return response entity object holding response status and data of updated region object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateRegion(Region objRegion, UserInfo userInfo)throws Exception {
		return regionDAO.updateRegion(objRegion, userInfo);
	}
	/**
	 * This method id used to delete an entry in region table
	 * @param objRegion [Region] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an Region object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteRegion(Region objRegion, UserInfo userInfo)throws Exception {
		return regionDAO.deleteRegion(objRegion, userInfo);
	}

}
