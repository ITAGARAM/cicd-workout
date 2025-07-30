package com.agaramtech.qualis.organization.service.section;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;

/**
 * This interface declaration holds methods to perform CRUD operation on 'section' table
 */
public interface SectionService {
	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available section(s) with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of section records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSection(final UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active section object based
	 * on the specified nsectioncode.
	 * @param nsectioncode [int] primary key of section object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSectionById(final int nsectionCode,UserInfo userInfo) throws Exception ;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to section table.
	 * @param objSection [Section] object holding details to be added in section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSection(final Section objSection,UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 *  update entry in section table.
	 * @param objSection [Section] object holding details to be updated in section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSection(final Section objSection,UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in section table.
	 * @param objSection [Section] object holding detail to be deleted from section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSection(final Section objSection,UserInfo userInfo) throws Exception;
}
