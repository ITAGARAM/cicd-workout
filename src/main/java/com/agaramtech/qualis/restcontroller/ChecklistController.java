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

import com.agaramtech.qualis.checklist.model.Checklist;
import com.agaramtech.qualis.checklist.model.ChecklistVersion;
import com.agaramtech.qualis.checklist.model.ChecklistVersionQB;
import com.agaramtech.qualis.checklist.model.ChecklistVersionTemplate;
import com.agaramtech.qualis.checklist.service.checklist.ChecklistService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the checklist Service methods.
 */
@RestController
@RequestMapping("/checklist")
public class ChecklistController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistController.class);

	private RequestContext requestContext;
	private final ChecklistService checklistService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param checklistService checklistService
	 */
	public ChecklistController(RequestContext requestContext, ChecklistService checklistService) {
		super();
		this.requestContext = requestContext;
		this.checklistService = checklistService;
	}

	@PostMapping(value = "/getChecklist")
	public ResponseEntity<Object> getChecklist(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getChecklist -->"+userInfo);
		return checklistService.getChecklist(userInfo);

	}

	@PostMapping(value = "/createChecklist")
	public ResponseEntity<Object> createChecklist(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final Checklist objChecklist = objMapper.convertValue(inputMap.get("checklist"), new TypeReference<Checklist>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.createChecklist(objChecklist, userInfo);
	}

	@PostMapping(value = "/updateChecklist")
	public ResponseEntity<Object> updateChecklist(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Checklist objChecklist = objMapper.convertValue(inputMap.get("checklist"), new TypeReference<Checklist>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.updateChecklist(objChecklist, userInfo);
	}

	@PostMapping(value = "/deleteChecklist")
	public ResponseEntity<Object> deleteChecklist(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Checklist objChecklist = objMapper.convertValue(inputMap.get("checklist"), new TypeReference<Checklist>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.deleteChecklist(objChecklist, userInfo);
	}

	@PostMapping(value = "/getActiveChecklistById")
	public ResponseEntity<Object> getActiveChecklistById(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistCode = (int) inputMap.get("nchecklistcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getActiveChecklistById(nchecklistCode, userInfo);
	}

	@PostMapping(value = "/getChecklistVersion")
	public ResponseEntity<Object> getChecklistVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		int nchecklistCode = (int) inputMap.get("nchecklistcode");
	final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getActiveChecklistVersionByChecklist(nchecklistCode, userInfo);
	}

	@PostMapping(value = "/getActiveChecklistVersionById")
	public ResponseEntity<Object> getActiveChecklistVersionById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistVersionCode = (int) inputMap.get("nchecklistversioncode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getActiveChecklistVersionById(nchecklistVersionCode, userInfo);
	}

	@PostMapping(value = "/createChecklistVersion")
	public ResponseEntity<Object> createChecklistVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final ChecklistVersion objChecklistVersion = objMapper.convertValue(inputMap.get("checklistversion"),
				new TypeReference<ChecklistVersion>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.createChecklistVersion(objChecklistVersion, userInfo);
	}

	@PostMapping(value = "/updateChecklistVersion")
	public ResponseEntity<Object> updateChecklistVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistVersion objChecklistVersion = objMapper.convertValue(inputMap.get("checklistversion"),
				new TypeReference<ChecklistVersion>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.updateChecklistVersion(objChecklistVersion, userInfo);
	}

	@PostMapping(value = "/deleteChecklistVersion")
	public ResponseEntity<Object> deleteChecklistVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistVersion objChecklistVersion = objMapper.convertValue(inputMap.get("checklistversion"),
				new TypeReference<ChecklistVersion>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.deleteChecklistVersion(objChecklistVersion, userInfo);
	}

	@PostMapping(value = "/approveChecklistVersion")
	public ResponseEntity<Object> approveChecklistVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistVersion objChecklistVersion = objMapper.convertValue(inputMap.get("checklistversion"),
				new TypeReference<ChecklistVersion>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.approveChecklistVersion(objChecklistVersion, userInfo);
	}

	@PostMapping(value = "/getChecklistVersionQB")
	public ResponseEntity<Object> getChecklistVersionQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistVersionCode = (int) inputMap.get("nchecklistversioncode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getChecklistVersionQB(nchecklistVersionCode, userInfo);
	}

	@PostMapping(value = "/createChecklistVersionQB")
	public ResponseEntity<Object> createChecklistVersionQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final List<ChecklistVersionQB> objChecklistVersionQB = objMapper.convertValue(inputMap.get("checklistversionqb"),
				new TypeReference<List<ChecklistVersionQB>>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.createChecklistVersionQB(objChecklistVersionQB, userInfo);
	}

	@PostMapping(value = "/updateChecklistVersionQB")
	public ResponseEntity<Object> updateChecklistVersionQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistVersionQB objChecklistVersionQB = objMapper.convertValue(inputMap.get("checklistversionqb"),
				new TypeReference<ChecklistVersionQB>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.updateChecklistVersionQB(objChecklistVersionQB, userInfo);
	}

	@PostMapping(value = "/deleteChecklistVersionQB")
	public ResponseEntity<Object> deleteChecklistVersionQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistVersionQB objChecklistVersionQB = objMapper.convertValue(inputMap.get("checklistversionqb"),
				new TypeReference<ChecklistVersionQB>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.deleteChecklistVersionQB(objChecklistVersionQB, userInfo);
	}

	@PostMapping(value = "/getActiveChecklistVersionQBById")
	public ResponseEntity<Object> getActiveChecklistVersionQBById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistVersionQBCode = (int) inputMap.get("nchecklistversionqbcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getActiveChecklistVersionQBById(nchecklistVersionQBCode, userInfo);
	}

	@PostMapping(value = "/getVersionQBAddEditData")
	public ResponseEntity<Object> getVersionQBAddEditData(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistversioncode = (int) inputMap.get("nchecklistversioncode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getVersionQBAddEditData(nchecklistversioncode, userInfo);
	}

	@PostMapping(value = "/getAvailableChecklistQB")
	public ResponseEntity<Object> getAvailableChecklistQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistversioncode = (int) inputMap.get("nchecklistversioncode");
		final int nchecklistQBCategoryCode = (int) inputMap.get("nchecklistqbcategorycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getAvailableChecklistQB(nchecklistQBCategoryCode, nchecklistversioncode);
	}

	@PostMapping(value = "/viewTemplate")
	public ResponseEntity<Object> viewTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistVersionCode = (int) inputMap.get("nchecklistversioncode");
		final int flag = (int) inputMap.get("flag");
		final int ntransactionResultCode = (int) inputMap.get("ntransactionresultcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.viewTemplate(nchecklistVersionCode, flag, ntransactionResultCode, userInfo);
	}

	@PostMapping(value = "/createUpdateChecklistVersionTemplate")
	public ResponseEntity<Object> createUpdateChecklistVersionTemplate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final List<ChecklistVersionTemplate> lstChecklistVersionTemplate = objMapper.convertValue(
				inputMap.get("checklistversiontemplate"), new TypeReference<List<ChecklistVersionTemplate>>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.createUpdateChecklistVersionTemplate(lstChecklistVersionTemplate, userInfo);
	}

	@PostMapping(value = "/getApprovedChecklist")
	public ResponseEntity<Object> getApprovedChecklist(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checklistService.getApprovedChecklist(userInfo.getNmastersitecode());
	}

}
