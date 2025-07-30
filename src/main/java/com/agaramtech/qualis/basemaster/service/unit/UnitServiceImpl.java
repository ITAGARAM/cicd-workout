package com.agaramtech.qualis.basemaster.service.unit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'unit' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class UnitServiceImpl implements UnitService {

	private final UnitDAO unitDAO;
	private final CommonFunction commonFunction;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param unitDAO UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public UnitServiceImpl(UnitDAO unitDAO, CommonFunction commonFunction) {
		this.unitDAO = unitDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available units with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of unit records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception {
		
		return unitDAO.getUnit(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active unit object based
	 * on the specified nunitCode.
	 * @param nunitCode [int] primary key of unit object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveUnitById(int nunitCode,UserInfo userInfo) throws Exception {
		
		final Unit unit = unitDAO.getActiveUnitById(nunitCode,userInfo);
		if (unit == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			return new ResponseEntity<>(unit, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to unit  table.
	 * @param objUnit [Unit] object holding details to be added in unit table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createUnit(Unit objUnit,UserInfo userInfo) throws Exception {
		return unitDAO.createUnit(objUnit,userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in unit  table.
	 * @param objUnit [Unit] object holding details to be updated in unit table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateUnit(Unit objUnit,UserInfo userInfo) throws Exception {
		return unitDAO.updateUnit(objUnit,userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in unit  table.
	 * @param objUnit [Unit] object holding detail to be deleted from unit table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUnit(Unit objUnit,UserInfo userInfo) throws Exception {		
		return unitDAO.deleteUnit(objUnit,userInfo);
	}

}
