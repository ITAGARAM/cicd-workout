package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.audittrail.service.AuditTrailService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the audittrail Service methods.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 21-- 2021
 */
@RestController
@RequestMapping("/audittrail")
public class AuditTrailController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditTrailController.class);

	private final AuditTrailService auditTrailService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext    RequestContext to hold the request
	 * @param auditTrailService AuditTrailService
	 */
	public AuditTrailController(RequestContext requestContext, AuditTrailService auditTrailService) {
		super();
		this.requestContext = requestContext;
		this.auditTrailService = auditTrailService;
	}

	/**
	 * This method is used to retrieve list of all/specified active audittrail
	 * details and their associated
	 * 
	 * @param inputMap [Map] map object with key as (userinfo and currentdate)
	 * @return response entity object holding response status and list of
	 *         all/specified active audittrail details
	 */
	@PostMapping(value = "/getAuditTrail")
	public ResponseEntity<Object> getAuditTrail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("Get COntroller -->" + userInfo);
		return auditTrailService.getAuditTrail(userInfo, currentUIDate);
	}

	@PostMapping(value = "/getAuditTrailDetails")
	public ResponseEntity<Object> getAuditTrailDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditTrailService.getAuditTrailDetails(inputMap);
	}

	@PostMapping(value = "/getAuditTrailDetail")
	public ResponseEntity<Object> getAuditTrailDetail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nmodulecode = (int) inputMap.get("modulecode");
		final int nformcode = (int) inputMap.get("formcode");
		final int nusercode = (int) inputMap.get("usercode");
		final int nuserrole = (int) inputMap.get("userrole");
		final int nviewtypecode = (int) inputMap.get("viewtypecode");
		final String saudittraildate = (String) inputMap.get("saudittraildate");
		requestContext.setUserInfo(userInfo);
		return auditTrailService.getAuditTrailByDate(userInfo, fromDate, toDate, nmodulecode, nformcode, nusercode,
				nuserrole, nviewtypecode, saudittraildate);

	}

	@PostMapping(value = "/getFilterAuditTrailRecords")
	public ResponseEntity<Object> getFilterAuditTrailRecords(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final int nmodulecode = (int) inputMap.get("modulecode");
		final int nformcode = (int) inputMap.get("formcode");
		final int nusercode = (int) inputMap.get("usercode");
		final int nuserrole = (int) inputMap.get("userrole");
		final int nviewtypecode = (int) inputMap.get("viewtypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditTrailService.getFilterAuditTrailRecords(userInfo, fromDate, toDate, nmodulecode, nformcode,
				nusercode, nuserrole, nviewtypecode);
	}

	@PostMapping(value = "/getFormNameByModule")
	public ResponseEntity<Object> getFormNameByModule(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmodulecode = (int) inputMap.get("modulecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditTrailService.getFormNameByModule(nmodulecode);
	}

	@PostMapping(value = "/getexportdata")
	public ResponseEntity<Object> getexportdata(@RequestBody Map<String, Object> objmap) throws Exception {
//			final ObjectMapper objmapper = new ObjectMapper();
//			final String fromDate = (String)inputMap.get("fromDate");
//			final String toDate = (String)inputMap.get("toDate");
//			final int nmodulecode = (int) inputMap.get("modulecode");
//			final int nformcode = (int) inputMap.get("formcode");
//			final int nusercode = (int) inputMap.get("usercode");
//			final int nuserrole = (int) inputMap.get("userrole");
//			final int nviewtypecode = (int) inputMap.get("viewtypecode");
//			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditTrailService.getexportdata(objmap);
	}
}