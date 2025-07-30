package com.agaramtech.qualis.restcontroller;
 
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.project.model.Disease;
import com.agaramtech.qualis.project.service.disease.DiseaseService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class is used to perform CRUD Operation on "Disease" table by
 * implementing methods from its interface.
 * 
 * 
 * @author Mullai Balaji.V 
 * BGSI-2
 * 26/06/2025 
 * @version 
 */

@RestController
@RequestMapping("/disease")
public class DiseaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DiseaseController.class);

	private RequestContext requestContext;
	private final DiseaseService diseaseService;

	public DiseaseController(RequestContext requestContext, DiseaseService diseaseService) {
		super();
		this.requestContext = requestContext;
		this.diseaseService = diseaseService;
	}

	/**
	 * This Method is used to get the over all disease with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of disease with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getDisease")
	public ResponseEntity<Object> getDisease(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getDisease called");
		requestContext.setUserInfo(userInfo);

		return diseaseService.getDisease(userInfo);

	}

	/**
	 * This method will is used to make a new entry to disease table.
	 * 
	 * @param inputMap map object holding params ( disease [Disease] object holding
	 *                 details to be added in disease table, userinfo [UserInfo]
	 *                 holding logged in user details ) Input:{ "disease": {
	 *                 "sdiseasename": "corono" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 254,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the disease
	 *         already exists/ list of diseases along with the newly added disease.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createDisease")
	public ResponseEntity<Object> createDisease(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final Disease objDisease = objMapper.convertValue(inputMap.get("disease"), new TypeReference<Disease>() {
		});

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return diseaseService.createDisease(objDisease, userInfo);
	}

	/**
	 * This method is used to update selected disease details.
	 * 
	 * @param inputMap [map object holding params( disease [disease] object holding
	 *                 details to be updated in disease table, userinfo [UserInfo]
	 *                 holding logged in user details) Input:{ "disease":
	 *                 {"ndiseasecode":1,"sdiseasename": "m","sdiseasesynonym": "m",
	 *                 "sdescription": "m", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 254,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         disease record is not available/ list of all diseases and along with
	 *         the updated disease.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateDisease")
	public ResponseEntity<Object> updateDisease(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Disease objDisease = objMapper.convertValue(inputMap.get("disease"), new TypeReference<Disease>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return diseaseService.updateDisease(objDisease, userInfo);
	}

	/**
	 * This method is used to delete an entry in disease table
	 * 
	 * @param inputMap [Map] object with keys of disease entity and UserInfo object.
	 *                 Input:{ "disease": {"ndiseasecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 254,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         disease record is not available/ string message as 'Record is used
	 *         in....' when the disease is associated in transaction / list of all
	 *         diseases excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteDisease")
	public ResponseEntity<Object> deleteDisease(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Disease objDisease = objMapper.convertValue(inputMap.get("disease"), new TypeReference<Disease>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return diseaseService.deleteDisease(objDisease, userInfo);
	}

	/**
	 * This method is used to retrieve a specific disease record.
	 * 
	 * @param inputMap [Map] map object with "ndiseasecode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "ndiseasecode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 254,"nmastersitecode":
	 *                 -1,"nmodulecode": 1, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with disease object for the specified primary key /
	 *         with string message as 'Deleted' if the disease record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveDiseaseById")
	public ResponseEntity<Object> getActiveDiseaseById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int ndiseaseCode = (int) inputMap.get("ndiseasecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return diseaseService.getActiveDiseaseById(ndiseaseCode, userInfo);
	}

}
