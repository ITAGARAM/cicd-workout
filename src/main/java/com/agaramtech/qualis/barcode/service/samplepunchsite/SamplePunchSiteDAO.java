package com.agaramtech.qualis.barcode.service.samplepunchsite;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.SamplePunchSite;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'SamplePunchSite' table
 */
public interface SamplePunchSiteDAO {

	/**
	 * This interface declaration is used to get all the available samplepunchsites with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of samplepunchsite records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSamplePunchSite(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available projecttypes with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of projecttype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getProjecttype(UserInfo userinfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available sampletypes with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampletype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSampleType(final int nprojecttypecode, UserInfo userinfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to samplepunchsite table.
	 * @param samplePunchSite [SamplePunchSite] object holding details to be added in samplepunchsite table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active samplepunchsite object based
	 * on the specified nsamplepunchsitecode.
	 * @param nsamplepunchsitecode [int] primary key of samplepunchsite object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SamplePunchSite getActiveSamplePunchSiteById(int nsamplepunchsitecode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in samplepunchsite  table.
	 * @param samplePunchSite [SamplePunchSite] object holding details to be updated in samplepunchsite table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete an entry in samplepunchsite  table.
	 * @param samplePunchSite [SamplePunchSite] object holding detail to be deleted from samplepunchsite table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception;

}
