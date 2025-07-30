package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.credential.model.UserRoleScreenControlHide;
import com.agaramtech.qualis.credential.model.UsersRoleScreenHide;
import com.agaramtech.qualis.credential.service.usersrolescreenhide.UsersRoleScreenHideService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/userscreenhide")
public class UsersRoleScreenHideController {

	private RequestContext requestContext;
	private final UsersRoleScreenHideService usersRoleScreenHideService;

	public UsersRoleScreenHideController(RequestContext requestContext,
			UsersRoleScreenHideService usersRoleScreenHideService) {
		super();
		this.requestContext = requestContext;
		this.usersRoleScreenHideService = usersRoleScreenHideService;
	}

	@PostMapping(value = "/getUserScreenhide")
	public ResponseEntity<Object> getUserScreenhide(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		Integer nuserrolescreencode = null;
		if (inputMap.get("nuserrolescreencode") != null) {
			nuserrolescreencode = (Integer) inputMap.get("nuserrolescreencode");
		}
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) usersRoleScreenHideService.getUserScreenhide(nuserrolescreencode, userInfo);
	}

	@PostMapping(value = "/getUserScreenhidecomboo")
	public ResponseEntity<Object> getUserScreenhidecomboo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final Integer nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return (ResponseEntity<Object>) usersRoleScreenHideService.getUserScreenhidecomboo(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/getAvailableUserScreenhide")
	public ResponseEntity<Object> getAvailableUserScreenhide(@RequestBody Map<String, Object> inputMap)	throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		Integer nusercode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		if (inputMap.get("nusercode") != null) {
			nusercode = (Integer) inputMap.get("nusercode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return usersRoleScreenHideService.getAvailableUserScreenhide(nusercode, nuserrolecode, userInfo);// siteCode);
	}

	@PostMapping(value = "/createUserScreenhide")
	public ResponseEntity<Object> createUserScreenhide(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersRoleScreenHideService.createUserScreenhide(inputMap);
	}

	@PostMapping(value = "/deleteUserScreenhide")
	public ResponseEntity<Object> deleteUserScreenhide(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserrolecode = null;
		Integer nusercode = null;
		final List<UsersRoleScreenHide> lstusersrolescreen = (List<UsersRoleScreenHide>) objmapper
				.convertValue(inputMap.get("screenrights"), new TypeReference<List<UsersRoleScreenHide>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		nusercode = (Integer) inputMap.get("usercode");
		requestContext.setUserInfo(userInfo);
		return usersRoleScreenHideService.deleteUserScreenhide(lstusersrolescreen, userInfo, nuserrolecode,
				nusercode);
	}

	@PostMapping(value = "/getSingleSelectUserScreenhide")
	public ResponseEntity<Object> getSingleSelectUserScreenhide(@RequestBody Map<String, Object> inputMap)throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<UsersRoleScreenHide> lstusersrolescreen = (List<UsersRoleScreenHide>) objmapper
				.convertValue(inputMap.get("screenrights"), new TypeReference<List<UsersRoleScreenHide>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String userRoleScreenCode = (String) inputMap.get("nuserrolescreencode");
		requestContext.setUserInfo(userInfo);
		return usersRoleScreenHideService.getSingleSelectUserScreenhide(lstusersrolescreen, userInfo,
				userRoleScreenCode, inputMap);
	}

	@PostMapping(value = "/updateScreenHideControlRights")
	public ResponseEntity<Object> updateScreenHideControlRights(@RequestBody Map<String, Object> inputMap)throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		Integer nflag = null;
		Integer nneedrights = null;
		UserRoleScreenControlHide userroleController = null;
		if (inputMap.containsKey("screenrights")) {
			userroleController = objmapper.convertValue(inputMap.get("screenrights"), UserRoleScreenControlHide.class);
		}
		final List<UsersRoleScreenHide> lstusersrolescreen = (List<UsersRoleScreenHide>) objmapper
				.convertValue(inputMap.get("selectedscreenrights"), new TypeReference<List<UsersRoleScreenHide>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		nflag = (Integer) inputMap.get("nflag");
		if (inputMap.get("nneedesign") == null) {
			nneedrights = (int) inputMap.get("needrights");
		} else {
			nneedrights = (int) inputMap.get("nneedesign");
		}
		requestContext.setUserInfo(userInfo);
		return usersRoleScreenHideService.updateScreenHideControlRights(userInfo, userroleController,
				lstusersrolescreen, nflag, nneedrights, inputMap);
	}

	@PostMapping(value = "/updateListControlRights")
	public ResponseEntity<Object> updateListControlRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nneedrights = (int) inputMap.get("needrights");
		final int nhidescreencode = (int) inputMap.get("nusersrolehidescreencode");
		return usersRoleScreenHideService.updateListControlRights(nneedrights, nhidescreencode, inputMap);
	}

	@PostMapping(value = "/getSearchScreenHide")
	public ResponseEntity<Object> getSearchScreenHide(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		String nuserrolescreencode = null;
		int nusercode = 0;
		if (inputMap.get("nformcode") != null) {
			nuserrolescreencode = (String) inputMap.get("nformcode");
		}
		if (inputMap.get("nusercode") != null) {
			nusercode = (int) inputMap.get("nusercode");
		}
		return (ResponseEntity<Object>) usersRoleScreenHideService.getSearchScreenHide(nuserrolescreencode,
				objUserInfo, nusercode);
	}

}
