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
import com.agaramtech.qualis.quotation.model.DiscountBand;
import com.agaramtech.qualis.quotation.service.discountband.DiscountBandService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the DiscountBand Service methods
 * 
 * @author ATE172
 * @version 9.0.0.1
 */
@RestController
@RequestMapping("/discountband")
public class DiscountBandController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscountBandController.class);

	private RequestContext requestContext;
	private final DiscountBandService discountbandService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext      RequestContext to hold the request
	 * @param discountbandService DiscountBandService
	 */
	public DiscountBandController(RequestContext requestContext, DiscountBandService discountbandService) {
		super();
		this.requestContext = requestContext;
		this.discountbandService = discountbandService;
	}

	/**
	 * This method is used to retrieve list of available discountBand.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         discountBand
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getDiscountBand")
	public ResponseEntity<Object> getDiscountBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getDiscountBand() called");
		requestContext.setUserInfo(userInfo);
		return discountbandService.getDiscountBand(userInfo);
	}

	/**
	 * This method will is used to make a new entry to discountBand table.
	 * 
	 * @param inputMap map object holding params ( discountBand [DiscountBand]
	 *                 object holding details to be added in discountBand table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "discountBand": { "sdiscountbandname": "10%" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 176,"nmastersitecode": -1,"nmodulecode": 75,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         discountBand already exists/ list of discountBand along with the
	 *         newly added discountBand.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createDiscountBand")
	public ResponseEntity<Object> createDiscountBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DiscountBand discountBand = objmapper.convertValue(inputMap.get("discountband"), DiscountBand.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return discountbandService.createDiscountBand(discountBand, userInfo);
	}

	/**
	 * This method is used to retrieve a specific discountBand record.
	 * 
	 * @param inputMap [Map] map object with "ndiscountbandcode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "ndiscountbandcode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 176,"nmastersitecode": -1,"nmodulecode": 75, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with DiscountBand object for the specified primary key
	 *         / with string message as 'Deleted' if the discountBand record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveDiscountBandById")
	public ResponseEntity<Object> getActiveDiscountBandById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndiscountbandcode = (Integer) inputMap.get("ndiscountbandcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return discountbandService.getActiveDiscountBandById(ndiscountbandcode, userInfo);
	}

	/**
	 * This method is used to update selected discountBand details.
	 * 
	 * @param inputMap [map object holding params( discountBand [DiscountBand]
	 *                 object holding details to be updated in discountBand table,
	 *                 userinfo [UserInfo] holding logged in user details) Input:{
	 *                 "discountBand": {"ndiscountbandcode":1,"sdiscountbandname":
	 *                 "m", "sdescription": "m", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 176,"nmastersitecode": -1, "nmodulecode": 75,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         discountBand record is not available/ list of all discountBand and
	 *         along with the updated discountBand.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateDiscountBand")
	public ResponseEntity<Object> updateDiscountBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DiscountBand discountband = objmapper.convertValue(inputMap.get("discountband"), DiscountBand.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return discountbandService.updateDiscountBand(discountband, userInfo);
	}

	/**
	 * This method is used to delete an entry in DiscountBand table
	 * 
	 * @param inputMap [Map] object with keys of DiscountBand entity and UserInfo
	 *                 object. Input:{ "discountBand": {"ndiscountbandcode":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 176,"nmastersitecode": -1, "nmodulecode": 75,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         discountBand record is not available/ string message as 'Record is
	 *         used in....' when the discountBand is associated in transaction /
	 *         list of all discountBand excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteDiscountBand")
	public ResponseEntity<Object> deleteDiscountBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DiscountBand discountband = objmapper.convertValue(inputMap.get("discountband"), DiscountBand.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return discountbandService.deleteDiscountBand(discountband, userInfo);
	}
}
