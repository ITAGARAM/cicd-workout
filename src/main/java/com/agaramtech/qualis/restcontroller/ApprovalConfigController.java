package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.service.approvalconfig.ApprovalConfigService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Approval Configuration Service methods.
 */

@RestController
@RequestMapping(value = "/approvalconfig")
public class ApprovalConfigController {

	private final ApprovalConfigService approvalConfigService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext        RequestContext to hold the request
	 * @param approvalConfigService ApprovalConfigService
	 */
	public ApprovalConfigController(RequestContext requestContext, ApprovalConfigService approvalConfigService) {
		super();
		this.requestContext = requestContext;
		this.approvalConfigService = approvalConfigService;
	}

	@PostMapping(value = "/getApprovalConfigFilter")
	public ResponseEntity<Object> getApprovalConfigFilter(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getApprovalConfigFilter(inputMap, userInfo);
	}

	@PostMapping(value = "/getApprovalConfigVersion", headers = "Accept=application/json")
	public ResponseEntity<Object> getApprovalConfigVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getApprovalConfigVersion(inputMap, userInfo);
	}

	@PostMapping(value = "/getApprovalConfigRoleDetails", headers = "Accept=application/json")
	public ResponseEntity<Object> getApprovalConfigRoleDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		int napprovalConfigRoleCode = (int) inputMap.get("napprovalconfigrolecode");
		int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getApprovalConfigRoleDetails(napprovalConfigRoleCode, napprovalsubtypecode,
				userInfo);
	}

	@PostMapping(value = "/getUserRoleApprovalConfig", headers = "Accept=application/json")
	public ResponseEntity<Object> getUserRoleApprovalConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getUserRoleApprovalConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/getAvailableComboData", headers = "Accept=application/json")
	public ResponseEntity<Object> getAvailableComboData(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getAvailableComboData(napprovalsubtypecode, userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/createApprovalConfigVersion", headers = "Accept=application/json")
	public ResponseEntity<Object> createApprovalConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.createApprovalConfig((Map<String, Object>) inputMap.get("approvalconfigversion"));
	}

	@PostMapping(value = "/getApprovalConfigEditData", headers = "Accept=application/json")
	public ResponseEntity<Object> getApprovalConfigEditData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getApprovalConfigEditData(inputMap);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/updateApprovalConfigVersion", headers = "Accept=application/json")
	public ResponseEntity<Object> updateApprovalConfigVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService
				.updateApprovalConfigVersion((Map<String, Object>) inputMap.get("approvalconfigversion"), userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deleteApprovalConfigVersion", headers = "Accept=application/json")
	public ResponseEntity<Object> deleteApprovalConfigVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService
				.deleteApprovalConfigVersion((Map<String, Object>) inputMap.get("approvalconfigversion"), userInfo);
	}

	@PostMapping(value = "/updateapproveApprovalConfigVersion", headers = "Accept=application/json")
	public ResponseEntity<Object> approveApprovalConfigVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.approveApprovalConfigVersion(inputMap, userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/copyApprovalConfigVersion", headers = "Accept=application/json")
	public ResponseEntity<Object> copyApprovalConfigVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService
				.copyApprovalConfigVersion((Map<String, Object>) inputMap.get("approvalconfigversion"), userInfo);
	}

	@PostMapping(value = "/getCopyRegType", headers = "Accept=application/json")
	public ResponseEntity<Object> getCopyRegType(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		int napprovalconfigversioncode = (int) inputMap.get("napprovalconfigversioncode");
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getCopyRegType(napprovalconfigversioncode, napprovalsubtypecode, userInfo);
	}

	@PostMapping(value = "/getCopyRegSubType", headers = "Accept=application/json")
	public ResponseEntity<Object> getCopyRegSubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nregTypeCode = (int) inputMap.get("nregtypecode");
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getCopyRegSubType(nregTypeCode, userInfo);
	}

	@PostMapping(value = "/setDefaultFilterStatus", headers = "Accept=application/json")
	public ResponseEntity<Object> setDefaultFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.setDefaultFilterStatus(inputMap);
	}

	@PostMapping(value = "/setDefaultDecisionStatus", headers = "Accept=application/json")
	public ResponseEntity<Object> setDefaultDecisionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.setDefaultDecisionStatus(inputMap);
	}

	@PostMapping(value = "/getDesignTemplateMapping", headers = "Accept=application/json")
	public ResponseEntity<Object> getDesignTemplateMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalConfigService.getDesignTemplateMapping(inputMap, userInfo);
	}
}
