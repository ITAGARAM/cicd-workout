package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.configuration.model.CommonHolidays;
import com.agaramtech.qualis.configuration.model.HolidayYearVersion;
import com.agaramtech.qualis.configuration.model.HolidaysYear;
import com.agaramtech.qualis.configuration.model.PublicHolidays;
import com.agaramtech.qualis.configuration.model.UserBasedHolidays;
import com.agaramtech.qualis.configuration.service.holidayplanner.HolidayPlannerService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Holiday planner Service methods.
 */

@RestController
@RequestMapping("/holidayplanner")
public class HolidayPlannerController {
	
	private static final Log LOGGER = LogFactory.getLog(HolidayPlannerController.class);
	
	private final RequestContext requestContext;
	private final HolidayPlannerService holidayPlannerService;
	
	public HolidayPlannerController(RequestContext requestContext, HolidayPlannerService holidayPlannerService) {
		super();
		this.requestContext = requestContext;
		this.holidayPlannerService = holidayPlannerService;
	}

	/**
	 * This method is used to retrieve list of all active Holidaysyear(s) and their
	 * associated details (Year Version, Common Holidays and Public Holidays
	 * details)for specific site.
	 * 
	 * @param inputMap userInfo
	 * @return response object with list of active Holidaysyear that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getHolidayPlanner")
	public ResponseEntity<Object> getHolidayPlanner(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nyearcode = 0;
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getHolidayPlanner(userInfo, nyearcode);
	}

	/**
	 * This method is used to retrieve list of all active Holidaysyear(s) and their
	 * associated details (Year Version, Common Holidays and Public Holidays
	 * details)for specific selected year.
	 * 
	 * @param inputMap userInfo
	 * @return response object with list of active Holidaysyear that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getSelectionAllHolidayPlanner")
	public ResponseEntity<Object> getSelectionAllHolidayPlanner(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nyearcode = (Integer) inputMap.get("nyearcode");
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getSampleAppearance -->");
		return holidayPlannerService.getSelectionAllHolidayPlanner(userInfo, nyearcode, 0);

	}

	/**
	 * This method is used to retrieve selected active holidaysyear detail.
	 * 
	 * @param inputMap [Map] map object with "nyearcode" and "userInfo" as keys for
	 *                 which the data is to be fetched
	 * @return response object with selected active holidaysyear
	 */
	@PostMapping(value = "/getYearByID")
	public ResponseEntity<Object> getYearByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nyearcode = (Integer) inputMap.get("nyearcode");
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getYearByID(nyearcode, userInfo);

	}

	/**
	 * This method is used to retrieve selected active holidayyearversion detail.
	 * 
	 * @param inputMap [Map] map object with "nyearcode", "nholidayyearversion" and
	 *                 "userInfo" as keys for which the data is to be fetched
	 * @return response object with selected active holidayyearversion
	 */
	@PostMapping(value = "/getYearVersionByID")
	public ResponseEntity<Object> getYearVersionByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nyearcode = (Integer) inputMap.get("nyearcode");
		final int nholidayYearVersion = (Integer) inputMap.get("nholidayyearversion");
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getYearVersionByID(nholidayYearVersion, nyearcode, userInfo);
	}

	/**
	 * This method is used to retrieve selected active commonholidays detail.
	 * 
	 * @param inputMap [Map] map object with "nyearcode", "nholidayyearversion",
	 *                 "ncommonholidaycode" and "userInfo" as keys for which the
	 *                 data is to be fetched
	 * @return response object with selected active commonholidays
	 */
	@PostMapping(value = "/getCommonHolidaysByID")
	public ResponseEntity<Object> getCommonHolidaysByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncommonholidaycode = (Integer) inputMap.get("ncommonholidaycode");
		final int nholidayYearVersion = (Integer) inputMap.get("nholidayYearVersion");
		final int nyearcode = (Integer) inputMap.get("nyearcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getCommonHolidaysByID(ncommonholidaycode, nholidayYearVersion, nyearcode,
				userInfo);
	}

	/**
	 * This method is used to retrieve selected active publicholidays detail.
	 * 
	 * @param inputMap [Map] map object with "nyearcode", "nholidayyearversion",
	 *                 "npublicholidaycode" and "userInfo" as keys for which the
	 *                 data is to be fetched
	 * @return response object with selected active publicholidays
	 */
	@PostMapping(value = "/getPublicHolidaysByID")
	public ResponseEntity<Object> getPublicHolidaysByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int npublicholidaycode = (Integer) inputMap.get("npublicholidaycode");
		final int nholidayYearVersion = (Integer) inputMap.get("nholidayYearVersion");
		final int nyearcode = (Integer) inputMap.get("nyearcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getPublicHolidaysByID(npublicholidaycode, nholidayYearVersion, nyearcode,
				userInfo);
	}

	/**
	 * This method is used to retrieve list of selected active common and
	 * publicholidays detail.
	 * 
	 * @param inputMap [Map] map object with "nyearcode" and "nholidayyearversion"
	 *                 as keys for which the data is to be fetched
	 * @return response object with selected active common and publicholidays lists.
	 */
	@PostMapping(value = "/getSelectedCommonAndPublicHolidays")
	public ResponseEntity<Object> getSelectedCommonAndPublicHolidays(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nholidayYearVersion = (Integer) inputMap.get("nholidayYearVersion");
		final int nyearcode = (Integer) inputMap.get("nyearcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getSelectedCommonAndPublicHolidays(nyearcode, nholidayYearVersion, userInfo);
	}

	/**
	 * This method is used to add new holidaysyear for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "holidaysyear" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added holidaysyear entity
	 */
	@PostMapping(value = "/createHolidaysYear")
	public ResponseEntity<Object> createHolidayYear(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final HolidaysYear holidaysYear = objmapper.convertValue(inputMap.get("holidaysyear"), HolidaysYear.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.createHolidayYear(holidaysYear, userInfo);
	}

	/**
	 * This method is used to update holidaysyear for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "holidaysyear" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added holidaysyear entity
	 */
	@PostMapping(value = "/updateHolidaysYear")
	public ResponseEntity<Object> updateHolidayYear(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final HolidaysYear holidaysYear = objmapper.convertValue(inputMap.get("holidaysyear"), HolidaysYear.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.updateHolidayYear(holidaysYear, userInfo);
	}

	/**
	 * This method is used to delete holidaysyear for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "holidaysyear" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added holidaysyear entity
	 */
	@PostMapping(value = "/deleteHolidaysYear")
	public ResponseEntity<Object> deleteHolidayYear(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final HolidaysYear holidaysYear = objmapper.convertValue(inputMap.get("holidaysyear"), HolidaysYear.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.deleteHolidayYear(holidaysYear, userInfo);
	}

	/**
	 * This method is used to approve holidayyearversion for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "holidayyearversion" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added holidayyearversion entity
	 */
	@PostMapping(value = "/approveHolidayYearVersion")
	public ResponseEntity<Object> ApproveYearVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final HolidayYearVersion holidayYearVersion = objmapper.convertValue(inputMap.get("holidayyearversion"),
				HolidayYearVersion.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.ApproveYearVersion(holidayYearVersion, userInfo);
	}

	/**
	 * This method is used to add new holidayyearversion for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "holidayyearversion" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added holidayyearversion entity
	 */
	@PostMapping(value = "/createHolidayYearVersion")
	public ResponseEntity<Object> createYearVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final HolidayYearVersion holidayYearVersion = objmapper.convertValue(inputMap.get("holidayyearversion"),
				HolidayYearVersion.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.createYearVersion(holidayYearVersion, userInfo);
	}

	/**
	 * This method is used to delete holidayyearversion for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "holidayyearversion" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added holidayyearversion entity
	 */
	@PostMapping(value = "/deleteHolidayYearVersion")
	public ResponseEntity<Object> deleteYearVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final HolidayYearVersion holidayYearVersion = objmapper.convertValue(inputMap.get("holidayyearversion"),
				HolidayYearVersion.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.deleteYearVersion(holidayYearVersion, userInfo);
	}

	/**
	 * This method is used to update commonholidays for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "commonholidays" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added commonholidays entity
	 */
	@PostMapping(value = "/updateCommonHolidays")
	public ResponseEntity<Object> updateCommonHolidays(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CommonHolidays commonHolidays = objmapper.convertValue(inputMap.get("commonholidays"),
				CommonHolidays.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.updateCommonHolidays(commonHolidays, userInfo);
	}

	/**
	 * This method is used to add new publicholidays for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "publicholidays" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added publicholidays entity
	 */
	@PostMapping(value = "/createPublicHolidays")
	public ResponseEntity<Object> createPublicHolidays(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final PublicHolidays publicHolidays = objmapper.convertValue(inputMap.get("publicholidays"),
				PublicHolidays.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.createPublicHolidays(publicHolidays, userInfo);
	}

	/**
	 * This method is used to update publicholidays for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "publicholidays" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added publicholidays entity
	 */
	@PostMapping(value = "/updatePublicHolidays")
	public ResponseEntity<Object> updatePublicHolidays(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final PublicHolidays publicHolidays = objmapper.convertValue(inputMap.get("publicholidays"),
				PublicHolidays.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.updatePublicHolidays(publicHolidays, userInfo);
	}

	/**
	 * This method is used to delete publicholidays for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "publicholidays" and "userinfo"
	 *                 entities.
	 * @return response entity of newly added publicholidays entity
	 */
	@PostMapping(value = "/deletePublicHolidays")
	public ResponseEntity<Object> deletePublicHolidays(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final PublicHolidays publicHolidays = objmapper.convertValue(inputMap.get("publicholidays"),
				PublicHolidays.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.deletePublicHolidays(publicHolidays, userInfo);
	}

	@PostMapping(value = "/createUserBasedHolidays")
	public ResponseEntity<Object> createUserBasedHolidays(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final UserBasedHolidays userBasedHolidays = objmapper.convertValue(inputMap.get("userbasedholidays"),
				UserBasedHolidays.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.createUserBasedHolidays(userBasedHolidays, userInfo);
	}

	@PostMapping(value = "/deleteUserBasedHoliday")
	public ResponseEntity<Object> deleteUserBasedHolidays(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final UserBasedHolidays userBasedHolidays = objmapper.convertValue(inputMap.get("userbasedholiday"),
				UserBasedHolidays.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.deleteUserBasedHolidays(userBasedHolidays, userInfo);
	}

	@PostMapping(value = "/updateUserBasedHolidays")
	public ResponseEntity<Object> updateUserBasedHolidays(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final UserBasedHolidays userBasedHolidays = objmapper.convertValue(inputMap.get("userbasedholidays"),
				UserBasedHolidays.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.updateUserBasedHolidays(userBasedHolidays, userInfo);
	}

	@PostMapping(value = "/getUserBasedHolidaysByID")
	public ResponseEntity<Object> getUserBasedHolidaysByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nuserbasedholidaycode = (Integer) inputMap.get("nuserbasedholidaycode");
		final int nholidayYearVersion = (Integer) inputMap.get("nholidayYearVersion");
		final int nyearcode = (Integer) inputMap.get("nyearcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getUserBasedHolidaysByID(nuserbasedholidaycode, nholidayYearVersion, nyearcode,
				userInfo);
	}

	@PostMapping(value = "/getUsers")
	public ResponseEntity<Object> getUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return holidayPlannerService.getUsers(userInfo);
	}
}
