package com.agaramtech.qualis.contactmaster.service.manufacturer;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.contactmaster.model.Manufacturer;
import com.agaramtech.qualis.contactmaster.model.ManufacturerContactInfo;
import com.agaramtech.qualis.contactmaster.model.ManufacturerFile;
import com.agaramtech.qualis.contactmaster.model.ManufacturerSiteAddress;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'manufacturer'
 * table through its implementation class.
 * 
 * @author ATE090
 * @version
 * @since 21- Nov- 2020
 */
public interface ManufacturerService {

	/**
	 * This method is used to retrieve list of all active manufacturer(s) and their
	 * associated details (Manufacturer Site details and Manufacturer Contacts
	 * details)
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Manufaturer, ManufacturerSite and Contact detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getManufacturer(UserInfo userInfo, final int nmanufcode) throws Exception;

	/**
	 * This interface declaration is used to retrieve active manufacturer object
	 * based on the specified nmanufcode.
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and data of
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getManufacturerById(final int nmanufCode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active manufacturersite object
	 * based on the specified nmanufsitecode.
	 * 
	 * @param nmanufcode     [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param nmanufSiteCode [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @return response entity object holding response status and data of
	 *         manufacturersite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSiteManufacturerById(final int nmanufCode, final int nmanufSiteCode,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active manufacturercontact
	 * object based on the specified nmanufContactCode.
	 * 
	 * @param nmanufcode        [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @param nmanufSiteCode    [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @param nmanufContactCode [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @param userInfo
	 * @return response entity object holding response status and data of
	 *         manufacturercontact object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getContactManufacturerById(final int nmanufCode, final int nmanufSiteCode,
			final int nmanufContactCode, final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve list of all active manufacturer(s) and their
	 * associated details (Manufacturer Site details and Manufacturer Contacts
	 * details)
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Manufaturer, ManufacturerSite and Contact detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getManufacturerWithSiteAndContactDetails(final UserInfo userInfo,
			final int nmanufcode) throws Exception;

	/**
	 * This method is used to retrieve list of all active manufacturer site details
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         ManufacturerSite details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<ManufacturerSiteAddress> getManufacturerSiteDetails(final UserInfo userInfo, final int nmanufcode)
			throws Exception;

	/**
	 * This method is used to retrieve list of all active manufacturer contact
	 * details based on the site
	 * 
	 * @param nmanufcode     [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param nmanufSiteCode [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @return response entity object holding response status and list of
	 *         ManufacturerContact details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<ManufacturerContactInfo> getContactManufacturerBySite(final int nmanufCode, final int nmanufSiteCode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to manufacturer table.
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createManufacturer(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in manufacturer table.
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateManufacturer(final Manufacturer manufacturer, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in manufacturer table.
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteManufacturer(final Manufacturer manufacturer, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to
	 * manufacturersiteaddress table.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSiteAddress(final ManufacturerSiteAddress manufacturerSite,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in manufacturersiteaddress
	 * table.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSiteAddress(final ManufacturerSiteAddress manufacturerSite,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in manufacturersiteaddress
	 * table.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSiteAddress(final ManufacturerSiteAddress manufacturerSite,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to
	 * manufacturercontactinfo table.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createContactInfo(final ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry to manufacturercontactinfo
	 * table.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateContactInfo(final ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry to manufacturercontactinfo
	 * table.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteContactInfo(final ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createManufacturerFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> editManufacturerFile(final ManufacturerFile objManufacturerFile,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> updateManufacturerFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> deleteManufacturerFile(final ManufacturerFile objManufacturerFile,
			final UserInfo objUserInfo) throws Exception;

	public Map<String, Object> viewAttachedManufacturerFile(final ManufacturerFile objManufacturerFile,
			final UserInfo objUserInfo) throws Exception;

}
