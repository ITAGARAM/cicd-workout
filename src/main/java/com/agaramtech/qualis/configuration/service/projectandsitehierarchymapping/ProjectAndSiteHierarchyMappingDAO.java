package com.agaramtech.qualis.configuration.service.projectandsitehierarchymapping;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.configuration.model.ProjectAndSiteHierarchyMapping;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'projectsitehierarchymapping'
 * @author Mullai Balaji.V 
 * BGSI-7
 * 03/07/2025 
 */
public interface ProjectAndSiteHierarchyMappingDAO {

	/**
	 * This interface declaration is used to get all the available projectsitehierarchymapping with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of projectsitehierarchymapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getProjectAndSiteHierarchyMapping(UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to add a new entry to projectsitehierarchymapping table.
	 * 
	 * @param objProjectAndSiteHierarchyMapping [projectsitehierarchymapping] object holding details to be added in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         projectsitehierarchymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in projectsitehierarchymapping table.
	 * 
	 * @param objProjectAndSiteHierarchyMapping [projectsitehierarchymapping] object holding detail to be deleted from disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         projectsitehierarchymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception;
     
	public ResponseEntity<Object> getSitemap(final UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> getBioProject(final UserInfo userInfo) throws Exception ;


	
}
