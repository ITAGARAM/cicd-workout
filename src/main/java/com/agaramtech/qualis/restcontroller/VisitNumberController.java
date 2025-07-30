package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.VisitNumber;
import com.agaramtech.qualis.barcode.service.visitnumber.VisitNumberService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the VisitNumber Service methods
 * 
 */
@RestController
@RequestMapping("/visitnumber")
public class VisitNumberController {
	private static final Logger LOGGER = LoggerFactory.getLogger(VisitNumberController.class);

	private final RequestContext requestContext;

	private final VisitNumberService visitnumberService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext     RequestContext to hold the request
	 * @param visitnumberService VisitNumberService
	 */
	public VisitNumberController(RequestContext requestContext, VisitNumberService visitnumberService) {
		super();
		this.requestContext = requestContext;
		this.visitnumberService = visitnumberService;
	}

	/**
	 * This Method is used to get the over all visitnumber with respect to Project
	 * Type
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of visitnumber with respect to
	 *         Project type and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getVisitNumber")
	public ResponseEntity<Object> getVisitNumber(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleCycle called");
		requestContext.setUserInfo(userInfo);
		return visitnumberService.getVisitNumber(userInfo);

	}

	/**
	 * This method will is used to make a new entry to visitnumber table.
	 * 
	 * @param inputMap map object holding params ( visitnumber [VisitNumber] object
	 *                 holding details to be added in visitnumber table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "visitnumber": { "nprojecttypecode": 1, "svisitnumber":
	 *                 "1-visit", ncodelength:1, scode:2 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled": 4,
	 *                 "ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                 "nformcode": 195,"nmastersitecode": -1,"nmodulecode": 76,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                 "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                 "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US",
	 *                 "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * 
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         visitnumber already exists/ list of visitnumbers along with the newly
	 *         added visitnumber.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createVisitNumber")
	public ResponseEntity<Object> createVisitNumber(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final VisitNumber visitNumber = objMapper.convertValue(inputMap.get("visitnumber"), VisitNumber.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return visitnumberService.createVisitNumber(visitNumber, userInfo);
	}

	/**
	 * This method is used to retrieve a specific visitnumber record.
	 * 
	 * @param inputMap [Map] map object with "nvisitnumbercode" and "userinfo" as
	 *                 keys for which the data is to be fetched 
	 *                 Input:{
	 *                        "nvisitnumbercode": 1, 
	 *                        "userinfo":{ 
	 *                        "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
	 *                        "ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                        "nmastersitecode": -1,"nmodulecode": 76, "nreasoncode": 195,
	 *                        "nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,
	 *                        "nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
	 *                        "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00",
	 *                        "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode":"en-US",
	 *                        "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                        "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "",
	 *                        "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 *                        
	 * @return ResponseEntity with VisitNumber object for the specified primary key
	 *         / with string message as 'Already Deleted' if the visitnumber record
	 *         is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveVisitNumberById")
	public ResponseEntity<Object> getActiveVisitNumberById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nvisitnumbercode = (Integer) inputMap.get("nvisitnumbercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return visitnumberService.getActiveVisitNumberById(nvisitnumbercode, userInfo);
	}

	/**
	 * This method is used to update selected visitnumber details.
	 * 
	 * @param inputMap [map object holding params( 
	 *             visitNumber [VisitNumber] object holding details to be updated in visitnumber table, 
	 *             userinfo [UserInfo] holding logged in user details) 
	 *             Input:{
	 *                   "visitNumber": {
	 *                          "nvisitnumbercode":1,"svisitnumber": "1-visit", "nprojecttypecode": 1, 
	 *                          "ncodelength": 1, "scode":3 },
	 *                   "userinfo":{ 
	 *                          "activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,
	 *                          "ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 195,
	 *                          "nmastersitecode": -1, "nmodulecode": 76, "nreasoncode": 0,
	 *                          "nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,
	 *                          "nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
	 *                          "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00",
	 *                          "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US",
	 *                          "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                          "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                          "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         visitnumber record is not available/ list of all visitnumbers and
	 *         along with the updated visitnumber.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateVisitNumber")
	public ResponseEntity<Object> updateVisitNumber(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final VisitNumber visitNumber = objMapper.convertValue(inputMap.get("visitnumber"), VisitNumber.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return visitnumberService.updateVisitNumber(visitNumber, userInfo);
	}

	/**
	 * This method is used to delete an entry in VisitNumber table
	 * 
	 * @param inputMap [Map] object with keys of VisitNumber entity and UserInfo object. 
	 *                 Input:{ 
	 *                     "visitnumber": {"nvisitnumbercode":1},
	 *                     "userinfo":{ 
	 *                         "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                         "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":-1,
	 *                         "nformcode": 195,"nmastersitecode": -1, "nmodulecode":76,
	 *                         "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                         "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                         "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                         "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US",
	 *                         "slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                         "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                         "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                         "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 *                         
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         visitnumber record is not available/ string message as 'Record is
	 *         used in....' when the visitnumber is associated in transaction / list
	 *         of all visitnumbers excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteVisitNumber")
	public ResponseEntity<Object> deleteVisitNumber(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final VisitNumber visitNumber = objMapper.convertValue(inputMap.get("visitnumber"), VisitNumber.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return visitnumberService.deleteVisitNumber(visitNumber, userInfo);
	}
}
