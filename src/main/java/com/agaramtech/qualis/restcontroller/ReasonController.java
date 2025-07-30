package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.model.Reason;
import com.agaramtech.qualis.basemaster.service.reason.ReasonService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/reason")
public class ReasonController {

	private final ReasonService reasonService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param reasonService  ReasonService
	 */
	public ReasonController(RequestContext requestContext, ReasonService reasonService) {
		super();
		this.requestContext = requestContext;
		this.reasonService = reasonService;
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return reasonService.getReason(userInfo);

	}

	/**
	 * This method will is used to make a new entry to unit table.
	 * 
	 * @param inputMap map object holding params ( reason [Reason] object holding
	 *                 details to be added in reason table, userinfo [UserInfo]
	 *                 holding logged in user details ) Input:{ "reason": {
	 *                 "sreasonname": "cm"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 162,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the reason
	 *         already exists/ list of units along with the newly added reason.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createReason")
	public ResponseEntity<Object> createReason(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Reason reason = objmapper.convertValue(inputMap.get("reason"), Reason.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reasonService.createReason(reason, userInfo);

	}

	/**
	 * This method is used to update selected reason details.
	 * 
	 * @param inputMap [map object holding params( reason [Reason] object holding
	 *                 details to be updated in reason table, userinfo [UserInfo]
	 *                 holding logged in user details) Input:{ "reason":
	 *                 {"nreasoncode":1,"sreasonname": "m", "sdescription": "m",
	 *                 "ndefaultstatus": 3 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 162,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
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
	 * @return ResponseEntity with string message as 'Already Deleted' if the reason
	 *         record is not available/ list of all units and along with the updated
	 *         unit.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateReason")
	public ResponseEntity<Object> updateReason(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Reason reason = objmapper.convertValue(inputMap.get("reason"), Reason.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reasonService.updateReason(reason, userInfo);

	}

	/**
	 * This method is used to retrieve a specific reason record.
	 * 
	 * @param inputMap [Map] map object with "nreasoncode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "nreasoncode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 162,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with reason object for the specified primary key /
	 *         with string message as 'Deleted' if the reason record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveReasonById")
	public ResponseEntity<Object> getActiveReasonById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nreasoncode = (Integer) inputMap.get("nreasoncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reasonService.getActiveReasonById(nreasoncode, userInfo);

	}

	/**
	 * This method is used to delete an entry in reason table
	 * 
	 * @param inputMap [Map] object with keys of reason entity and UserInfo object.
	 *                 Input:{ "reason": {"nreasoncode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 162,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the reason
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the reason is associated in transaction / list of all reasons
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteReason")
	public ResponseEntity<Object> deleteReason(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Reason reason = objmapper.convertValue(inputMap.get("reason"), Reason.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reasonService.deleteReason(reason, userInfo);

	}

}
