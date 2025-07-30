package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.service.userroletemplate.UserRoleTemplateService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Userrole Template Service methods.
 */
@RestController
@RequestMapping("/userroletemplate")
public class UserRoleTemplateController {

	private RequestContext requestContext;
	private final UserRoleTemplateService userRoleTemplateService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param unitService    UnitService
	 */
	public UserRoleTemplateController(RequestContext requestContext, UserRoleTemplateService userRoleTemplateService) {
		super();
		this.requestContext = requestContext;
		this.userRoleTemplateService = userRoleTemplateService;
	}

	@PostMapping(value = "/getApprovalSubType")
	public ResponseEntity<Object> getApprovalSubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nmoduletypecode = (int) inputMap.get("nmoduletypecode");
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getApprovalSubType(nmoduletypecode, userInfo);
	}

	@PostMapping(value = "/getApprovalRegSubType")
	public ResponseEntity<Object> getApprovalRegSubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		int nregtypecode = -1;
		final int nflag = (int) inputMap.get("nflag");
		if ((int) inputMap.get("nflag") == 2) {
			nregtypecode = (int) inputMap.get("nregtypecode");
		}
		final int isregneed = (int) inputMap.get("isregneed");
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final int ntemplatecode = (int) inputMap.get("ntemplatecode");
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getApprovalRegSubType(nflag, nregtypecode, isregneed, napprovalsubtypecode,
				ntemplatecode, userInfo);
	}

	@PostMapping(value = "/getUserRoleforTree")
	public ResponseEntity<Object> getUserRoleforTree(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getUserRoleforTree(userInfo, nregtypecode, nregsubtypecode,
				napprovalsubtypecode);
	}

	@PostMapping(value = "/getUserroletemplate")
	public ResponseEntity<Object> getUserroletemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		int nmoduletypecode = 1;
		if (inputMap.containsKey("nmoduletypecode")) {
			nmoduletypecode = (int) inputMap.get("nmoduletypecode");
		}
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getApprovalSubType(nmoduletypecode, userInfo);
	}

	@PostMapping(value = "/getUserroletemplatelist")
	public ResponseEntity<Object> getUserroletemplatelist(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getUserroletemplate(inputMap, userInfo);
	}

	@PostMapping(value = "/getUserroletemplatebyId")
	public ResponseEntity<Object> getUserroletemplatebyId(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getUserroletemplatebyId(ntreeversiontempcode, userInfo);
	}

	@PostMapping(value = "/getTreetemplate")
	public ResponseEntity<Object> getTreetemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getTreetemplate(ntreeversiontempcode, userInfo);
	}

	@PostMapping(value = "/createUserRoleTemplatemaster")
	public ResponseEntity<Object> createUserRoleTemplatemaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.createUserRoleTemplatemaster(inputMap);
	}

	@PostMapping(value = "/updateEditUserRoleTemplatemaster")
	public ResponseEntity<Object> updateEditUserRoleTemplatemaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.updateEditUserRoleTemplatemaster(inputMap);
	}

	@PostMapping(value = "/deleteUserroleTemplatemaster")
	public ResponseEntity<Object> deleteUserroleTemplatemaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int ntreetemplatecode = (int) inputMap.get("ntreetemplatecode");
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final int isregneed = (int) inputMap.get("isregneed");
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.deleteUserroleTemplatemaster(napprovalsubtypecode, ntreeversiontempcode,
				ntreetemplatecode, isregneed, nregtypecode, nregsubtypecode, userInfo);
	}

	@PostMapping(value = "/approveUserroleTemplatemaster")
	public ResponseEntity<Object> approveUserroleTemplatemaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.approveUserroleTemplatemaster(inputMap, userInfo);
	}

	@PostMapping(value = "/getFilterSubmit")
	public ResponseEntity<Object> getFilterSubmit(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleTemplateService.getUserroletemplate(inputMap, userInfo);
	}

}
