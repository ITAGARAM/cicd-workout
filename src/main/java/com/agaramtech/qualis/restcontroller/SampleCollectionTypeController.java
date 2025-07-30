package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.SampleCollectionType;
import com.agaramtech.qualis.barcode.service.samplecollectiontype.SampleCollectionTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the samplecollectiontype Service methods
 */
@RestController
@RequestMapping("/samplecollectiontype")
public class SampleCollectionTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCollectionTypeController.class);

	private RequestContext requestContext;
	private final SampleCollectionTypeService sampleCollectionTypeService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext              RequestContext to hold the request
	 * @param sampleCollectionTypeService SampleCollectionTypeService
	 */
	public SampleCollectionTypeController(RequestContext requestContext,
			SampleCollectionTypeService sampleCollectionTypeService) {
		super();
		this.requestContext = requestContext;
		this.sampleCollectionTypeService = sampleCollectionTypeService;
	}

	/**
	 * This method is used to retrieve list of available samplecollectiontype(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         samplecollectiontypes
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleCollectionType")
	public ResponseEntity<Object> getSampleCollectionType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleCollectionType called");
		requestContext.setUserInfo(userInfo);
		return sampleCollectionTypeService.getSampleCollectionType(userInfo);
	}

	/**
	 * This method is used to retrieve a specific samplecollectiontype record.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return ResponseEntity with SampleCollectionType object for the specified
	 *         primary key / with string message as 'Already Deleted' if the
	 *         samplecollectiontype record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSampleCollectionTypeById")
	public ResponseEntity<Object> getActiveSampleCollectionTypeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsamplecollectiontypecode = (Integer) inputMap.get("nsamplecollectiontypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleCollectionTypeService.getActiveSampleCollectionTypeById(nsamplecollectiontypecode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to samplecollectiontype table.
	 * 
	 * @param inputMap map object holding params ( 
	 *            sampleCollectionType [SampleCollectionType] object holding details to be added in samplecollectiontype table,
	 *            userinfo [UserInfo] holding logged in user details ) 
	 *            Input:{ 
	 *                "samplecollectiontype": {
	 *                       "nprojecttypecode": 1, "scode": 03,"nproductcode": 3},
	 *                 "userinfo":{ 
	 *                       "activelanguagelist": ["en-US"], "isutcenabled": 4,
	 *                       "ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                       "nformcode": 190,"nmastersitecode": -1,"nmodulecode": 76,
	 *                       "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                       "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                       "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                       "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US",
	 *                       "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                       "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                       "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "",
	 *                       "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 *                       
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         samplecollectiontype already exists/ list of samplecollectiontype
	 *         along with the newly added samplecollectiontype.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSampleCollectionType")
	public ResponseEntity<Object> createSampleCollectionType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleCollectionType collectiontubetype = objMapper.convertValue(inputMap.get("samplecollectiontype"),
				SampleCollectionType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCollectionTypeService.createSampleCollectionType(collectiontubetype, userInfo);
	}

	/**
	 * This method is used to update selected samplecollectiontype details.
	 * 
	 * @param inputMap [map object holding params( 
	 *            samplecollectiontype [SampleCollectionType] object holding details to be updated in samplecollectiontype table,
	 *            userinfo [UserInfo] holding logged in user details) 
	 *            Input:{ 
	 *                "samplecollectiontype":{
	 *                      "nprojecttypecode":1,"nproductcode": 3,"scode": 04, },
	 *                 "userinfo":{ 
	 *                      "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                      "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                      "nformcode": 190,"nmastersitecode": -1, "nmodulecode": 76,
	 *                      "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                      "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                      "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                      "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US",
	 *                      "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                      "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                      "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                      "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         samplecollectiontype record is not available/ list of all
	 *         samplecollectiontypes and along with the updated
	 *         samplecollectiontypes.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleCollectionType")
	public ResponseEntity<Object> updateSampleCollectionType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleCollectionType collectiontubetype = objMapper.convertValue(inputMap.get("samplecollectiontype"),
				SampleCollectionType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCollectionTypeService.updateSampleCollectionType(collectiontubetype, userInfo);
	}

	/**
	 * This method is used to delete an entry in SampleCollectionType table
	 * 
	 * @param inputMap [Map] object with keys of SampleCollectionType entity and UserInfo object. 
	 *               Input:{ 
	 *                     "samplecollectiontype":{"nsamplecollectiontypecode":1}, 
	 *                     "userinfo":{
	 *                            "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                            "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                            "nformcode": 190,"nmastersitecode": -1, "nmodulecode": 76,
	 *                            "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                            "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                            "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                            "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US",
	 *                            "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                            "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                            "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                            "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 *                            
	 * @return ResponseEntity with string message as 'Already deleted' if the samplecollectiontype
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the samplecollectiontype is associated in transaction / list of all samplecollectiontypes
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleCollectionType")
	public ResponseEntity<Object> deleteSampleCollectionType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SampleCollectionType samplecollectiontype = objMapper.convertValue(inputMap.get("samplecollectiontype"),
				SampleCollectionType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCollectionTypeService.deleteSampleCollectionType(samplecollectiontype, userInfo);
	}
}
