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

import com.agaramtech.qualis.control.service.ControlService;
import com.agaramtech.qualis.credential.model.UserRoleScreenControl;
import com.agaramtech.qualis.credential.model.UsersRoleScreen;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the control Service methods.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 02-04-2025
 */

@RestController
@RequestMapping("/screenrights")
public class ScreenRightsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScreenRightsController.class);

	private RequestContext requestContext;
	private final ControlService objControlService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext     RequestContext to hold the request
	 * @param testpackageService testpackageService
	 */
	public ScreenRightsController(RequestContext requestContext, ControlService objControlService) {
		super();
		this.requestContext = requestContext;
		this.objControlService = objControlService;
	}

	/**
	 * This method is used to retrieve list of all/specified active screen(s) and
	 * their associated details(control and Esignature assigned for the Screen). If
	 * nuserrolescreencode = null, fetches entire screen list based on last userrole
	 * code else fetches specified Screen and their associated details
	 * 
	 * @param inputMap [Map] map object with "userinfo" as key for which the list is
	 *                 to be fetched
	 * @return response object with list of active Screen that are to be listed for
	 *         the specified site
	 */
	@PostMapping(value = "/getScreenRights")
	public ResponseEntity<Object> getScreenRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		Integer nuserrolescreencode = null;
		if (inputMap.get("nuserrolescreencode") != null) {
			nuserrolescreencode = (Integer) inputMap.get("nuserrolescreencode");
		}
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) objControlService.getScreenRights(nuserrolescreencode, userInfo);

	}

	/**
	 * This method is used to retrieve list of active screen for the specified site.
	 * 
	 * @param inputMap [Map] map object with "nuserrolecode" as key for which the
	 *                 list is to be fetched
	 * @return response object with list of active screenrights that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getScreenRightsByUserRoleCode")
	public ResponseEntity<Object> getScreenRightsByUserRoleCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objControlService.getScreenRightsByUserRoleCode(nuserrolecode, userInfo);// siteCode);

	}

	/**
	 * This method is used to retrieve list of active screen for the specified site.
	 * This method is used to retrieve list of active screen based on userrole code
	 * .
	 * 
	 * @param inputMap [Map] map object with "nuserrolecode" as key for which the
	 *                 list is to be fetched
	 * @return response object with list of active screen that are to be listed for
	 *         the specified site
	 */
	@PostMapping(value = "/getAvailableScreen")
	public ResponseEntity<Object> getAvailableScreen(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return objControlService.getAvailableScreen(nuserrolecode, userInfo);// siteCode);

	}

	/**
	 * This method is used to add new screenrights for the specified Site and
	 * specified userrole.
	 * 
	 * @param inputMap [Map] object with keys of screenrights entity.
	 * @return response entity of newly added screenrights entity
	 */
	@PostMapping(value = "/createScreenRights")
	public ResponseEntity<Object> createScreenRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objControlService.createScreenRights(inputMap);

	}

	/**
	 * This method is used to retrieve list of active control and esignature for the
	 * specified site .
	 * 
	 * @param inputMap [Map] map object with "nuserrolescreencode" as key for which
	 *                 the list is to be fetched
	 * @return response object with list of active screenrights that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getControlRights")
	public ResponseEntity<Object> getControlRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserrolescreencode = null;
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		nuserrolescreencode = (Integer) inputMap.get("nuserrolescreencode");
		requestContext.setUserInfo(userInfo);
		return objControlService.getControlRights(userInfo, nuserrolescreencode);
	}

	/**
	 * This method is used to add new controlrights for the specified Site .
	 * 
	 * @param inputMap [Map] object with keys of controlrights entity.
	 * @return response entity of newly added controlrights entity
	 */

	@PostMapping(value = "/updateControlRights")
	public ResponseEntity<Object> createControlRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		Integer nflag = null;
		UserRoleScreenControl userroleController = null;
		if (inputMap.containsKey("screenrights")) {

			userroleController = objmapper.convertValue(inputMap.get("screenrights"), UserRoleScreenControl.class);
		}
		List<UsersRoleScreen> lstusersrolescreen = (List<UsersRoleScreen>) objmapper
				.convertValue(inputMap.get("selectedscreenrights"), new TypeReference<List<UsersRoleScreen>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		nflag = (Integer) inputMap.get("nflag");

		final int nneedrights = (int) inputMap.get("nneedrights");

		requestContext.setUserInfo(userInfo);
		return objControlService.createControlRights(userInfo, userroleController, lstusersrolescreen, nflag,
				nneedrights);

	}

	/**
	 * This method is used to add new Esignature for the specified Site .
	 * 
	 * @param inputMap [Map] object with keys of Esignature entity.
	 * @return response entity of newly added Esignature entity
	 */

	@PostMapping(value = "/updateEsign")
	public ResponseEntity<Object> createEsign(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		Integer nflag = null;
		final UserRoleScreenControl userRoleScreenControl = objmapper.convertValue(inputMap.get("screenrights"),
				UserRoleScreenControl.class);
		final List<UsersRoleScreen> lstusersrolescreen = (List<UsersRoleScreen>) objmapper
				.convertValue(inputMap.get("selectedscreenrights"), new TypeReference<List<UsersRoleScreen>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nneedesign = (int) inputMap.get("nneedesign");
		nflag = (Integer) inputMap.get("nflag");

		requestContext.setUserInfo(userInfo);
		return objControlService.createEsign(userInfo, userRoleScreenControl, lstusersrolescreen, nflag, nneedesign);

	}

	/**
	 * This method is used to delete screen for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of screenrights entity.
	 * @return response entity of deleted screenrights entity
	 */

	@PostMapping(value = "/deleteScreenRights")
	public ResponseEntity<Object> deleteScreenRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		Integer nuserrolecode = null;

		final List<UsersRoleScreen> lstusersrolescreen = (List<UsersRoleScreen>) objmapper
				.convertValue(inputMap.get("screenrights"), new TypeReference<List<UsersRoleScreen>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		nuserrolecode = (Integer) inputMap.get("nuserrolecode");

		requestContext.setUserInfo(userInfo);
		return objControlService.deleteScreenRights(lstusersrolescreen, userInfo, nuserrolecode);

	}

	/**
	 * This method is used to retrieve list of active screen for the specified site.
	 * 
	 * @param inputMap [Map] object is used to retrieve list of active screen based
	 *                 on lstusersrolescreen .
	 * @return response object with list of active screen that are to be listed for
	 *         the specified site
	 */

	@PostMapping(value = "/getSingleSelectScreenRights")
	public ResponseEntity<Object> getSingleSelectScreenRights(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());

		final List<UsersRoleScreen> lstusersrolescreen = (List<UsersRoleScreen>) objmapper
				.convertValue(inputMap.get("screenrights"), new TypeReference<List<UsersRoleScreen>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nuserrolecode = (int) inputMap.get("nuserrolecode");

		requestContext.setUserInfo(userInfo);
		return objControlService.getSingleSelectScreenRights(lstusersrolescreen, userInfo, nuserrolecode);

	}

	/**
	 * This method is used to retrieve list of active userrole for the specified
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nuserrolecode" as key for which the
	 *                 list is to be fetched
	 * @return response object with list of active userrole that are to be listed
	 *         for the specified site
	 */
	@PostMapping(value = "/getUserRole")
	public ResponseEntity<Object> getUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserrolecode = null;
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		nuserrolecode = (Integer) inputMap.get("nuserrolecode");

		requestContext.setUserInfo(userInfo);
		return objControlService.getUserRole(nuserrolecode, userInfo);

	}

	/**
	 * This method is used to copy screen for selected userrole .
	 * 
	 * @param inputMap [Map] object with keys of userrole entity.
	 * @return response object with list of active userrole that are to be listed
	 *         for the specified site
	 */
	@PostMapping(value = "/copyScreenRights")
	public ResponseEntity<Object> CopyScreenRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objControlService.CopyScreenRights(inputMap);

	}

	/**
	 * This method is used to retrieve list of active screen for the specified site
	 * .
	 * 
	 * @param inputMap [Map] map object with "nuserrolecode" as key for which the
	 *                 list is to be fetched.
	 * @return response object with list of active screen that are to be listed for
	 *         the specified site
	 */
	@PostMapping(value = "/getCopyUserRoleCode")
	public ResponseEntity<Object> getCopyUserRoleCode(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return objControlService.getCopyUserRoleCode(nuserrolecode, userInfo);// siteCode);

	}

	@PostMapping(value = "/getSearchScreenRights")
	public ResponseEntity<Object> getSearchScreenRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		String nuserrolescreencode = null;
		if (inputMap.get("nuserrolescreencode") != null) {
			nuserrolescreencode = (String) inputMap.get("nuserrolescreencode");
		}
		return (ResponseEntity<Object>) objControlService.getSearchScreenRights(nuserrolescreencode, objUserInfo);

	}
}
