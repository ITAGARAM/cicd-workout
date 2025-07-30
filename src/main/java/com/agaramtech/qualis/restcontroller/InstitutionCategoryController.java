package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;
import com.agaramtech.qualis.submitter.service.institutioncategory.InstitutionCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the InstitutionCategory Service methods.
 */
@RestController
@RequestMapping("/institutioncategory")
public class InstitutionCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionCategoryController.class);
	private RequestContext requestContext;
	private final InstitutionCategoryService institutioncategoryService;
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext             RequestContext to hold the request
	 * @param InstitutionCategoryService InstitutionCategoryService
	 */
	public InstitutionCategoryController(RequestContext requestContext, InstitutionCategoryService institutioncategoryService) {
		super();
		this.requestContext = requestContext;
		this.institutioncategoryService = institutioncategoryService;
	}

	/**
	 * This method is used to retrieve list of available InstitutionCategorys.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         InstitutionCategorys
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getInstitutionCategory")
	public ResponseEntity<List<InstitutionCategory>> getInstitutionCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("In getInstitutionCategory EndPoint");
		return institutioncategoryService.getInstitutionCategory(userInfo);
	}

	/**
	 * This method will is used to make a new entry to InstitutionCategory table.
	 * 
	 * @param inputMap map object holding params ( InstitutionCategory
	 *                 [InstitutionCategory] object holding details to be added in
	 *                 InstitutionCategory table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "InstitutionCategory": {
	 *                 "sInstitutionCategoryname": "Medical Institute",
	 *                 "sInstitutionCategorysynonym": "Medical Institute" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 151,"nmastersitecode": -1,"nmodulecode": 70,
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
	 *         InstitutionCategory already exists/ list of InstitutionCategorys
	 *         along with the newly added InstitutionCategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createInstitutionCategory")
	public ResponseEntity<? extends Object> createInstitutionCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final InstitutionCategory institutioncategory = objMapper.convertValue(inputMap.get("institutioncategory"),
				InstitutionCategory.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return institutioncategoryService.createInstitutionCategory(institutioncategory, userInfo);
	}

	/**
	 * This method is used to retrieve a specific InstitutionCategory record.
	 * 
	 * @param inputMap [Map] map object with "nInstitutionCategorycode" and
	 *                 "userinfo" as keys for which the data is to be fetched
	 *                 Input:{ "nInstitutionCategorycode": 1, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 151,"nmastersitecode": -1,"nmodulecode": 70,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with InstitutionCategory object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         InstitutionCategory record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveInstitutionCategoryById")
	public ResponseEntity<Object> getActiveInstitutionCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninstitutioncatcode = (int) inputMap.get("ninstitutioncatcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutioncategoryService.getActiveInstitutionCategoryById(ninstitutioncatcode, userInfo);
	}

	/**
	 * This method is used to update selected InstitutionCategory details.
	 * 
	 * @param inputMap [map object holding params( InstitutionCategory
	 *                 [InstitutionCategory] object holding details to be updated in
	 *                 InstitutionCategory table, userinfo [UserInfo] holding logged
	 *                 in user details) Input:{ "InstitutionCategory":
	 *                 {"nInstitutionCategorycode":1,"sInstitutionCategoryname":
	 *                 "m","sInstitutionCategorysynonym": "m", "sdescription": "m",
	 *                 "ndefaultstatus": 3 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 151,"nmastersitecode": -1, "nmodulecode": 70, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         InstitutionCategory record is not available/ list of all
	 *         InstitutionCategorys and along with the updated InstitutionCategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateInstitutionCategory")
	public ResponseEntity<? extends Object> updateInstitutionCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final InstitutionCategory institutioncategory = objMapper.convertValue(inputMap.get("institutioncategory"),
				InstitutionCategory.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return institutioncategoryService.updateInstitutionCategory(institutioncategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in InstitutionCategory table
	 * 
	 * @param inputMap [Map] object with keys of InstitutionCategory entity and
	 *                 UserInfo object. Input:{ "InstitutionCategory":
	 *                 {"nInstitutionCategorycode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 151,"nmastersitecode": -1, "nmodulecode": 70,
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
	 *         InstitutionCategory record is not available/ string message as
	 *         'Record is used in....' when the InstitutionCategory is associated in
	 *         transaction / list of all InstitutionCategorys excluding the deleted
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteInstitutionCategory")
	public ResponseEntity<? extends Object> deleteInstitutionCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final InstitutionCategory institutioncategory = objMapper.convertValue(inputMap.get("institutioncategory"),
				InstitutionCategory.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return institutioncategoryService.deleteInstitutionCategory(institutioncategory, userInfo);
	}

}
