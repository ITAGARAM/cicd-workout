package com.agaramtech.qualis.configuration.service.projectandsitehierarchymapping;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.configuration.model.ProjectAndSiteHierarchyMapping;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'projectsitehierarchymapping' table through
 * its DAO layer.
 * @author Mullai Balaji.V 
 * BGSI-7
 * 01/07/2025 
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ProjectAndSiteHierarchyMappingServiceImpl implements ProjectAndSiteHierarchyMappingService {

	private final ProjectAndSiteHierarchyMappingDAO projectAndSiteHierarchyMappingDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param projectAndSiteHierarchyMappingDAO    ProjectAndSiteHierarchyMappingDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	
	public ProjectAndSiteHierarchyMappingServiceImpl(ProjectAndSiteHierarchyMappingDAO projectAndSiteHierarchyMappingDAO,CommonFunction commonFunction) {
	         this.projectAndSiteHierarchyMappingDAO=projectAndSiteHierarchyMappingDAO;
	         this.commonFunction=commonFunction;
	}
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available projectandsitehierarchymapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of projectandsitehierarchymapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProjectAndSiteHierarchyMapping(UserInfo userInfo) throws Exception {

		return projectAndSiteHierarchyMappingDAO.getProjectAndSiteHierarchyMapping(userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to projectsitehierarchymapping table.
	 * 
	 * @param objProjectAndSiteHierarchyMapping [projectandsitehierarchymapping] object holding details to be added in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         projectandsitehierarchymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception {
		return projectAndSiteHierarchyMappingDAO.createProjectAndSiteHierarchyMapping(objProjectAndSiteHierarchyMapping, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in SiteHierarchyMapping( table.
	 * 
	 * @param objProjectAndSiteHierarchyMapping [projectsitehierarchymapping] object holding detail to be deleted from disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception {
		return projectAndSiteHierarchyMappingDAO.deleteProjectAndSiteHierarchyMapping(objProjectAndSiteHierarchyMapping, userInfo);
	}
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available projectandsitehierarchymapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of projectandsitehierarchymapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSitemap(final UserInfo userInfo) throws Exception 
	{
          return projectAndSiteHierarchyMappingDAO.getSitemap(userInfo);

	}
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available BioProject with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of projectandsitehierarchymapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBioProject(final UserInfo userInfo) throws Exception 
	{
		return projectAndSiteHierarchyMappingDAO.getBioProject(userInfo);
	}

}
