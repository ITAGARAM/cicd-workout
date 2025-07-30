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
import com.agaramtech.qualis.submitter.model.Submitter;
import com.agaramtech.qualis.submitter.model.SubmitterMapping;
import com.agaramtech.qualis.submitter.service.submitter.SubmitterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/submitter")
public class SubmitterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubmitterController.class);

	private RequestContext requestContext;
	private final SubmitterService submitterService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext   RequestContext to hold the request
	 * @param submitterService SubmitterService
	 */
	public SubmitterController(RequestContext requestContext, SubmitterService submitterService) {
		super();
		this.requestContext = requestContext;
		this.submitterService = submitterService;
	}

	@PostMapping(value = "/getSubmitter")
	public ResponseEntity<Map<String, Object>> getSubmitter(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getSubmitter");
		inputMap.put("nflag", 1);
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitter(inputMap, userInfo);
	}

	@PostMapping(value = "/getInstitutionCategory")
	public ResponseEntity<Map<String, Object>> getInstitutionCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getInstitutionCategory(inputMap, userInfo);
	}

	@PostMapping(value = "/getInstitution")
	public ResponseEntity<Object> getInstitution(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getInstitution(inputMap, userInfo);
	}

	@PostMapping(value = "/getInstitutionByCategory")
	public ResponseEntity<Object> getInstitutionByCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getInstitutionByCategory(inputMap, userInfo);
	}

	@PostMapping(value = "/getInstitutionSite")
	public ResponseEntity<Map<String, Object>> getInstitutionSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getInstitutionSite(inputMap, userInfo);
	}

	@PostMapping(value = "/getInstitutionSiteByInstitution")
	public ResponseEntity<Map<String, Object>> getInstitutionSiteByInstitution(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getInstitutionSiteByInstitution(inputMap, userInfo);
	}

	@PostMapping(value = "/getSubmitterByFilter")
	public ResponseEntity<Map<String, Object>> getSubmitterByFilter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitterByFilter(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveSubmitterByFilter")
	public ResponseEntity<Object> getActiveSubmitterByFilter(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getActiveSubmitterByFilter(inputMap, userInfo);
	}

	@PostMapping(value = "/getInstitutionDepartment")
	public ResponseEntity<Object> getInstitutionDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getInstitutionDepartment(userInfo);
	}

	@PostMapping(value = "/getActiveSubmitterById")
	public ResponseEntity<Object> getActiveSubmitterById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String ssubmitterCode = (String) inputMap.get("ssubmittercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getActiveSubmitterById(ssubmitterCode, userInfo);
	}

	@PostMapping(value = "/createSubmitter")
	public ResponseEntity<Object> createSubmitter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Submitter objSubmitter = objMapper.convertValue(inputMap.get("submitter"),
				new TypeReference<Submitter>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.createSubmitter(objSubmitter, userInfo);

	}

	@PostMapping(value = "/updateSubmitter")
	public ResponseEntity<Object> updateSubmitter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Submitter objSubmitter = objMapper.convertValue(inputMap.get("submitter"),
				new TypeReference<Submitter>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.updateSubmitter(objSubmitter, userInfo);
	}

	@PostMapping(value = "/deleteSubmitter")
	public ResponseEntity<Object> deleteSubmitter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Submitter objSubmitter = objMapper.convertValue(inputMap.get("submitter"),
				new TypeReference<Submitter>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.deleteSubmitter(objSubmitter, userInfo);

	}

	@PostMapping(value = "/retireSubmitter")
	public ResponseEntity<Object> retireSubmitter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Submitter objSubmitter = objMapper.convertValue(inputMap.get("submitter"),
				new TypeReference<Submitter>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.retireSubmitter(objSubmitter, userInfo);
	}

	@PostMapping(value = "/getSelectedSubmitterDetail")
	public ResponseEntity<Object> getSelectedSubmitterDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Submitter objSubmitter = objmapper.convertValue(inputMap.get("submitter"),
				new TypeReference<Submitter>() {
				});
		final String ssubmitterCode = (String) inputMap.get("ssubmittercode");
		requestContext.setUserInfo(userInfo);
		return submitterService.getSelectedSubmitterDetail(userInfo, ssubmitterCode, objSubmitter);
	}

	@PostMapping(value = "/getSubmitterDetailByAll")
	public ResponseEntity<Object> getSubmitterDetailByAll(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitterDetailByAll(inputMap, userInfo);
	}

	@PostMapping(value = "/createSubmitterMapping")
	public ResponseEntity<Object> createSupplierMatrix(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<SubmitterMapping> submitterMapping = objmapper.convertValue(inputMap.get("submittermapping"),
				new TypeReference<List<SubmitterMapping>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return submitterService.createSubmitterMapping(submitterMapping, userInfo);

	}

	@PostMapping(value = "/deleteSubmitterMapping")
	public ResponseEntity<Object> deleteSubmitterMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final SubmitterMapping submitterMapping = objmapper.convertValue(inputMap.get("submittermapping"),
				SubmitterMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return submitterService.deleteSubmitterMapping(submitterMapping, userInfo);
	}

	@PostMapping(value = "/getSubmitterBySubmitterCode")
	public ResponseEntity<Object> getSubmitterBySubmitterCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitterBySubmitterCode(inputMap, userInfo);
	}

	@PostMapping(value = "/getSubmitterInstitutionCategoryCombo")
	public ResponseEntity<Object> getSubmitterMappingCombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitterInstitutionCategoryCombo(inputMap, userInfo);
	}

	@PostMapping(value = "/getSubmitterInstitutionCombo")
	public ResponseEntity<Object> getSubmitterInstitutionCombo(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitterInstitutionCombo(inputMap, userInfo);
	}

	@PostMapping(value = "/getSubmitterInstitutionSiteCombo")
	public ResponseEntity<Object> getSubmitterInstitutionSiteCombo(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitterInstitutionSiteCombo(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveSubmitterByInstitution")
	public ResponseEntity<Object> getActiveSubmitterByInstitution(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		Integer ninstitutionCode = null;
		if (inputMap.get("ninstitutioncode") != null) {
			ninstitutionCode = (Integer) inputMap.get("ninstitutioncode");
		}
		requestContext.setUserInfo(userInfo);
		return submitterService.getActiveSubmitterByInstitution(ninstitutionCode, userInfo);
	}

	@PostMapping(value = "/getSubmitterByInstitutionSite")
	public ResponseEntity<Object> getSubmitterByInstitutionSite(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		Integer ninstitutionSiteCode = null;
		if (inputMap.get("ninstitutionsitecode") != null) {
			ninstitutionSiteCode = (Integer) inputMap.get("ninstitutionsitecode");
		}
		requestContext.setUserInfo(userInfo);
		return submitterService.getSubmitterByInstitutionSite(ninstitutionSiteCode, userInfo, inputMap);
	}

}
