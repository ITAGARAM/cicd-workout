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
import com.agaramtech.qualis.configuration.model.ApprovalStatusConfig;
import com.agaramtech.qualis.configuration.service.approvalstatusconfig.ApprovalStatusConfigService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/approvalstatusconfig")
public class ApprovalStatusConfigController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalStatusConfigController.class);

	private RequestContext requestContext;
	private final ApprovalStatusConfigService approvalStatusConfigService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext              RequestContext to hold the request
	 * @param approvalStatusConfigService ApprovalStatusConfigService
	 */
	public ApprovalStatusConfigController(RequestContext requestContext,
			ApprovalStatusConfigService approvalStatusConfigService) {
		super();
		this.requestContext = requestContext;
		this.approvalStatusConfigService = approvalStatusConfigService;
	}

	/**
	 * This Method is used to get the over all units with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of unit with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getApprovalStatusConfig")
	public ResponseEntity<Object> getApprovalStatusConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getApprovalStatusConfig()");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.getApprovalStatusConfig(userInfo);
	}

	@PostMapping(value = "/closeFilterService")
	public ResponseEntity<Object> closeFilterService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final int nformcode = (int) inputMap.get("nformcode");
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.closeFilterService(nsampletypecode, nformcode, nregsubtypecode, nregtypecode,
				napprovalsubtypecode, userInfo);
	}

	@PostMapping(value = "/createApprovalStatusConfig")
	public ResponseEntity<Object> createApprovalStatusConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		final List<ApprovalStatusConfig> objSelecedList = objmap.convertValue(inputMap.get("approvalstatusconfig"),
				new TypeReference<List<ApprovalStatusConfig>>() {
				});
		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.createApprovalStatusConfig(objSelecedList, userInfo);
	}

	@PostMapping(value = "/getRegTypeBySampleType")
	public ResponseEntity<Object> getRegTypeBySampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.getRegTypeBySampleType(nsampletypecode, userInfo);
	}

	@PostMapping(value = "/getRegSubTypeByRegtype")
	public ResponseEntity<Object> getRegSubTypeByRegtype(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.getRegSubTypeByRegtype(nregtypecode, userInfo);
	}

	@PostMapping(value = "/getTransactionStatus")
	public ResponseEntity<Object> getTransactionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.getTransactionStatus(userInfo);
	}

	@PostMapping(value = "/getStatusFunction")
	public ResponseEntity<Object> getStatusFunction(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nformcode = (int) inputMap.get("nformcode");
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.getStatusFunction(nformcode, userInfo);
	}

	@PostMapping(value = "/getTransactionForms")
	public ResponseEntity<Object> getTransactionForms(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.getTransactionForms(nregtypecode, nregsubtypecode, userInfo);
	}

	@PostMapping(value = "/deleteApprovalStatusConfig")
	public ResponseEntity<Object> deleteApprovalStatusConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		objmap.registerModule(new JavaTimeModule());
		final ApprovalStatusConfig objApprovalStatusConfig = objmap.convertValue(inputMap.get("approvalstatusconfig"),
				ApprovalStatusConfig.class);
		final UserInfo userinfo = objmap.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userinfo);
		return approvalStatusConfigService.deleteApprovalStatusConfig(objApprovalStatusConfig, nsampletypecode,
				userinfo);
	}

	@PostMapping(value = "/getFilterSubmit")
	public ResponseEntity<Object> getFilterSubmit(@RequestBody Map<String, Object> inputMap) throws Exception {
		int nsampletypecode = -1;
		int napprovalsubtypecode = -1;
		int nregtypecode = -1;
		int nregsubtypecode = -1;
		int nformCode = -1;
		final ObjectMapper objmapper = new ObjectMapper();
		if ((inputMap.containsKey("nsampletypecode")) && (inputMap.containsKey("nregtypecode"))
				&& (inputMap.containsKey("nregsubtypecode"))) {
			nsampletypecode = (Integer) inputMap.get("nsampletypecode");
			nregtypecode = (Integer) inputMap.get("nregtypecode");
			nregsubtypecode = (Integer) inputMap.get("nregsubtypecode");
			nformCode = (Integer) inputMap.get("nformcode");
			napprovalsubtypecode = (Integer) inputMap.get("napprovalsubtypecode");
		}
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalStatusConfigService.getFilterSubmit(nsampletypecode, napprovalsubtypecode, nregtypecode,
				nregsubtypecode, nformCode, userInfo);
	}

}