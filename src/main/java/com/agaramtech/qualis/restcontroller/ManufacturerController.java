package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.contactmaster.model.Manufacturer;
import com.agaramtech.qualis.contactmaster.model.ManufacturerContactInfo;
import com.agaramtech.qualis.contactmaster.model.ManufacturerFile;
import com.agaramtech.qualis.contactmaster.model.ManufacturerSiteAddress;
import com.agaramtech.qualis.contactmaster.service.manufacturer.ManufacturerService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Manufacturer Service methods.
*/
@RestController
@RequestMapping("/manufacturer")
public class ManufacturerController {

	private static final Log LOGGER = LogFactory.getLog(ManufacturerController.class);
	private final RequestContext requestContext;
	private final ManufacturerService manufacturerService;

	public ManufacturerController(RequestContext requestContext, ManufacturerService manufacturerService) {
		super();
		this.requestContext = requestContext;
		this.manufacturerService = manufacturerService;
	}

	/**
	 * This method is used to retrieve list of all active manufacturer(s) and their
	 * associated details (Manufacturer Site details and Manufacturer Contacts
	 * details)for specific site.
	 * 
	 * @param inputMap userInfo
	 * @return response object with list of active manufacturer that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getManufacturer")
	public ResponseEntity<Object> getManufacturer(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nmanufCode = 0;
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getSampleAppearance -->");
		return manufacturerService.getManufacturer(userInfo, nmanufCode);

	}

	/**
	 * This method is used to retrieve selected active manufacturer detail.
	 * 
	 * @param inputMap [Map] map object with "nmanufcode" and "userInfo" as keys for
	 *                 which the data is to be fetched
	 * @return response object with selected active manufacturer
	 */
	@PostMapping(value = "/getManufacturerById")
	public ResponseEntity<Object> getManufacturerById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmanufCode = (Integer) inputMap.get("nmanufcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.getManufacturerById(nmanufCode, userInfo);

	}

	/**
	 * This method is used to retrieve selected active manufacturer site detail.
	 * 
	 * @param inputMap [Map] map object with "nmanufcode" and "nmanufsitecode" as
	 *                 keys for which the data is to be fetched
	 * @return response object with selected active manufacturer site
	 */
	@PostMapping(value = "/getSiteManufacturerById")
	public ResponseEntity<Object> getSiteManufacturerById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nmanufCode = (Integer) inputMap.get("nmanufcode");
		final int nmanufSiteCode = (Integer) inputMap.get("nmanufsitecode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.getSiteManufacturerById(nmanufCode, nmanufSiteCode, userInfo);
	}

	/**
	 * This method is used to retrieve selected active manufacturer contact detail.
	 * 
	 * @param inputMap [Map] map object with "nmanufcode" , "nmanufsitecode" and
	 *                 "nmanufcontactcode" as keys for which the data is to be
	 *                 fetched
	 * @return response object with selected active manufacturer contact
	 */
	@PostMapping(value = "/getContactManufacturerById")
	public ResponseEntity<Object> getContactManufacturerById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final int nmanufCode = (Integer) inputMap.get("nmanufcode");
		final int nmanufSiteCode = (Integer) inputMap.get("nmanufsitecode");
		final int nmanufContactCode = (Integer) inputMap.get("nmanufcontactcode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.getContactManufacturerById(nmanufCode, nmanufSiteCode, nmanufContactCode, userInfo);
	}

