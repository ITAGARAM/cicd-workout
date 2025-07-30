package com.agaramtech.qualis.organization.service.section;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;

/**
 * This class holds methods to perform CRUD operation on 'section' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class SectionServiceImpl implements SectionService  {
	
	private final SectionDAO sectionDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param sectionDAO SectionDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public SectionServiceImpl(SectionDAO sectionDAO, CommonFunction commonFunction) {
		this.sectionDAO = sectionDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available section(s) with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of section object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of section records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSection(final UserInfo userInfo) throws Exception {
		
		return sectionDAO.getSection(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active section object based
	 * on the specified nsectionCode.
	 * @param nsectionCode [int] primary key of section object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSectionById(final int nsectionCode,final UserInfo userInfo) throws Exception {
		
		final Section objSection = sectionDAO.getActiveSectionById(nsectionCode,userInfo);
		
		if (objSection == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			return new ResponseEntity<>(objSection, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to section table.
	 * @param objSection [Section] object holding details to be added in section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSection(final Section objSection,final UserInfo userInfo) throws Exception {
		return sectionDAO.createSection(objSection,userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in section table.
	 * @param objSection [Section] object holding details to be updated in section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSection(final Section objSection,final UserInfo userInfo) throws Exception {
		return sectionDAO.updateSection(objSection,userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in section table.
	 * @param objSection [Section] object holding detail to be deleted from section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSection(final Section objSection,final UserInfo userInfo) throws Exception {		
		return sectionDAO.deleteSection(objSection,userInfo);
	}
}
