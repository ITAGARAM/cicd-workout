package com.agaramtech.qualis.project.service.projecttype;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectType;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'projectType' table
 */
public interface ProjectTypeService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available projectType with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of projectType records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<ProjectType>> getProjectType(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to projectType table.
	 * 
	 * @param objProjectType [ProjectType] object holding details to be added in
	 *                       projectType table
	 * @param userInfo       [UserInfo] holding logged in user details and
	 *                       nmasterSiteCode [int] primary key of site object for
	 *                       which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         projectType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createProjectType(final ProjectType projectType, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active projectType object based on the specified nprojecttypeCode.
	 * 
	 * @param nprojecttypeCode [int] primary key of projectType object
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         projectType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveProjectTypeById(final int nprojecttypecode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in projectType table.
	 * 
	 * @param objProjectType [ProjectType] object holding details to be updated in
	 *                       projectType table
	 * @param userInfo       [UserInfo] holding logged in user details and
	 *                       nmasterSiteCode [int] primary key of site object for
	 *                       which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         projectType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateProjectType(final ProjectType projecttype, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in projectType table.
	 * 
	 * @param objProjectType [ProjectType] object holding detail to be deleted from
	 *                       projectType table
	 * @param userInfo       [UserInfo] holding logged in user details and
	 *                       nmasterSiteCode [int] primary key of site object for
	 *                       which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         projectType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteProjectType(final ProjectType projecttype, final UserInfo userInfo)
			throws Exception;

}
