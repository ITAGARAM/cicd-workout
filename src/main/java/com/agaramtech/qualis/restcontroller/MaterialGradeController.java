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
import com.agaramtech.qualis.materialmanagement.service.materialgrade.MaterialGradeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the MaterialGrade Service methods.
 * 
 * @author ATE203
 * @version 9.0.0.1
 * @since 27- July- 2022
 */
@RestController
@RequestMapping("/materialgrade")
public class MaterialGradeController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialGradeController.class);
	private RequestContext requestContext;
	private final MaterialGradeService materialgradeService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext       RequestContext to hold the request
	 * @param materialgradeService MaterialGradeService
	 */
	public MaterialGradeController(RequestContext requestContext, MaterialGradeService materialgradeService) {
		super();
		this.requestContext = requestContext;
		this.materialgradeService = materialgradeService;
	}

	/**
	 * This method is used to retrieve list of available materialgrade.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         materialgrades
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getMaterialGrade")
	public ResponseEntity<Object> getMaterialGrade(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		LOGGER.info("At getMaterialGrade EndPoint");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return materialgradeService.getMaterialGrade(userInfo);
	}

	/**
	 * This method will is used to make a new entry to materialgrade table.
	 * 
	 * @param inputMap map object holding params ( materialgrade [MaterialGrade]
	 *                 object holding details to be added in materialgrade table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "materialgrade": { "smaterialgradename": "cm",
	 *                 "smaterialgradesynonym": "cm" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 156,"nmastersitecode": -1,"nmodulecode": 8,
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
	 *         materialgrade already exists/ list of materialgrades along with the
	 *         newly added materialgrade.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createMaterialGrade")
	public ResponseEntity<Object> createMaterialGrade(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialgradeService.createMaterialGrade(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve a specific materialgrade record.
	 * 
	 * @param inputMap [Map] map object with "nmaterialgradenitcode" and "userinfo"
	 *                 as keys for which the data is to be fetched Input:{
	 *                 "nmaterialgradecode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 156,"nmastersitecode": -1,"nmodulecode": 8, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with materialgrade object for the specified primary
	 *         key / with string message as 'Deleted' if the materialgraderecord is
	 *         not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveMaterialGradeById")
	public ResponseEntity<Object> getActiveMaterialGradeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmaterialgradecode = (Integer) inputMap.get("nmaterialgradecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialgradeService.getActiveMaterialGradeById(nmaterialgradecode, userInfo);
	}

	/**
	 * This method is used to update selected materialgrade details.
	 * 
	 * @param inputMap [map object holding params( materialgrade [MaterialGrade]
	 *                 object holding details to be updated in materialgrade table,
	 *                 userinfo [UserInfo] holding logged in user details) Input:{
	 *                 "materialgrade":
	 *                 {"nmaterialgradecode":1,"smaterialgradename":
	 *                 "m","smaterialgradesynonym": "m", "sdescription": "m",
	 *                 "ndefaultstatus": 3 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 156,"nmastersitecode": -1, "nmodulecode": 8, "nreasoncode":
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
	 * @return ResponseEntity with string message as 'Already Deleted' if the unit
	 *         record is not available/ list of all units and along with the updated
	 *         unit.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateMaterialGrade")
	public ResponseEntity<Object> updateMaterialGrade(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialgradeService.updateMaterialGrade(inputMap, userInfo);
	}

	/**
	 * This method is used to delete an entry in materialgrade table
	 * 
	 * @param inputMap [Map] object with keys of materialgrade entity and UserInfo
	 *                 object. Input:{ "materialgrade ": {"nmaterialgrade code":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 156,"nmastersitecode": -1, "nmodulecode": 8,
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
	 *         materialgrade record is not available/ string message as 'Record is
	 *         used in....' when the materialgrade is associated in transaction /
	 *         list of all materialgrade s excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteMaterialGrade")
	public ResponseEntity<Object> deleteMaterialGrade(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialgradeService.deleteMaterialGrade(inputMap, userInfo);
	}
}
