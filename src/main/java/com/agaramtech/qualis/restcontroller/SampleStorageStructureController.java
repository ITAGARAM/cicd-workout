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
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageVersion;
import com.agaramtech.qualis.storagemanagement.service.samplestoragestructure.SampleStorageStructureService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/samplestoragelocation")
public class SampleStorageStructureController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageStructureController.class);

	private RequestContext requestContext;
	private final SampleStorageStructureService sampleStorageStructureService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 *
	 * @param requestContext                RequestContext to hold the request
	 * @param SampleStorageStructureService SampleStorageStructureService
	 */
	public SampleStorageStructureController(RequestContext requestContext,
			SampleStorageStructureService sampleStorageStructureService) {
		super();
		this.requestContext = requestContext;
		this.sampleStorageStructureService = sampleStorageStructureService;
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getSampleStorageLocation")
	public ResponseEntity<Object> getSampleStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		LOGGER.info("getSampleStorageStructure()");
		final ObjectMapper objMapper = new ObjectMapper();
		int nstorageCategoryCode = 0;
		if (inputMap.get("nstoragecategorycode") != null) {
			nstorageCategoryCode = (Integer) inputMap.get("nstoragecategorycode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getSampleStorageStructure(0, nstorageCategoryCode, 0, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1},nstoragecategorycode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleStorageLocationByCategory")
	public ResponseEntity<Object> getSampleStorageStructureByCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nstorageCategoryCode = (Integer) inputMap.get("nstoragecategorycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getSampleStorageStructure(0, nstorageCategoryCode, 0, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1},nsamplestoragelocationcode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSelectedSampleStorageLocation")
	public ResponseEntity<? extends Object> getSelectedSampleStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampleStorageLocationCode = Integer.valueOf(inputMap.get("nsamplestoragelocationcode").toString());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getSelectedSampleStorageStructure(nsampleStorageLocationCode, 0, userInfo);

	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US"},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getActiveSampleStorageVersion")
	public ResponseEntity<? extends Object> getActiveSampleStorageVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampleStorageVersionCode = (Integer) inputMap.get("nsamplestorageversioncode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getActiveSampleStorageVersion(nsampleStorageVersionCode, userInfo);

	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getSampleStorageVersionByID")
	public ResponseEntity<Object> getSampleStorageVersionByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampleStorageVersionCode = (Integer) inputMap.get("nsamplestorageversioncode");
		final int nsampleStorageLocationCode = (Integer) inputMap.get("nsamplestoragelocationcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getSampleStorageVersionByID(nsampleStorageVersionCode,
				nsampleStorageLocationCode, userInfo);

	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSampleStorageLocation")
	public ResponseEntity<Object> createSampleStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleStorageStructure sampleStorageLocation = objmapper
				.convertValue(inputMap.get("sampleStorageLocation"), SampleStorageStructure.class);
		final SampleStorageVersion sampleStorageVersion = objmapper.convertValue(inputMap.get("sampleStorageVersion"),
				SampleStorageVersion.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.createSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleStorageLocation")
	public ResponseEntity<Object> updateSampleStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleStorageStructure sampleStorageLocation = objmapper
				.convertValue(inputMap.get("sampleStorageLocation"), SampleStorageStructure.class);
		final SampleStorageVersion sampleStorageVersion = objmapper.convertValue(inputMap.get("sampleStorageVersion"),
				SampleStorageVersion.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.updateSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleStorageLocation")
	public ResponseEntity<Object> deleteSampleStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleStorageStructure sampleStorageLocation = objmapper
				.convertValue(inputMap.get("sampleStorageLocation"), SampleStorageStructure.class);
		final SampleStorageVersion sampleStorageVersion = objmapper.convertValue(inputMap.get("sampleStorageVersion"),
				SampleStorageVersion.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.deleteSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "containerlastnode": ["Label"], "containerpath": [ {
	 *                 "scontainerpath": "root > Label > Label > Label > Label" } ],
	 *                 "containerpathsize": 1, "containers": [],
	 *                 "propertyValidation": { "locationEnd": false, "storageStart":
	 *                 false, "storageEnd": true }, "sampleStorageLocation": {
	 *                 "nstatus": 1, "nstoragecategorycode": 3,
	 *                 "nsamplestoragelocationcode": 46 }, "sampleStorageVersion": {
	 *                 "nstatus": 1, "napprovalstatus": 8,
	 *                 "nsamplestorageversioncode": 48,
	 *                 "nsamplestoragelocationcode": 46 },
	 *                 "selectedSampleStorageLocation": { "dmodifieddate":
	 *                 "2025-06-10T06:40:10.000+00:00", "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nmappingtranscode": 8,
	 *                 "nneedautomapping": 4, "nneedposition": 4, "nnoofcontainer":
	 *                 1, "nproductcode": -1, "nprojecttypecode": -1, "nquantity":
	 *                 0, "nrow": 1, "nsamplestoragelocationcode": 46, "nsitecode":
	 *                 1, "nstatus": 1, "nstoragecategorycode": 3, "nunitcode": -1,
	 *                 "scontainerstructurename": "NA", "scontainertype": "NA",
	 *                 "sdirection": "Left to Right", "sneedautomapping": "No",
	 *                 "sproductname": "NA", "sprojecttypename": "NA",
	 *                 "ssamplestoragelocationname": "Ss", "sstoragecategoryname":
	 *                 "Deep Freezer (-80°C)", "stransdisplaystatus": "Draft",
	 *                 "sunitname": "NA" }, "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 71, "nformcode": 168, "nmastersitecode":
	 *                 -1, "nsitecode": 1, "ntranssitecode": 1, "nusersitecode": -1,
	 *                 "ndeptcode": -1, "nreasoncode": 0, "nisstandaloneserver": 4,
	 *                 "nissyncserver": 3, "nlogintypecode": 1,
	 *                 "nsiteadditionalinfo": 4, "ntimezonecode": -1,
	 *                 "istimezoneshow": 4, "isutcenabled": 4, "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "activelanguagelist": ["en-US"],
	 *                 "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/ApproveSampleStorageLocation")
	public ResponseEntity<Object> approveSampleStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleStorageStructure sampleStorageLocation = objmapper
				.convertValue(inputMap.get("sampleStorageLocation"), SampleStorageStructure.class);
		final SampleStorageVersion sampleStorageVersion = objmapper.convertValue(inputMap.get("sampleStorageVersion"),
				SampleStorageVersion.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.approveSampleStorageVersion(sampleStorageLocation, sampleStorageVersion,
				inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "nstoragecategorycode": 3, "nsamplestoragelocationcode": 46
	 *                 }, "nsamplestoragelocationcode": 46, "nstatus": 1,
	 *                 "nstoragecategorycode": 3, "sampleStorageVersion": {
	 *                 "nstatus": 1, "napprovalstatus": 31,
	 *                 "nsamplestorageversioncode": 48,
	 *                 "nsamplestoragelocationcode": 46, "jsondata": {
	 *                 "nprojecttypecode": -1, "nquantity": 0, "nunitcode": -1,
	 *                 "nproductcode": -1, "nneedautomapping": 4, "nneedposition":
	 *                 4, "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "nrow": 1, "ncolumn": 1, "ndirectionmastercode": 1,
	 *                 "nnoofcontainer": 1 } }, "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 71, "nformcode": 168, "nmastersitecode":
	 *                 -1, "nsitecode": 1, "ntranssitecode": 1, "nusersitecode": -1,
	 *                 "ndeptcode": -1, "nreasoncode": 0, "nisstandaloneserver": 4,
	 *                 "nissyncserver": 3, "nlogintypecode": 1,
	 *                 "nsiteadditionalinfo": 4, "ntimezonecode": -1,
	 *                 "istimezoneshow": 4, "isutcenabled": 4, "slanguagefilename":
	 *                 "Msg_en_US", "slanguagetypecode": "en-US", "slanguagename":
	 *                 "English", "activelanguagelist": ["en-US"],
	 *                 "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 *                 * @return response entity object holding response status and
	 *                 list of all StorageStructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/copySampleStorageVersion")
	public ResponseEntity<Object> copySampleStorageVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleStorageStructure sampleStorageLocation = objmapper
				.convertValue(inputMap.get("sampleStorageLocation"), SampleStorageStructure.class);
		final SampleStorageVersion sampleStorageVersion = objmapper.convertValue(inputMap.get("sampleStorageVersion"),
				SampleStorageVersion.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.copySampleStorageVersion(sampleStorageLocation, sampleStorageVersion,
				userInfo);

	}
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getProjectType")
	public ResponseEntity<Object> getProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getProjectType(userInfo);
	}
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getProduct")
	public ResponseEntity<Object> getProduct(@RequestBody Map<String, Object> obj) throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		final UserInfo userInfo = objmap.convertValue(obj.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getProduct(userInfo);
	}

	@PostMapping(value = "/addinfoSampleStorageLocation")
	public ResponseEntity<Object> addinfoSampleStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleStorageStructure sampleStorageLocation = objmapper
				.convertValue(inputMap.get("sampleStorageLocation"), SampleStorageStructure.class);
		final SampleStorageVersion sampleStorageVersion = objmapper.convertValue(inputMap.get("sampleStorageVersion"),
				SampleStorageVersion.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.addinfoSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}
	//ALPDJ21-32-comment by Vignesh(23-07-2025)-->There is no need to fetch the Site ID and Instrument ID.
	/*
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 inputMap:[{"userinfo":{nmastersitecode":
	 *                 -1},"nstoragecategorycode":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 
	@PostMapping(value = "/getinstrumentID")
	public ResponseEntity<Object> getInstrumentID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getInstrumentID(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 inputMap:[{"userinfo":{nmastersitecode":
	 *                 -1},"nstoragecategorycode":1,"ninstrumentcode":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 
	@PostMapping(value = "/getsite")
	public ResponseEntity<Object> getsite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleStorageStructureService.getsite(inputMap, userInfo);
	}
*/

}
