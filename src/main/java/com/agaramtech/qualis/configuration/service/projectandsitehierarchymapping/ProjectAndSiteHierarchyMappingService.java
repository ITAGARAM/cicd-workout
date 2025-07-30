package com.agaramtech.qualis.configuration.service.projectandsitehierarchymapping;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.configuration.model.ProjectAndSiteHierarchyMapping;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'projectsitehierarchymapping' table
 * @author Mullai Balaji.V 
 * BGSI-7
 * 3/07/2025 
 */
public interface ProjectAndSiteHierarchyMappingService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available ProjectAndSiteHierarchyMappings with respect to site public interface
	 * ProjectAndSiteHierarchyMappingService {
	 * 
	 * /** This service interface declaration will access the DAO layer that is used
	 * to get all the available ProjectAndSiteHierarchyMappings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of ProjectAndSiteHierarchyMapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public ResponseEntity<Object> getProjectAndSiteHierarchyMapping(UserInfo userInfo) throws Exception;	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to ProjectAndSiteHierarchyMapping table.
	 * 
	 * @param objProjectAndSiteHierarchyMapping [ProjectAndSiteHierarchyMapping] object holding details to be added in ProjectAndSiteHierarchyMapping
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         ProjectAndSiteHierarchyMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in ProjectAndSiteHierarchyMapping table.
	 * 
	 * @param objProjectAndSiteHierarchyMapping [ProjectAndSiteHierarchyMapping] object holding detail to be deleted from ProjectAndSiteHierarchyMapping
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         ProjectAndSiteHierarchyMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception;
    
	
	
	 /** This service interface declaration will access the DAO layer that is used
	 * to get all the available SiteHierarchyMapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of ProjectAndSiteHierarchyMapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSitemap(final UserInfo userInfo) throws Exception ;
	
	 /** This service interface declaration will access the DAO layer that is used
		 * to get all the available BioProject with respect to site
		 * 
		 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
		 *                 [int] primary key of site object for which the list is to be
		 *                 fetched
		 * @return a response entity which holds the list of ProjectAndSiteHierarchyMapping records with
		 *         respect to site
		 * @throws Exception that are thrown in the DAO layer
		 */
	public ResponseEntity<Object> getBioProject(final UserInfo userInfo) throws Exception ;


}
