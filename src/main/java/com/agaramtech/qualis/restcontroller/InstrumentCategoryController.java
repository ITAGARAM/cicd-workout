package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.instrumentmanagement.service.instrumentcategory.InstrumentCategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class has methods of instrument category screen
 */

@RestController
@RequestMapping("/instrumentcategory")
public class InstrumentCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentCategoryController.class);

	private final InstrumentCategoryService objInstrumentCategoryService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext               RequestContext to hold the request
	 * @param objInstrumentCategoryService InstrumentCategoryService
	 */
	public InstrumentCategoryController(InstrumentCategoryService objInstrumentCategoryService,
			RequestContext requestContext) {
		super();
		this.objInstrumentCategoryService = objInstrumentCategoryService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available instrumentcategory(s) used
	 * to get list of instrument category for combo.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         instrumentcategories
	 * @throws Exception exception
	 */
	@PostMapping(value = "/fetchinstrumentcategory")
	public ResponseEntity<Object> fetchInstrumentCategory(@RequestBody Map<String, Object> obj) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentCategoryService.fetchInstrumentCategory(userInfo);
	}

	/**
	 * This method is used to retrieve list of available instrumentcategory(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         instrumentcategories
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getInstrumentCategory")
	public ResponseEntity<Object> getInstrumentCategory(@RequestBody Map<String, Object> obj) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getInstrumentCategory");
		return objInstrumentCategoryService.getInstrumentCategory(userInfo);
	}

	/**
	 * This method will is used to make a new entry to instrumentcategory table.
	 * 
	 * @param inputMap map object holding params ( instrumentcategory
	 *                 [InstrumentCategory] object holding details to be added in
	 *                 instrumentcategory table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 7, "nformcode": 27, "nreasoncode": 0,
	 *                 "slanguagetypecode": "en-US", "sreportlanguagecode": "en-US",
	 *                 "nsitecode": 1, "ntranssitecode": 1, "nmastersitecode": -1,
	 *                 "sreason": "", "susername": "QuaLIS Admin", "suserrolename":
	 *                 "QuaLIS Admin", "slanguagefilename": "Msg_en_US",
	 *                 "nusersitecode": -1, "sloginid": "system", "sdeptname": "NA",
	 *                 "ndeptcode": -1, "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "ntimezonecode": -1, "stimezoneid": "Europe/London",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitedate": "dd/MM/yyyy",
	 *                 "nlogintypecode": 1}, "instrumentcategory": { "nsitecode":
	 *                 -1, "sinstrumentcatname": "INS CAT", "sdescription": "",
	 *                 "nstatus": 1, "ndefaultstatus": 4, "ncategorybasedflow": 4,
	 *                 "ncalibrationreq": 4 }}
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         instrumentcategory already exists/ list of instrumentcategories along
	 *         with the newly added instrumentcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createInstrumentCategory")
	public ResponseEntity<Object> createInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstrumentCategory instrumentCategory = objmapper.convertValue(inputMap.get("instrumentcategory"),
				InstrumentCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentCategoryService.createInstrumentCategory(instrumentCategory, userInfo);
	}

	/**
	 * This method is used to update selected instrumentcategory details.
	 * 
	 * @param inputMap [map object holding params( instrumentcategory
	 *                 [InstrumentCategory] object holding details to be updated in
	 *                 instrumentcategory table, userinfo [UserInfo] holding logged
	 *                 in user details) Input:{ "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 7, "nformcode": 27, "nreasoncode": 0,
	 *                 "slanguagetypecode": "en-US", "sreportlanguagecode": "en-US",
	 *                 "nsitecode": 1, "ntranssitecode": 1, "nmastersitecode": -1,
	 *                 "sreason": "", "susername": "QuaLIS Admin", "suserrolename":
	 *                 "QuaLIS Admin", "slanguagefilename": "Msg_en_US",
	 *                 "nusersitecode": -1, "sloginid": "system", "sdeptname": "NA",
	 *                 "ndeptcode": -1, "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "ntimezonecode": -1, "stimezoneid": "Europe/London",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitedate": "dd/MM/yyyy",
	 *                 "nlogintypecode": 1}, "instrumentcategory": {
	 *                 "ninstrumentcatcode": 3, "ncalibrationreq": 4,
	 *                 "ncategorybasedflow": 3, "sinstrumentcatname": "INS CAT -
	 *                 Updated", "sdescription": "New DES", "ndefaultstatus": 4,
	 *                 "dmodifieddate": 1744265551, "nsitecode": -1, "nstatus": 1}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         instrumentcategory record is not available/ list of all
	 *         instrumentcategories and along with the updated instrumentcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateInstrumentCategory")
	public ResponseEntity<Object> updateProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentCategory instrumentCategory = objmapper.convertValue(inputMap.get("instrumentcategory"),
				InstrumentCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentCategoryService.updateInstrumentCategory(instrumentCategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in instrumentcategory table
	 * 
	 * @param inputMap [Map] object with keys of instrumentcategory entity and
	 *                 UserInfo object. Input:{ "instrumentcategory":
	 *                 {"ninstrumentcatcode": 3}, "userinfo":{ "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 7, "nformcode": 27, "nreasoncode": 0,
	 *                 "slanguagetypecode": "en-US", "sreportlanguagecode": "en-US",
	 *                 "nsitecode": 1, "ntranssitecode": 1, "nmastersitecode": -1,
	 *                 "sreason": "", "susername": "QuaLIS Admin", "suserrolename":
	 *                 "QuaLIS Admin", "slanguagefilename": "Msg_en_US",
	 *                 "nusersitecode": -1, "sloginid": "system", "sdeptname": "NA",
	 *                 "ndeptcode": -1, "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "ntimezonecode": -1, "stimezoneid": "Europe/London",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitedate": "dd/MM/yyyy",
	 *                 "nlogintypecode": 1}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         instrumentcategory record is not available/ string message as 'Record
	 *         is used in....' when the instrumentcategory is associated in
	 *         transaction / list of all instrumentcategories excluding the deleted
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteInstrumentCategory")
	public ResponseEntity<Object> deleteInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentCategory insrumentCategory = objmapper.convertValue(inputMap.get("instrumentcategory"),
				InstrumentCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentCategoryService.deleteInstrumentCategory(insrumentCategory, userInfo);
	}

	/**
	 * This method is used to retrieve a specific instrumentcategory record.
	 * 
	 * @param inputMap [Map] map object with "ninstrumentcatcode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "ninstrumentcatcode": 3, "userinfo":{ "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 7, "nformcode": 27, "nreasoncode": 0,
	 *                 "slanguagetypecode": "en-US", "sreportlanguagecode": "en-US",
	 *                 "nsitecode": 1, "ntranssitecode": 1, "nmastersitecode": -1,
	 *                 "sreason": "", "susername": "QuaLIS Admin", "suserrolename":
	 *                 "QuaLIS Admin", "slanguagefilename": "Msg_en_US",
	 *                 "nusersitecode": -1, "sloginid": "system", "sdeptname": "NA",
	 *                 "ndeptcode": -1, "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "ntimezonecode": -1, "stimezoneid": "Europe/London",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitedate": "dd/MM/yyyy",
	 *                 "nlogintypecode": 1}}
	 * @return ResponseEntity with InstrumentCategory object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         instrumentcategory record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveInstrumentCategoryById")
	public ResponseEntity<Object> getActiveInstrumentCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninstrumentcatcode = (int) inputMap.get("ninstrumentcatcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) objInstrumentCategoryService.getActiveInstrumentCategoryById(ninstrumentcatcode,
				userInfo);
	}
	
	//ALPDJ21-31-->Added by Vignesh(23-07-2025)-->Added the new dropdown Instrument Type-Standard, Storage
		/**
		 * This method is used to get the InstrumentType from instrumenttype table
		 * 
		 * @param userInfo object is used for fetched the list of active records based
		 *                 on site
		 * @return response entity object holding response status and data of deleted
		 *         InstrumentType object
		 * @throws Exception that are thrown in the DAO layer
		 */
	@PostMapping(value = "/getInstrumentType")
	public ResponseEntity<Object> getInstrumentType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentCategoryService.getInstrumentType(userInfo);
	}
	
}
