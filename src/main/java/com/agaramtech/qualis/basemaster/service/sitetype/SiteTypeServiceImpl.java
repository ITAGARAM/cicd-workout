package com.agaramtech.qualis.basemaster.service.sitetype;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.basemaster.model.SiteType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'sitetype' table through its DAO layer.
 */
/**
 * @author sujatha.v
 * BGSI-5
 * 02/07/2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SiteTypeServiceImpl implements SiteTypeService {

	private final SiteTypeDAO siteTypeDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param siteTypeDAO SiteTypeDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public SiteTypeServiceImpl(SiteTypeDAO siteTypeDAO, CommonFunction commonFunction) {
		super();
		this.siteTypeDAO = siteTypeDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available sitetype with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sitetype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSiteType(UserInfo userInfo) throws Exception {
		return siteTypeDAO.getSiteType(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active sitetype object based
	 * on the specified nsitetypecode.
	 * @param nsitetypecode [int] primary key of sitetype object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSiteTypeById(int nsitetypecode, UserInfo userInfo) throws Exception {

		final SiteType objSiteType = siteTypeDAO.getActiveSiteTypeById(nsitetypecode, userInfo);
		if (objSiteType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(objSiteType, HttpStatus.OK);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to sitetype table.
	 * @param objSiteType [SiteType] object holding details to be added in sitetype table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSiteType(SiteType objSiteType, UserInfo userInfo) throws Exception {
		return siteTypeDAO.createSiteType(objSiteType, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in sitetype table.
	 * @param objSiteType [SiteType] object holding details to be updated in sitetype table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSiteType(SiteType objSiteType, UserInfo userInfo) throws Exception {
		return siteTypeDAO.updateSiteType(objSiteType, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in sitetype table.
	 * @param objSiteType [SiteType] object holding detail to be deleted from sitetype table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSiteType(SiteType objSiteType, UserInfo userInfo) throws Exception {
		return siteTypeDAO.deleteSiteType(objSiteType, userInfo);
	}
}
