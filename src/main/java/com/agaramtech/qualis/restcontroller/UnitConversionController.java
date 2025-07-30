package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.model.UnitConversion;
import com.agaramtech.qualis.basemaster.service.unitconversion.UnitConversionService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the Country Service methods.
 */
@RestController
@RequestMapping("/unitconversion")
public class UnitConversionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitConversionController.class);

	private RequestContext requestContext;
	private final UnitConversionService unitConversionService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param countryService CountryService
	 */
	public UnitConversionController(RequestContext requestContext, UnitConversionService unitConversionService) {
		super();
		this.requestContext = requestContext;
		this.unitConversionService = unitConversionService;
	}

	/**
	 * This method is used to retrieve list of available unit conversion(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all unit
	 *         conversion(s)
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getUnitConversion")
	public ResponseEntity<Object> getUnitConversion(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("at getUnitConversion");
		return unitConversionService.getUnitConversion(userInfo);
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         unit(s)
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getUnit")
	public ResponseEntity<Object> getUnit(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return unitConversionService.getUnit(userInfo);
	}

	/**
	 * This method is used to retrieve list of available conversion operator(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         conversion operator
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getConversionOperator")
	public ResponseEntity<Object> getConversionOperator(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return unitConversionService.getConversionOperator(userInfo);
	}

	/**
	 * This method will is used to make a new entry to country table.
	 * 
	 * @param inputMap map object holding params ( unitConversion [UnitConversion]
	 *                 object holding details to be added in unitconversion table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "unitconversion":{"nsitecode": -1,"sunitname":
	 *                 "mm","nsourceunitcode": 2,"nunitconversioncode":
	 *                 null,"sunitname1": "","ndestinationunitcode": 3,"soperator":
	 *                 "*", "noperatorcode": 3,"nconversionfactor": 10} ,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 206,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the country
	 *         already exists/ list of countries along with the newly added country.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createUnitConversion")
	public ResponseEntity<Object> createUnitConversion(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UnitConversion objUnitConversion = objMapper.convertValue(inputMap.get("unitconversion"),
				new TypeReference<UnitConversion>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return unitConversionService.createUnitConversion(objUnitConversion, userInfo);
	}

	/**
	 * This method is used to update selected unitconversion details.
	 * 
	 * @param inputMap [map object holding params( unitConversion [UnitConversion]
	 *                 object holding details to be added in unitconversion table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "unitconversion":{"nsitecode": -1,"sunitname":
	 *                 "mm","nsourceunitcode": 2,"nunitconversioncode":
	 *                 null,"sunitname1": "","ndestinationunitcode": 3,"soperator":
	 *                 "*", "noperatorcode": 3,"nconversionfactor": 10} ,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 206,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         unitconversion record is not available/ list of all unitconversion
	 *         and along with the updated unitconversion for given master site code.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateUnitConversion")
	public ResponseEntity<Object> updateUnitConversion(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		UnitConversion objUnitConversion = objMapper.convertValue(inputMap.get("unitconversion"),
				new TypeReference<UnitConversion>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return unitConversionService.updateUnitConversion(objUnitConversion, userInfo);
	}

	/**
	 * This method is used to delete an entry in unitconversion table
	 * 
	 * @param inputMap [Map] object with keys of UnitConversion entity and UserInfo
	 *                 object. Input:{ "unitconversion":{"nsitecode":
	 *                 -1,"sunitname": "mm","nsourceunitcode":
	 *                 2,"nunitconversioncode": null,"sunitname1":
	 *                 "","ndestinationunitcode": 3,"soperator": "*",
	 *                 "noperatorcode": 3,"nconversionfactor": 10} , "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 206,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         unitconversion record is not available/ string message as 'Record is
	 *         used in....' when the unitconversion is associated in transaction /
	 *         list of all unitconversions excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteUnitConversion")
	public ResponseEntity<Object> deleteUnitConversion(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		UnitConversion objUnit = objMapper.convertValue(inputMap.get("unitconversion"),
				new TypeReference<UnitConversion>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return unitConversionService.deleteUnitConversion(objUnit, userInfo);
	}

	/**
	 * This method is used to retrieve a specific unitconversion record for given
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nunitconversioncode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "nunitconversioncode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 206,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with UnitConversion object for the specified primary
	 *         key / with string message as 'Deleted' if the unitconversion record
	 *         is not available for given Site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveUnitConversionById")
	public ResponseEntity<Object> getActiveUnitConversionById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int nunitConversionCode = (int) inputMap.get("nunitconversioncode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return unitConversionService.getActiveUnitConversionById(nunitConversionCode, userInfo);
	}

}