	/**
	 * This method is used to retrieve list of manufacturer site and contact detail
	 * for the selected manufacturer.
	 * 
	 * @param inputMap [Map] map object with "nmanufcode" and "userinfo" as keys for
	 *                 which the data is to be fetched
	 * @return response object with list of manufacturer contact and site details
	 */
	@PostMapping(value = "/getManufacturerWithSiteAndContactDetails")
	public ResponseEntity<Object> getManufacturerWithSiteAndContactDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nmanufCode = (Integer) inputMap.get("nmanufcode");
		requestContext.setUserInfo(userInfo);
		return manufacturerService.getManufacturerWithSiteAndContactDetails(userInfo, nmanufCode);
	}

	/**
	 * This method is used to add new manufacturer for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturer" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/createManufacturer")
	public ResponseEntity<Object> createManufacturer(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.createManufacturer(inputMap, userInfo);
	}

	/**
	 * This method is used update manufacturer for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturer" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/updateManufacturer")
	public ResponseEntity<Object> updateManufacturer(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Manufacturer manufacturer = objmapper.convertValue(inputMap.get("manufacturer"), Manufacturer.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.updateManufacturer(manufacturer, userInfo);
	}

	/**
	 * This method is used delete manufacturer for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturer" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/deleteManufacturer")
	public ResponseEntity<Object> deleteManufacturer(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Manufacturer manufacturer = objmapper.convertValue(inputMap.get("manufacturer"), Manufacturer.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.deleteManufacturer(manufacturer, userInfo);
	}

	/**
	 * This method is used to add new manufacturer site for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturersiteaddress" and
	 *                 "userinfo" entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/createSiteAddress")
	public ResponseEntity<Object> createSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ManufacturerSiteAddress manufacturerSite = objmapper.convertValue(inputMap.get("manufacturersiteaddress"),
				ManufacturerSiteAddress.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.createSiteAddress(manufacturerSite, userInfo);
	}

	/**
	 * This method is used to update manufacturer site for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturersiteaddress" and
	 *                 "userinfo" entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/updateSiteAddress")
	public ResponseEntity<Object> updateSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ManufacturerSiteAddress manufacturerSite = objmapper.convertValue(inputMap.get("manufacturersiteaddress"),
				ManufacturerSiteAddress.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.updateSiteAddress(manufacturerSite, userInfo);
	}

	/**
	 * This method is used to delete manufacturer site for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturersiteaddress" and
	 *                 "userinfo" entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/deleteSiteAddress")
	public ResponseEntity<Object> deleteSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ManufacturerSiteAddress manufacturerSite = objmapper.convertValue(inputMap.get("manufacturersiteaddress"),
				ManufacturerSiteAddress.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.deleteSiteAddress(manufacturerSite, userInfo);
	}

	/**
	 * This method is used to add new manufacturercontactinfo for the specified
	 * Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturercontactinfo" and
	 *                 "userinfo" entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/createContactInfo")
	public ResponseEntity<Object> createContactInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ManufacturerContactInfo manufacturerContact = objmapper
				.convertValue(inputMap.get("manufacturercontactinfo"), ManufacturerContactInfo.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.createContactInfo(manufacturerContact, userInfo);
	}

	/**
	 * This method is used to update manufacturercontactinfo for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturercontactinfo" and
	 *                 "userinfo" entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/updateContactInfo")
	public ResponseEntity<Object> updateContactInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ManufacturerContactInfo manufacturerContact = objmapper
				.convertValue(inputMap.get("manufacturercontactinfo"), ManufacturerContactInfo.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.updateContactInfo(manufacturerContact, userInfo);
	}

	/**
	 * This method is used to delete manufacturercontactinfo for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturercontactinfo" and
	 *                 "userinfo" entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/deleteContactInfo")
	public ResponseEntity<Object> deleteContactInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ManufacturerContactInfo manufacturerContact = objmapper
				.convertValue(inputMap.get("manufacturercontactinfo"), ManufacturerContactInfo.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.deleteContactInfo(manufacturerContact, userInfo);
	}

	/**
	 * This method is used to retrieve list of manufacturer contact for the selected
	 * manufacturer site.
	 * 
	 * @param inputMap [Map] map object with "nmanufcode" and "nmanufsitecode" as
	 *                 keys for which the data is to be fetched
	 * @return response object with list of manufacturer contact details
	 */
	@PostMapping(value = "/getContactManufacturerBySite")
	public List<ManufacturerContactInfo> getContactManufacturerBySite(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final int nmanufCode = (Integer) inputMap.get("nmanufcode");
		final int nmanufSiteCode = (Integer) inputMap.get("nmanufsitecode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.getContactManufacturerBySite(nmanufCode, nmanufSiteCode, userInfo);
	}

	@PostMapping(value = "/createManufacturerFile")
	public ResponseEntity<Object> createManufacturerFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.createManufacturerFile(request, userInfo);

	}

	@PostMapping(value = "/editManufacturerFile")
	public ResponseEntity<Object> editManufacturerFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ManufacturerFile objManufacturerFile = objMapper.convertValue(inputMap.get("manufacturerfile"),
				ManufacturerFile.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.editManufacturerFile(objManufacturerFile, userInfo);
	}

	@PostMapping(value = "/updateManufacturerFile")
	public ResponseEntity<Object> updateManufacturerFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.updateManufacturerFile(request, userInfo);
	}

	@PostMapping(value = "/deleteManufacturerFile")
	public ResponseEntity<Object> deleteManufacturerFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ManufacturerFile objManufacturerFile = objMapper.convertValue(inputMap.get("manufacturerfile"),
				ManufacturerFile.class);
		requestContext.setUserInfo(userInfo);
		return manufacturerService.deleteManufacturerFile(objManufacturerFile, userInfo);
	}

	@PostMapping(value = "/viewAttachedManufacturerFile")
	public ResponseEntity<Object> viewAttachedManufacturerFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ManufacturerFile objManufacturerFile = objMapper.convertValue(inputMap.get("manufacturerfile"),
				ManufacturerFile.class);
		Map<String, Object> outputMap = manufacturerService.viewAttachedManufacturerFile(objManufacturerFile, userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
}
