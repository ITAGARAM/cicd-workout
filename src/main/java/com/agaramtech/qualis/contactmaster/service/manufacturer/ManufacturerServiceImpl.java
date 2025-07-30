package com.agaramtech.qualis.contactmaster.service.manufacturer;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.contactmaster.model.Manufacturer;
import com.agaramtech.qualis.contactmaster.model.ManufacturerContactInfo;
import com.agaramtech.qualis.contactmaster.model.ManufacturerFile;
import com.agaramtech.qualis.contactmaster.model.ManufacturerSiteAddress;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'Manufacturer' table
 * through its DAO layer.
 * 
  */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

	private final ManufacturerDAO manufacturerDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active manufacturer(s) and their
	 * associated details (Manufacturer Site details and Manufacturer Contacts
	 * details) on the specified site through its DAO layer
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Manufaturer, ManufacturerSite and Contact detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getManufacturer(final UserInfo userInfo, final int nmanufcode) throws Exception {
		return manufacturerDAO.getManufacturer(userInfo, nmanufcode);
	}

	/**
	 * This method is used to retrieve active manufacturer object based on the
	 * specified nmanufcode through its DAO layer.
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and data of
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getManufacturerById(final int nmanufCode, final UserInfo userInfo) throws Exception {
		return manufacturerDAO.getManufacturerById(nmanufCode, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active manufacturer(s) and their
	 * associated details (Manufacturer Site details and Manufacturer Contacts
	 * details) on the specified site through its DAO layer
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Manufaturer, ManufacturerSite and Contact detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getManufacturerWithSiteAndContactDetails(final UserInfo userInfo,
			final int nmanufcode) throws Exception {
		return manufacturerDAO.getManufacturerWithSiteAndContactDetails(userInfo, nmanufcode);
	}

	/**
	 * This method is used to retrieve list of all active manufacturer site details
	 * through its DAO layer
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         ManufacturerSite details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public List<ManufacturerSiteAddress> getManufacturerSiteDetails(final UserInfo userInfo, final int nmanufcode)
			throws Exception {
		return manufacturerDAO.getManufacturerSiteDetails(userInfo, nmanufcode);
	}

	/**
	 * This method is used to add a new entry to manufacturer table through its DAO
	 * layer .
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createManufacturer(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return manufacturerDAO.createManufacturer(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in manufacturer table through its DAO
	 * layer .
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateManufacturer(final Manufacturer manufacturer, UserInfo userInfo)
			throws Exception {
		return manufacturerDAO.updateManufacturer(manufacturer, userInfo);
	}

	/**
	 * This method is used to delete entry in manufacturer table through its DAO
	 * layer .
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteManufacturer(final Manufacturer manufacturer, final UserInfo userInfo)
			throws Exception {
		return manufacturerDAO.deleteManufacturer(manufacturer, userInfo);
	}

	/**
	 * This method is used to add a new entry to manufacturersiteaddress table
	 * through its DAO layer.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSiteAddress(final ManufacturerSiteAddress manufacturerSite,
			final UserInfo userInfo) throws Exception {
		return manufacturerDAO.createSiteAddress(manufacturerSite, userInfo);
	}

	/**
	 * This method is used to update entry in manufacturersiteaddress table through
	 * its DAO layer.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSiteAddress(final ManufacturerSiteAddress manufacturerSite,
			final UserInfo userInfo) throws Exception {
		return manufacturerDAO.updateSiteAddress(manufacturerSite, userInfo);
	}

	/**
	 * This method is used to delete entry in manufacturersiteaddress table through
	 * its DAO layer.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSiteAddress(final ManufacturerSiteAddress manufacturerSite,
			final UserInfo userInfo) throws Exception {
		return manufacturerDAO.deleteSiteAddress(manufacturerSite, userInfo);
	}

	/**
	 * This method is used to add a new entry to manufacturercontactinfo table
	 * through its DAO layer.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createContactInfo(final ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception {
		return manufacturerDAO.createContactInfo(manufacturerContact, userInfo);
	}

	/**
	 * This method is used to update entry to manufacturercontactinfo table through
	 * its DAO layer.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateContactInfo(final ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception {
		return manufacturerDAO.updateContactInfo(manufacturerContact, userInfo);
	}

	/**
	 * This method is used to delete entry to manufacturercontactinfo table through
	 * its DAO layer.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteContactInfo(final ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception {
		return manufacturerDAO.deleteContactInfo(manufacturerContact, userInfo);
	}

	/**
	 * This method is used to retrieve active manufacturersite object based on the
	 * specified nmanufsitecode through its DAO layer.
	 * 
	 * @param nmanufcode     [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param nmanufSiteCode [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @return response entity object holding response status and data of
	 *         manufacturersite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSiteManufacturerById(final int nmanufCode, final int nmanufSiteCode,
			UserInfo userInfo) throws Exception {
		return manufacturerDAO.getSiteManufacturerById(nmanufCode, nmanufSiteCode, userInfo);

	}

	/**
	 * This method is used to retrieve active manufacturercontact object based on
	 * the specified nmanufContactCode through its DAO layer.
	 * 
	 * @param nmanufcode        [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @param nmanufSiteCode    [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @param nmanufContactCode [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @return response entity object holding response status and data of
	 *         manufacturercontact object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getContactManufacturerById(final int nmanufCode, final int nmanufSiteCode,
			final int nmanufContactCode, final UserInfo userInfo) throws Exception {
		ManufacturerContactInfo manu = manufacturerDAO.getContactManufacturerById(nmanufCode, nmanufSiteCode,
				nmanufContactCode);
		if (manu != null) {
			return new ResponseEntity<Object>(manu, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to retrieve list of all active manufacturer contact
	 * details based on the site through its DAO layer
	 * 
	 * @param nmanufcode     [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param nmanufSiteCode [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @return response entity object holding response status and list of
	 *         ManufacturerContact details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public List<ManufacturerContactInfo> getContactManufacturerBySite(final int nmanufCode, final int nmanufSiteCode,
			final UserInfo userInfo) throws Exception {
		return manufacturerDAO.getContactManufacturerBySite(nmanufCode, nmanufSiteCode, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> createManufacturerFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return manufacturerDAO.createManufacturerFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> editManufacturerFile(final ManufacturerFile objManufacturerFile,
			final UserInfo objUserInfo) throws Exception {
		return manufacturerDAO.editManufacturerFile(objManufacturerFile, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateManufacturerFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return manufacturerDAO.updateManufacturerFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteManufacturerFile(final ManufacturerFile objManufacturerFile,
			final UserInfo objUserInfo) throws Exception {
		return manufacturerDAO.deleteManufacturerFile(objManufacturerFile, objUserInfo);
	}

	@Transactional
	@Override
	public Map<String, Object> viewAttachedManufacturerFile(final ManufacturerFile objManufacturerFile,
			final UserInfo objUserInfo) throws Exception {
		return manufacturerDAO.viewAttachedManufacturerFile(objManufacturerFile, objUserInfo);
	}

}
